package com.example.wordsprint.controller;

import com.example.wordsprint.common.PageResult;
import com.example.wordsprint.common.Result;
import com.example.wordsprint.dto.PublicWordPageQuery;
import com.example.wordsprint.security.CurrentUserProvider;
import com.example.wordsprint.service.PublicWordService;
import com.example.wordsprint.vo.PublicWordImportResultVO;
import com.example.wordsprint.vo.PublicWordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public-words")
public class PublicWordController {

    private final PublicWordService publicWordService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public Result<PageResult<PublicWordVO>> page(@Valid PublicWordPageQuery query) {
        return Result.success(publicWordService.page(query));
    }

    @PostMapping("/{id}/import")
    public Result<PublicWordImportResultVO> importToMyWordCard(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return Result.success(publicWordService.importToUserWordCard(userId, id));
    }
}
