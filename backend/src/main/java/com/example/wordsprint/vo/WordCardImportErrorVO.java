package com.example.wordsprint.vo;

import lombok.Data;

@Data
public class WordCardImportErrorVO {

    private Integer lineNumber;

    private String reason;
}
