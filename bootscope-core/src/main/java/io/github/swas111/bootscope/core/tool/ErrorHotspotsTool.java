package io.github.swas111.bootscope.core.tool;

import java.util.Comparator;
import java.util.List;

import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;
import io.github.swas111.bootscope.core.ToolResponse;
import io.github.swas111.bootscope.core.model.RequestError;
import io.github.swas111.bootscope.core.spi.HttpMetricsSource;

/** {@code error_hotspots}: endpoints returning 4xx/5xx, most frequent first. */
public final class ErrorHotspotsTool {

    public static final int DEFAULT_PAGE_SIZE = 10;

    private final HttpMetricsSource source;
    private final Redactor redactor;
    private final OutputBudget budget;

    public ErrorHotspotsTool(HttpMetricsSource source, Redactor redactor, OutputBudget budget) {
        this.source = source;
        this.redactor = redactor;
        this.budget = budget;
    }

    public ToolResponse run(int page) {
        List<RequestError> errors = source.requestErrors().stream()
                .filter(e -> e.status() >= 400 && e.count() > 0)
                .sorted(Comparator.comparingLong(RequestError::count).reversed())
                .toList();

        if (errors.isEmpty()) {
            return ToolSupport.respond(budget, redactor, "No 4xx/5xx responses recorded.", List.of(), null);
        }

        long server = errors.stream().filter(RequestError::isServerError).mapToLong(RequestError::count).sum();
        long client = errors.stream().filter(RequestError::isClientError).mapToLong(RequestError::count).sum();

        List<RequestError> slice = ToolSupport.page(errors, page, DEFAULT_PAGE_SIZE);
        boolean more = ToolSupport.hasMore(errors.size(), page, DEFAULT_PAGE_SIZE);

        String summary = errors.size() + " erroring endpoint(s): " + server + " server (5xx), " + client + " client (4xx)";
        List<String> items = slice.stream()
                .map(e -> e.method() + " " + e.uri() + " — HTTP " + e.status() + " ×" + e.count())
                .toList();

        return ToolSupport.respond(budget, redactor, summary, items,
                more ? "call again with page: " + (Math.max(1, page) + 1) : null);
    }
}
