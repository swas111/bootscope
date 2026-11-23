package io.github.swas111.bootscope.core.model;

import java.util.Map;

/** A cheap JVM pressure snapshot — memory, threads and GC. Never a full thread dump. */
public record JvmSnapshot(
        long heapUsedBytes,
        long heapMaxBytes,
        long nonHeapUsedBytes,
        int liveThreads,
        int daemonThreads,
        long gcCount,
        long gcTimeMs,
        Map<String, Integer> threadStates) {

    public JvmSnapshot {
        threadStates = threadStates == null ? Map.of() : Map.copyOf(threadStates);
    }

    /** Heap used as a fraction of max (0..1), or -1 when max is unknown. */
    public double heapUsedFraction() {
        return heapMaxBytes > 0 ? (double) heapUsedBytes / heapMaxBytes : -1;
    }
}
