package com.quiz.servlet.participant;

import com.quiz.dao.QuizDAO;
import com.quiz.dao.ReminderDAO;
import com.quiz.exception.QuizException;
import com.quiz.exception.ValidationException;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.QuizStatus;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/** Set, view and delete quiz reminders. A background thread notifies when one is due. */
@WebServlet("/participant/reminders")
public class RemindersServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final transient ReminderDAO dao = new ReminderDAO();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        User me = currentUser(req);
        req.setAttribute("reminders", dao.findByParticipant(me.getId()));
        req.setAttribute("quizzes", new QuizDAO().findApprovedFor(me.getId()));
        req.setAttribute("preselect", intParam(req, "quiz"));
        render(req, resp, "participant/reminders", "Quiz reminders", "reminders");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException, QuizException {
        User me = currentUser(req);
        String action = param(req, "action");

        if ("delete".equals(action)) {
            dao.delete(intParam(req, "id"), me.getId());
            flash(req, "success", "Reminder removed.");
        } else if ("add".equals(action)) {
            Quiz<Question> quiz = new QuizDAO().findById(intParam(req, "quizId"));
            if (quiz == null || quiz.getStatus() != QuizStatus.APPROVED) {
                throw new QuizException("Please choose an available quiz.");
            }
            LocalDateTime when;
            try {
                when = LocalDateTime.parse(param(req, "remindAt"));
            } catch (DateTimeParseException e) {
                throw new ValidationException("Please choose a valid date and time.");
            }
            if (when.isBefore(LocalDateTime.now().minusMinutes(1))) {
                throw new ValidationException("The reminder time must be in the future.");
            }
            String note = Validator.optional(req.getParameter("note"), "Note", 200);
            dao.add(me.getId(), quiz.getId(), Timestamp.valueOf(when), note);
            flash(req, "success", "Reminder set for " + when.toString().replace('T', ' ') + ".");
        } else {
            throw new QuizException("Unknown action.");
        }
        redirect(req, resp, "/participant/reminders");
    }
}
