package io.github.swas111.bootscope.core.model;

/**
 * Aggregated timing for one HTTP endpoint (from {@code http.server.requests}).
 *
 * @param p95Ms the 95th-percentile latency, or null when percentile histograms are not enabled
 */
public record RequestTiming(
        String method,
        String uri,
        long count,
        double meanMs,
        double maxMs,
        Double p95Ms) {

    /** Latency used for ranking: p95 when available, otherwise mean. */
    public double rankMs() {
        return p95Ms != null ? p95Ms : meanMs;
    }
}
