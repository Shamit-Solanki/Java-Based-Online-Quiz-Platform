package com.quiz.model;

/** Quiz creator: builds quizzes, reviews results and talks to participants. */
public class QuizCreator extends User {

    private static final long serialVersionUID = 1L;

    public QuizCreator(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, Role.CREATOR);
    }

    @Override
    public String getRoleDescription() {
        return "Creates Java quizzes, reviews results and gives feedback.";
    }
}
