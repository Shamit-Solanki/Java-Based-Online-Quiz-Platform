package com.quiz.servlet.common;

import com.quiz.dao.UserDAO;
import com.quiz.exception.QuizException;
import com.quiz.exception.ValidationException;
import com.quiz.model.Notification;
import com.quiz.model.Role;
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

/** Public sign-up for participants (can be switched off in the system settings). */
@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("registrationOpen", AppSettings.getBoolean("allow_registration"));
        render(req, resp, "register", "Create account", "");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        req.setAttribute("registrationOpen", AppSettings.getBoolean("allow_registration"));
        String name = param(req, "name");
        String email = param(req, "email");
        try {
            if (!AppSettings.getBoolean("allow_registration")) {
                throw new QuizException("Public sign-up is currently closed. Please contact an administrator.");
            }
            name = Validator.required(name, "Name", 100);
            email = Validator.email(email);
            String password = Validator.password(req.getParameter("password"));
            if (!password.equals(req.getParameter("confirm"))) {
                throw new ValidationException("The two passwords do not match.");
            }
            new UserDAO().create(name, email, password, Role.PARTICIPANT, true);
            NotificationService.toAdmins(Notification.Level.INFO, name + " just registered as a participant.", "/admin/users");
            flash(req, "success", "Account created! You can sign in now.");
            redirect(req, resp, "/login");
        } catch (QuizException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            render(req, resp, "register", "Create account", "");
        }
    }
}
