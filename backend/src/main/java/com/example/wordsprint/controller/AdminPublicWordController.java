package com.example.wordsprint.controller;

import com.example.wordsprint.common.Result;
import com.example.wordsprint.security.CurrentUserProvider;
import com.example.wordsprint.service.PublicWordService;
import com.example.wordsprint.vo.PublicWordCsvImportResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/public-words")
public class AdminPublicWordController {

    private final PublicWordService publicWordService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/import/csv")
    public Result<PublicWordCsvImportResultVO> importCsv(@RequestParam("file") MultipartFile file) {
        currentUserProvider.requireAdmin();
        return Result.success(publicWordService.importCsv(file));
    }
}
