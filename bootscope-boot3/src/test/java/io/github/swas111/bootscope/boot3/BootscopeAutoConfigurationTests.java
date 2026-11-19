package io.github.swas111.bootscope.boot3;

import io.github.swas111.bootscope.core.DiagnosticsInfo;

import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Same proof as the Boot 4 adapter, against the Boot 3.5 Actuator class.
 */
class BootscopeAutoConfigurationTests {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BootscopeAutoConfiguration.class));

    @Test
    void registersToolWhenActuatorPresentAndEnabledByDefault() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(BootscopeDiagnosticsTools.class);
            assertThat(context).hasSingleBean(DiagnosticsInfo.class);
            assertThat(context).hasSingleBean(BootscopeProperties.class);
        });
    }

    @Test
    void backsOffWhenDisabled() {
        runner.withPropertyValues("bootscope.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(BootscopeDiagnosticsTools.class));
    }

    @Test
    void backsOffWhenActuatorAbsent() {
        runner.withClassLoader(new FilteredClassLoader(HealthEndpoint.class))
                .run(context -> assertThat(context).doesNotHaveBean(BootscopeDiagnosticsTools.class));
    }

    @Test
    void toolReturnsSafeReadOnlyStatusLine() {
        runner.run(context -> {
            BootscopeDiagnosticsTools tools = context.getBean(BootscopeDiagnosticsTools.class);
            assertThat(tools.bootscopePing())
                    .contains("bootscope")
                    .contains("read-only");
        });
    }
}
