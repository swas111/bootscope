package io.github.swas111.bootscope.boot4.source;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.swas111.bootscope.core.model.HealthComponent;
import io.github.swas111.bootscope.core.model.HealthSnapshot;
import io.github.swas111.bootscope.core.spi.HealthSource;

import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.IndicatedHealthDescriptor;

/**
 * Boot 4 {@link HealthSource}: reads the in-process {@link HealthEndpoint} bean.
 *
 * <p>Boot 4's {@code HealthEndpoint.health()} returns a {@link HealthDescriptor} tree — a
 * {@link CompositeHealthDescriptor} with named child components, each possibly an
 * {@link IndicatedHealthDescriptor} carrying details. We flatten the top level into the core's
 * {@link HealthSnapshot}. (Details are masked later by the Redactor, not here.)
 */
public final class Boot4HealthSource implements HealthSource {

    private final HealthEndpoint healthEndpoint;

    public Boot4HealthSource(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @Override
    public HealthSnapshot health() {
        HealthDescriptor root = healthEndpoint.health();
        String status = statusCode(root);

        List<HealthComponent> components = new ArrayList<>();
        if (root instanceof CompositeHealthDescriptor composite) {
            for (Map.Entry<String, HealthDescriptor> e : composite.getComponents().entrySet()) {
                HealthDescriptor child = e.getValue();
                components.add(new HealthComponent(e.getKey(), statusCode(child), details(child)));
            }
        } else {
            components.add(new HealthComponent("application", status, details(root)));
        }
        return new HealthSnapshot(status, components);
    }

    private static String statusCode(HealthDescriptor d) {
        return d.getStatus() != null ? d.getStatus().getCode() : "UNKNOWN";
    }

    private static Map<String, String> details(HealthDescriptor d) {
        if (!(d instanceof IndicatedHealthDescriptor ind) || ind.getDetails() == null) {
            return Map.of();
        }
        Map<String, String> out = new LinkedHashMap<>();
        ind.getDetails().forEach((k, v) -> out.put(k, String.valueOf(v)));
        return out;
    }
}
