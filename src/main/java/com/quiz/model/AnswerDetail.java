package com.quiz.model;

import java.io.Serializable;

/** A question together with what the participant answered (used for reports). */
public class AnswerDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private int number;
    private Question question;
    private String selectedOption;

    public AnswerDetail(int number, Question question, String selectedOption) {
        this.number = number;
        this.question = question;
        this.selectedOption = selectedOption;
    }

    public boolean isAnswered() {
        return selectedOption != null && !selectedOption.isBlank();
    }

    public boolean isCorrect() {
        return question.isCorrect(selectedOption);
    }

    public String getSelectedText() { return question.getOption(selectedOption); }
    public String getCorrectText() { return question.getOption(question.getCorrectOption()); }

    public int getNumber() { return number; }
    public Question getQuestion() { return question; }
    public String getSelectedOption() { return selectedOption; }
}
