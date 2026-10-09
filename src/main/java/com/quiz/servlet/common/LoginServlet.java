package com.quiz.servlet.common;

import com.quiz.dao.UserDAO;
import com.quiz.exception.QuizException;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.filter.AuthFilter;
import com.quiz.util.LoginThrottle;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/** Shows the login form and signs users in. */
@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = req.getSession(false) == null ? null : currentUser(req);
        if (user != null) {
            redirect(req, resp, user.getDashboardPath());
            return;
        }
        render(req, resp, "login", "Sign in", "");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        String email = param(req, "email").toLowerCase();
        String password = req.getParameter("password");
        String client = req.getRemoteAddr();

        if (LoginThrottle.isBlocked(client, email)) {
            showError(req, resp, email, "Too many failed attempts. Please wait a few minutes and try again.");
            return;
        }

        User user;
        try {
            user = new UserDAO().authenticate(email, password == null ? "" : password);
        } catch (QuizException e) {
            showError(req, resp, email, e.getMessage());
            return;
        }
        if (user == null) {
            LoginThrottle.recordFailure(client, email);
            showError(req, resp, email, "Invalid email or password.");
            return;
        }

        LoginThrottle.recordSuccess(client, email);
        // new session id after authentication prevents session fixation
        HttpSession old = req.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute(AuthFilter.CSRF_ATTRIBUTE, AuthFilter.newToken());
        session.setMaxInactiveInterval(60 * 60);
        redirect(req, resp, user.getDashboardPath());
    }

    private void showError(HttpServletRequest req, HttpServletResponse resp, String email, String message)
            throws ServletException, IOException {
        req.setAttribute("error", message);
        req.setAttribute("email", email);
        render(req, resp, "login", "Sign in", "");
    }
}
