package com.quiz.exception;

/**
 * Checked, user-facing application exception.
 * The message of a QuizException is safe to show to the end user
 * (e.g. "Email already registered"), unlike raw SQL errors.
 */
public class QuizException extends Exception {

    private static final long serialVersionUID = 1L;

    public QuizException(String message) {
        super(message);
    }

    public QuizException(String message, Throwable cause) {
        super(message, cause);
    }
}
