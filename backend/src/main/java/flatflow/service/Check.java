package flatflow.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Input checks from the D1 I/O spec, "Input fields, formats and validation rules". */
public final class Check {
    private Check() {}

    /** Required text, trimmed, with a length limit. Returns the trimmed text. */
    public static String text(String value, String field, int min, int max) {
        String v = value == null ? "" : value.trim();
        if (v.length() < min) throw ApiException.badRequest(min <= 1 ? field + " is required" : field + " must be at least " + min + " characters");
        if (v.length() > max) throw ApiException.badRequest(field + " must be at most " + max + " characters");
        return v;
    }

    /** Optional text: empty is fine. */
    public static String optionalText(String value, String field, int max) {
        String v = value == null ? "" : value.trim();
        if (v.length() > max) throw ApiException.badRequest(field + " must be at most " + max + " characters");
        return v;
    }

    /** A date in YYYY-MM-DD form. */
    public static String date(String value, String field) {
        if (value == null || value.isBlank()) throw ApiException.badRequest(field + " is required");
        try {
            return LocalDate.parse(value.trim()).toString();
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest(field + " must look like 2026-11-20");
        }
    }

    public static int required(Integer value, String field) {
        if (value == null || value <= 0) throw ApiException.badRequest(field + " is required");
        return value;
    }
}
