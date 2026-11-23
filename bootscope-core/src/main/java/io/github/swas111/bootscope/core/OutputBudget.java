package io.github.swas111.bootscope.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Caps a tool response to a character budget so it never floods the AI's context window.
 *
 * <p>The summary is always kept (hard-truncated only if it alone blows the budget). Items are
 * added greedily until the next one would exceed the budget; any dropped items flip
 * {@code truncated} to true and attach the caller's paging hint.
 */
public final class OutputBudget {

    /** Default response budget, per the design plan (~4,000 characters). */
    public static final int DEFAULT_MAX_CHARS = 4000;

    private static final String ELLIPSIS = "…";

    /** Floor that always leaves room for the summary plus a truncation note. */
    public static final int MIN_MAX_CHARS = 64;

    private final int maxChars;

    public OutputBudget(int maxChars) {
        if (maxChars < MIN_MAX_CHARS) {
            throw new IllegalArgumentException("maxChars must be >= " + MIN_MAX_CHARS + ", was " + maxChars);
        }
        this.maxChars = maxChars;
    }

    public static OutputBudget ofDefault() {
        return new OutputBudget(DEFAULT_MAX_CHARS);
    }

    public int maxChars() {
        return maxChars;
    }

    /**
     * Fit a summary and items within the budget.
     *
     * @param summary  the one-line summary (always included, truncated if itself too long)
     * @param items    candidate detail lines, in priority order (most important first)
     * @param pageHint hint shown when items are dropped, e.g. "call again with page: 2"
     */
    public ToolResponse fit(String summary, List<String> items, String pageHint) {
        String safeSummary = summary == null ? "" : summary;
        List<String> safeItems = items == null ? List.of() : items;

        // Pass 1: try to fit summary + all items within the full budget (no note needed).
        if (safeSummary.length() <= maxChars) {
            List<String> kept = new ArrayList<>();
            int used = safeSummary.length();
            boolean dropped = false;
            for (String item : safeItems) {
                String line = item == null ? "" : item;
                int lineCost = 3 + line.length(); // "\n- " + line
                if (used + lineCost > maxChars) {
                    dropped = true;
                    break;
                }
                kept.add(line);
                used += lineCost;
            }
            if (!dropped) {
                return new ToolResponse(safeSummary, kept, false, null);
            }
        }

        // Pass 2: we are truncating, so the rendered form will carry a truncation note.
        // Reserve its length and fit summary + items into the remaining content budget.
        int contentBudget = Math.max(0, maxChars - renderTruncationNote(pageHint).length());

        String renderedSummary = safeSummary;
        if (renderedSummary.length() > contentBudget) {
            int cutTo = Math.max(0, contentBudget - ELLIPSIS.length());
            renderedSummary = renderedSummary.substring(0, cutTo) + ELLIPSIS;
        }

        List<String> kept = new ArrayList<>();
        int used = renderedSummary.length();
        for (String item : safeItems) {
            String line = item == null ? "" : item;
            int lineCost = 3 + line.length();
            if (used + lineCost > contentBudget) {
                break;
            }
            kept.add(line);
            used += lineCost;
        }

        return new ToolResponse(renderedSummary, kept, true, pageHint);
    }

    private static String renderTruncationNote(String pageHint) {
        String base = "\n(truncated: true";
        if (pageHint != null && !pageHint.isBlank()) {
            base += "; " + pageHint;
        }
        return base + ")";
    }
}
