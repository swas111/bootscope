package io.github.swas111.bootscope.core.tool;

import java.util.Comparator;
import java.util.List;

import io.github.swas111.bootscope.core.Format;
import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;
import io.github.swas111.bootscope.core.ToolResponse;
import io.github.swas111.bootscope.core.model.RequestTiming;
import io.github.swas111.bootscope.core.spi.HttpMetricsSource;

/** {@code slowest_endpoints}: endpoints ranked by latency (p95 where available, else mean). */
public final class SlowestEndpointsTool {

    public static final int DEFAULT_PAGE_SIZE = 10;

    private final HttpMetricsSource source;
    private final Redactor redactor;
    private final OutputBudget budget;

    public SlowestEndpointsTool(HttpMetricsSource source, Redactor redactor, OutputBudget budget) {
        this.source = source;
        this.redactor = redactor;
        this.budget = budget;
    }

    public ToolResponse run(int page) {
        List<RequestTiming> ranked = source.requestTimings().stream()
                .sorted(Comparator.comparingDouble(RequestTiming::rankMs).reversed())
                .toList();

        if (ranked.isEmpty()) {
            return ToolSupport.respond(budget, redactor,
                    "No HTTP request metrics yet (http.server.requests empty).", List.of(), null);
        }

        List<RequestTiming> slice = ToolSupport.page(ranked, page, DEFAULT_PAGE_SIZE);
        boolean more = ToolSupport.hasMore(ranked.size(), page, DEFAULT_PAGE_SIZE);

        String summary = ranked.size() + " endpoint(s) by latency (slowest first)";
        List<String> items = slice.stream().map(t -> {
            String p95 = t.p95Ms() != null ? "p95=" + Format.millis(t.p95Ms()) + " " : "";
            return t.method() + " " + t.uri() + " — " + p95
                    + "mean=" + Format.millis(t.meanMs())
                    + " max=" + Format.millis(t.maxMs())
                    + " count=" + t.count();
        }).toList();

        return ToolSupport.respond(budget, redactor, summary, items,
                more ? "call again with page: " + (Math.max(1, page) + 1) : null);
    }
}
