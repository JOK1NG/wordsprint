package com.example.wordsprint.vo;

import lombok.Data;

@Data
public class PublicWordImportResultVO {

    private Long wordCardId;

    private Boolean imported;

    private String word;
}
