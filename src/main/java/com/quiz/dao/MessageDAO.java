package com.quiz.dao;

import com.quiz.model.Contact;
import com.quiz.model.Message;
import com.quiz.model.Role;
import com.quiz.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database access for direct messages between creators and participants. */
public class MessageDAO {

    public void send(int senderId, int receiverId, String text) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "INSERT INTO messages(sender_id, receiver_id, message_text) VALUES(?,?,?)")) {
            p.setInt(1, senderId);
            p.setInt(2, receiverId);
            p.setString(3, text);
            p.executeUpdate();
        }
    }

    /** Conversation between two users, oldest first. */
    public List<Message> thread(int me, int other, int limit) throws SQLException {
        String sql = "SELECT * FROM (SELECT m.*, u.name AS sender_name FROM messages m JOIN users u ON u.id = m.sender_id "
                   + "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) "
                   + "ORDER BY m.id DESC LIMIT ?) t ORDER BY t.id";
        List<Message> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, me);
            p.setInt(2, other);
            p.setInt(3, other);
            p.setInt(4, me);
            p.setInt(5, limit);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(map(r));
                }
            }
        }
        return list;
    }

    public void markThreadRead(int me, int other) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "UPDATE messages SET is_read = 1 WHERE receiver_id = ? AND sender_id = ? AND is_read = 0")) {
            p.setInt(1, me);
            p.setInt(2, other);
            p.executeUpdate();
        }
    }

    public int countUnread(int me) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM messages WHERE receiver_id=? AND is_read=0")) {
            p.setInt(1, me);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    /**
     * People the user can talk to (all active users of the other role), with the
     * unread count and last message. Conversations with activity come first.
     */
    public List<Contact> contacts(int me, Role otherRole) throws SQLException {
        String last = "SELECT %s FROM messages m WHERE (m.sender_id = u.id AND m.receiver_id = ?) "
                    + "OR (m.sender_id = ? AND m.receiver_id = u.id) ORDER BY m.id DESC LIMIT 1";
        String sql = "SELECT u.id, u.name, u.email, "
                   + " (SELECT COUNT(*) FROM messages m WHERE m.sender_id = u.id AND m.receiver_id = ? AND m.is_read = 0) AS unread, "
                   + " (" + String.format(last, "m.message_text") + ") AS last_text, "
                   + " (" + String.format(last, "m.created_at") + ") AS last_at "
                   + "FROM users u WHERE u.role = ? AND u.active = 1 "
                   + "ORDER BY (last_at IS NULL), last_at DESC, u.name";
        List<Contact> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, me);
            p.setInt(2, me);
            p.setInt(3, me);
            p.setInt(4, me);
            p.setInt(5, me);
            p.setString(6, otherRole.name());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Contact ct = new Contact();
                    ct.setUserId(r.getInt("id"));
                    ct.setName(r.getString("name"));
                    ct.setEmail(r.getString("email"));
                    ct.setUnread(r.getInt("unread"));
                    ct.setLastMessage(r.getString("last_text"));
                    ct.setLastMessageAt(r.getTimestamp("last_at"));
                    list.add(ct);
                }
            }
        }
        return list;
    }

    /** Latest messages that involve the user (dashboard "interaction history"). */
    public List<Message> recent(int me, int limit) throws SQLException {
        String sql = "SELECT m.*, u.name AS sender_name FROM messages m JOIN users u ON u.id = m.sender_id "
                   + "WHERE m.sender_id = ? OR m.receiver_id = ? ORDER BY m.id DESC LIMIT ?";
        List<Message> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, me);
            p.setInt(2, me);
            p.setInt(3, limit);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(map(r));
                }
            }
        }
        return list;
    }

    private Message map(ResultSet r) throws SQLException {
        Message m = new Message();
        m.setId(r.getInt("id"));
        m.setSenderId(r.getInt("sender_id"));
        m.setReceiverId(r.getInt("receiver_id"));
        m.setSenderName(r.getString("sender_name"));
        m.setText(r.getString("message_text"));
        m.setRead(r.getBoolean("is_read"));
        m.setCreatedAt(r.getTimestamp("created_at"));
        return m;
    }
}
