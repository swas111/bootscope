package io.github.swas111.bootscope.core;

/**
 * Dummy diagnostics used in Phase 2 to prove the starter wiring end to end.
 *
 * <p>Plain Java, no Spring: it is the "fat core" the thin adapters call. The real tools replace
 * this in Phase 3, but the shape stays — logic lives here, the adapter only exposes it as a tool.
 */
public final class DiagnosticsInfo {

    private final String version;

    public DiagnosticsInfo(String version) {
        this.version = (version == null || version.isBlank()) ? "unknown" : version;
    }

    /** A fixed, safe status line proving the tool is reachable. Contains no app data or secrets. */
    public String ping() {
        return "bootscope " + version + " ready: diagnostics wiring is live (read-only).";
    }

    public String version() {
        return version;
    }
}
