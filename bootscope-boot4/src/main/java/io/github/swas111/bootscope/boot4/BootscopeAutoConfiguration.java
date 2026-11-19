package io.github.swas111.bootscope.boot4;

import io.github.swas111.bootscope.core.DiagnosticsInfo;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.context.annotation.Bean;

/**
 * The Bootscope starter's auto-configuration (Spring Boot 4.x / Spring AI 2.0.x).
 *
 * <p>Spring Boot finds this class through
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * and applies it only when both conditions hold:
 * <ul>
 *   <li>{@link ConditionalOnClass} — Actuator's {@link HealthEndpoint} is on the classpath
 *       (so there is something to diagnose); the whole config backs off otherwise.</li>
 *   <li>{@link ConditionalOnProperty} — {@code bootscope.enabled} is true (the default).</li>
 * </ul>
 * When active it registers one {@code @McpTool} bean, which Spring AI's annotation scanner
 * then publishes to the app's MCP server.
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
