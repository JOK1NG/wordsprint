package com.example.wordsprint.vo;

import lombok.Data;

import java.util.List;

@Data
public class StudyQuestionVO {

    private String questionId;

    private Long wordCardId;

    private String word;

    private String meaning;

    private List<String> options;
}
