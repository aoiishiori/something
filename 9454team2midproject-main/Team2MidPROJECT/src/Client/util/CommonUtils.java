package Client.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * CommonUtils — shared utility class for JSON parsing, date handling,
 * and other common operations used across the client application.
 */
public class CommonUtils {

    // =====================================================
    // JSON UTILITIES
    // =====================================================

    /**
     * Extracts a string field value from a flat JSON object string.
     * Works for simple string and number values.
     * e.g. extractField("{\"role\":\"BUYER\"}", "role") → "BUYER"
     */
    public static String extractField(String json, String fieldName) {
        if (json == null || fieldName == null || json.isEmpty()) return "";

        String key = "\"" + fieldName + "\"";
        int keyIdx = json.indexOf(key);
        if (keyIdx == -1) return "";

        int colonIdx = json.indexOf(":", keyIdx + key.length());
        if (colonIdx == -1) return "";

        int valueStart = colonIdx + 1;
        while (valueStart < json.length() && json.charAt(valueStart) == ' ') valueStart++;
        if (valueStart >= json.length()) return "";

        char first = json.charAt(valueStart);
        if (first == '"') {
            int end = valueStart + 1;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                end++;
            }
            return json.substring(valueStart + 1, end)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n");
        } else {
            int end = valueStart;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == ',' || c == '}' || c == ']' || c == '\n') break;
                end++;
            }
            return json.substring(valueStart, end).trim();
        }
    }

    /**
     * Escapes a string value for safe inclusion inside a JSON string.
     * e.g. escapeJson("hello \"world\"") → "hello \\\"world\\\""
     */
    public static String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Legacy alias kept so no controller code breaks.
     * Points to escapeJson — XML escaping is no longer used.
     */
    public static String escapeXML(String text) {
        return escapeJson(text);
    }

    /**
     * Legacy alias — kept so no controller code breaks during transition.
     * Use extractField() for new code.
     */
    public static String extractTag(String json, String fieldName) {
        return extractField(json, fieldName);
    }

    // =====================================================
    // DATE UTILITIES
    // =====================================================

    /** Standard date format used throughout the application (yyyy-MM-dd) */
    public static final DateTimeFormatter STANDARD_DATE_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Normalizes a date string to the standard format (yyyy-MM-dd).
     */
    public static String normalizeExpiryDate(String raw) {
        if (raw == null) return null;

        String v = raw.trim();
        if (v.isEmpty() || "YYYY-MM-DD".equalsIgnoreCase(v)) return null;

        LocalDate d = tryParseDate(v, STANDARD_DATE_FORMAT);
        if (d == null) d = tryParseDate(v, DateTimeFormatter.ofPattern("M/d/uuuu"));
        if (d == null) d = tryParseDate(v, DateTimeFormatter.ofPattern("MM/dd/uuuu"));
        if (d == null) d = tryParseDate(v, DateTimeFormatter.ofPattern("uuuu/M/d"));
        if (d == null) d = tryParseDate(v, DateTimeFormatter.ofPattern("uuuu/MM/dd"));

        if (d == null) return null;
        return d.format(STANDARD_DATE_FORMAT);
    }

    /**
     * Attempts to parse a date string using the specified format.
     */
    public static LocalDate tryParseDate(String value, DateTimeFormatter fmt) {
        if (value == null || fmt == null) return null;
        try {
            return LocalDate.parse(value, fmt);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    /** Validates if a date string is in a valid format. */
    public static boolean isValidDate(String dateStr) {
        return normalizeExpiryDate(dateStr) != null;
    }

    /** Gets the current date in standard format (yyyy-MM-dd). */
    public static String getCurrentDate() {
        return LocalDate.now().format(STANDARD_DATE_FORMAT);
    }

    /** Checks if a date is in the past (expired). */
    public static boolean isExpired(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        LocalDate date = tryParseDate(dateStr, STANDARD_DATE_FORMAT);
        if (date == null) return false;
        return date.isBefore(LocalDate.now());
    }

    /** Checks if a date is in the future. */
    public static boolean isFutureDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        LocalDate date = tryParseDate(dateStr, STANDARD_DATE_FORMAT);
        if (date == null) return false;
        return date.isAfter(LocalDate.now());
    }
}