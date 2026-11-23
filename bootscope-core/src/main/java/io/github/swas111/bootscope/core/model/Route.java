package io.github.swas111.bootscope.core.model;

/** One request mapping: HTTP method(s), the URL pattern, and the handler that serves it. */
public record Route(String methods, String pattern, String handler) {
}
