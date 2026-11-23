package io.github.swas111.bootscope.core.tool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;
import io.github.swas111.bootscope.core.ToolResponse;
import io.github.swas111.bootscope.core.model.HealthComponent;
import io.github.swas111.bootscope.core.model.HealthSnapshot;
import io.github.swas111.bootscope.core.spi.HealthSource;

/** {@code health_check}: overall health, DOWN components first, details masked. */
public final class HealthCheckTool {

    private final HealthSource source;
    private final Redactor redactor;
    private final OutputBudget budget;

    public HealthCheckTool(HealthSource source, Redactor redactor, OutputBudget budget) {
        this.source = source;
        this.redactor = redactor;
        this.budget = budget;
    }

    public ToolResponse run() {
        HealthSnapshot health = source.health();
        long down = health.downCount();
        String summary = health.status() + ": " + health.components().size()
                + " component(s), " + down + " down";

        // DOWN components first, then the rest, each alphabetical.
        List<HealthComponent> ordered = health.components().stream()
                .sorted(Comparator.comparing(HealthComponent::isUp).thenComparing(HealthComponent::name))
                .toList();

        List<String> items = new ArrayList<>();
        for (HealthComponent c : ordered) {
            StringBuilder line = new StringBuilder(c.name()).append(": ").append(c.status());
            if (!c.details().isEmpty()) {
                String details = c.details().entrySet().stream()
                        .map(e -> e.getKey() + "=" + redactor.redactValue(e.getKey(), e.getValue()))
                        .collect(Collectors.joining(", "));
                line.append(" (").append(details).append(')');
            }
            items.add(line.toString());
        }
        return ToolSupport.respond(budget, redactor, summary, items, "some components omitted");
    }
}
