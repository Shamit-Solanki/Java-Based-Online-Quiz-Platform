package com.quiz.servlet.admin;

import com.quiz.dao.QuizDAO;
import com.quiz.dao.ResultDAO;
import com.quiz.dao.StatsDAO;
import com.quiz.servlet.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Platform-wide performance reports with graphical summaries. */
@WebServlet("/admin/reports")
public class AdminReportsServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        StatsDAO stats = new StatsDAO();
        List<Map<String, Object>> perDay = stats.attemptsPerDay(14);
        Map<String, Integer> bands = stats.scoreDistribution();

        req.setAttribute("totals", stats.totals());
        req.setAttribute("quizPerformance", stats.quizPerformance(10));
        req.setAttribute("perDay", perDay);
        req.setAttribute("maxDay", Math.max(1, perDay.stream().mapToInt(m -> (Integer) m.get("count")).max().orElse(0)));
        req.setAttribute("bands", bands);
        req.setAttribute("maxBand", Math.max(1, bands.values().stream().mapToInt(Integer::intValue).max().orElse(0)));
        req.setAttribute("quizzes", new QuizDAO().findAll());
        req.setAttribute("leaders", new ResultDAO().leaderboard(10));
        render(req, resp, "admin/reports", "Performance reports", "reports");
    }
}
