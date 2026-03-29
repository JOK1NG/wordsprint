package com.example.wordsprint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("study_plan")
public class StudyPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer dailyTargetCount;

    private Integer reviewTargetCount;

    private Integer reminderEnabled;

    private LocalTime reminderTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
