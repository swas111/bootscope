package io.github.swas111.bootscope.core.spi;

import io.github.swas111.bootscope.core.model.JvmSnapshot;

/** Supplies a {@link JvmSnapshot}. Adapters implement this from Micrometer JVM metrics. */
@FunctionalInterface
public interface JvmSource {
    JvmSnapshot jvmSnapshot();
}
