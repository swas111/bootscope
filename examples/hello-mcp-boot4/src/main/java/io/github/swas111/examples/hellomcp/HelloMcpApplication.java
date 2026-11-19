package io.github.swas111.examples.hellomcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Phase 1 sandbox: the smallest Spring Boot app that runs an MCP server.
 *
 * <p>The MCP server starter on the classpath auto-configures a Streamable HTTP endpoint at
 * {@code POST /mcp}; any {@code @McpTool}-annotated bean method is published to MCP clients
 * (like Claude Code). There is no AI model here — this app is only the tool <em>server</em>.
 */
@SpringBootApplication
public class HelloMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloMcpApplication.class, args);
    }
}
