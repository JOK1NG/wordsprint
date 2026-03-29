package com.example.wordsprint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("public_word_library")
public class PublicWordLibrary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String word;

    private String phonetic;

    private String meaning;

    private String exampleSentence;

    private String levelTag;

    private String sourceName;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
