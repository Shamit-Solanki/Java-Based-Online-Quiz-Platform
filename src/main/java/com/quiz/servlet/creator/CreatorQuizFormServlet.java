package com.quiz.servlet.creator;

import com.quiz.dao.QuizDAO;
import com.quiz.exception.QuizException;
import com.quiz.exception.ValidationException;
import com.quiz.model.Notification;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.service.NotificationService;
import com.quiz.util.AppSettings;
import com.quiz.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Create a new quiz or edit a draft / rejected one. */
@WebServlet("/creator/quiz-form")
public class CreatorQuizFormServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private static final int MAX_QUESTIONS = 50;
    private final transient QuizDAO dao = new QuizDAO();

    @Override
    protected String failurePath(HttpServletRequest req) {
        return "/creator/quizzes";
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, QuizException {
        User me = currentUser(req);
        int id = intParam(req, "id");
        Quiz<Question> quiz;
        List<Question> questions = new ArrayList<>();

        if (id > 0) {
            quiz = dao.findById(id);
            if (quiz == null || quiz.getCreatorId() != me.getId()) {
                throw new QuizException("Quiz not found.");
            }
            if (!quiz.isEditable()) {
                throw new QuizException("This quiz is pending or approved and can no longer be edited.");
            }
            questions = dao.getQuestions(id);
        } else {
            quiz = new Quiz<>();
            quiz.setDurationMinutes(AppSettings.getInt("default_duration"));
        }
        show(req, resp, quiz, questions, null);
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, QuizException {
        User me = currentUser(req);
        Quiz<Question> quiz = new Quiz<>();
        List<Question> questions = new ArrayList<>();
        quiz.setId(intParam(req, "id"));
        quiz.setCreatorId(me.getId());
        // keep what the user typed so the form can be shown again on a validation error
        quiz.setTitle(param(req, "title"));
        quiz.setDescription(param(req, "description"));
        quiz.setDurationMinutes(intParam(req, "duration"));
        collectQuestions(req, questions);

        try {
            quiz.setTitle(Validator.required(quiz.getTitle(), "Title", 200));
            quiz.setDescription(Validator.optional(quiz.getDescription(), "Description", 500));
            quiz.setDurationMinutes(Validator.intInRange(req.getParameter("duration"), "Duration", 1, 300));
            validateQuestions(questions);
            questions.forEach(quiz::addQuestion);

            int id = quiz.getId();
            if (id > 0) {
                dao.update(quiz);
            } else {
                id = dao.create(quiz);
            }
            if ("submit".equals(param(req, "action"))) {
                dao.submitForApproval(id, me.getId());
                NotificationService.toAdmins(Notification.Level.WARNING,
                        me.getName() + " submitted \"" + quiz.getTitle() + "\" for approval.", "/admin/quizzes");
                flash(req, "success", "Quiz saved and sent to the administrator for approval.");
            } else {
                flash(req, "success", "Quiz saved as draft. Submit it for approval when you are ready.");
            }
            redirect(req, resp, "/creator/quizzes");
        } catch (ValidationException e) {
            show(req, resp, quiz, questions, e.getMessage());
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, Quiz<Question> quiz,
                      List<Question> questions, String error) throws ServletException, IOException {
        req.setAttribute("quiz", quiz);
        req.setAttribute("questions", questions);
        req.setAttribute("error", error);
        req.setAttribute("maxQuestions", MAX_QUESTIONS);
        render(req, resp, "creator/quiz-form", quiz.getId() > 0 ? "Edit quiz" : "Create quiz", "create");
    }

    /** Reads the parallel arrays posted by the dynamic question cards. */
    private void collectQuestions(HttpServletRequest req, List<Question> out) {
        String[] text = req.getParameterValues("question");
        String[] a = req.getParameterValues("optionA");
        String[] b = req.getParameterValues("optionB");
        String[] c = req.getParameterValues("optionC");
        String[] d = req.getParameterValues("optionD");
        String[] correct = req.getParameterValues("correct");
        String[] why = req.getParameterValues("explanation");
        if (text == null || a == null || b == null || c == null || d == null || correct == null) {
            return;
        }
        int n = text.length;
        for (int i = 0; i < n && i < MAX_QUESTIONS + 1; i++) {
            if (i >= a.length || i >= b.length || i >= c.length || i >= d.length || i >= correct.length) {
                break;
            }
            Question q = new Question();
            q.setQuestionText(text[i].trim());
            q.setOptionA(a[i].trim());
            q.setOptionB(b[i].trim());
            q.setOptionC(c[i].trim());
            q.setOptionD(d[i].trim());
            q.setCorrectOption(correct[i].trim().toUpperCase());
            q.setExplanation(why != null && i < why.length ? why[i].trim() : "");
            out.add(q);
        }
    }

    private void validateQuestions(List<Question> questions) throws ValidationException {
        if (questions.isEmpty()) {
            throw new ValidationException("Add at least one question.");
        }
        if (questions.size() > MAX_QUESTIONS) {
            throw new ValidationException("A quiz can have at most " + MAX_QUESTIONS + " questions.");
        }
        int n = 1;
        for (Question q : questions) {
            String label = "Question " + n++;
            q.setQuestionText(Validator.required(q.getQuestionText(), label + " text", 500));
            q.setOptionA(Validator.required(q.getOptionA(), label + " option A", 255));
            q.setOptionB(Validator.required(q.getOptionB(), label + " option B", 255));
            q.setOptionC(Validator.required(q.getOptionC(), label + " option C", 255));
            q.setOptionD(Validator.required(q.getOptionD(), label + " option D", 255));
            q.setCorrectOption(Validator.option(q.getCorrectOption(), label + " correct answer"));
            q.setExplanation(Validator.optional(q.getExplanation(), label + " explanation", 500));
        }
    }
}
