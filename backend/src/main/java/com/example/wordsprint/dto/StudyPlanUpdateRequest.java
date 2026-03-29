package com.example.wordsprint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class StudyPlanUpdateRequest {

    @NotNull
    @Min(1)
    @Max(500)
    private Integer dailyTargetCount;

    @NotNull
    @Min(1)
    @Max(500)
    private Integer reviewTargetCount;

    @NotNull
    private Boolean reminderEnabled;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime reminderTime;
}
