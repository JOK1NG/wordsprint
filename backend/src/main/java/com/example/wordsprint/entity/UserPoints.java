package com.example.wordsprint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_points")
public class UserPoints {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer totalPoints;

    private Integer streakDays;

    private Integer maxStreakDays;

    private Integer totalStudied;

    private Integer totalCorrect;

    private Integer totalDurationSeconds;

    private LocalDate lastCheckinDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
