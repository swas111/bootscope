package io.github.swas111.bootscope.boot4;

import io.github.swas111.bootscope.core.DiagnosticsInfo;

import org.springframework.ai.mcp.annotation.McpTool;

/**
 * Phase 2 placeholder tool. A thin {@code @McpTool} wrapper over the core's {@link DiagnosticsInfo}.
 *
 * <p>It exists to prove the starter registers a tool only when Actuator is present and
 * {@code bootscope.enabled} is on. Real diagnostics tools replace it in Phase 3, but the shape
 * stays: the adapter only exposes core logic, and every tool is marked read-only.
 */
public class BootscopeDiagnosticsTools {

    private final DiagnosticsInfo diagnostics;

    public BootscopeDiagnosticsTools(DiagnosticsInfo diagnostics) {
        this.diagnostics = diagnostics;
    }

    @McpTool(
            name = "bootscope_ping",
            description = "Confirms Bootscope is installed and wired into this app's MCP server. Returns a fixed, safe status line.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false))
    public String bootscopePing() {
        return diagnostics.ping();
    }
}
