package com.quiz.model;

import java.io.Serializable;

/** A single multiple-choice question with four options (A-D). */
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int quizId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private String explanation;

    public Question() {
    }

    public Question(int id, int quizId, String questionText, String optionA, String optionB,
                    String optionC, String optionD, String correctOption, String explanation) {
        this.id = id;
        this.quizId = quizId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.explanation = explanation;
    }

    /** Returns the text of option A, B, C or D (case-insensitive). */
    public String getOption(String letter) {
        if (letter == null) {
            return null;
        }
        switch (letter.toUpperCase()) {
            case "A": return optionA;
            case "B": return optionB;
            case "C": return optionC;
            case "D": return optionD;
            default: return null;
        }
    }

    public boolean isCorrect(String selected) {
        return selected != null && selected.equalsIgnoreCase(correctOption);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
