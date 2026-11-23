package io.github.swas111.bootscope.boot4.source;

import java.time.Duration;

import io.github.swas111.bootscope.core.model.RequestError;
import io.github.swas111.bootscope.core.model.RequestTiming;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies aggregation of http.server.requests timers by (method,uri) and (…,status). */
class MicrometerHttpMetricsSourceTest {

    @Test
    void aggregatesTimingsByEndpointAndErrorsByStatus() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        // /orders: two status timers (200 and 500) should merge into one timing row.
        timer(registry, "GET", "/orders", "200").record(Duration.ofMillis(100));
        timer(registry, "GET", "/orders", "200").record(Duration.ofMillis(300));
        timer(registry, "GET", "/orders", "500").record(Duration.ofMillis(50));
        timer(registry, "GET", "/health", "200").record(Duration.ofMillis(5));

        MicrometerHttpMetricsSource source = new MicrometerHttpMetricsSource(registry);

        RequestTiming orders = source.requestTimings().stream()
                .filter(t -> t.uri().equals("/orders")).findFirst().orElseThrow();
        assertThat(orders.count()).isEqualTo(3);           // 2 @200 + 1 @500
        assertThat(orders.maxMs()).isGreaterThanOrEqualTo(300);
        assertThat(orders.p95Ms()).isNull();               // no percentile histogram configured

        RequestError err = source.requestErrors().stream()
                .filter(e -> e.uri().equals("/orders")).findFirst().orElseThrow();
        assertThat(err.status()).isEqualTo(500);
        assertThat(err.count()).isEqualTo(1);
        assertThat(err.isServerError()).isTrue();
        // 2xx must never appear as an error.
        assertThat(source.requestErrors()).noneMatch(e -> e.status() < 400);
    }

    private static Timer timer(SimpleMeterRegistry registry, String method, String uri, String status) {
        return Timer.builder("http.server.requests")
                .tags("method", method, "uri", uri, "status", status)
                .register(registry);
    }
}
