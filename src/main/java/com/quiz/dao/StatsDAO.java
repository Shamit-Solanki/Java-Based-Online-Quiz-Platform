package com.quiz.dao;

import com.quiz.util.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Aggregate queries that feed the charts on the dashboards. */
public class StatsDAO {

    /** Average % per approved quiz, most attempted first (for a bar chart). */
    public List<Map<String, Object>> quizPerformance(int limit) throws SQLException {
        String sql = "SELECT q.title, COUNT(*) AS attempts, ROUND(AVG(a.score * 100 / a.total_questions), 1) AS avg_pct "
                   + "FROM quiz_attempts a JOIN quizzes q ON q.id = a.quiz_id "
                   + "WHERE a.submitted_at IS NOT NULL AND a.total_questions > 0 "
                   + "GROUP BY q.id, q.title ORDER BY attempts DESC, q.title LIMIT ?";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, limit);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("title", r.getString("title"));
                    row.put("attempts", r.getInt("attempts"));
                    row.put("avg", r.getDouble("avg_pct"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    /** Submitted attempts per day for the last N days, missing days filled with 0. */
    public List<Map<String, Object>> attemptsPerDay(int days) throws SQLException {
        String sql = "SELECT DATE(submitted_at) AS d, COUNT(*) AS n FROM quiz_attempts "
                   + "WHERE submitted_at IS NOT NULL AND submitted_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) "
                   + "GROUP BY DATE(submitted_at)";
        Map<LocalDate, Integer> counts = new HashMap<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, days - 1);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Date d = r.getDate("d");
                    counts.put(d.toLocalDate(), r.getInt("n"));
                }
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            Map<String, Object> row = new HashMap<>();
            row.put("label", day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            row.put("count", counts.getOrDefault(day, 0));
            rows.add(row);
        }
        return rows;
    }

    /** Number of attempts per score band, in display order. */
    public Map<String, Integer> scoreDistribution() throws SQLException {
        String sql = "SELECT "
                   + " COALESCE(SUM(p < 40), 0), COALESCE(SUM(p >= 40 AND p < 60), 0), "
                   + " COALESCE(SUM(p >= 60 AND p < 80), 0), COALESCE(SUM(p >= 80), 0) "
                   + "FROM (SELECT score * 100 / total_questions AS p FROM quiz_attempts "
                   + "      WHERE submitted_at IS NOT NULL AND total_questions > 0) t";
        Map<String, Integer> bands = new LinkedHashMap<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            r.next();
            bands.put("0-39%", r.getInt(1));
            bands.put("40-59%", r.getInt(2));
            bands.put("60-79%", r.getInt(3));
            bands.put("80-100%", r.getInt(4));
        }
        return bands;
    }

    /** Totals shown in the admin stat tiles. */
    public Map<String, Object> totals() throws SQLException {
        String sql = "SELECT COUNT(*) AS attempts, COALESCE(ROUND(AVG(score * 100 / total_questions), 1), 0) AS avg_pct "
                   + "FROM quiz_attempts WHERE submitted_at IS NOT NULL AND total_questions > 0";
        Map<String, Object> m = new HashMap<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            r.next();
            m.put("attempts", r.getInt("attempts"));
            m.put("avgPercent", r.getDouble("avg_pct"));
        }
        return m;
    }
}
