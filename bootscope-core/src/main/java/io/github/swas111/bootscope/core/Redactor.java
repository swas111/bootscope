package io.github.swas111.bootscope.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Masks secrets and personal data before anything leaves a tool.
 *
 * <p>Two levels of defence (CLAUDE.md hard rule #2 — every response passes through here):
 * <ul>
 *   <li><b>Key-aware</b> — {@link #redactValue(String, String)} masks a value whose key names a
 *       secret (password, token, secret, api-key, credential, ...) regardless of the value.</li>
 *   <li><b>Content scan</b> — {@link #redact(String)} masks secret-shaped substrings anywhere:
 *       PEM private keys, JWTs, cloud keys (AWS {@code AKIA}, Google {@code AIza}),
 *       credential-bearing URLs, bearer tokens, emails, and long digit runs.</li>
 * </ul>
 * Matches are replaced wholesale with {@value #MASK}; over-masking is acceptable, leaking is not.
 */
public final class Redactor {

    public static final String MASK = "***";

    /** Key names whose values are always masked. */
    private static final Pattern SENSITIVE_KEY = Pattern.compile(
            "(?i).*(password|passwd|pwd|secret|token|api[_-]?key|access[_-]?key|private[_-]?key"
                    + "|client[_-]?secret|credential|authorization|auth[_-]?key|session[_-]?id|cookie).*");

    // Order matters: more specific patterns first.
    private static final Pattern[] VALUE_PATTERNS = {
            // PEM private key blocks.
            Pattern.compile("-----BEGIN (?:[A-Z ]+ )?PRIVATE KEY-----[\\s\\S]*?-----END (?:[A-Z ]+ )?PRIVATE KEY-----"),
            // JWTs (three base64url segments).
            Pattern.compile("eyJ[A-Za-z0-9_-]{5,}\\.[A-Za-z0-9_-]{5,}\\.[A-Za-z0-9_-]{5,}"),
            // Credential-bearing URLs: scheme://user:pass@host  (bootscope:allow-secret)
            Pattern.compile("[a-zA-Z][a-zA-Z0-9+.-]*://[^\\s/:@]+:[^\\s/@]+@[^\\s]+"),
            // AWS access key id.
            Pattern.compile("AKIA[0-9A-Z]{16}"),
            // Google API key.
            Pattern.compile("AIza[0-9A-Za-z_-]{35}"),
            // GitHub tokens.
            Pattern.compile("gh[pousr]_[0-9A-Za-z]{20,}"),
            // Slack tokens.
            Pattern.compile("xox[baprs]-[0-9A-Za-z-]{10,}"),
            // Bearer tokens in text.
            Pattern.compile("(?i)bearer\\s+[A-Za-z0-9._-]{10,}"),
            // Email addresses (personal data).
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"),
            // Long digit runs (card / account numbers, phone-ish), 12+ digits possibly spaced/dashed.
            Pattern.compile("\\b(?:\\d[ -]?){12,}\\d\\b"),
    };

    /** Scan free text and mask any secret-shaped or personal substrings. */
    public String redact(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String out = text;
        for (Pattern p : VALUE_PATTERNS) {
            out = p.matcher(out).replaceAll(MASK);
        }
        return out;
    }

    /** Mask a value whose key names a secret; otherwise content-scan the value. */
    public String redactValue(String key, String value) {
        if (value == null) {
            return null;
        }
        if (key != null && SENSITIVE_KEY.matcher(key).matches()) {
            return MASK;
        }
        return redact(value);
    }

    /** True if a key name denotes a secret (its value should never be shown). */
    public boolean isSensitiveKey(String key) {
        return key != null && SENSITIVE_KEY.matcher(key).matches();
    }

    /** Redact every entry of a key/value map, preserving order. */
    public Map<String, String> redact(Map<String, String> map) {
        if (map == null) {
            return Map.of();
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            out.put(e.getKey(), redactValue(e.getKey(), e.getValue()));
        }
        return out;
    }
}
