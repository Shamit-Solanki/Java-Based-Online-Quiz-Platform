package com.quiz.servlet.creator;

import com.quiz.dao.ResultDAO;
import com.quiz.model.QuizResult;
import com.quiz.servlet.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** All submitted attempts on the creator's quizzes, ready to review and grade. */
@WebServlet("/creator/results")
public class CreatorResultsServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        List<QuizResult> results = new ResultDAO().findByCreator(currentUser(req).getId());
        req.setAttribute("results", results);
        // sorted, de-duplicated quiz titles for the filter drop-down
        req.setAttribute("quizTitles", new TreeSet<>(results.stream()
                .map(QuizResult::getQuizTitle).collect(Collectors.toSet())));
        render(req, resp, "creator/results", "Quiz results", "results");
    }
}
