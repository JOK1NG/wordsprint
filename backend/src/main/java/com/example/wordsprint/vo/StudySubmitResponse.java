package com.example.wordsprint.vo;

import lombok.Data;

@Data
public class StudySubmitResponse {

    private Boolean isCorrect;

    private String correctAnswer;

    private Integer familiarityLevel;

    private String memoryStatus;
}
