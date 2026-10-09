package com.quiz.model;

/** Administrator: manages users, approves quizzes and configures the system. */
public class Admin extends User {

    private static final long serialVersionUID = 1L;

    public Admin(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, Role.ADMIN);
    }

    @Override
    public String getRoleDescription() {
        return "Manages users, approves quiz content and configures the system.";
    }
}
