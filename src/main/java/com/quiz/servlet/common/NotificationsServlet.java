package com.quiz.servlet.common;

import com.quiz.dao.NotificationDAO;
import com.quiz.model.Notification;
import com.quiz.model.User;
import com.quiz.servlet.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Lists the user's notifications (admins: system alerts) and lets them clear them. */
@WebServlet("/notifications")
public class NotificationsServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private final transient NotificationDAO dao = new NotificationDAO();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        User me = currentUser(req);
        List<Notification> list = dao.findByUser(me.getId(), 100);
        // keep the "unread" flags in this response, then mark everything read
        dao.markAllRead(me.getId());
        req.setAttribute("notifications", list);
        render(req, resp, "common/notifications", "Notifications", "notifications");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        User me = currentUser(req);
        String action = param(req, "action");
        if ("delete".equals(action)) {
            dao.delete(intParam(req, "id"), me.getId());
        } else if ("clear".equals(action)) {
            dao.clearAll(me.getId());
            flash(req, "success", "All notifications cleared.");
        }
        redirect(req, resp, "/notifications");
    }
}
