package com.example.wordsprint.vo;

import lombok.Data;

import java.util.List;

@Data
public class StudyRandomResponse {

    private String sessionId;

    private String mode;

    private List<StudyQuestionVO> questions;
}
