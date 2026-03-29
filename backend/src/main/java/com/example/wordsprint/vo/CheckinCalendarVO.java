package com.example.wordsprint.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CheckinCalendarVO {

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer currentStreakDays;

    private List<CheckinCalendarDayVO> days;
}
