package io.github.swas111.bootscope.core.spi;

import java.util.List;

import io.github.swas111.bootscope.core.model.RequestError;
import io.github.swas111.bootscope.core.model.RequestTiming;

/**
 * Supplies per-endpoint HTTP metrics. Adapters implement this from the {@code MeterRegistry}'s
 * {@code http.server.requests} timers (by URI/method for timings, by status for errors).
 */
public interface HttpMetricsSource {

    List<RequestTiming> requestTimings();

    List<RequestError> requestErrors();
}
