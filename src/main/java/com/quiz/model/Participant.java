package com.quiz.model;

/** Participant: takes timed quizzes and reviews personal performance. */
public class Participant extends User {

    private static final long serialVersionUID = 1L;

    public Participant(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, Role.PARTICIPANT);
    }

    @Override
    public String getRoleDescription() {
        return "Takes timed Java quizzes and tracks performance.";
    }
}
