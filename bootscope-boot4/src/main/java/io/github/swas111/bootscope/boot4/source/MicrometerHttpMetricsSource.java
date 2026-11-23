package io.github.swas111.bootscope.boot4.source;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.github.swas111.bootscope.core.model.RequestError;
import io.github.swas111.bootscope.core.model.RequestTiming;
import io.github.swas111.bootscope.core.spi.HttpMetricsSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;

/**
 * {@link HttpMetricsSource} over the {@code MeterRegistry}'s {@code http.server.requests} timers.
 *
 * <p>Each unique tag combination (uri, method, status, ...) is a separate Micrometer timer. We
 * aggregate them by (method, uri) for latency and by (method, uri, status) for error counts.
 * p95 is reported per group as the max of the members' p95s when percentile histograms are on,
 * otherwise null (an approximation, since percentiles are not exactly additive).
 *
 * <p>Micrometer's API is identical on Boot 3 and Boot 4, so the Boot 3 adapter reuses this class.
 */
public final class MicrometerHttpMetricsSource implements HttpMetricsSource {

    private static final String METER = "http.server.requests";

    private final MeterRegistry registry;

    public MicrometerHttpMetricsSource(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<RequestTiming> requestTimings() {
        Map<String, double[]> acc = new LinkedHashMap<>(); // key -> [count, totalMs, max, p95]
        Map<String, String[]> labels = new LinkedHashMap<>();
        for (Timer t : registry.find(METER).timers()) {
            String method = tag(t, "method");
            String uri = tag(t, "uri");
            String key = method + " " + uri;
            double[] a = acc.computeIfAbsent(key, k -> new double[] {0, 0, 0, -1});
            a[0] += t.count();
            a[1] += t.totalTime(TimeUnit.MILLISECONDS);
            a[2] = Math.max(a[2], t.max(TimeUnit.MILLISECONDS));
            double p95 = p95Millis(t);
            if (p95 >= 0) {
                a[3] = Math.max(a[3], p95);
            }
            labels.putIfAbsent(key, new String[] {method, uri});
        }
        List<RequestTiming> out = new ArrayList<>();
        for (Map.Entry<String, double[]> e : acc.entrySet()) {
            double[] a = e.getValue();
            String[] l = labels.get(e.getKey());
            long count = (long) a[0];
            double mean = count > 0 ? a[1] / count : 0;
            Double p95 = a[3] >= 0 ? a[3] : null;
            out.add(new RequestTiming(l[0], l[1], count, mean, a[2], p95));
        }
        return out;
    }

    @Override
    public List<RequestError> requestErrors() {
        Map<String, long[]> acc = new LinkedHashMap<>(); // key -> [count]
        Map<String, Object[]> labels = new LinkedHashMap<>();
        for (Timer t : registry.find(METER).timers()) {
            int status = parseStatus(tag(t, "status"));
            if (status < 400) {
                continue;
            }
            String method = tag(t, "method");
            String uri = tag(t, "uri");
            String key = method + " " + uri + " " + status;
            acc.computeIfAbsent(key, k -> new long[] {0})[0] += t.count();
            labels.putIfAbsent(key, new Object[] {method, uri, status});
        }
        List<RequestError> out = new ArrayList<>();
        for (Map.Entry<String, long[]> e : acc.entrySet()) {
            Object[] l = labels.get(e.getKey());
            out.add(new RequestError((String) l[0], (String) l[1], (int) l[2], e.getValue()[0]));
        }
        return out;
    }

    private static String tag(Timer t, String key) {
        String v = t.getId().getTag(key);
        return v != null ? v : "?";
    }

    private static int parseStatus(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static double p95Millis(Timer t) {
        for (ValueAtPercentile v : t.takeSnapshot().percentileValues()) {
            if (Math.abs(v.percentile() - 0.95) < 1e-6) {
                return v.value(TimeUnit.MILLISECONDS);
            }
        }
        return -1;
    }
}
