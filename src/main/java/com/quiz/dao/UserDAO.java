package com.quiz.dao;

import com.quiz.exception.QuizException;
import com.quiz.model.Role;
import com.quiz.model.User;
import com.quiz.util.DBConnection;
import com.quiz.util.PasswordUtil;
import com.quiz.util.UserFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Database access for the users table. */
public class UserDAO implements CrudDAO<User> {

    /**
     * Checks credentials.
     *
     * @return the user, or null when the email/password is wrong
     * @throws QuizException when the account exists but has been disabled
     */
    public User authenticate(String email, String password) throws SQLException, QuizException {
        User user = findByEmail(email);
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            return null;
        }
        if (!user.isActive()) {
            throw new QuizException("This account has been disabled. Please contact an administrator.");
        }
        return user;
    }

    public User findByEmail(String email) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            p.setString(1, email);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    /** Creates a user from a plain-text password (hashed here) and returns the new id. */
    public int create(String name, String email, String password, Role role, boolean active)
            throws SQLException, QuizException {
        if (findByEmail(email) != null) {
            throw new QuizException("An account with this email already exists.");
        }
        String sql = "INSERT INTO users(name,email,password_hash,role,active) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, name);
            p.setString(2, email);
            p.setString(3, PasswordUtil.hash(password));
            p.setString(4, role.name());
            p.setBoolean(5, active);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    /** Updates profile fields; the password is only changed when newPassword is not blank. */
    public void update(int id, String name, String email, Role role, boolean active, String newPassword)
            throws SQLException, QuizException {
        User existing = findByEmail(email);
        if (existing != null && existing.getId() != id) {
            throw new QuizException("Another account already uses this email.");
        }
        boolean changePassword = newPassword != null && !newPassword.isBlank();
        String sql = "UPDATE users SET name=?, email=?, role=?, active=?"
                + (changePassword ? ", password_hash=?" : "") + " WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            int i = 1;
            p.setString(i++, name);
            p.setString(i++, email);
            p.setString(i++, role.name());
            p.setBoolean(i++, active);
            if (changePassword) {
                p.setString(i++, PasswordUtil.hash(newPassword));
            }
            p.setInt(i, id);
            p.executeUpdate();
        }
    }

    public void updatePassword(int id, String newPassword) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")) {
            p.setString(1, PasswordUtil.hash(newPassword));
            p.setInt(2, id);
            p.executeUpdate();
        }
    }

    @Override
    public User findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        return query("SELECT * FROM users ORDER BY created_at DESC, id DESC", null);
    }

    public List<User> findByRole(Role role) throws SQLException {
        return query("SELECT * FROM users WHERE role = ? AND active = 1 ORDER BY name", role.name());
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    /** Number of users for every role (roles without users map to 0). */
    public Map<Role, Integer> countByRole() throws SQLException {
        Map<Role, Integer> counts = new EnumMap<>(Role.class);
        for (Role r : Role.values()) {
            counts.put(r, 0);
        }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT role, COUNT(*) FROM users GROUP BY role");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                counts.put(Role.valueOf(r.getString(1)), r.getInt(2));
            }
        }
        return counts;
    }

    public int countActiveAdmins() throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role='ADMIN' AND active=1");
             ResultSet r = p.executeQuery()) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    private List<User> query(String sql, String param) throws SQLException {
        List<User> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            if (param != null) {
                p.setString(1, param);
            }
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    list.add(map(r));
                }
            }
        }
        return list;
    }

    private User map(ResultSet r) throws SQLException {
        User u = UserFactory.create(r.getInt("id"), r.getString("name"), r.getString("email"),
                r.getString("password_hash"), Role.valueOf(r.getString("role")));
        u.setActive(r.getBoolean("active"));
        u.setCreatedAt(r.getTimestamp("created_at"));
        return u;
    }
}
