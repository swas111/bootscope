package io.github.swas111.bootscope.core.model;

/** Count of responses for one endpoint and HTTP status (used for 4xx/5xx hotspots). */
public record RequestError(String method, String uri, int status, long count) {

    public boolean isServerError() {
        return status >= 500;
    }

    public boolean isClientError() {
        return status >= 400 && status < 500;
    }
}
