package com.quiz.servlet.participant;

import com.quiz.dao.ResultDAO;
import com.quiz.model.LeaderboardEntry;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Overall ranking of all participants plus the current user's standing. */
@WebServlet("/participant/leaderboard")
public class LeaderboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        User me = currentUser(req);
        List<LeaderboardEntry> board = new ResultDAO().leaderboard(0);
        req.setAttribute("board", board);
        req.setAttribute("mine", board.stream().filter(e -> e.getUserId() == me.getId()).findFirst().orElse(null));
        render(req, resp, "participant/leaderboard", "Leaderboard", "leaderboard");
    }
}
