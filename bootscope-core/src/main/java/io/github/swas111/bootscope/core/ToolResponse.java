package io.github.swas111.bootscope.core;

import java.util.List;

/**
 * The one shape every Bootscope tool returns: a one-line {@code summary}, then the top {@code
 * items}, then a {@code truncated} flag with an optional {@code moreHint} when data was dropped.
 *
 * <p>Immutable value object. Build the budget-trimmed form with {@link OutputBudget#fit} rather
 * than constructing directly, so responses always respect the size cap.
 */
public final class ToolResponse {

    private final String summary;
    private final List<String> items;
    private final boolean truncated;
    private final String moreHint;

    public ToolResponse(String summary, List<String> items, boolean truncated, String moreHint) {
        this.summary = summary == null ? "" : summary;
        this.items = items == null ? List.of() : List.copyOf(items);
        this.truncated = truncated;
        this.moreHint = moreHint;
    }

    public String summary() {
        return summary;
    }

    public List<String> items() {
        return items;
    }

    public boolean truncated() {
        return truncated;
    }

    public String moreHint() {
        return moreHint;
    }

    /** Canonical text form: summary first, then "- item" lines, then a truncation note if any. */
    public String render() {
        StringBuilder sb = new StringBuilder(summary);
        for (String item : items) {
            sb.append("\n- ").append(item);
        }
        if (truncated) {
            sb.append("\n(truncated: true");
            if (moreHint != null && !moreHint.isBlank()) {
                sb.append("; ").append(moreHint);
            }
            sb.append(')');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return render();
    }
}
