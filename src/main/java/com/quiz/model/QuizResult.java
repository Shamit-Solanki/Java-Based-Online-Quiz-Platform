package com.quiz.model;

import java.io.Serializable;
import java.sql.Timestamp;

/** One attempt of a participant at a quiz (a row of quiz_attempts). */
public class QuizResult implements Scorable, Serializable {

    private static final long serialVersionUID = 1L;

    private int attemptId;
    private int quizId;
    private int participantId;
    private int score;
    private int autoScore;
    private int total;
    private String quizTitle;
    private String participantName;
    private int durationMinutes;
    private Timestamp startedAt;
    private Timestamp submittedAt;
    private String feedback;
    private String gradedByName;
    private int elapsedSeconds;
    private int passPercent = 40;

    public boolean isSubmitted() {
        return submittedAt != null;
    }

    public boolean isPassed() {
        return getPercentage() >= passPercent;
    }

    /** True when a creator has changed the automatic score or left feedback. */
    public boolean isReviewed() {
        return feedback != null && !feedback.isBlank();
    }

    @Override
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    @Override
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }
    public int getAutoScore() { return autoScore; }
    public void setAutoScore(int autoScore) { this.autoScore = autoScore; }
    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }
    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public Timestamp getStartedAt() { return startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = startedAt; }
    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getGradedByName() { return gradedByName; }
    public void setGradedByName(String gradedByName) { this.gradedByName = gradedByName; }
    public int getElapsedSeconds() { return elapsedSeconds; }
    public void setElapsedSeconds(int elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }
    public int getPassPercent() { return passPercent; }
    public void setPassPercent(int passPercent) { this.passPercent = passPercent; }

    /** Seconds the participant still has (never negative). */
    public int getRemainingSeconds() {
        return Math.max(0, durationMinutes * 60 - elapsedSeconds);
    }
}
