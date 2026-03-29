package com.example.wordsprint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class WordCardPageQuery {

    @Min(1)
    private long pageNum = 1;

    @Min(1)
    @Max(100)
    private long pageSize = 10;

    private String keyword;

    private String tag;

    private String memoryStatus;

    @Min(0)
    private Integer minFamiliarity;

    @Min(0)
    private Integer maxFamiliarity;

    private Boolean wrongOnly;
}
