package com.quiz.util;

import com.quiz.exception.ValidationException;
import java.util.regex.Pattern;

/** Server-side validation helpers (client-side checks are only a convenience). */
public final class Validator {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validator() {
    }

    public static String required(String value, String field, int maxLength) throws ValidationException {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) {
            throw new ValidationException(field + " is required.");
        }
        if (v.length() > maxLength) {
            throw new ValidationException(field + " must be at most " + maxLength + " characters.");
        }
        return v;
    }

    public static String optional(String value, String field, int maxLength) throws ValidationException {
        String v = value == null ? "" : value.trim();
        if (v.length() > maxLength) {
            throw new ValidationException(field + " must be at most " + maxLength + " characters.");
        }
        return v;
    }

    public static String email(String value) throws ValidationException {
        String v = required(value, "Email", 150).toLowerCase();
        if (!EMAIL.matcher(v).matches()) {
            throw new ValidationException("Please enter a valid email address.");
        }
        return v;
    }

    public static String password(String value) throws ValidationException {
        if (value == null || value.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
        if (value.length() > 100) {
            throw new ValidationException("Password is too long.");
        }
        return value;
    }

    public static int intInRange(String value, String field, int min, int max) throws ValidationException {
        try {
            int n = Integer.parseInt(value == null ? "" : value.trim());
            if (n < min || n > max) {
                throw new ValidationException(field + " must be between " + min + " and " + max + ".");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a number.");
        }
    }

    public static String option(String value, String field) throws ValidationException {
        String v = value == null ? "" : value.trim().toUpperCase();
        if (!v.matches("[ABCD]")) {
            throw new ValidationException(field + " must be A, B, C or D.");
        }
        return v;
    }
}
