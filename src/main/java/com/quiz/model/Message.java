package com.quiz.model;

import java.io.Serializable;
import java.sql.Timestamp;

/** A direct message between a quiz creator and a participant. */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int senderId;
    private int receiverId;
    private String senderName;
    private String text;
    private boolean read;
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
