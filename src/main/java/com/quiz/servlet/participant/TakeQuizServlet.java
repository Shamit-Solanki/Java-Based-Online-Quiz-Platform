package com.quiz.servlet.participant;

import com.quiz.dao.QuizDAO;
import com.quiz.dao.ResultDAO;
import com.quiz.exception.QuizException;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.QuizResult;
import com.quiz.model.QuizStatus;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.service.QuizGrader;
import com.quiz.util.AppSettings;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Starts (or resumes) a timed attempt.
 * The clock starts when the attempt row is created and is measured by the database,
 * so refreshing the page or editing the JavaScript timer cannot give extra time.
 */
@WebServlet("/participant/take")
public class TakeQuizServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final transient QuizDAO quizDAO = new QuizDAO();
    private final transient ResultDAO resultDAO = new ResultDAO();

    @Override
    protected String failurePath(HttpServletRequest req) {
        return "/participant/quizzes";
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, QuizException {
        User me = currentUser(req);
        int quizId = intParam(req, "id");

        Quiz<Question> quiz = quizDAO.findById(quizId);
        if (quiz == null || quiz.getStatus() != QuizStatus.APPROVED) {
            throw new QuizException("That quiz is not available.");
        }

        QuizResult attempt = resultDAO.findOpenAttempt(quizId, me.getId());
        if (attempt != null && attempt.getRemainingSeconds() <= 0) {
            // the participant left and the time ran out: close it with no answers
            closeExpired(attempt);
            attempt = null;
        }

        List<Question> questions = quizDAO.getQuestions(quizId);
        if (attempt == null) {
            if (questions.isEmpty()) {
                throw new QuizException("This quiz has no questions yet.");
            }
            int max = AppSettings.getInt("max_attempts");
            if (resultDAO.countAttempts(quizId, me.getId()) >= max) {
                throw new QuizException("You have used all " + max + " attempts for this quiz.");
            }
            attempt = resultDAO.findById(resultDAO.startAttempt(quizId, me.getId(), questions.size()));
        }

        // stable per-attempt random order: different for everybody, identical after a refresh
        Collections.shuffle(questions, new Random(attempt.getAttemptId()));

        req.setAttribute("quiz", quiz);
        req.setAttribute("attempt", attempt);
        req.setAttribute("questions", questions);
        render(req, resp, "participant/take", quiz.getTitle(), "take");
    }

    private void closeExpired(QuizResult attempt) throws SQLException {
        List<Question> questions = quizDAO.getQuestions(attempt.getQuizId());
        QuizGrader.Grade grade = QuizGrader.grade(questions, new HashMap<>());
        try {
            resultDAO.saveSubmission(attempt.getAttemptId(), grade.getScore(), grade.getDetails());
        } catch (QuizException alreadySubmitted) {
            // another tab closed it first - nothing to do
        }
    }
}
