package io.github.swas111.bootscope.boot4.source;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.github.swas111.bootscope.core.model.JvmSnapshot;
import io.github.swas111.bootscope.core.spi.JvmSource;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * {@link JvmSource} over Micrometer JVM metrics (jvm.memory.*, jvm.threads.*, jvm.gc.pause).
 * Shared with the Boot 3 adapter — the meter names are identical.
 */
public final class MicrometerJvmSource implements JvmSource {

    private final MeterRegistry registry;

    public MicrometerJvmSource(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public JvmSnapshot jvmSnapshot() {
        long heapUsed = (long) sumGauge("jvm.memory.used", "area", "heap");
        long heapMax = (long) sumPositiveGauge("jvm.memory.max", "area", "heap");
        long nonHeapUsed = (long) sumGauge("jvm.memory.used", "area", "nonheap");
        int live = (int) firstGauge("jvm.threads.live");
        int daemon = (int) firstGauge("jvm.threads.daemon");

        long gcCount = 0;
        long gcTimeMs = 0;
        for (Timer t : registry.find("jvm.gc.pause").timers()) {
            gcCount += t.count();
            gcTimeMs += (long) t.totalTime(TimeUnit.MILLISECONDS);
        }

        Map<String, Integer> states = new LinkedHashMap<>();
        for (Gauge g : registry.find("jvm.threads.states").gauges()) {
            String state = g.getId().getTag("state");
            if (state != null) {
                states.merge(state, (int) g.value(), Integer::sum);
            }
        }

        return new JvmSnapshot(heapUsed, heapMax, nonHeapUsed, live, daemon, gcCount, gcTimeMs, states);
    }

    private double sumGauge(String name, String tagKey, String tagValue) {
        return registry.find(name).tag(tagKey, tagValue).gauges().stream()
                .mapToDouble(Gauge::value).sum();
    }

    private double sumPositiveGauge(String name, String tagKey, String tagValue) {
        return registry.find(name).tag(tagKey, tagValue).gauges().stream()
                .mapToDouble(Gauge::value).filter(v -> v > 0).sum();
    }

    private double firstGauge(String name) {
        Gauge g = registry.find(name).gauge();
        return g != null ? g.value() : 0;
    }
}
