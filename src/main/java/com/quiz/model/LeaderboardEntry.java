package com.quiz.model;

import java.io.Serializable;

/** One participant's overall standing, based on best attempt per quiz. */
public class LeaderboardEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private int rank;
    private int userId;
    private String name;
    private int quizzesTaken;
    private int totalScore;
    private double avgPercent;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuizzesTaken() { return quizzesTaken; }
    public void setQuizzesTaken(int quizzesTaken) { this.quizzesTaken = quizzesTaken; }
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    public double getAvgPercent() { return avgPercent; }
    public void setAvgPercent(double avgPercent) { this.avgPercent = avgPercent; }
}
