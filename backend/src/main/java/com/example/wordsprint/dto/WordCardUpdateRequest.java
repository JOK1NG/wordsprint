package com.example.wordsprint.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class WordCardUpdateRequest {

    @NotBlank
    private String word;

    private String phonetic;

    @NotBlank
    private String meaning;

    private String exampleSentence;

    private List<String> tags;

    private Boolean isPublic;
}
