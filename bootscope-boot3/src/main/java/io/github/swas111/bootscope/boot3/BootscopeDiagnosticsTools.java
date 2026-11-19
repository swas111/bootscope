package io.github.swas111.bootscope.boot3;

import io.github.swas111.bootscope.core.DiagnosticsInfo;

import org.springaicommunity.mcp.annotation.McpTool;

/**
 * Phase 2 placeholder tool for Spring Boot 3.5 / Spring AI 1.1.x.
 *
 * <p>Identical behaviour to the Boot 4 adapter's tool, but note the different annotation package:
 * in Spring AI 1.1.x the MCP annotations live in {@code org.springaicommunity.mcp.annotation}
 * (they move to {@code org.springframework.ai.mcp.annotation} in Spring AI 2.0.x). This is exactly
 * why each Boot generation gets its own thin adapter.
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
