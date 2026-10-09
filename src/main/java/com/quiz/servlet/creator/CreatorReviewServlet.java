package com.quiz.servlet.creator;

import com.quiz.dao.ResultDAO;
import com.quiz.exception.QuizException;
import com.quiz.exception.ValidationException;
import com.quiz.model.Notification;
import com.quiz.model.QuizResult;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.service.NotificationService;
import com.quiz.dao.QuizDAO;
import com.quiz.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Detailed review of one attempt: see every answer, adjust the score, leave feedback. */
@WebServlet("/creator/review")
public class CreatorReviewServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final transient ResultDAO results = new ResultDAO();

    /** Stay on the same review page after a problem; fall back to the list for bad ids. */
    @Override
    protected String failurePath(HttpServletRequest req) {
        String id = req.getParameter("id");
        boolean posted = "POST".equalsIgnoreCase(req.getMethod());
        return posted && id != null && id.matches("\\d+") ? "/creator/review?id=" + id : "/creator/results";
    }

    /** Loads the attempt and makes sure it belongs to a quiz of the current creator. */
    private QuizResult loadOwned(HttpServletRequest req, int attemptId) throws SQLException, QuizException {
        QuizResult attempt = results.findById(attemptId);
        if (attempt == null || !attempt.isSubmitted()
                || new QuizDAO().findById(attempt.getQuizId()).getCreatorId() != currentUser(req).getId()) {
            throw new QuizException("Result not found.");
        }
        return attempt;
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, QuizException {
        QuizResult attempt = loadOwned(req, intParam(req, "id"));
        req.setAttribute("attempt", attempt);
        req.setAttribute("details", results.getAnswerDetails(attempt.getAttemptId(), attempt.getQuizId()));
        render(req, resp, "creator/review", "Review result", "results");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException, QuizException {
        User me = currentUser(req);
        int id = intParam(req, "id");
        QuizResult attempt = loadOwned(req, id);

        int score = Validator.intInRange(req.getParameter("score"), "Score", 0, attempt.getTotal());
        String feedback = Validator.optional(req.getParameter("feedback"), "Feedback", 1000);
        if (feedback.isEmpty() && score == attempt.getScore()) {
            throw new ValidationException("Change the score or write some feedback before saving.");
        }
        if (!results.grade(id, me.getId(), score, feedback)) {
            throw new QuizException("The result could not be updated.");
        }
        NotificationService.toUser(attempt.getParticipantId(), Notification.Level.SUCCESS,
                me.getName() + " reviewed your result for \"" + attempt.getQuizTitle() + "\".",
                "/participant/report?id=" + id);
        flash(req, "success", "Review saved and the participant has been notified.");
        redirect(req, resp, "/creator/review?id=" + id);
    }
}
