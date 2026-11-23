package io.github.swas111.bootscope.core.spi;

import io.github.swas111.bootscope.core.model.HealthSnapshot;

/** Supplies a {@link HealthSnapshot}. Adapters implement this from Actuator's HealthEndpoint. */
@FunctionalInterface
public interface HealthSource {
    HealthSnapshot health();
}
