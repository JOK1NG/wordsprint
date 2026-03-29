package com.example.wordsprint.controller;

import com.example.wordsprint.common.PageResult;
import com.example.wordsprint.common.Result;
import com.example.wordsprint.dto.WordCardCreateRequest;
import com.example.wordsprint.dto.WordCardPageQuery;
import com.example.wordsprint.dto.WordCardUpdateRequest;
import com.example.wordsprint.security.CurrentUserProvider;
import com.example.wordsprint.service.WordCardService;
import com.example.wordsprint.vo.WordCardImportResultVO;
import com.example.wordsprint.vo.WordCardVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/word-cards")
public class WordCardController {

    private final WordCardService wordCardService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public Result<WordCardVO> create(@Valid @RequestBody WordCardCreateRequest request) {
        return Result.success(wordCardService.create(currentUserProvider.getCurrentUserId(), request));
    }

    @PutMapping("/{id}")
    public Result<WordCardVO> update(@PathVariable Long id, @Valid @RequestBody WordCardUpdateRequest request) {
        return Result.success(wordCardService.update(currentUserProvider.getCurrentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        wordCardService.delete(currentUserProvider.getCurrentUserId(), id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<WordCardVO> getById(@PathVariable Long id) {
        return Result.success(wordCardService.getById(currentUserProvider.getCurrentUserId(), id));
    }

    @GetMapping
    public Result<PageResult<WordCardVO>> page(@Valid WordCardPageQuery query) {
        return Result.success(wordCardService.page(currentUserProvider.getCurrentUserId(), query));
    }

    @PostMapping("/import/csv")
    public Result<WordCardImportResultVO> importCsv(@RequestParam("file") MultipartFile file) {
        return Result.success(wordCardService.importCsv(currentUserProvider.getCurrentUserId(), file));
    }
}
