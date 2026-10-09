package com.quiz.servlet;

import com.quiz.dao.MessageDAO;
import com.quiz.dao.NotificationDAO;
import com.quiz.exception.QuizException;
import com.quiz.model.User;
import com.quiz.util.AppSettings;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Parent of all page servlets (inheritance + template method).
 *
 * Subclasses override {@link #handleGet} and/or {@link #handlePost} and may throw
 * {@link SQLException} or {@link QuizException} freely:
 * <ul>
 *   <li>a QuizException becomes a red flash message and a redirect;</li>
 *   <li>an SQLException is logged and shown on the friendly error page.</li>
 * </ul>
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    protected static final String VIEWS = "/WEB-INF/views/";

    // ------------------------------------------------------------ template methods

    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, QuizException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException, QuizException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /** Where to send the user after a QuizException (default: this servlet's own page). */
    protected String failurePath(HttpServletRequest req) {
        return req.getServletPath();
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        dispatch(req, resp, false);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        dispatch(req, resp, true);
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse resp, boolean post)
            throws ServletException, IOException {
        try {
            if (post) {
                handlePost(req, resp);
            } else {
                handleGet(req, resp);
            }
        } catch (QuizException e) {
            flash(req, "error", e.getMessage());
            redirect(req, resp, failurePath(req));
        } catch (SQLException e) {
            log("Database error while handling " + req.getRequestURI(), e);
            throw new ServletException("A database error occurred. Please make sure MySQL is running.", e);
        }
    }

    // ------------------------------------------------------------ helpers for subclasses

    protected User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("user");
    }

    /** Forwards to /WEB-INF/views/{view}.jsp after filling the attributes every page needs. */
    protected void render(HttpServletRequest req, HttpServletResponse resp, String view, String title, String nav)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", title);
        req.setAttribute("nav", nav);
        req.setAttribute("platformName", AppSettings.get("platform_name"));
        req.setAttribute("announcement", AppSettings.get("announcement"));
        User user = currentUser(req);
        if (user != null) {
            try {
                req.setAttribute("unreadNotifications", new NotificationDAO().countUnread(user.getId()));
                req.setAttribute("unreadMessages", new MessageDAO().countUnread(user.getId()));
            } catch (SQLException e) {
                log("Could not load unread counters", e);
            }
        }
        req.getRequestDispatcher(VIEWS + view + ".jsp").forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /** One-shot message shown as a toast on the next page ("success", "error", "info"). */
    protected void flash(HttpServletRequest req, String type, String text) {
        req.getSession().setAttribute("flash", new String[]{type, text});
    }

    protected String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    /** Integer request parameter, or 0 when missing or not a number. */
    protected int intParam(HttpServletRequest req, String name) {
        try {
            return Integer.parseInt(param(req, name));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
