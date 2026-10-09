package com.quiz.servlet.admin;

import com.quiz.dao.NotificationDAO;
import com.quiz.dao.QuizDAO;
import com.quiz.dao.StatsDAO;
import com.quiz.dao.UserDAO;
import com.quiz.model.QuizStatus;
import com.quiz.model.Role;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Admin home: key numbers, charts, pending approvals and system alerts. */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        User me = currentUser(req);
        UserDAO users = new UserDAO();
        QuizDAO quizzes = new QuizDAO();
        StatsDAO stats = new StatsDAO();

        Map<Role, Integer> roleCounts = users.countByRole();
        Map<QuizStatus, Integer> statusCounts = quizzes.countByStatus();
        int totalUsers = roleCounts.values().stream().mapToInt(Integer::intValue).sum();
        int totalQuizzes = statusCounts.values().stream().mapToInt(Integer::intValue).sum();

        List<Map<String, Object>> perDay = stats.attemptsPerDay(7);
        int maxDay = perDay.stream().mapToInt(m -> (Integer) m.get("count")).max().orElse(0);

        req.setAttribute("roleCounts", roleCounts);
        req.setAttribute("statusCounts", statusCounts);
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("totalQuizzes", totalQuizzes);
        req.setAttribute("totals", stats.totals());
        req.setAttribute("quizPerformance", stats.quizPerformance(6));
        req.setAttribute("perDay", perDay);
        req.setAttribute("maxDay", Math.max(maxDay, 1));
        req.setAttribute("pending", quizzes.findByStatus(QuizStatus.PENDING));
        req.setAttribute("alerts", new NotificationDAO().findByUser(me.getId(), 6));
        req.setAttribute("recentUsers", users.findAll().stream().limit(5).toList());
        render(req, resp, "admin/dashboard", "Admin dashboard", "dashboard");
    }
}
