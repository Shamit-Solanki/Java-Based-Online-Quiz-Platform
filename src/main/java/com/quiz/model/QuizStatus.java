package com.quiz.model;

/**
 * Lifecycle of a quiz:
 * DRAFT -> PENDING (submitted by creator) -> APPROVED or REJECTED (by admin).
 * Only APPROVED quizzes are visible to participants.
 */
public enum QuizStatus {
    DRAFT("Draft", "neutral"),
    PENDING("Pending approval", "warning"),
    APPROVED("Approved", "success"),
    REJECTED("Rejected", "danger");

    private final String label;
    private final String badge;

    QuizStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    /** CSS badge modifier used by the stylesheet. */
    public String getBadge() {
        return badge;
    }

    /** A creator may edit or delete a quiz only before it is approved. */
    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }
}
