package com.example.wordsprint.controller;

import com.example.wordsprint.common.Result;
import com.example.wordsprint.dto.StudyRandomQuery;
import com.example.wordsprint.dto.StudySubmitRequest;
import com.example.wordsprint.security.CurrentUserProvider;
import com.example.wordsprint.service.StudyService;
import com.example.wordsprint.vo.StudyRandomResponse;
import com.example.wordsprint.vo.StudySubmitResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study")
public class StudyController {

    private final StudyService studyService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/random")
    public Result<StudyRandomResponse> random(@Valid StudyRandomQuery query) {
        return Result.success(studyService.random(currentUserProvider.getCurrentUserId(), query));
    }

    @PostMapping("/submit")
    public Result<StudySubmitResponse> submit(@Valid @RequestBody StudySubmitRequest request) {
        return Result.success(studyService.submit(currentUserProvider.getCurrentUserId(), request));
    }
}
