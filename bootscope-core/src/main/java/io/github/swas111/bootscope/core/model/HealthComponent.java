package io.github.swas111.bootscope.core.model;

import java.util.Map;

/** One health contributor: its name, status (UP/DOWN/...), and optional details. */
public record HealthComponent(String name, String status, Map<String, String> details) {

    public HealthComponent {
        details = details == null ? Map.of() : Map.copyOf(details);
    }

    public boolean isUp() {
        return "UP".equalsIgnoreCase(status);
    }
}
