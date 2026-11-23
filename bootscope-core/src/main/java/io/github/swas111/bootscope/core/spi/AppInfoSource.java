package io.github.swas111.bootscope.core.spi;

import io.github.swas111.bootscope.core.model.AppInfo;

/** Supplies {@link AppInfo}. Adapters implement this from Actuator's InfoEndpoint + JVM runtime. */
@FunctionalInterface
public interface AppInfoSource {
    AppInfo appInfo();
}
