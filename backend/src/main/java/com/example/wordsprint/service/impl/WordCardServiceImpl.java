package com.example.wordsprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.common.ErrorCode;
import com.example.wordsprint.common.PageResult;
import com.example.wordsprint.dto.WordCardCreateRequest;
import com.example.wordsprint.dto.WordCardPageQuery;
import com.example.wordsprint.dto.WordCardUpdateRequest;
import com.example.wordsprint.entity.WordCard;
import com.example.wordsprint.mapper.WordCardMapper;
import com.example.wordsprint.service.WordCardService;
import com.example.wordsprint.vo.WordCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WordCardServiceImpl implements WordCardService {

    private static final String DEFAULT_SOURCE_TYPE = "PRIVATE";
    private static final String DEFAULT_MEMORY_STATUS = "NEW";

    private final WordCardMapper wordCardMapper;

    @Override
    @Transactional
    public WordCardVO create(Long userId, WordCardCreateRequest request) {
        WordCard wordCard = new WordCard();
        wordCard.setUserId(userId);
        wordCard.setWord(request.getWord().trim());
        wordCard.setPhonetic(trimToNull(request.getPhonetic()));
        wordCard.setMeaning(request.getMeaning().trim());
        wordCard.setExampleSentence(trimToNull(request.getExampleSentence()));
        wordCard.setTags(joinTags(request.getTags()));
        wordCard.setSourceType(DEFAULT_SOURCE_TYPE);
        wordCard.setFamiliarityLevel(0);
        wordCard.setMemoryStatus(DEFAULT_MEMORY_STATUS);
        wordCard.setWrongCount(0);
        wordCard.setCorrectCount(0);
        wordCard.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()) ? 1 : 0);
        wordCard.setIsDeleted(0);

        wordCardMapper.insert(wordCard);
        return toVO(requireOwnedWordCard(userId, wordCard.getId()));
    }

    @Override
    @Transactional
    public WordCardVO update(Long userId, Long id, WordCardUpdateRequest request) {
        WordCard wordCard = requireOwnedWordCard(userId, id);
        wordCard.setWord(request.getWord().trim());
        wordCard.setPhonetic(trimToNull(request.getPhonetic()));
        wordCard.setMeaning(request.getMeaning().trim());
        wordCard.setExampleSentence(trimToNull(request.getExampleSentence()));
        wordCard.setTags(joinTags(request.getTags()));
        wordCard.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()) ? 1 : 0);

        wordCardMapper.updateById(wordCard);
        return toVO(requireOwnedWordCard(userId, id));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        WordCard wordCard = requireOwnedWordCard(userId, id);
        wordCardMapper.deleteById(wordCard.getId());
    }

    @Override
    public WordCardVO getById(Long userId, Long id) {
        return toVO(requireOwnedWordCard(userId, id));
    }

    @Override
    public PageResult<WordCardVO> page(Long userId, WordCardPageQuery query) {
        if (query.getMinFamiliarity() != null && query.getMaxFamiliarity() != null
                && query.getMinFamiliarity() > query.getMaxFamiliarity()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "最小熟练度不能大于最大熟练度");
        }

        Page<WordCard> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<WordCard> wrapper = new LambdaQueryWrapper<WordCard>()
                .eq(WordCard::getUserId, userId)
                .like(StringUtils.hasText(query.getTag()), WordCard::getTags, query.getTag())
                .eq(StringUtils.hasText(query.getMemoryStatus()), WordCard::getMemoryStatus, query.getMemoryStatus())
                .ge(query.getMinFamiliarity() != null, WordCard::getFamiliarityLevel, query.getMinFamiliarity())
                .le(query.getMaxFamiliarity() != null, WordCard::getFamiliarityLevel, query.getMaxFamiliarity())
                .gt(Boolean.TRUE.equals(query.getWrongOnly()), WordCard::getWrongCount, 0)
                .orderByDesc(WordCard::getUpdatedAt)
                .orderByDesc(WordCard::getId);

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(condition -> condition
                    .like(WordCard::getWord, query.getKeyword())
                    .or()
                    .like(WordCard::getMeaning, query.getKeyword()));
        }

        Page<WordCard> result = wordCardMapper.selectPage(page, wrapper);
        List<WordCardVO> list = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private WordCard requireOwnedWordCard(Long userId, Long id) {
        WordCard wordCard = wordCardMapper.selectOne(new LambdaQueryWrapper<WordCard>()
                .eq(WordCard::getId, id)
                .eq(WordCard::getUserId, userId)
                .last("LIMIT 1"));
        if (wordCard == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "单词卡不存在");
        }
        return wordCard;
    }

    private WordCardVO toVO(WordCard wordCard) {
        WordCardVO vo = new WordCardVO();
        vo.setId(wordCard.getId());
        vo.setWord(wordCard.getWord());
        vo.setPhonetic(wordCard.getPhonetic());
        vo.setMeaning(wordCard.getMeaning());
        vo.setExampleSentence(wordCard.getExampleSentence());
        vo.setTags(splitTags(wordCard.getTags()));
        vo.setFamiliarityLevel(wordCard.getFamiliarityLevel());
        vo.setMemoryStatus(wordCard.getMemoryStatus());
        vo.setWrongCount(wordCard.getWrongCount());
        vo.setCorrectCount(wordCard.getCorrectCount());
        vo.setLastStudiedAt(wordCard.getLastStudiedAt());
        vo.setIsPublic(wordCard.getIsPublic() != null && wordCard.getIsPublic() == 1);
        vo.setCreatedAt(wordCard.getCreatedAt());
        vo.setUpdatedAt(wordCard.getUpdatedAt());
        return vo;
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        String joinedTags = tags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining(","));
        return StringUtils.hasText(joinedTags) ? joinedTags : null;
    }

    private List<String> splitTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
