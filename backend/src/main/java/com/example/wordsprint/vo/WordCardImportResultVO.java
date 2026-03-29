package com.example.wordsprint.vo;

import lombok.Data;

import java.util.List;

@Data
public class WordCardImportResultVO {

    private Integer totalRows;

    private Integer successCount;

    private Integer failedCount;

    private List<WordCardImportErrorVO> errors;
}
