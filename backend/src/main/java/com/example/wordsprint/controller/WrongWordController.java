package com.example.wordsprint.controller;

import com.example.wordsprint.common.PageResult;
import com.example.wordsprint.common.Result;
import com.example.wordsprint.dto.WrongWordPageQuery;
import com.example.wordsprint.security.CurrentUserProvider;
import com.example.wordsprint.service.WrongWordService;
import com.example.wordsprint.vo.StudyRandomResponse;
import com.example.wordsprint.vo.WrongWordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wrong-words")
public class WrongWordController {

    private final WrongWordService wrongWordService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public Result<Map<String, Object>> list(@Valid WrongWordPageQuery query) {
        Long userId = currentUserProvider.getCurrentUserId();
        PageResult<WrongWordVO> pageResult = wrongWordService.list(userId, query);

        // 获取统计信息
        int activeCount = wrongWordService.countByStatus(userId, "ACTIVE");
        int totalCount = wrongWordService.countByStatus(userId, null);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getList());
        result.put("total", pageResult.getTotal());
        result.put("pageNum", pageResult.getPageNum());
        result.put("pageSize", pageResult.getPageSize());
        result.put("activeCount", activeCount);
        result.put("totalCount", totalCount);

        return Result.success(result);
    }

    @PostMapping("/{wordCardId}/remove")
    public Result<Void> remove(@PathVariable Long wordCardId) {
        wrongWordService.remove(currentUserProvider.getCurrentUserId(), wordCardId);
        return Result.success();
    }

    @PostMapping("/{wordCardId}/restore")
    public Result<Void> restore(@PathVariable Long wordCardId) {
        wrongWordService.restore(currentUserProvider.getCurrentUserId(), wordCardId);
        return Result.success();
    }

    @GetMapping("/practice")
    public Result<StudyRandomResponse> practice(@RequestParam(required = false) Integer size) {
        return Result.success(wrongWordService.practice(currentUserProvider.getCurrentUserId(), size));
    }
}
