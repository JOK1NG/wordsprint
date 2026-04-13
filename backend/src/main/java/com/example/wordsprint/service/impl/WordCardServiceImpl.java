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
import com.example.wordsprint.vo.WordCardImportErrorVO;
import com.example.wordsprint.vo.WordCardImportResultVO;
import com.example.wordsprint.vo.WordCardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordCardServiceImpl implements WordCardService {

    private static final String DEFAULT_SOURCE_TYPE = "PRIVATE";
    private static final String DEFAULT_MEMORY_STATUS = "NEW";
    private static final int WORD_MAX_LENGTH = 100;
    private static final int MEANING_MAX_LENGTH = 255;
    private static final int PHONETIC_MAX_LENGTH = 100;
    private static final int EXAMPLE_MAX_LENGTH = 500;
    private static final int TAGS_MAX_LENGTH = 255;
    private static final int MAX_ERROR_ITEMS = 50;
    private static final long MAX_CSV_FILE_SIZE = 20L * 1024 * 1024;
    private static final int BATCH_SIZE = 200;

    private final WordCardMapper wordCardMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String POOL_KEY_PREFIX = "wordsprint:pool:";

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

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
        invalidateRandomPool(userId);
        log.info("单词卡创建: userId={}, wordCardId={}, word={}", userId, wordCard.getId(), wordCard.getWord());
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
        invalidateRandomPool(userId);
        log.info("单词卡更新: userId={}, wordCardId={}", userId, id);
        return toVO(requireOwnedWordCard(userId, id));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        WordCard wordCard = requireOwnedWordCard(userId, id);
        wordCardMapper.deleteById(wordCard.getId());
        invalidateRandomPool(userId);
        log.info("单词卡删除: userId={}, wordCardId={}", userId, id);
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

    @Override
    public WordCardImportResultVO importCsv(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请上传 CSV 文件");
        }
        if (file.getSize() > MAX_CSV_FILE_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CSV 文件不能超过 20MB");
        }

        String fileName = file.getOriginalFilename();
        if (StringUtils.hasText(fileName) && !fileName.toLowerCase().endsWith(".csv")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 .csv 文件");
        }

        int totalRows = 0;
        int successCount;
        List<WordCardImportErrorVO> errors = new ArrayList<>();
        List<WordCardImportRow> validRows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            boolean firstDataRowHandled = false;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                if (!StringUtils.hasText(line)) {
                    continue;
                }

                List<String> columns;
                try {
                    columns = parseCsvLine(line);
                } catch (IllegalArgumentException ex) {
                    appendError(errors, lineNumber, ex.getMessage());
                    continue;
                }

                if (!firstDataRowHandled && isHeader(columns)) {
                    firstDataRowHandled = true;
                    continue;
                }
                firstDataRowHandled = true;

                totalRows++;
                try {
                    WordCard wordCard = buildWordCardFromCsvRow(userId, columns);
                    WordCardImportRow row = new WordCardImportRow();
                    row.wordCard = wordCard;
                    row.lineNumber = lineNumber;
                    validRows.add(row);
                } catch (BusinessException ex) {
                    appendError(errors, lineNumber, ex.getMessage());
                } catch (Exception ex) {
                    appendError(errors, lineNumber, "写入失败，请检查数据格式");
                }
            }
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "读取 CSV 文件失败");
        }

        successCount = persistImportedWordCards(validRows, errors);

        if (successCount > 0) {
            invalidateRandomPool(userId);
        }

        WordCardImportResultVO result = new WordCardImportResultVO();
        result.setTotalRows(totalRows);
        result.setSuccessCount(successCount);
        result.setFailedCount(totalRows - successCount);
        result.setErrors(errors);
        log.info("词卡CSV导入完成: userId={}, total={}, success={}, failed={}", userId, totalRows, successCount, totalRows - successCount);
        return result;
    }

    private int persistImportedWordCards(List<WordCardImportRow> rows, List<WordCardImportErrorVO> errors) {
        if (rows.isEmpty()) {
            return 0;
        }

        if (jdbcTemplate == null) {
            return persistWordCardsOneByOne(rows, errors);
        }

        int success = 0;
        for (int start = 0; start < rows.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, rows.size());
            List<WordCardImportRow> chunk = rows.subList(start, end);
            try {
                batchInsertWordCardsChunk(chunk);
                success += chunk.size();
            } catch (Exception ex) {
                success += persistWordCardsOneByOne(chunk, errors);
            }
        }
        return success;
    }

    private int persistWordCardsOneByOne(List<WordCardImportRow> rows, List<WordCardImportErrorVO> errors) {
        int success = 0;
        for (WordCardImportRow row : rows) {
            try {
                wordCardMapper.insert(row.wordCard);
                success++;
            } catch (BusinessException ex) {
                appendError(errors, row.lineNumber, ex.getMessage());
            } catch (Exception ex) {
                appendError(errors, row.lineNumber, "写入失败，请检查数据格式");
            }
        }
        return success;
    }

    private void batchInsertWordCardsChunk(List<WordCardImportRow> chunk) {
        StringBuilder sql = new StringBuilder("INSERT INTO word_card ")
                .append("(user_id, word, phonetic, meaning, example_sentence, tags, source_type, ")
                .append("familiarity_level, memory_status, wrong_count, correct_count, is_public, is_deleted) VALUES ");

        Object[] args = new Object[chunk.size() * 13];
        int argIndex = 0;
        for (int i = 0; i < chunk.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append("(?,?,?,?,?,?,?,?,?,?,?,?,?)");

            WordCard wordCard = chunk.get(i).wordCard;
            args[argIndex++] = wordCard.getUserId();
            args[argIndex++] = wordCard.getWord();
            args[argIndex++] = wordCard.getPhonetic();
            args[argIndex++] = wordCard.getMeaning();
            args[argIndex++] = wordCard.getExampleSentence();
            args[argIndex++] = wordCard.getTags();
            args[argIndex++] = wordCard.getSourceType();
            args[argIndex++] = wordCard.getFamiliarityLevel();
            args[argIndex++] = wordCard.getMemoryStatus();
            args[argIndex++] = wordCard.getWrongCount();
            args[argIndex++] = wordCard.getCorrectCount();
            args[argIndex++] = wordCard.getIsPublic();
            args[argIndex++] = wordCard.getIsDeleted();
        }

        jdbcTemplate.update(sql.toString(), args);
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

    private WordCard buildWordCardFromCsvRow(Long userId, List<String> columns) {
        String word = trimToNull(getColumn(columns, 0));
        String meaning = trimToNull(getColumn(columns, 1));

        if (!StringUtils.hasText(word) || !StringUtils.hasText(meaning)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "word 和 meaning 不能为空");
        }
        if (word.length() > WORD_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "word 长度不能超过 100");
        }
        if (meaning.length() > MEANING_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "meaning 长度不能超过 255");
        }

        String phonetic = trimToNull(getColumn(columns, 2));
        String exampleSentence = trimToNull(getColumn(columns, 3));
        List<String> tags = parseTagsFromCsv(getColumn(columns, 4));
        boolean isPublic = parseIsPublic(getColumn(columns, 5));

        if (phonetic != null && phonetic.length() > PHONETIC_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "phonetic 长度不能超过 100");
        }
        if (exampleSentence != null && exampleSentence.length() > EXAMPLE_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "exampleSentence 长度不能超过 500");
        }
        String joinedTags = joinTags(tags);
        if (joinedTags != null && joinedTags.length() > TAGS_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "tags 长度不能超过 255");
        }

        WordCard wordCard = new WordCard();
        wordCard.setUserId(userId);
        wordCard.setWord(word);
        wordCard.setPhonetic(phonetic);
        wordCard.setMeaning(meaning);
        wordCard.setExampleSentence(exampleSentence);
        wordCard.setTags(joinedTags);
        wordCard.setSourceType(DEFAULT_SOURCE_TYPE);
        wordCard.setFamiliarityLevel(0);
        wordCard.setMemoryStatus(DEFAULT_MEMORY_STATUS);
        wordCard.setWrongCount(0);
        wordCard.setCorrectCount(0);
        wordCard.setIsPublic(isPublic ? 1 : 0);
        wordCard.setIsDeleted(0);
        return wordCard;
    }

    private List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (ch == ',' && !inQuotes) {
                columns.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }

        if (inQuotes) {
            throw new IllegalArgumentException("CSV 引号未闭合");
        }
        columns.add(current.toString());
        return columns;
    }

    private boolean isHeader(List<String> columns) {
        String first = normalizeHeader(getColumn(columns, 0));
        String second = normalizeHeader(getColumn(columns, 1));
        return ("word".equals(first) || "单词".equals(first))
                && ("meaning".equals(second) || "词义".equals(second));
    }

    private String normalizeHeader(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().toLowerCase()
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "");
    }

    private List<String> parseTagsFromCsv(String rawTags) {
        if (!StringUtils.hasText(rawTags)) {
            return Collections.emptyList();
        }
        return Arrays.stream(rawTags.split("[|,，;；]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private boolean parseIsPublic(String rawIsPublic) {
        if (!StringUtils.hasText(rawIsPublic)) {
            return false;
        }

        String value = rawIsPublic.trim().toLowerCase();
        if ("1".equals(value) || "true".equals(value) || "yes".equals(value) || "y".equals(value)) {
            return true;
        }
        if ("0".equals(value) || "false".equals(value) || "no".equals(value) || "n".equals(value)) {
            return false;
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "isPublic 仅支持 true/false/1/0");
    }

    private String getColumn(List<String> columns, int index) {
        if (index < 0 || index >= columns.size()) {
            return null;
        }
        return columns.get(index);
    }

    private void appendError(List<WordCardImportErrorVO> errors, int lineNumber, String reason) {
        if (errors.size() >= MAX_ERROR_ITEMS) {
            return;
        }
        WordCardImportErrorVO error = new WordCardImportErrorVO();
        error.setLineNumber(lineNumber);
        error.setReason(reason);
        errors.add(error);
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

    private void invalidateRandomPool(Long userId) {
        stringRedisTemplate.delete(POOL_KEY_PREFIX + userId);
    }

    private static class WordCardImportRow {
        private WordCard wordCard;
        private int lineNumber;
    }
}
