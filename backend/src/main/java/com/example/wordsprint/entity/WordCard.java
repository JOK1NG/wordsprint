package com.example.wordsprint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("word_card")
public class WordCard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String word;

    private String phonetic;

    private String meaning;

    private String exampleSentence;

    private String tags;

    private String sourceType;

    private Integer familiarityLevel;

    private String memoryStatus;

    private Integer wrongCount;

    private Integer correctCount;

    private LocalDateTime lastStudiedAt;

    private Integer isPublic;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
