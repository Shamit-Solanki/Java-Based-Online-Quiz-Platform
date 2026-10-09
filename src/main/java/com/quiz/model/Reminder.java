package com.quiz.model;

import java.io.Serializable;
import java.sql.Timestamp;

/** A participant's reminder to take a quiz at a given time. */
public class Reminder implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int participantId;
    private int quizId;
    private String quizTitle;
    private Timestamp remindAt;
    private String note;
    private boolean notified;
    private boolean due;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }
    public Timestamp getRemindAt() { return remindAt; }
    public void setRemindAt(Timestamp remindAt) { this.remindAt = remindAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
    /** True when the reminder time has passed (computed by the database clock). */
    public boolean isDue() { return due; }
    public void setDue(boolean due) { this.due = due; }
}
