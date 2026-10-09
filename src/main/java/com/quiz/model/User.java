package com.quiz.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Abstract base for every account type.
 * Subclasses (Admin, QuizCreator, Participant) override the abstract methods,
 * which gives us runtime polymorphism for role specific behaviour.
 */
public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active = true;
    private Timestamp createdAt;

    protected User(int id, String name, String email, String passwordHash, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /** Short sentence describing what this kind of user can do. */
    public abstract String getRoleDescription();

    /** Path (relative to the context root) of this user's landing page. */
    public String getDashboardPath() {
        return role.getHomePath();
    }

    /** First letters of the name, used for avatar bubbles. */
    public String getInitials() {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        String initials = parts[0].substring(0, 1);
        if (parts.length > 1) {
            initials += parts[parts.length - 1].substring(0, 1);
        }
        return initials.toUpperCase();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
