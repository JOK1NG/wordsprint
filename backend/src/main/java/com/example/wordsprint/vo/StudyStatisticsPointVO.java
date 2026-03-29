package com.example.wordsprint.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudyStatisticsPointVO {

    private LocalDate date;

    private Integer studyCount;

    private Integer correctCount;

    private Double accuracyRate;

    private Integer durationSeconds;

    private Integer pointsEarned;
}
