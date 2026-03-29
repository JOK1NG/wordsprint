package com.example.wordsprint.vo;

import lombok.Data;

@Data
public class PublicWordCsvImportErrorVO {

    private Integer lineNumber;

    private String reason;
}
