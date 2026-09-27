package dev.nazhida.kivra.permissions;

import java.util.Locale;

public final class DurationParser {
    private DurationParser() {}

    public static long parseMillis(String input) {
        String value = input.toLowerCase(Locale.ROOT).trim();
        if (value.length() < 2) throw new IllegalArgumentException("Invalid duration");
        long amount = Long.parseLong(value.substring(0, value.length() - 1));
        char unit = value.charAt(value.length() - 1);
        long multiplier = switch (unit) {
            case 'm' -> 60_000L;
            case 'h' -> 3_600_000L;
            case 'd' -> 86_400_000L;
            case 'w' -> 604_800_000L;
            default -> throw new IllegalArgumentException("Use m, h, d or w");
        };
        return Math.multiplyExact(amount, multiplier);
    }
}
