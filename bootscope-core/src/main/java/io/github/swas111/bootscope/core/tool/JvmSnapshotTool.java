package io.github.swas111.bootscope.core.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.swas111.bootscope.core.Format;
import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;
import io.github.swas111.bootscope.core.ToolResponse;
import io.github.swas111.bootscope.core.model.JvmSnapshot;
import io.github.swas111.bootscope.core.spi.JvmSource;

/** {@code jvm_snapshot}: memory, GC and thread pressure. Never a full thread dump. */
public final class JvmSnapshotTool {

    private final JvmSource source;
    private final Redactor redactor;
    private final OutputBudget budget;

    public JvmSnapshotTool(JvmSource source, Redactor redactor, OutputBudget budget) {
        this.source = source;
        this.redactor = redactor;
        this.budget = budget;
    }

    public ToolResponse run() {
        JvmSnapshot j = source.jvmSnapshot();
        double frac = j.heapUsedFraction();
        String heapPct = frac >= 0 ? " (" + Format.percent(frac) + ")" : "";
        String summary = "Heap " + Format.bytes(j.heapUsedBytes()) + " / " + Format.bytes(j.heapMaxBytes())
                + heapPct + ", " + j.liveThreads() + " threads";

        List<String> items = new ArrayList<>();
        items.add("heap: " + Format.bytes(j.heapUsedBytes()) + " used of " + Format.bytes(j.heapMaxBytes()) + heapPct);
        items.add("non-heap: " + Format.bytes(j.nonHeapUsedBytes()) + " used");
        items.add("threads: " + j.liveThreads() + " live, " + j.daemonThreads() + " daemon");
        if (!j.threadStates().isEmpty()) {
            StringBuilder states = new StringBuilder("thread states: ");
            boolean first = true;
            for (Map.Entry<String, Integer> e : j.threadStates().entrySet()) {
                if (!first) {
                    states.append(", ");
                }
                states.append(e.getKey()).append('=').append(e.getValue());
                first = false;
            }
            items.add(states.toString());
        }
        items.add("gc: " + j.gcCount() + " collections, " + j.gcTimeMs() + " ms total");

        return ToolSupport.respond(budget, redactor, summary, items, null);
    }
}
