package com.example.wordsprint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("study_record")
public class StudyRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long wordCardId;

    private String studyMode;

    private String answerContent;

    private String correctAnswer;

    private Integer isCorrect;

    private Integer durationSeconds;

    private LocalDateTime studiedAt;
}
