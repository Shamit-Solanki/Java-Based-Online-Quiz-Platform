package com.quiz.model;

/** Anything that has a score out of a total (quiz attempts, leaderboard rows ...). */
public interface Scorable {

    int getScore();

    int getTotal();

    /** Score as a percentage (0-100), rounded to one decimal. */
    default double getPercentage() {
        if (getTotal() <= 0) {
            return 0;
        }
        return Math.round(getScore() * 1000.0 / getTotal()) / 10.0;
    }

    /** Letter grade derived from the percentage. */
    default String getGrade() {
        double p = getPercentage();
        if (p >= 90) return "A+";
        if (p >= 80) return "A";
        if (p >= 70) return "B";
        if (p >= 60) return "C";
        if (p >= 40) return "D";
        return "F";
    }
}
