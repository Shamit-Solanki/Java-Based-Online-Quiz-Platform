package com.quiz.servlet.common;

import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Landing page at the context root. Signed-in users go straight to their dashboard. */
@WebServlet(urlPatterns = {"", "/home"})
public class HomeServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = req.getSession(false) == null ? null : currentUser(req);
        if (user != null) {
            redirect(req, resp, user.getDashboardPath());
            return;
        }
        render(req, resp, "home", "Welcome", "");
    }
}
