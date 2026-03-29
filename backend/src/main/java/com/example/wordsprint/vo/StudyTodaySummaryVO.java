package com.example.wordsprint.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudyTodaySummaryVO {

    private LocalDate date;

    private Integer studyCount;

    private Integer correctCount;

    private Double accuracyRate;

    private Integer durationSeconds;

    private Integer pointsEarned;

    private Boolean checkedIn;

    private Integer streakDays;

    private Integer totalPoints;

    private Integer pendingReviewCount;

    private Integer totalStudied;

    private Integer totalCorrect;
}
