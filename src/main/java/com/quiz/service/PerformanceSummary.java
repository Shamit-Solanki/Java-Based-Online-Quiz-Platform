package com.quiz.service;

import com.quiz.model.QuizResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Aggregates a participant's attempts into the numbers shown on the dashboards. */
public final class PerformanceSummary {

    private final int completed;
    private final double avgPercent;
    private final double bestPercent;
    private final int passed;
    private final List<QuizResult> trend;

    private PerformanceSummary(int completed, double avgPercent, double bestPercent, int passed, List<QuizResult> trend) {
        this.completed = completed;
        this.avgPercent = avgPercent;
        this.bestPercent = bestPercent;
        this.passed = passed;
        this.trend = trend;
    }

    /**
     * @param newestFirst submitted attempts ordered newest first (as returned by ResultDAO)
     */
    public static PerformanceSummary of(List<QuizResult> newestFirst) {
        double sum = 0;
        double best = 0;
        int passed = 0;
        for (QuizResult r : newestFirst) {
            sum += r.getPercentage();
            best = Math.max(best, r.getPercentage());
            if (r.isPassed()) {
                passed++;
            }
        }
        int n = newestFirst.size();
        double avg = n == 0 ? 0 : Math.round(sum * 10 / n) / 10.0;

        // last 10 attempts in chronological order for the trend chart
        List<QuizResult> recent = new ArrayList<>(newestFirst.subList(0, Math.min(10, n)));
        Collections.reverse(recent);
        return new PerformanceSummary(n, avg, best, passed, recent);
    }

    public int getCompleted() { return completed; }
    public double getAvgPercent() { return avgPercent; }
    public double getBestPercent() { return bestPercent; }
    public int getPassed() { return passed; }
    public List<QuizResult> getTrend() { return trend; }

    public int getPassRate() {
        return completed == 0 ? 0 : (int) Math.round(passed * 100.0 / completed);
    }
}
