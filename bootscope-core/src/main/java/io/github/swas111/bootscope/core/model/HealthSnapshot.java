package io.github.swas111.bootscope.core.model;

import java.util.List;

/** Overall health plus its components. */
public record HealthSnapshot(String status, List<HealthComponent> components) {

    public HealthSnapshot {
        components = components == null ? List.of() : List.copyOf(components);
    }

    public long downCount() {
        return components.stream().filter(c -> !c.isUp()).count();
    }
}
