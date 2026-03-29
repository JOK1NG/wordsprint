package com.example.wordsprint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_checkin")
public class DailyCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate checkinDate;

    private Integer studyCount;

    private Integer correctCount;

    private Integer totalDurationSeconds;

    private Integer pointsEarned;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
