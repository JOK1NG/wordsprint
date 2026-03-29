package com.example.wordsprint.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class StudyStatisticsVO {

    private String rangeType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer rangeStudyCount;

    private Integer rangeCorrectCount;

    private Double rangeAccuracyRate;

    private Integer rangeDurationSeconds;

    private Integer rangePointsEarned;

    private Integer totalStudied;

    private Integer totalCorrect;

    private Double totalAccuracyRate;

    private Integer totalPoints;

    private Integer streakDays;

    private Integer maxStreakDays;

    private Integer pendingReviewCount;

    private Integer totalWrongCount;

    private List<StudyStatisticsPointVO> trend;
}
