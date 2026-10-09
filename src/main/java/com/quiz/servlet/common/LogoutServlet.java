package com.quiz.servlet.common;

import com.quiz.servlet.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Ends the session. POST only (protected by the CSRF token). */
@WebServlet("/logout")
public class LogoutServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // flash needs a session, so start a fresh one for the message
        flash(req, "info", "You have been signed out.");
        redirect(req, resp, "/login");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        redirect(req, resp, "/");
    }
}
