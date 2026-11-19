package io.github.swas111.bootscope.boot3;

import io.github.swas111.bootscope.core.DiagnosticsInfo;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * The Bootscope starter's auto-configuration for Spring Boot 3.5 / Spring AI 1.1.x.
 *
 * <p>Same shape as the Boot 4 adapter, but with the Boot 3 Actuator package: here
 * {@link HealthEndpoint} is {@code org.springframework.boot.actuate.health.HealthEndpoint}
 * (Boot 4 moved it to {@code org.springframework.boot.health.actuate.endpoint}).
 */
@AutoConfiguration
@ConditionalOnClass(HealthEndpoint.class)
@ConditionalOnProperty(prefix = "bootscope", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BootscopeProperties.class)
public class BootscopeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    DiagnosticsInfo bootscopeDiagnosticsInfo() {
        String version = BootscopeAutoConfiguration.class.getPackage().getImplementationVersion();
        return new DiagnosticsInfo(version != null ? version : "0.1.0-dev");
    }

    @Bean
    @ConditionalOnMissingBean
    BootscopeDiagnosticsTools bootscopeDiagnosticsTools(DiagnosticsInfo diagnosticsInfo) {
        return new BootscopeDiagnosticsTools(diagnosticsInfo);
    }
}
