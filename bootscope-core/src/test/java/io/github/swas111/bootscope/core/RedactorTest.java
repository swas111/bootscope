package io.github.swas111.bootscope.core;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedactorTest {

    private final Redactor redactor = new Redactor();

    @Test
    void masksValuesOfSensitiveKeys() {
        assertThat(redactor.redactValue("password", "hunter2")).isEqualTo("***");
        assertThat(redactor.redactValue("spring.datasource.password", "pg_pw")).isEqualTo("***");
        assertThat(redactor.redactValue("api-key", "abc123")).isEqualTo("***");
        assertThat(redactor.redactValue("AUTHORIZATION", "Basic xyz")).isEqualTo("***");
    }

    @Test
    void leavesNonSensitiveKeysAlone() {
        assertThat(redactor.redactValue("payment.timeout", "2s")).isEqualTo("2s");
        assertThat(redactor.redactValue("server.port", "8080")).isEqualTo("8080");
        assertThat(redactor.isSensitiveKey("username")).isFalse();
        assertThat(redactor.isSensitiveKey("db.password")).isTrue();
    }

    @Test
    void masksSecretShapedContent() {
        // Tokens assembled from fragments so no contiguous provider-format literal is committed.
        String aws = "AKIA" + "IOSFODNN7EXAMPLE";
        assertThat(redactor.redact("key " + aws + " here")).doesNotContain(aws);

        String dbPass = "s3cret" + "x";
        assertThat(redactor.redact("jdbc postgres://user:" + dbPass + "@db:5432/app")).doesNotContain(dbPass);

        String jwt = "eyJhbGciOiJIUzI1NiJ9" + "." + "eyJzdWIiOiIxMjMifQ" + "." + "abc123signature_x";
        assertThat(redactor.redact("token=" + jwt)).doesNotContain("signature_x");

        String email = "alice" + "@" + "example.com";
        assertThat(redactor.redact("contact " + email)).doesNotContain(email);
    }

    @Test
    void preservesOrdinaryDiagnosticText() {
        String text = "GET /orders is slow: p95=820ms, error-rate=3%, pool=8/10";
        assertThat(redactor.redact(text)).isEqualTo(text);
    }

    @Test
    void redactsWholeMap() {
        Map<String, String> in = new LinkedHashMap<>();
        in.put("server.port", "8080");
        in.put("spring.datasource.password", "pg_pw");
        Map<String, String> out = redactor.redact(in);
        assertThat(out).containsEntry("server.port", "8080");
        assertThat(out).containsEntry("spring.datasource.password", "***");
    }
}
