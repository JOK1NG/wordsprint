package com.example.wordsprint.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudyStatisticsQuery {

    @NotBlank
    private String rangeType;
}
