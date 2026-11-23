# Bootscope

**A Spring Boot starter that lets an AI assistant safely inspect a running Spring Boot app from the
inside — through MCP, read-only, with secrets masked and every call bounded.**

Add one dependency, and tools like Claude Code, Cursor or Copilot can ask your *running* app
questions — "is it healthy?", "why is `/orders` slow?", "which config value is active for
`payment.timeout`?" — and get computed, summarised answers instead of raw Actuator JSON. There is
**no AI inside the starter**: it only exposes tools; the assistant is the AI.

> **Status: early development.** Foundations, a hello-world MCP server, and the starter
> auto-configuration are done and tested; the real diagnostics tools are in progress. See
> [Project status](#project-status). Not yet published to Maven Central.

---

## Why

On-call and local debugging often means copy-pasting logs and `/actuator` JSON into a chat. Bootscope
makes that a first-class, **auditable, read-only** channel instead:

- **Answer-shaped tools**, not an endpoint mirror — `slowest_endpoints`, `recent_errors`,
  `whats_wrong_now` compute p95 latency, error rates and grouped exceptions.
- **Read-only by design** — no write, shutdown, or heap-dump tool exists *at all*, not even behind a
  flag.
- **Secure by default** — every response passes through a secret `Redactor` and a size `OutputBudget`;
  authentication, rate limits and an audit log are part of the design.
- **Two Boot generations** — one shared core with a thin adapter each for Spring Boot 3.5 and 4.x.

The full design — goals, the twelve-tool catalogue, architecture, decisions, security model and build
phases — lives in **[docs/DESIGN_PLAN.md](docs/DESIGN_PLAN.md)**.

## How it works

```
AI assistant ──POST /mcp──▶  Your Spring Boot app (one starter dependency)
 (Claude, Cursor)            ├─ Spring AI MCP server · Streamable HTTP, stateless
                             ├─ Safety layer: auth · rate limit · mask secrets · ~4 000-char cap · audit
                             └─ Read-only @McpTool tools  ──▶ Actuator beans · MeterRegistry · error buffer
```

A tool call enters through the Spring AI MCP endpoint, runs one read-only tool, and comes back
**masked, size-capped and audited**. Tools read Actuator endpoint beans and Micrometer metrics
**in-process** — never the app's own Actuator over HTTP.

## Modules

| Module | What it is |
| --- | --- |
| `bootscope-core` | Plain Java 17, no Spring: tool logic, `Redactor`, `OutputBudget`, response shape; most tests live here |
| `bootscope-boot3` | Adapter for Spring Boot 3.5 + Spring AI 1.1.x — translates Boot 3 beans into core interfaces |
| `bootscope-boot4` | Adapter for Spring Boot 4.x + Spring AI 2.0.x (modular Actuator, Jackson 3) |
| `bootscope-spring-boot3-starter` / `bootscope-spring-boot4-starter` | POM-only, one-dependency starters |
| `examples/` | Runnable learning sandboxes (not part of the build reactor) |

**Fat core, thin adapters:** anything that can be written once lives in `bootscope-core`; an adapter
only translates one Boot generation's beans into the core's interfaces. The two generations genuinely
differ — e.g. the MCP `@McpTool` annotation and the Actuator `HealthEndpoint` live in different
packages per generation — which is exactly why there are two adapters.

## Getting started (preview)

> Starters are not on Maven Central yet. To try the current build, `mvn install` locally first
> (see [Building](#building)), then add the starter for your Boot generation:

```xml
<!-- Spring Boot 4.x apps -->
<dependency>
  <groupId>io.github.swas111</groupId>
  <artifactId>bootscope-spring-boot4-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <type>pom</type>
</dependency>
<!-- Spring Boot 3.5 apps: use bootscope-spring-boot3-starter instead -->
```

Your app also needs an MCP server endpoint (`spring-ai-starter-mcp-server-webmvc`) and
`spring-boot-starter-actuator`. Minimal configuration:

```yaml
spring:
  ai:
    mcp:
      server:
        protocol: STATELESS   # stateless Streamable HTTP, served at POST /mcp

bootscope:
  enabled: true               # master switch for the starter
```

A complete, runnable demo is in [`examples/demo-starter-boot4`](examples/demo-starter-boot4) (and the
Boot 3 equivalent). Run it and the Bootscope tools appear on the app's MCP endpoint.

## Configuration

Properties use the `bootscope` prefix. Currently implemented: `enabled`. The remaining properties
below are specified in the design plan and land with their phases.

| Property | Default | Purpose |
| --- | --- | --- |
| `bootscope.enabled` | `true` | Turn the starter on or off |
| `bootscope.tools.<name>.enabled` | `true` | Switch a single tool off |
| `bootscope.auth.mode` | `api-key` | `api-key`, `oauth2`, or `none` (loopback only) |
| `bootscope.output.max-chars` | `4000` | Response budget per call |
| `bootscope.rate-limit.per-minute` | `60` | Calls per client per minute |
| `bootscope.errors.buffer-size` | `500` | Recent ERROR events kept in memory |
| `bootscope.config.allowed-keys` | empty | Keys whose values may be shown unmasked |

## Safety model

- **Read-only, always.** No tool can change, restart, dump or evict anything.
- **Nothing leaks.** Every response passes the `Redactor` (key-aware masking + content scanning for
  keys, tokens, JWTs, credential URLs, emails, long digit runs). A **secret-leak test suite** asserts
  no fake secret survives, and is reused against every tool's real output.
- **Nothing floods.** The `OutputBudget` caps each response (~4 000 chars): summary first, top items,
  then a `truncated` flag with a paging hint.
- **In-process only.** Tools read Spring beans directly, so Actuator web exposure is not required.

## Building

Requires **JDK 17+** and **Maven 3.9+**.

```bash
# Build everything and run the tests
mvn verify

# Install the modules to your local ~/.m2 so the example apps can resolve them
mvn install -DskipTests

# Run the Boot 4 demo (MCP server on http://localhost:8766/mcp)
mvn -f examples/demo-starter-boot4/pom.xml spring-boot:run
```

A pre-commit hook (`scripts/check-secrets.sh`, enabled with
`git config core.hooksPath .githooks`) scans staged changes for secrets; CI runs the same scan plus a
build matrix over Linux/Windows and JDK 17/21.

## Versions

Pinned to the latest **stable** releases (re-checked 2026-11-19):

| | Spring Boot | Spring AI | MCP Java SDK |
| --- | --- | --- | --- |
| Boot 3 adapter | 3.5.16 | 1.1.8 | 0.18.3 |
| Boot 4 adapter | 4.1.2 | 2.0.2 | 2.0.0 |

## Project status

Built in phases (see the plan for the full roadmap):

- ✅ **Phase 0 — Foundations:** multi-module Maven skeleton, secret check, CI matrix.
- ✅ **Phase 1 — MCP basics:** hello-world MCP server with one `@McpTool`
  ([`examples/hello-mcp-boot4`](examples/hello-mcp-boot4)).
- ✅ **Phase 2 — Starter mechanics:** auto-configuration registers a tool only when Actuator is present
  and `bootscope.enabled` is on; both Boot generations at parity.
- 🚧 **Phase 3 — v0.1 core tools:** the safety layer (`Redactor`, `OutputBudget`, leak suite) is done;
  the six read-only tools (`app_overview`, `health_check`, `slowest_endpoints`, `error_hotspots`,
  `jvm_snapshot`, `api_routes`) and demo-shop apps are next.
- ⬜ **Phase 4+ —** diagnosis tools, hardening (auth, rate limits, audit), fleet mode, v1.0 release.

## License

[Apache-2.0](LICENSE).
