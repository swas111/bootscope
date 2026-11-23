package io.github.swas111.bootscope.boot4;

import io.github.swas111.bootscope.boot4.source.Boot4AppInfoSource;
import io.github.swas111.bootscope.boot4.source.Boot4HealthSource;
import io.github.swas111.bootscope.boot4.source.MicrometerHttpMetricsSource;
import io.github.swas111.bootscope.boot4.source.MicrometerJvmSource;
import io.github.swas111.bootscope.boot4.source.MvcRoutesSource;
import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;
import io.github.swas111.bootscope.core.spi.HttpMetricsSource;
import io.github.swas111.bootscope.core.tool.ApiRoutesTool;
import io.github.swas111.bootscope.core.tool.AppOverviewTool;
import io.github.swas111.bootscope.core.tool.ErrorHotspotsTool;
import io.github.swas111.bootscope.core.tool.HealthCheckTool;
import io.github.swas111.bootscope.core.tool.JvmSnapshotTool;
import io.github.swas111.bootscope.core.tool.SlowestEndpointsTool;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * The Bootscope v0.1 tools for Spring Boot 4.x, exposed over MCP.
 *
 * <p>Each method is a thin, read-only {@code @McpTool} wrapper: it builds the matching core
 * data-source from in-process beans and delegates to the core tool (which masks and size-caps the
 * result). Dependencies are injected as {@link ObjectProvider}s so a tool degrades to a clear
 * "not available" message when its backing bean is absent, instead of breaking the whole starter.
 */
public class BootscopeTools {

    private final Redactor redactor;
    private final OutputBudget budget;
    private final Environment environment;
    private final ObjectProvider<BuildProperties> buildProperties;
    private final ObjectProvider<GitProperties> gitProperties;
    private final ObjectProvider<HealthEndpoint> healthEndpoint;
    private final ObjectProvider<MeterRegistry> meterRegistry;
    private final ObjectProvider<RequestMappingHandlerMapping> handlerMapping;

    public BootscopeTools(Redactor redactor, OutputBudget budget, Environment environment,
                          ObjectProvider<BuildProperties> buildProperties,
                          ObjectProvider<GitProperties> gitProperties,
                          ObjectProvider<HealthEndpoint> healthEndpoint,
                          ObjectProvider<MeterRegistry> meterRegistry,
                          ObjectProvider<RequestMappingHandlerMapping> handlerMapping) {
        this.redactor = redactor;
        this.budget = budget;
        this.environment = environment;
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
        this.healthEndpoint = healthEndpoint;
        this.meterRegistry = meterRegistry;
        this.handlerMapping = handlerMapping;
    }

    @McpTool(name = "bootscope_ping",
            description = "Confirms Bootscope is installed and wired into this app's MCP server.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String bootscopePing() {
        return "bootscope ready: diagnostics wiring is live (read-only).";
    }

    @McpTool(name = "app_overview",
            description = "What this app is: name, version, git commit, active profiles and uptime.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String appOverview() {
        var source = new Boot4AppInfoSource(environment,
                buildProperties.getIfAvailable(), gitProperties.getIfAvailable());
        return new AppOverviewTool(source, redactor, budget).run().render();
    }

    @McpTool(name = "health_check",
            description = "Is the app healthy? Lists health components, DOWN first, with details (secrets masked).",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String healthCheck() {
        HealthEndpoint he = healthEndpoint.getIfAvailable();
        if (he == null) {
            return unavailable("Actuator HealthEndpoint");
        }
        return new HealthCheckTool(new Boot4HealthSource(he), redactor, budget).run().render();
    }

    @McpTool(name = "slowest_endpoints",
            description = "Endpoints ranked by latency (p95 where available), slowest first.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String slowestEndpoints(
            @McpToolParam(description = "1-based page number (default 1)", required = false) Integer page) {
        MeterRegistry mr = meterRegistry.getIfAvailable();
        if (mr == null) {
            return unavailable("MeterRegistry (Micrometer metrics)");
        }
        HttpMetricsSource source = new MicrometerHttpMetricsSource(mr);
        return new SlowestEndpointsTool(source, redactor, budget).run(pageOrFirst(page)).render();
    }

    @McpTool(name = "error_hotspots",
            description = "Endpoints returning 4xx/5xx responses, most frequent first.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String errorHotspots(
            @McpToolParam(description = "1-based page number (default 1)", required = false) Integer page) {
        MeterRegistry mr = meterRegistry.getIfAvailable();
        if (mr == null) {
            return unavailable("MeterRegistry (Micrometer metrics)");
        }
        HttpMetricsSource source = new MicrometerHttpMetricsSource(mr);
        return new ErrorHotspotsTool(source, redactor, budget).run(pageOrFirst(page)).render();
    }

    @McpTool(name = "jvm_snapshot",
            description = "JVM memory, GC and thread pressure. Never a full thread dump.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String jvmSnapshot() {
        MeterRegistry mr = meterRegistry.getIfAvailable();
        if (mr == null) {
            return unavailable("MeterRegistry (Micrometer metrics)");
        }
        return new JvmSnapshotTool(new MicrometerJvmSource(mr), redactor, budget).run().render();
    }

    @McpTool(name = "api_routes",
            description = "Which routes exist and which handler serves them, paged.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String apiRoutes(
            @McpToolParam(description = "1-based page number (default 1)", required = false) Integer page) {
        RequestMappingHandlerMapping mapping = handlerMapping.getIfAvailable();
        if (mapping == null) {
            return unavailable("Spring MVC RequestMappingHandlerMapping");
        }
        return new ApiRoutesTool(new MvcRoutesSource(mapping), redactor, budget).run(pageOrFirst(page)).render();
    }

    private static int pageOrFirst(Integer page) {
        return page == null ? 1 : page;
    }

    private String unavailable(String what) {
        return "unavailable: this app has no " + what + ", so this tool has no data to read.";
    }
}
