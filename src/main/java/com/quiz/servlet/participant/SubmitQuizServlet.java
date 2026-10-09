package com.quiz.servlet.participant;

import com.quiz.dao.QuizDAO;
import com.quiz.dao.ResultDAO;
import com.quiz.exception.QuizException;
import com.quiz.model.Notification;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.QuizResult;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.service.NotificationService;
import com.quiz.service.QuizGrader;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Grades a submitted attempt automatically and redirects to the performance report. */
@WebServlet("/participant/submit")
public class SubmitQuizServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    /** Seconds allowed after the deadline for network delay and the auto-submit. */
    private static final int GRACE_SECONDS = 60;

    @Override
    protected String failurePath(HttpServletRequest req) {
        return "/participant/quizzes";
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException, QuizException {
        User me = currentUser(req);
        ResultDAO resultDAO = new ResultDAO();
        QuizDAO quizDAO = new QuizDAO();

        int attemptId = intParam(req, "attemptId");
        QuizResult attempt = resultDAO.findById(attemptId);
        if (attempt == null || attempt.getParticipantId() != me.getId()) {
            throw new QuizException("Attempt not found.");
        }
        if (attempt.isSubmitted()) {
            redirect(req, resp, "/participant/report?id=" + attemptId);
            return;
        }

        boolean late = attempt.getElapsedSeconds() > attempt.getDurationMinutes() * 60 + GRACE_SECONDS;
        List<Question> questions = quizDAO.getQuestions(attempt.getQuizId());
        Map<Integer, String> chosen = new HashMap<>();
        if (!late) {
            for (Question q : questions) {
                chosen.put(q.getId(), req.getParameter("q_" + q.getId()));
            }
        }

        QuizGrader.Grade grade = QuizGrader.grade(questions, chosen);
        resultDAO.saveSubmission(attemptId, grade.getScore(), grade.getDetails());

        Quiz<Question> quiz = quizDAO.findById(attempt.getQuizId());
        if (quiz != null) {
            NotificationService.toUser(quiz.getCreatorId(), Notification.Level.INFO,
                    me.getName() + " completed \"" + quiz.getTitle() + "\" (" + grade.getScore() + "/" + questions.size() + ").",
                    "/creator/review?id=" + attemptId);
        }
        if (late) {
            flash(req, "error", "Time was up before your answers arrived, so this attempt was recorded without answers.");
        }
        redirect(req, resp, "/participant/report?id=" + attemptId + "&done=1");
    }
}
