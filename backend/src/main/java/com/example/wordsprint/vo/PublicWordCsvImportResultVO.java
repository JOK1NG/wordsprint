package com.example.wordsprint.vo;

import lombok.Data;

import java.util.List;

@Data
public class PublicWordCsvImportResultVO {

    private Integer totalRows;

    private Integer insertedCount;

    private Integer updatedCount;

    private Integer failedCount;

    private List<PublicWordCsvImportErrorVO> errors;
}
