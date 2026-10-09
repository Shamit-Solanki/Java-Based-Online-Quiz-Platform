package com.quiz.servlet.participant;

import com.quiz.dao.MessageDAO;
import com.quiz.dao.QuizDAO;
import com.quiz.dao.ReminderDAO;
import com.quiz.dao.ResultDAO;
import com.quiz.model.LeaderboardEntry;
import com.quiz.model.QuizResult;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.service.PerformanceSummary;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/** Participant home: history, performance snapshot, reminders, leaderboard and inbox. */
@WebServlet("/participant/dashboard")
public class ParticipantDashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        User me = currentUser(req);
        ResultDAO resultDAO = new ResultDAO();
        List<QuizResult> results = resultDAO.findByParticipant(me.getId());
        List<LeaderboardEntry> board = resultDAO.leaderboard(0);

        LeaderboardEntry mine = board.stream().filter(e -> e.getUserId() == me.getId()).findFirst().orElse(null);

        req.setAttribute("summary", PerformanceSummary.of(results));
        req.setAttribute("history", results.stream().limit(6).collect(Collectors.toList()));
        req.setAttribute("top", board.stream().limit(5).collect(Collectors.toList()));
        req.setAttribute("mine", mine);
        req.setAttribute("participants", board.size());
        req.setAttribute("reminders", new ReminderDAO().findByParticipant(me.getId()).stream()
                .limit(4).collect(Collectors.toList()));
        req.setAttribute("inbox", new MessageDAO().recent(me.getId(), 4));
        req.setAttribute("availableQuizzes", new QuizDAO().findApprovedFor(me.getId()).size());
        render(req, resp, "participant/dashboard", "My dashboard", "dashboard");
    }
}
