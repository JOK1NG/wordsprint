package com.example.wordsprint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wrong_word")
public class WrongWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long wordCardId;

    private Integer wrongCount;

    private Integer resolvedCorrectStreak;

    private LocalDateTime lastWrongAt;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
