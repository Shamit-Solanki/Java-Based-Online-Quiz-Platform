package com.quiz.servlet.creator;

import com.quiz.dao.QuizDAO;
import com.quiz.exception.QuizException;
import com.quiz.model.Notification;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Quiz history of the logged-in creator, with submit-for-approval and delete actions. */
@WebServlet("/creator/quizzes")
public class CreatorQuizzesServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final transient QuizDAO dao = new QuizDAO();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        req.setAttribute("quizzes", dao.findByCreator(currentUser(req).getId()));
        render(req, resp, "creator/quizzes", "My quizzes", "quizzes");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException, QuizException {
        User me = currentUser(req);
        int id = intParam(req, "id");
        String action = param(req, "action");

        if ("submit".equals(action)) {
            dao.submitForApproval(id, me.getId());
            NotificationService.toAdmins(Notification.Level.WARNING,
                    me.getName() + " submitted a quiz for approval.", "/admin/quizzes");
            flash(req, "success", "Quiz sent to the administrator for approval.");
        } else if ("delete".equals(action)) {
            if (!dao.deleteOwned(id, me.getId())) {
                throw new QuizException("Only your own draft or rejected quizzes can be deleted.");
            }
            flash(req, "success", "Quiz deleted.");
        } else {
            throw new QuizException("Unknown action.");
        }
        redirect(req, resp, "/creator/quizzes");
    }
}
