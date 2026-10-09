package com.quiz.servlet.participant;

import com.quiz.dao.ResultDAO;
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

/** Full performance overview: trend chart and the complete attempt history. */
@WebServlet("/participant/reports")
public class ParticipantReportsServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        User me = currentUser(req);
        List<QuizResult> results = new ResultDAO().findByParticipant(me.getId());
        req.setAttribute("results", results);
        req.setAttribute("summary", PerformanceSummary.of(results));
        render(req, resp, "participant/reports", "Performance reports", "reports");
    }
}
