package io.github.swas111.bootscope.core.tool;

import java.util.Comparator;
import java.util.List;

import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;
import io.github.swas111.bootscope.core.ToolResponse;
import io.github.swas111.bootscope.core.model.Route;
import io.github.swas111.bootscope.core.spi.RoutesSource;

/** {@code api_routes}: which routes exist and which handler serves them, paged. */
public final class ApiRoutesTool {

    public static final int DEFAULT_PAGE_SIZE = 20;

    private final RoutesSource source;
    private final Redactor redactor;
    private final OutputBudget budget;

    public ApiRoutesTool(RoutesSource source, Redactor redactor, OutputBudget budget) {
        this.source = source;
        this.redactor = redactor;
        this.budget = budget;
    }

    public ToolResponse run(int page) {
        List<Route> routes = source.routes().stream()
                .sorted(Comparator.comparing(Route::pattern, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (routes.isEmpty()) {
            return ToolSupport.respond(budget, redactor, "No routes found.", List.of(), null);
        }

        List<Route> slice = ToolSupport.page(routes, page, DEFAULT_PAGE_SIZE);
        boolean more = ToolSupport.hasMore(routes.size(), page, DEFAULT_PAGE_SIZE);

        String summary = routes.size() + " route(s)";
        List<String> items = slice.stream().map(r -> {
            String methods = (r.methods() == null || r.methods().isBlank()) ? "ANY" : r.methods();
            String handler = (r.handler() == null || r.handler().isBlank()) ? "" : " -> " + r.handler();
            return methods + " " + r.pattern() + handler;
        }).toList();

        return ToolSupport.respond(budget, redactor, summary, items,
                more ? "call again with page: " + (Math.max(1, page) + 1) : null);
    }
}
