package com.quiz.service;

import com.quiz.dao.NotificationDAO;
import com.quiz.model.Notification;
import com.quiz.model.Role;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenience wrapper for sending notifications.
 * A failed notification must never break the action that triggered it, so
 * SQL errors are logged and swallowed here.
 */
public final class NotificationService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());
    private static final NotificationDAO DAO = new NotificationDAO();

    private NotificationService() {
    }

    public static void toUser(int userId, Notification.Level level, String text, String link) {
        try {
            DAO.add(userId, level, text, link);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Could not store notification", e);
        }
    }

    /** Alert for every administrator ("system alerts" panel). */
    public static void toAdmins(Notification.Level level, String text, String link) {
        try {
            DAO.addForRole(Role.ADMIN, level, text, link);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Could not store admin alert", e);
        }
    }
}
