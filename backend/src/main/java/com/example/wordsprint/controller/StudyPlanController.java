package com.example.wordsprint.controller;

import com.example.wordsprint.common.Result;
import com.example.wordsprint.dto.StudyPlanUpdateRequest;
import com.example.wordsprint.security.CurrentUserProvider;
import com.example.wordsprint.service.StudyPlanService;
import com.example.wordsprint.vo.StudyPlanVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-plan")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public Result<StudyPlanVO> get() {
        return Result.success(studyPlanService.get(currentUserProvider.getCurrentUserId()));
    }

    @PutMapping
    public Result<StudyPlanVO> update(@Valid @RequestBody StudyPlanUpdateRequest request) {
        return Result.success(studyPlanService.update(currentUserProvider.getCurrentUserId(), request));
    }
}
