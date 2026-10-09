package com.quiz.model;

import java.io.Serializable;
import java.sql.Timestamp;

/** A person the current user can chat with, plus unread message count. */
public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    private int userId;
    private String name;
    private String email;
    private int unread;
    private String lastMessage;
    private Timestamp lastMessageAt;

    public String getInitials() {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] p = name.trim().split("\\s+");
        String s = p[0].substring(0, 1);
        if (p.length > 1) {
            s += p[p.length - 1].substring(0, 1);
        }
        return s.toUpperCase();
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getUnread() { return unread; }
    public void setUnread(int unread) { this.unread = unread; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public Timestamp getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Timestamp lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}
