package com.quiz.model;

/** The three user types supported by the platform. */
public enum Role {

    ADMIN("Admin", "/admin/dashboard"),
    CREATOR("Quiz Creator", "/creator/dashboard"),
    PARTICIPANT("Participant", "/participant/dashboard");

    private final String label;
    private final String homePath;

    Role(String label, String homePath) {
        this.label = label;
        this.homePath = homePath;
    }

    public String getLabel() {
        return label;
    }

    public String getHomePath() {
        return homePath;
    }

    /** Lower-case URL prefix, e.g. "admin" for ADMIN. */
    public String getPrefix() {
        return name().toLowerCase();
    }

    /** Safe parse that returns null instead of throwing. */
    public static Role fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
