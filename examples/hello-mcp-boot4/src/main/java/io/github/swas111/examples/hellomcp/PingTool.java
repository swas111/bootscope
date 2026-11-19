package io.github.swas111.examples.hellomcp;

import java.time.Instant;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * One hello-world MCP tool.
 *
 * <p>{@code @McpTool} turns a plain method into a tool the AI can discover and call. The
 * {@code name} and {@code description} become the tool's published metadata, and each
 * {@code @McpToolParam} describes an argument so the client can build a correct JSON schema.
 * Spring AI scans for these automatically — no manual registration.
 */
@Component
public class PingTool {

    @McpTool(name = "ping", description = "Health probe: echoes a greeting and the server time to confirm the MCP connection works.")
    public String ping(
            @McpToolParam(description = "Name to greet", required = false) String name) {
        String who = (name == null || name.isBlank()) ? "world" : name.trim();
        return "Hello, " + who + "! Bootscope hello-mcp is alive at " + Instant.now() + ".";
    }
}
