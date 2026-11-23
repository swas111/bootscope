package io.github.swas111.bootscope.core.tool;

import java.util.List;

import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;
import io.github.swas111.bootscope.core.ToolResponse;

/** Shared plumbing: redact everything, then fit to the budget. Also 1-based paging. */
final class ToolSupport {

    private ToolSupport() {
    }

    /** Redact the summary and every item, then apply the output budget. */
    static ToolResponse respond(OutputBudget budget, Redactor redactor,
                                String summary, List<String> items, String pageHint) {
        String safeSummary = redactor.redact(summary);
        List<String> safeItems = items.stream().map(redactor::redact).toList();
        return budget.fit(safeSummary, safeItems, pageHint);
    }

    /** 1-based page slice; out-of-range pages yield an empty slice. */
    static <T> List<T> page(List<T> list, int page, int size) {
        int p = Math.max(1, page);
        int sz = Math.max(1, size);
        int from = Math.min((p - 1) * sz, list.size());
        int to = Math.min(from + sz, list.size());
        return list.subList(from, to);
    }

    /** True when there are more items beyond the given page. */
    static boolean hasMore(int total, int page, int size) {
        return (long) Math.max(1, page) * Math.max(1, size) < total;
    }
}
