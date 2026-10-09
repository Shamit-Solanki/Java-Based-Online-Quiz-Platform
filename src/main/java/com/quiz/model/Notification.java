package com.quiz.model;

import java.io.Serializable;
import java.sql.Timestamp;

/** In-app notification; for admins these double as "system alerts". */
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Severity levels, mapped to colours in the stylesheet. */
    public enum Level { INFO, SUCCESS, WARNING, CRITICAL }

    private int id;
    private int userId;
    private Level level = Level.INFO;
    private String text;
    private String link;
    private boolean read;
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
