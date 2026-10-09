package com.quiz.service;

import com.quiz.model.AnswerDetail;
import com.quiz.model.Question;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Pure scoring logic (no database, no servlet API) so it is easy to unit test.
 */
public final class QuizGrader {

    /** Outcome of grading one attempt. */
    public static final class Grade {
        private final int score;
        private final List<AnswerDetail> details;

        Grade(int score, List<AnswerDetail> details) {
            this.score = score;
            this.details = Collections.unmodifiableList(details);
        }

        public int getScore() { return score; }
        public List<AnswerDetail> getDetails() { return details; }
    }

    private QuizGrader() {
    }

    /**
     * Grades the chosen options against the answer key.
     *
     * @param questions all questions of the quiz
     * @param chosen    question id -> selected letter (missing/invalid = unanswered)
     */
    public static Grade grade(List<Question> questions, Map<Integer, String> chosen) {
        List<AnswerDetail> details = new ArrayList<>();
        int score = 0;
        int number = 1;
        for (Question q : questions) {
            String selected = normalise(chosen.get(q.getId()));
            AnswerDetail d = new AnswerDetail(number++, q, selected);
            if (d.isCorrect()) {
                score++;
            }
            details.add(d);
        }
        return new Grade(score, details);
    }

    /** Accepts only A-D (any case); everything else counts as "no answer". */
    static String normalise(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim().toUpperCase();
        return v.matches("[ABCD]") ? v : null;
    }
}
