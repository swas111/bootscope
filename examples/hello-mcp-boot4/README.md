# hello-mcp (Boot 4) — Phase 1 sandbox

The smallest possible MCP server: one `@McpTool` exposed over **stateless Streamable HTTP**.
This is a *learning* app, deliberately **not** part of the Bootscope Maven reactor.

- Spring Boot 4.1.2 + Spring AI 2.0.2 (`spring-ai-starter-mcp-server-webmvc`)
- One tool: `ping(name?)` → a greeting + server time
- Endpoint: `POST http://localhost:8765/mcp`

## Run it

```bash
mvn -f examples/hello-mcp-boot4/pom.xml spring-boot:run
```

Look for `Registered tools: 1` and `Started HelloMcpApplication` in the log.
(The port is 8765 — set in `src/main/resources/application.yml`.)

## Poke it directly (no AI needed)

MCP is JSON-RPC. Every request is a POST to `/mcp` with both JSON and SSE accepted:

```bash
# initialize
curl -s -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -X POST http://localhost:8765/mcp \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"curl","version":"0"}}}'

# list tools
curl -s -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -X POST http://localhost:8765/mcp \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'

# call the tool
curl -s -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -X POST http://localhost:8765/mcp \
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"ping","arguments":{"name":"Swastik"}}}'
```

## Connect it to Claude Code

With the server running, register it (HTTP transport):

```bash
claude mcp add --transport http hello-mcp http://localhost:8765/mcp
```

Then in a Claude Code session, ask it to *"use the ping tool to greet me"*. Claude discovers
`ping` via `tools/list` and calls it. Remove it later with `claude mcp remove hello-mcp`.

## Inspect it with MCP Inspector

```bash
npx @modelcontextprotocol/inspector
```

Open the UI, choose **Streamable HTTP**, URL `http://localhost:8765/mcp`, Connect → the
**Tools** tab lists `ping` and lets you run it.

## Note for the real Bootscope tools

`@McpTool` defaults to `readOnlyHint=false, destructiveHint=true`. Bootscope's tools are all
read-only, so the shipped tools will set `annotations = @McpAnnotations(readOnlyHint = true)`
(and `destructiveHint = false`). That hint is part of how we signal "safe to call" to clients.
