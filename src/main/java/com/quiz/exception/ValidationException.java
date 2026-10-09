package com.quiz.exception;

/** Thrown when submitted form data fails validation. */
public class ValidationException extends QuizException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
