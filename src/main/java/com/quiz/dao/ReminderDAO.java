package com.quiz.dao;

import com.quiz.model.Reminder;
import com.quiz.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Database access for quiz reminders. */
public class ReminderDAO {

    public void add(int participantId, int quizId, Timestamp remindAt, String note) throws SQLException {
        String sql = "INSERT INTO reminders(participant_id, quiz_id, remind_at, note) VALUES(?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, participantId);
            p.setInt(2, quizId);
            p.setTimestamp(3, remindAt);
            p.setString(4, note);
            p.executeUpdate();
        }
    }

    public List<Reminder> findByParticipant(int participantId) throws SQLException {
        String sql = "SELECT r.*, q.title, (r.remind_at <= NOW()) AS is_due FROM reminders r "
                + "JOIN quizzes q ON q.id = r.quiz_id WHERE r.participant_id = ? ORDER BY r.remind_at";
        List<Reminder> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, participantId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(map(r));
                }
            }
        }
        return list;
    }

    /** Reminders whose time has come but have not produced a notification yet. */
    public List<Reminder> findDueUnnotified() throws SQLException {
        String sql = "SELECT r.*, q.title, 1 AS is_due FROM reminders r "
                + "JOIN quizzes q ON q.id = r.quiz_id WHERE r.notified = 0 AND r.remind_at <= NOW()";
        List<Reminder> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public void markNotified(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE reminders SET notified=1 WHERE id=?")) {
            p.setInt(1, id);
            p.executeUpdate();
        }
    }

    public boolean delete(int id, int participantId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM reminders WHERE id=? AND participant_id=?")) {
            p.setInt(1, id);
            p.setInt(2, participantId);
            return p.executeUpdate() > 0;
        }
    }

    private Reminder map(ResultSet r) throws SQLException {
        Reminder x = new Reminder();
        x.setId(r.getInt("id"));
        x.setParticipantId(r.getInt("participant_id"));
        x.setQuizId(r.getInt("quiz_id"));
        x.setQuizTitle(r.getString("title"));
        x.setRemindAt(r.getTimestamp("remind_at"));
        x.setNote(r.getString("note"));
        x.setNotified(r.getBoolean("notified"));
        x.setDue(r.getBoolean("is_due"));
        return x;
    }
}
