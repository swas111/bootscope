package io.github.swas111.bootscope.core.spi;

import java.util.List;

import io.github.swas111.bootscope.core.model.Route;

/** Supplies the app's {@link Route}s. Adapters implement this from Actuator's MappingsEndpoint. */
@FunctionalInterface
public interface RoutesSource {
    List<Route> routes();
}
