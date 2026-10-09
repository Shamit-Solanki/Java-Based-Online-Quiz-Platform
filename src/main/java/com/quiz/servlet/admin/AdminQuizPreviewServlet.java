package com.quiz.servlet.admin;

import com.quiz.dao.QuizDAO;
import com.quiz.exception.QuizException;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.servlet.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Read-only view of a quiz (with answer key) so the admin can decide on approval. */
@WebServlet("/admin/quiz")
public class AdminQuizPreviewServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String failurePath(HttpServletRequest req) {
        return "/admin/quizzes";
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, QuizException {
        QuizDAO dao = new QuizDAO();
        Quiz<Question> quiz = dao.findById(intParam(req, "id"));
        if (quiz == null) {
            throw new QuizException("Quiz not found.");
        }
        req.setAttribute("quiz", quiz);
        req.setAttribute("questions", dao.getQuestions(quiz.getId()));
        render(req, resp, "admin/quiz-preview", "Review quiz", "quizzes");
    }
}
