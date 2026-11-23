package io.github.swas111.bootscope.core;

import java.time.Duration;
import java.util.Locale;

/** Small, locale-stable formatting helpers for human-readable tool output. */
public final class Format {

    private Format() {
    }

    /** Bytes as KB/MB/GB with one decimal (e.g. 512.0 MB). */
    public static String bytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        String[] units = {"KB", "MB", "GB", "TB"};
        double value = bytes;
        int i = -1;
        do {
            value /= 1024.0;
            i++;
        } while (value >= 1024 && i < units.length - 1);
        return String.format(Locale.ROOT, "%.1f %s", value, units[i]);
    }

    /** Milliseconds with no more than one decimal (e.g. 820 ms, 3.5 ms). */
    public static String millis(double ms) {
        if (ms >= 10 || ms == Math.floor(ms)) {
            return String.format(Locale.ROOT, "%.0f ms", ms);
        }
        return String.format(Locale.ROOT, "%.1f ms", ms);
    }

    public static String percent(double fraction) {
        return String.format(Locale.ROOT, "%.0f%%", fraction * 100);
    }

    /** Compact uptime (e.g. 1d 2h 3m, or 45s). */
    public static String uptime(Duration d) {
        if (d == null) {
            return "unknown";
        }
        long s = Math.max(0, d.getSeconds());
        long days = s / 86400;
        long hours = (s % 86400) / 3600;
        long mins = (s % 3600) / 60;
        long secs = s % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (days > 0 || hours > 0) {
            sb.append(hours).append("h ");
        }
        if (days > 0 || hours > 0 || mins > 0) {
            sb.append(mins).append("m ");
        }
        sb.append(secs).append('s');
        return sb.toString();
    }
}
