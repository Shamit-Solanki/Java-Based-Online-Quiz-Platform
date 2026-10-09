package com.quiz.servlet.participant;

import com.quiz.dao.QuizDAO;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.util.AppSettings;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Catalogue of approved quizzes the participant can start. */
@WebServlet("/participant/quizzes")
public class ParticipantQuizzesServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        User me = currentUser(req);
        req.setAttribute("quizzes", new QuizDAO().findApprovedFor(me.getId()));
        req.setAttribute("maxAttempts", AppSettings.getInt("max_attempts"));
        render(req, resp, "participant/quizzes", "Available quizzes", "quizzes");
    }
}
