package io.github.swas111.bootscope.boot4;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings bound from {@code bootscope.*} in the host app's configuration.
 *
 * <p>Phase 2 only needs {@code enabled}; the full property set from the design plan
 * (auth, output budget, rate limits, ...) arrives in later phases.
 */
@ConfigurationProperties(prefix = "bootscope")
public class BootscopeProperties {

    /** Master switch for the whole starter. */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
