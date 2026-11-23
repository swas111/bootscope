package io.github.swas111.bootscope.boot4.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.swas111.bootscope.core.model.Route;
import io.github.swas111.bootscope.core.spi.RoutesSource;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * {@link RoutesSource} over Spring MVC's {@link RequestMappingHandlerMapping} — a stable way to
 * enumerate routes and their handlers without parsing the Actuator mappings endpoint. Shared with
 * the Boot 3 adapter.
 */
public final class MvcRoutesSource implements RoutesSource {

    private final RequestMappingHandlerMapping handlerMapping;

    public MvcRoutesSource(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public List<Route> routes() {
        List<Route> routes = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> e : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = e.getKey();
            HandlerMethod hm = e.getValue();

            String methods = info.getMethodsCondition().getMethods().stream()
                    .map(Enum::name).sorted().collect(Collectors.joining(","));
            String patterns = String.join(", ", info.getPatternValues());
            String handler = hm.getBeanType().getSimpleName() + "#" + hm.getMethod().getName();

            routes.add(new Route(methods, patterns.isBlank() ? "(none)" : patterns, handler));
        }
        return routes;
    }
}
