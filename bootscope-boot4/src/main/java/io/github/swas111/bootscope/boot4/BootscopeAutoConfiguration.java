package io.github.swas111.bootscope.boot4;

import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Bootscope starter auto-configuration (Spring Boot 4.x / Spring AI 2.0.x).
 *
 * <p>Applied only when Actuator's {@link HealthEndpoint} is on the classpath and
 * {@code bootscope.enabled} is true (the default). It registers the safety layer
 * ({@link Redactor}, {@link OutputBudget}) and the {@link BootscopeTools} MCP tool bean, which
 * Spring AI's annotation scanner publishes to the app's MCP server.
 */
@AutoConfiguration
@ConditionalOnClass(HealthEndpoint.class)
@ConditionalOnProperty(prefix = "bootscope", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BootscopeProperties.class)
public class BootscopeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Redactor bootscopeRedactor() {
        return new Redactor();
    }

    @Bean
    @ConditionalOnMissingBean
    OutputBudget bootscopeOutputBudget(BootscopeProperties properties) {
        return new OutputBudget(properties.getOutput().getMaxChars());
    }

    @Bean
    @ConditionalOnMissingBean
    BootscopeTools bootscopeTools(Redactor redactor, OutputBudget budget, Environment environment,
                                  ObjectProvider<BuildProperties> buildProperties,
                                  ObjectProvider<GitProperties> gitProperties,
                                  ObjectProvider<HealthEndpoint> healthEndpoint,
                                  ObjectProvider<MeterRegistry> meterRegistry,
                                  // Actuator adds a second RequestMappingHandlerMapping; target the main one.
                                  @Qualifier("requestMappingHandlerMapping")
                                  ObjectProvider<RequestMappingHandlerMapping> handlerMapping) {
        return new BootscopeTools(redactor, budget, environment, buildProperties, gitProperties,
                healthEndpoint, meterRegistry, handlerMapping);
    }
}
