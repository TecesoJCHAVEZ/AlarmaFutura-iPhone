package com.tecesoj.alarmafutura;

public final class AlarmRules {
    private AlarmRules() {}

    public static String normalizedTitle(String text) {
        return text == null ? "" : text.trim();
    }

    public static void validate(String text, long timestampMillis, long nowMillis) {
        if (normalizedTitle(text).isEmpty()) {
            throw new IllegalArgumentException("Escribe el nombre de la alarma.");
        }
        if (timestampMillis < nowMillis + 60_000L) {
            throw new IllegalArgumentException("La fecha debe estar al menos un minuto en el futuro.");
        }
    }
}
