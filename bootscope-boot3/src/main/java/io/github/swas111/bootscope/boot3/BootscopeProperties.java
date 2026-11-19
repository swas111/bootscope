package io.github.swas111.bootscope.boot3;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings bound from {@code bootscope.*} in the host app's configuration (Spring Boot 3.5).
 *
 * <p>Mirror of the Boot 4 adapter's properties; kept per-adapter so each generation can diverge
 * later. Phase 2 only needs {@code enabled}.
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
