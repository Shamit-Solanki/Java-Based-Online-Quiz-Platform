package com.quiz.dao;

import com.quiz.model.Notification;
import com.quiz.model.Role;
import com.quiz.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database access for in-app notifications / admin system alerts. */
public class NotificationDAO {

    public void add(int userId, Notification.Level level, String text, String link) throws SQLException {
        String sql = "INSERT INTO notifications(user_id, level, message, link) VALUES(?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, level.name());
            p.setString(3, text.length() > 500 ? text.substring(0, 500) : text);
            p.setString(4, link);
            p.executeUpdate();
        }
    }

    /** Sends the same notification to every active user of a role (e.g. all admins). */
    public void addForRole(Role role, Notification.Level level, String text, String link) throws SQLException {
        String sql = "INSERT INTO notifications(user_id, level, message, link) "
                + "SELECT id, ?, ?, ? FROM users WHERE role = ? AND active = 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, level.name());
            p.setString(2, text.length() > 500 ? text.substring(0, 500) : text);
            p.setString(3, link);
            p.setString(4, role.name());
            p.executeUpdate();
        }
    }

    public List<Notification> findByUser(int userId, int limit) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC, id DESC LIMIT ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setInt(2, limit);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Notification n = new Notification();
                    n.setId(r.getInt("id"));
                    n.setUserId(userId);
                    n.setLevel(Notification.Level.valueOf(r.getString("level")));
                    n.setText(r.getString("message"));
                    n.setLink(r.getString("link"));
                    n.setRead(r.getBoolean("is_read"));
                    n.setCreatedAt(r.getTimestamp("created_at"));
                    list.add(n);
                }
            }
        }
        return list;
    }

    public int countUnread(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0")) {
            p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    public void markAllRead(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE notifications SET is_read=1 WHERE user_id=?")) {
            p.setInt(1, userId);
            p.executeUpdate();
        }
    }

    public void delete(int id, int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM notifications WHERE id=? AND user_id=?")) {
            p.setInt(1, id);
            p.setInt(2, userId);
            p.executeUpdate();
        }
    }

    public void clearAll(int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM notifications WHERE user_id=?")) {
            p.setInt(1, userId);
            p.executeUpdate();
        }
    }
}
