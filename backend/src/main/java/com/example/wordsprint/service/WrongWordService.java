package com.example.wordsprint.service;

import com.example.wordsprint.common.PageResult;
import com.example.wordsprint.dto.WrongWordPageQuery;
import com.example.wordsprint.vo.StudyRandomResponse;
import com.example.wordsprint.vo.WrongWordVO;

public interface WrongWordService {

    PageResult<WrongWordVO> list(Long userId, WrongWordPageQuery query);

    void remove(Long userId, Long wordCardId);

    void restore(Long userId, Long wordCardId);

    StudyRandomResponse practice(Long userId, Integer size);

    int countByStatus(Long userId, String status);
}
