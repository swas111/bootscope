package io.github.swas111.bootscope.core.tool;

import java.util.ArrayList;
import java.util.List;

import io.github.swas111.bootscope.core.Format;
import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;
import io.github.swas111.bootscope.core.ToolResponse;
import io.github.swas111.bootscope.core.model.AppInfo;
import io.github.swas111.bootscope.core.spi.AppInfoSource;

/** {@code app_overview}: what this app is — name, version, profiles, uptime. */
public final class AppOverviewTool {

    private final AppInfoSource source;
    private final Redactor redactor;
    private final OutputBudget budget;

    public AppOverviewTool(AppInfoSource source, Redactor redactor, OutputBudget budget) {
        this.source = source;
        this.redactor = redactor;
        this.budget = budget;
    }

    public ToolResponse run() {
        AppInfo info = source.appInfo();
        String name = orUnknown(info.name());
        String version = orUnknown(info.version());
        String summary = "App " + name + " v" + version + ", up " + Format.uptime(info.uptime());

        List<String> items = new ArrayList<>();
        items.add("profiles: " + (info.profiles().isEmpty() ? "(none)" : String.join(", ", info.profiles())));
        items.add("java: " + orUnknown(info.javaVersion()));
        if (info.gitCommit() != null && !info.gitCommit().isBlank()) {
            items.add("git: " + info.gitCommit());
        }
        return ToolSupport.respond(budget, redactor, summary, items, null);
    }

    private static String orUnknown(String s) {
        return s == null || s.isBlank() ? "unknown" : s;
    }
}
