package com.example.wordsprint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudyRandomQuery {

    @NotBlank
    private String mode;

    @Min(1)
    @Max(100)
    private Integer size = 10;
}
