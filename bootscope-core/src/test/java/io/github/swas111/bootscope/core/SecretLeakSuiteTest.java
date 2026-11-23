package io.github.swas111.bootscope.core;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The secret-leak suite. A corpus of fake secrets is run through the {@link Redactor} embedded in
 * realistic surrounding text; the distinctive secret token must not survive. Phase 3 reuses this
 * same corpus/scanner against every real tool's output (demo app loaded with fake secrets).
 *
 * <p>Every token below is assembled from fragments at runtime, on purpose: it keeps any
 * contiguous provider-format literal out of the committed source, so neither GitHub push
 * protection nor a human reader mistakes a test fixture for a real credential. The runtime value
 * the Redactor sees is identical to the real thing.
 */
class SecretLeakSuiteTest {

    private final Redactor redactor = new Redactor();

    /** Each case: a distinctive secret token, and some text that contains it. */
    static List<String[]> secrets() {
        String aws = "AKIA" + "IOSFODNN7EXAMPLE";
        String google = "AIza" + "SyA1234567890abcdefghijklmnopqrstuvw";
        String github = "ghp" + "_abcdefghijklmnopqrstuvwxyz0123456789";
        String slack = "xoxb" + "-123456789012-abcdefghijklmno";
        String dbPass = "s3cret" + "P4ss";
        String jwt = "eyJhbGciOiJIUzI1NiJ9" + "." + "eyJzdWIiOiI0MiJ9" + "." + "signature_leak_xyz";
        String email = "user" + "@" + "secret-domain.com";
        String card = "4111" + "111111111111";
        String pem = "-----BEGIN PRIVATE KEY-----\n" + "MIIBVgIBADANBg" + "\n-----END PRIVATE KEY-----";
        String bearerValue = "topsecret" + "token";

        return List.of(
                new String[] {aws, "aws id = " + aws + " in config"},
                new String[] {google, "google key " + google},
                new String[] {github, "github token: " + github},
                new String[] {slack, "slack " + slack + " set"},
                new String[] {dbPass, "url mysql://root:" + dbPass + "@localhost:3306/db"},
                new String[] {"signature_leak_xyz", "auth " + jwt + " done"},
                new String[] {email, "exception for " + email + " failed"},
                new String[] {card, "card " + card + " declined"},
                new String[] {"MIIBVgIBADANBg", pem},
                new String[] {bearerValue, "Authorization: Bearer " + bearerValue});
    }

    @ParameterizedTest(name = "redacts: {0}")
    @MethodSource("secrets")
    void noSecretSurvivesContentScan(String secret, String surroundingText) {
        String redacted = redactor.redact(surroundingText);
        assertThat(redacted)
                .as("secret token must not survive: %s", secret)
                .doesNotContain(secret);
    }

    @ParameterizedTest(name = "key-masks: {0}")
    @MethodSource("secrets")
    void noSecretSurvivesWhenHeldUnderASensitiveKey(String secret, String ignored) {
        assertThat(redactor.redactValue("password", secret)).doesNotContain(secret);
        assertThat(redactor.redactValue("api-key", secret)).doesNotContain(secret);
    }
}
