package com.quiz.servlet.admin;

import com.quiz.dao.QuizDAO;
import com.quiz.exception.QuizException;
import com.quiz.model.Notification;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.QuizStatus;
import com.quiz.servlet.BaseServlet;
import com.quiz.service.NotificationService;
import com.quiz.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Quiz content management: list all quizzes, approve / reject pending ones, delete. */
@WebServlet("/admin/quizzes")
public class AdminQuizzesServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final transient QuizDAO dao = new QuizDAO();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        req.setAttribute("quizzes", dao.findAll());
        req.setAttribute("counts", dao.countByStatus());
        render(req, resp, "admin/quizzes", "Quiz content", "quizzes");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException, QuizException {
        String action = param(req, "action");
        int id = intParam(req, "id");
        Quiz<Question> quiz = dao.findById(id);
        if (quiz == null) {
            throw new QuizException("Quiz not found.");
        }
        String back = "preview".equals(param(req, "from")) ? "/admin/quiz?id=" + id : "/admin/quizzes";

        switch (action) {
            case "approve":
                dao.review(id, QuizStatus.APPROVED, null);
                NotificationService.toUser(quiz.getCreatorId(), Notification.Level.SUCCESS,
                        "Your quiz \"" + quiz.getTitle() + "\" was approved and is now live.", "/creator/quizzes");
                flash(req, "success", "\"" + quiz.getTitle() + "\" approved and published to participants.");
                break;
            case "reject":
                String note = Validator.required(req.getParameter("note"), "Rejection reason", 500);
                dao.review(id, QuizStatus.REJECTED, note);
                NotificationService.toUser(quiz.getCreatorId(), Notification.Level.WARNING,
                        "Your quiz \"" + quiz.getTitle() + "\" was rejected: " + note, "/creator/quizzes");
                flash(req, "info", "\"" + quiz.getTitle() + "\" was rejected and the creator has been notified.");
                break;
            case "delete":
                dao.delete(id);
                NotificationService.toUser(quiz.getCreatorId(), Notification.Level.WARNING,
                        "Your quiz \"" + quiz.getTitle() + "\" was removed by an administrator.", null);
                flash(req, "success", "Quiz deleted.");
                back = "/admin/quizzes";
                break;
            default:
                throw new QuizException("Unknown action.");
        }
        redirect(req, resp, back);
    }
}
