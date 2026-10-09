package com.quiz.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * A quiz that holds a typed list of questions.
 * The generic bound ({@code T extends Question}) lets the same container work
 * with Question or any future specialised question type.
 */
public class Quiz<T extends Question> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String description;
    private int durationMinutes;
    private int creatorId;
    private String creatorName;
    private QuizStatus status = QuizStatus.DRAFT;
    private String reviewNote;
    private Timestamp createdAt;
    private final transient List<T> questions = new ArrayList<>();

    // Read-only statistics filled in by list queries
    private int questionCount;
    private int attemptCount;
    private double avgPercent;
    // Participant specific (filled by findApprovedFor)
    private int attemptsUsed;
    private int bestScore = -1;

    public Quiz() {
    }

    public void addQuestion(T question) {
        questions.add(question);
    }

    public List<T> getQuestions() { return questions; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public QuizStatus getStatus() { return status; }
    public void setStatus(QuizStatus status) { this.status = status; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public double getAvgPercent() { return avgPercent; }
    public void setAvgPercent(double avgPercent) { this.avgPercent = avgPercent; }
    public int getAttemptsUsed() { return attemptsUsed; }
    public void setAttemptsUsed(int attemptsUsed) { this.attemptsUsed = attemptsUsed; }
    public int getBestScore() { return bestScore; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }
    public boolean isEditable() { return status.isEditable(); }
}
