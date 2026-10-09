package com.quiz.dao;

import com.quiz.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Database access for the system_settings key/value table. */
public class SettingsDAO {

    public Map<String, String> findAll() throws SQLException {
        Map<String, String> map = new LinkedHashMap<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT setting_key, setting_value FROM system_settings");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                map.put(r.getString(1), r.getString(2));
            }
        }
        return map;
    }

    /** Saves all given settings atomically (all or nothing). */
    public void saveAll(Map<String, String> values) throws SQLException {
        String sql = "INSERT INTO system_settings(setting_key, setting_value) VALUES(?,?) "
                + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(sql)) {
                for (Map.Entry<String, String> e : values.entrySet()) {
                    p.setString(1, e.getKey());
                    p.setString(2, e.getValue());
                    p.addBatch();
                }
                p.executeBatch();
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
}
