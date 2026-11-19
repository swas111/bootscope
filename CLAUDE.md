# Bootscope: instructions for Claude

Bootscope is a Spring Boot starter that lets AI assistants safely inspect a running Spring Boot
app through MCP (read-only diagnostics tools). **Read `docs/DESIGN_PLAN.md` before doing any work:**
it holds the goals, tool catalogue, architecture, decisions, security model and build phases.

## How to work with the user

- The user is a Java/Spring Boot backend developer, new to AI and MCP. Explain each new concept
  (MCP, tool calling, auto-configuration details) briefly **before** writing the code for it.
- Follow the build phases in the plan in order; say which phase and step you are on.
- The user works on Windows with Git Bash: give shell commands in Bash syntax with forward slashes.
- Ask before changing a settled decision in the plan.

## Hard rules for the product

1. **No write tools, ever:** no shutdown, heap dump, full thread dump, log-level or environment
   changes, cache eviction, session deletion or refresh, not even behind a flag.
2. **Every tool response goes through the `Redactor` and `OutputBudget`.** No secret may appear in
   any output; the secret-leak test suite must pass.
3. **Tools read Spring beans in-process** (endpoint beans, `MeterRegistry`), never the app's own
   Actuator over HTTP.
4. **Fat core, thin adapters:** logic lives in `bootscope-core` (plain Java 17); `bootscope-boot3`
   (Spring Boot 3.5 + Spring AI 1.1.x) and `bootscope-boot4` (Spring Boot 4.x + Spring AI 2.0.x)
   only translate beans into core interfaces.
5. No AI calls inside the starter: summaries are computed by code.

## Repo rules

- Maven multi-module, Java 17 baseline, Apache-2.0 licence.
- Never commit secrets or put tokens in git remote URLs.
- Private notes go in `design/` (git-ignored).
- Check current versions of Spring Boot, Spring AI and the MCP Java SDK before pinning them.
