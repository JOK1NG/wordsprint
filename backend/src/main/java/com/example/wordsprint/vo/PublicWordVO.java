package com.example.wordsprint.vo;

import lombok.Data;

@Data
public class PublicWordVO {

    private Long id;

    private String word;

    private String phonetic;

    private String meaning;

    private String exampleSentence;

    private String levelTag;

    private String sourceName;
}
