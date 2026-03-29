package com.example.wordsprint.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WrongWordVO {

    private Long id;

    private Long wordCardId;

    private String word;

    private String meaning;

    private Integer wrongCount;

    private LocalDateTime lastWrongAt;

    private String status;

    private Integer resolvedCorrectStreak;
}
