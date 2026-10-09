package com.quiz.util;

import com.quiz.model.Admin;
import com.quiz.model.Participant;
import com.quiz.model.QuizCreator;
import com.quiz.model.Role;
import com.quiz.model.User;

/** Factory that builds the right User subclass for a role (polymorphism). */
public final class UserFactory {

    private UserFactory() {
    }

    public static User create(int id, String name, String email, String passwordHash, Role role) {
        switch (role) {
            case ADMIN:
                return new Admin(id, name, email, passwordHash);
            case CREATOR:
                return new QuizCreator(id, name, email, passwordHash);
            case PARTICIPANT:
            default:
                return new Participant(id, name, email, passwordHash);
        }
    }
}
