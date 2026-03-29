package com.example.wordsprint.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckinCalendarDayVO {

    private LocalDate date;

    private Boolean checkedIn;

    private Integer studyCount;

    private Integer correctCount;

    private Integer pointsEarned;
}
