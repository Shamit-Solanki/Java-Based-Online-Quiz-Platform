package com.quiz.servlet.common;

import com.quiz.dao.UserDAO;
import com.quiz.exception.QuizException;
import com.quiz.exception.ValidationException;
import com.quiz.model.Role;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.util.PasswordUtil;
import com.quiz.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Lets any signed-in user edit their name and change their password. */
@WebServlet("/profile")
public class ProfileServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        render(req, resp, "common/profile", "My profile", "profile");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException, QuizException {
        User me = currentUser(req);
        UserDAO dao = new UserDAO();
        String action = param(req, "action");

        if ("password".equals(action)) {
            // always re-read the hash from the database, never trust the session copy
            User fresh = dao.findById(me.getId());
            if (fresh == null || !PasswordUtil.verify(req.getParameter("current"), fresh.getPasswordHash())) {
                throw new QuizException("Your current password is incorrect.");
            }
            String next = Validator.password(req.getParameter("next"));
            if (!next.equals(req.getParameter("confirm"))) {
                throw new ValidationException("The new passwords do not match.");
            }
            dao.updatePassword(me.getId(), next);
            flash(req, "success", "Password changed.");
        } else {
            String name = Validator.required(req.getParameter("name"), "Name", 100);
            Role role = me.getRole();
            dao.update(me.getId(), name, me.getEmail(), role, true, null);
            me.setName(name);
            flash(req, "success", "Profile updated.");
        }
        redirect(req, resp, "/profile");
    }
}
