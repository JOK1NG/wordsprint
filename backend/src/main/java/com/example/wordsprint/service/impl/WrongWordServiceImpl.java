package com.example.wordsprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.common.ErrorCode;
import com.example.wordsprint.common.PageResult;
import com.example.wordsprint.dto.StudyRandomQuery;
import com.example.wordsprint.dto.WrongWordPageQuery;
import com.example.wordsprint.entity.WordCard;
import com.example.wordsprint.entity.WrongWord;
import com.example.wordsprint.mapper.WordCardMapper;
import com.example.wordsprint.mapper.WrongWordMapper;
import com.example.wordsprint.service.StudyService;
import com.example.wordsprint.service.WrongWordService;
import com.example.wordsprint.vo.StudyRandomResponse;
import com.example.wordsprint.vo.WrongWordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WrongWordServiceImpl implements WrongWordService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_RESOLVED = "RESOLVED";
    private static final String MODE_WRONG_REVIEW = "WRONG_REVIEW";

    private final WrongWordMapper wrongWordMapper;
    private final WordCardMapper wordCardMapper;
    private final StudyService studyService;

    @Override
    public PageResult<WrongWordVO> list(Long userId, WrongWordPageQuery query) {
        Page<WrongWord> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<WrongWord> wrapper = new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId)
                .orderByDesc(WrongWord::getLastWrongAt);

        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(WrongWord::getStatus, query.getStatus());
        }

        Page<WrongWord> wrongWordPage = wrongWordMapper.selectPage(page, wrapper);

        if (wrongWordPage.getRecords().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), wrongWordPage.getTotal(),
                    wrongWordPage.getCurrent(), wrongWordPage.getSize());
        }

        List<Long> wordCardIds = wrongWordPage.getRecords().stream()
                .map(WrongWord::getWordCardId)
                .distinct()
                .toList();

        List<WordCard> wordCards = wordCardMapper.selectList(
                new LambdaQueryWrapper<WordCard>()
                        .eq(WordCard::getUserId, userId)
                        .in(WordCard::getId, wordCardIds));

        Map<Long, WordCard> wordCardMap = wordCards.stream()
                .collect(Collectors.toMap(WordCard::getId, card -> card));

        List<WrongWordVO> voList = wrongWordPage.getRecords().stream()
                .map(wrongWord -> convertToVO(wrongWord, wordCardMap.get(wrongWord.getWordCardId())))
                .toList();

        return new PageResult<>(voList, wrongWordPage.getTotal(),
                wrongWordPage.getCurrent(), wrongWordPage.getSize());
    }

    @Override
    public void remove(Long userId, Long wordCardId) {
        WrongWord wrongWord = wrongWordMapper.selectOne(
                new LambdaQueryWrapper<WrongWord>()
                        .eq(WrongWord::getUserId, userId)
                        .eq(WrongWord::getWordCardId, wordCardId)
                        .last("LIMIT 1"));

        if (wrongWord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "错题不存在");
        }

        if (STATUS_RESOLVED.equals(wrongWord.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "该错题已被移除");
        }

        wrongWord.setStatus(STATUS_RESOLVED);
        wrongWordMapper.updateById(wrongWord);
        log.info("错题移除: userId={}, wordCardId={}", userId, wordCardId);
    }

    @Override
    public void restore(Long userId, Long wordCardId) {
        WrongWord wrongWord = wrongWordMapper.selectOne(
                new LambdaQueryWrapper<WrongWord>()
                        .eq(WrongWord::getUserId, userId)
                        .eq(WrongWord::getWordCardId, wordCardId)
                        .last("LIMIT 1"));

        if (wrongWord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "错题不存在");
        }

        wrongWord.setStatus(STATUS_ACTIVE);
        wrongWordMapper.updateById(wrongWord);
        log.info("错题恢复: userId={}, wordCardId={}", userId, wordCardId);
    }

    @Override
    public StudyRandomResponse practice(Long userId, Integer size) {
        StudyRandomQuery query = new StudyRandomQuery();
        query.setMode(MODE_WRONG_REVIEW);
        query.setSize(size == null ? 10 : size);

        return studyService.random(userId, query);
    }

    private WrongWordVO convertToVO(WrongWord wrongWord, WordCard wordCard) {
        WrongWordVO vo = new WrongWordVO();
        vo.setId(wrongWord.getId());
        vo.setWordCardId(wrongWord.getWordCardId());
        vo.setWrongCount(wrongWord.getWrongCount());
        vo.setLastWrongAt(wrongWord.getLastWrongAt());
        vo.setStatus(wrongWord.getStatus());
        vo.setResolvedCorrectStreak(wrongWord.getResolvedCorrectStreak());

        if (wordCard != null) {
            vo.setWord(wordCard.getWord());
            vo.setMeaning(wordCard.getMeaning());
        }

        return vo;
    }

    @Override
    public int countByStatus(Long userId, String status) {
        LambdaQueryWrapper<WrongWord> wrapper = new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(WrongWord::getStatus, status);
        }

        return Math.toIntExact(wrongWordMapper.selectCount(wrapper));
    }
}
