package io.github.swas111.bootscope.core.tool;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import io.github.swas111.bootscope.core.OutputBudget;
import io.github.swas111.bootscope.core.Redactor;
import io.github.swas111.bootscope.core.ToolResponse;
import io.github.swas111.bootscope.core.model.AppInfo;
import io.github.swas111.bootscope.core.model.HealthComponent;
import io.github.swas111.bootscope.core.model.HealthSnapshot;
import io.github.swas111.bootscope.core.model.JvmSnapshot;
import io.github.swas111.bootscope.core.model.RequestError;
import io.github.swas111.bootscope.core.model.RequestTiming;
import io.github.swas111.bootscope.core.model.Route;
import io.github.swas111.bootscope.core.spi.HttpMetricsSource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoreToolsTest {

    private final Redactor redactor = new Redactor();
    private final OutputBudget budget = OutputBudget.ofDefault();

    @Test
    void appOverviewSummarisesIdentityAndUptime() {
        AppInfo info = new AppInfo("shop", "1.4.2", "abc1234", List.of("prod", "eu"), "17.0.12", Duration.ofSeconds(3725));
        ToolResponse r = new AppOverviewTool(() -> info, redactor, budget).run();
        assertThat(r.summary()).contains("shop").contains("1.4.2").contains("1h 2m 5s");
        assertThat(r.items()).anyMatch(i -> i.contains("prod, eu"));
        assertThat(r.items()).anyMatch(i -> i.contains("git: abc1234"));
    }

    @Test
    void healthCheckPutsDownComponentsFirstAndMasksSecretDetails() {
        HealthSnapshot snap = new HealthSnapshot("DOWN", List.of(
                new HealthComponent("ping", "UP", Map.of()),
                new HealthComponent("db", "DOWN", Map.of("password", "pg_pw", "url", "jdbc:postgresql://db/app"))));
        ToolResponse r = new HealthCheckTool(() -> snap, redactor, budget).run();

        assertThat(r.summary()).contains("DOWN").contains("1 down");
        assertThat(r.items().get(0)).startsWith("db: DOWN");      // DOWN first
        assertThat(r.render()).doesNotContain("pg_pw");            // secret masked
        assertThat(r.render()).contains("password=***");
    }

    @Test
    void slowestEndpointsRanksByP95ThenMean() {
        HttpMetricsSource metrics = metrics(
                List.of(
                        new RequestTiming("GET", "/fast", 100, 5, 20, 8.0),
                        new RequestTiming("GET", "/orders", 50, 300, 900, 820.0),
                        new RequestTiming("GET", "/mid", 80, 100, 200, 150.0)),
                List.of());
        ToolResponse r = new SlowestEndpointsTool(metrics, redactor, budget).run(1);
        assertThat(r.items().get(0)).contains("/orders").contains("p95=820 ms");
        assertThat(r.items().get(2)).contains("/fast");
    }

    @Test
    void slowestEndpointsHandlesNoMetrics() {
        ToolResponse r = new SlowestEndpointsTool(metrics(List.of(), List.of()), redactor, budget).run(1);
        assertThat(r.summary()).contains("No HTTP request metrics");
        assertThat(r.items()).isEmpty();
    }

    @Test
    void errorHotspotsFiltersAndTotals() {
        HttpMetricsSource metrics = metrics(List.of(), List.of(
                new RequestError("GET", "/ok", 200, 999),       // not an error
                new RequestError("POST", "/pay", 500, 12),
                new RequestError("GET", "/missing", 404, 30)));
        ToolResponse r = new ErrorHotspotsTool(metrics, redactor, budget).run(1);
        assertThat(r.summary()).contains("2 erroring endpoint(s)").contains("12 server").contains("30 client");
        assertThat(r.items().get(0)).contains("/missing").contains("×30"); // most frequent first
        assertThat(r.render()).doesNotContain("/ok");
    }

    @Test
    void jvmSnapshotReportsHeapPercentAndThreads() {
        JvmSnapshot snap = new JvmSnapshot(512L * 1024 * 1024, 1024L * 1024 * 1024,
                64L * 1024 * 1024, 42, 20, 7, 150, Map.of("RUNNABLE", 10, "WAITING", 32));
        ToolResponse r = new JvmSnapshotTool(() -> snap, redactor, budget).run();
        assertThat(r.summary()).contains("50%").contains("42 threads");
        assertThat(r.items()).anyMatch(i -> i.contains("gc: 7 collections"));
    }

    @Test
    void apiRoutesSortsAndPages() {
        Route a = new Route("GET", "/a", "A#get");
        Route b = new Route("POST", "/b", "B#post");
        ToolResponse r = new ApiRoutesTool(() -> List.of(b, a), redactor, budget).run(1);
        assertThat(r.summary()).contains("2 route(s)");
        assertThat(r.items().get(0)).startsWith("GET /a");
    }

    private static HttpMetricsSource metrics(List<RequestTiming> timings, List<RequestError> errors) {
        return new HttpMetricsSource() {
            @Override
            public List<RequestTiming> requestTimings() {
                return timings;
            }

            @Override
            public List<RequestError> requestErrors() {
                return errors;
            }
        };
    }
}
