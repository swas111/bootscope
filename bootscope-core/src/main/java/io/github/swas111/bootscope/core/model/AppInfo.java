package io.github.swas111.bootscope.core.model;

import java.time.Duration;
import java.util.List;

/** What an app is: name, version, git commit, active profiles, Java version and uptime. */
public record AppInfo(
        String name,
        String version,
        String gitCommit,
        List<String> profiles,
        String javaVersion,
        Duration uptime) {

    public AppInfo {
        profiles = profiles == null ? List.of() : List.copyOf(profiles);
    }
}
