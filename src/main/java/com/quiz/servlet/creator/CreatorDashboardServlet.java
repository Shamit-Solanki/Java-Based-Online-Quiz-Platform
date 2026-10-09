package com.quiz.servlet.creator;

import com.quiz.dao.MessageDAO;
import com.quiz.dao.QuizDAO;
import com.quiz.dao.ResultDAO;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.QuizResult;
import com.quiz.model.QuizStatus;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/** Creator home: quiz counts, performance overview, newest submissions and inbox. */
@WebServlet("/creator/dashboard")
public class CreatorDashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        User me = currentUser(req);
        List<Quiz<Question>> quizzes = new QuizDAO().findByCreator(me.getId());
        List<QuizResult> results = new ResultDAO().findByCreator(me.getId());

        long approved = quizzes.stream().filter(q -> q.getStatus() == QuizStatus.APPROVED).count();
        long pending = quizzes.stream().filter(q -> q.getStatus() == QuizStatus.PENDING).count();
        double avg = results.stream().mapToDouble(QuizResult::getPercentage).average().orElse(0);
        long passed = results.stream().filter(QuizResult::isPassed).count();
        long awaitingReview = results.stream().filter(r -> !r.isReviewed()).count();

        // quizzes that already have attempts, for the performance bars
        List<Quiz<Question>> withAttempts = quizzes.stream()
                .filter(q -> q.getAttemptCount() > 0)
                .collect(Collectors.toList());

        req.setAttribute("quizzes", quizzes);
        req.setAttribute("approvedCount", approved);
        req.setAttribute("pendingCount", pending);
        req.setAttribute("attemptCount", results.size());
        req.setAttribute("avgPercent", Math.round(avg * 10) / 10.0);
        req.setAttribute("passRate", results.isEmpty() ? 0 : Math.round(passed * 100.0 / results.size()));
        req.setAttribute("awaitingReview", awaitingReview);
        req.setAttribute("performance", withAttempts);
        req.setAttribute("recentResults", results.stream().limit(5).collect(Collectors.toList()));
        req.setAttribute("rejected", quizzes.stream().filter(q -> q.getStatus() == QuizStatus.REJECTED)
                .collect(Collectors.toList()));
        req.setAttribute("inbox", new MessageDAO().recent(me.getId(), 5));
        render(req, resp, "creator/dashboard", "Creator dashboard", "dashboard");
    }
}
