package io.github.swas111.bootscope.boot4;

import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the starter's conditions with {@link ApplicationContextRunner}: the tools bean and the
 * safety-layer beans appear only when Actuator is present and {@code bootscope.enabled} is on.
 * No backing beans (HealthEndpoint, MeterRegistry) are supplied here, so tools that need them
 * degrade gracefully rather than failing context startup.
 */
class BootscopeAutoConfigurationTests {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BootscopeAutoConfiguration.class));

    @Test
    void registersToolsAndSafetyLayerWhenActuatorPresentAndEnabledByDefault() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(BootscopeTools.class);
            assertThat(context).hasSingleBean(Redactor.class);
            assertThat(context).hasSingleBean(OutputBudget.class);
            assertThat(context).hasSingleBean(BootscopeProperties.class);
        });
    }

    @Test
    void backsOffWhenDisabled() {
        runner.withPropertyValues("bootscope.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(BootscopeTools.class));
    }

    @Test
    void backsOffWhenActuatorAbsent() {
        runner.withClassLoader(new FilteredClassLoader(HealthEndpoint.class))
                .run(context -> assertThat(context).doesNotHaveBean(BootscopeTools.class));
    }

    @Test
    void pingIsReadOnlyAndAppOverviewWorksWithoutBackingBeans() {
        runner.run(context -> {
            BootscopeTools tools = context.getBean(BootscopeTools.class);
            assertThat(tools.bootscopePing()).contains("bootscope").contains("read-only");
            // app_overview needs only the Environment, so it answers even in this minimal context.
            assertThat(tools.appOverview()).contains("App ");
        });
    }

    @Test
    void outputBudgetHonoursConfiguredMaxChars() {
        runner.withPropertyValues("bootscope.output.max-chars=1234")
                .run(context -> assertThat(context.getBean(OutputBudget.class).maxChars()).isEqualTo(1234));
    }
}
