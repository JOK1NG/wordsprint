package com.example.wordsprint.service;

import com.example.wordsprint.common.PageResult;
import com.example.wordsprint.dto.WordCardCreateRequest;
import com.example.wordsprint.dto.WordCardPageQuery;
import com.example.wordsprint.dto.WordCardUpdateRequest;
import com.example.wordsprint.vo.WordCardVO;

public interface WordCardService {

    WordCardVO create(Long userId, WordCardCreateRequest request);

    WordCardVO update(Long userId, Long id, WordCardUpdateRequest request);

    void delete(Long userId, Long id);

    WordCardVO getById(Long userId, Long id);

    PageResult<WordCardVO> page(Long userId, WordCardPageQuery query);
}
