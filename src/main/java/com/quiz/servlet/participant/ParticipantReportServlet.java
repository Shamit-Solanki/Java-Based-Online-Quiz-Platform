package com.quiz.servlet.participant;

import com.quiz.dao.QuizDAO;
import com.quiz.dao.ResultDAO;
import com.quiz.exception.QuizException;
import com.quiz.model.AnswerDetail;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.QuizResult;
import com.quiz.servlet.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Detailed report of one attempt: score, grade, every answer with explanation, creator feedback. */
@WebServlet("/participant/report")
public class ParticipantReportServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String failurePath(HttpServletRequest req) {
        return "/participant/reports";
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, QuizException {
        ResultDAO dao = new ResultDAO();
        QuizResult attempt = dao.findById(intParam(req, "id"));
        if (attempt == null || attempt.getParticipantId() != currentUser(req).getId()) {
            throw new QuizException("Report not found.");
        }
        if (!attempt.isSubmitted()) {
            // unfinished attempt: send the participant back into the quiz
            redirect(req, resp, "/participant/take?id=" + attempt.getQuizId());
            return;
        }
        List<AnswerDetail> details = dao.getAnswerDetails(attempt.getAttemptId(), attempt.getQuizId());
        long correct = details.stream().filter(AnswerDetail::isCorrect).count();
        long skipped = details.stream().filter(a -> !a.isAnswered()).count();
        Quiz<Question> quiz = new QuizDAO().findById(attempt.getQuizId());

        req.setAttribute("attempt", attempt);
        req.setAttribute("quiz", quiz);
        req.setAttribute("details", details);
        req.setAttribute("correctCount", correct);
        req.setAttribute("skippedCount", skipped);
        req.setAttribute("wrongCount", details.size() - correct - skipped);
        req.setAttribute("justFinished", "1".equals(req.getParameter("done")));
        render(req, resp, "participant/report", "Performance report", "reports");
    }
}
