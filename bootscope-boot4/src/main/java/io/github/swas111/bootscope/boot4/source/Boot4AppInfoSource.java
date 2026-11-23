package io.github.swas111.bootscope.boot4.source;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.List;

import io.github.swas111.bootscope.core.model.AppInfo;
import io.github.swas111.bootscope.core.spi.AppInfoSource;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;

/**
 * Boot 4 {@link AppInfoSource}: app identity from the {@link Environment} plus the optional
 * {@link BuildProperties} / {@link GitProperties} beans, and uptime from the JVM runtime.
 */
public final class Boot4AppInfoSource implements AppInfoSource {

    private final Environment environment;
    private final BuildProperties buildProperties; // nullable
    private final GitProperties gitProperties;     // nullable

    public Boot4AppInfoSource(Environment environment, BuildProperties buildProperties, GitProperties gitProperties) {
        this.environment = environment;
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    @Override
    public AppInfo appInfo() {
        String name = buildProperties != null && buildProperties.getName() != null
                ? buildProperties.getName()
                : environment.getProperty("spring.application.name");
        String version = buildProperties != null ? buildProperties.getVersion() : null;
        String commit = gitProperties != null ? gitProperties.getShortCommitId() : null;
        Duration uptime = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
        return new AppInfo(name, version, commit, List.of(environment.getActiveProfiles()),
                System.getProperty("java.version"), uptime);
    }
}
