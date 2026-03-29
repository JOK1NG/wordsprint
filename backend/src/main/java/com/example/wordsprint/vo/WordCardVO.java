package com.example.wordsprint.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WordCardVO {

    private Long id;

    private String word;

    private String phonetic;

    private String meaning;

    private String exampleSentence;

    private List<String> tags;

    private Integer familiarityLevel;

    private String memoryStatus;

    private Integer wrongCount;

    private Integer correctCount;

    private LocalDateTime lastStudiedAt;

    private Boolean isPublic;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
