package com.example.wordsprint.controller;

import com.example.wordsprint.common.Result;
import com.example.wordsprint.security.CurrentUserProvider;
import com.example.wordsprint.service.StudyService;
import com.example.wordsprint.vo.CheckinCalendarVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checkin")
public class CheckinController {

    private final StudyService studyService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/calendar")
    public Result<CheckinCalendarVO> calendar() {
        return Result.success(studyService.calendar(currentUserProvider.getCurrentUserId()));
    }
}
