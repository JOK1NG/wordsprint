package com.example.wordsprint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class StudyPlanVO {

    private Integer dailyTargetCount;

    private Integer reviewTargetCount;

    private Boolean reminderEnabled;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime reminderTime;
}
