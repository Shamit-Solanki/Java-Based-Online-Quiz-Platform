package com.quiz.servlet.admin;

import com.quiz.dao.UserDAO;
import com.quiz.exception.QuizException;
import com.quiz.exception.ValidationException;
import com.quiz.model.Role;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import com.quiz.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** User management: list, create, edit, enable/disable and delete accounts. */
@WebServlet("/admin/users")
public class AdminUsersServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final transient UserDAO dao = new UserDAO();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        req.setAttribute("users", dao.findAll());
        req.setAttribute("roles", Role.values());
        render(req, resp, "admin/users", "User management", "users");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException, QuizException {
        User me = currentUser(req);
        String action = param(req, "action");
        int id = intParam(req, "id");

        switch (action) {
            case "create":
                create(req);
                break;
            case "update":
                update(req, me, id);
                break;
            case "toggle":
                toggle(me, id);
                flash(req, "success", "Account status updated.");
                break;
            case "delete":
                delete(me, id);
                flash(req, "success", "User deleted.");
                break;
            default:
                throw new QuizException("Unknown action.");
        }
        redirect(req, resp, "/admin/users");
    }

    private void create(HttpServletRequest req) throws SQLException, QuizException {
        String name = Validator.required(req.getParameter("name"), "Name", 100);
        String email = Validator.email(req.getParameter("email"));
        String password = Validator.password(req.getParameter("password"));
        Role role = parseRole(req.getParameter("role"));
        dao.create(name, email, password, role, true);
        flash(req, "success", "User \"" + name + "\" created as " + role.getLabel() + ".");
    }

    private void update(HttpServletRequest req, User me, int id) throws SQLException, QuizException {
        User target = requireUser(id);
        String name = Validator.required(req.getParameter("name"), "Name", 100);
        String email = Validator.email(req.getParameter("email"));
        Role role = parseRole(req.getParameter("role"));
        boolean active = "on".equals(req.getParameter("active")) || "true".equals(req.getParameter("active"));
        String password = req.getParameter("password");
        if (password != null && !password.isBlank()) {
            Validator.password(password);
        }
        guardLastAdmin(target, role, active);
        if (target.getId() == me.getId() && (!active || role != me.getRole())) {
            throw new QuizException("You cannot disable your own account or change your own role.");
        }
        dao.update(id, name, email, role, active, password);
        flash(req, "success", "User \"" + name + "\" updated.");
    }

    private void toggle(User me, int id) throws SQLException, QuizException {
        User target = requireUser(id);
        if (target.getId() == me.getId()) {
            throw new QuizException("You cannot disable your own account.");
        }
        guardLastAdmin(target, target.getRole(), !target.isActive());
        dao.update(id, target.getName(), target.getEmail(), target.getRole(), !target.isActive(), null);
    }

    private void delete(User me, int id) throws SQLException, QuizException {
        User target = requireUser(id);
        if (target.getId() == me.getId()) {
            throw new QuizException("You cannot delete your own account.");
        }
        guardLastAdmin(target, null, false);
        dao.delete(id);
    }

    /** Never allow the platform to end up without an active administrator. */
    private void guardLastAdmin(User target, Role newRole, boolean newActive) throws SQLException, QuizException {
        boolean staysActiveAdmin = newRole == Role.ADMIN && newActive;
        if (target.getRole() == Role.ADMIN && target.isActive() && !staysActiveAdmin
                && dao.countActiveAdmins() <= 1) {
            throw new QuizException("At least one active administrator must remain.");
        }
    }

    private User requireUser(int id) throws SQLException, QuizException {
        User u = dao.findById(id);
        if (u == null) {
            throw new QuizException("User not found.");
        }
        return u;
    }

    private Role parseRole(String value) throws ValidationException {
        Role role = Role.fromString(value);
        if (role == null) {
            throw new ValidationException("Please choose a role.");
        }
        return role;
    }
}
