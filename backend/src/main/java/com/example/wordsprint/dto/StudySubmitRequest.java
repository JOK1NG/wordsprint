package com.example.wordsprint.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudySubmitRequest {

    @NotNull
    private Long wordCardId;

    @NotBlank
    private String studyMode;

    @NotBlank
    private String answerContent;

    @NotNull
    @Min(0)
    private Integer durationSeconds = 0;
}
