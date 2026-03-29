package com.example.wordsprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.common.ErrorCode;
import com.example.wordsprint.common.PageResult;
import com.example.wordsprint.dto.PublicWordPageQuery;
import com.example.wordsprint.entity.PublicWordLibrary;
import com.example.wordsprint.entity.WordCard;
import com.example.wordsprint.mapper.PublicWordLibraryMapper;
import com.example.wordsprint.mapper.WordCardMapper;
import com.example.wordsprint.service.PublicWordService;
import com.example.wordsprint.vo.PublicWordCsvImportErrorVO;
import com.example.wordsprint.vo.PublicWordCsvImportResultVO;
import com.example.wordsprint.vo.PublicWordImportResultVO;
import com.example.wordsprint.vo.PublicWordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublicWordServiceImpl implements PublicWordService {

    private static final String SOURCE_TYPE_PUBLIC = "PUBLIC";
    private static final String DEFAULT_MEMORY_STATUS = "NEW";
    private static final int WORD_MAX_LENGTH = 100;
    private static final int MEANING_MAX_LENGTH = 255;
    private static final int PHONETIC_MAX_LENGTH = 100;
    private static final int EXAMPLE_MAX_LENGTH = 500;
    private static final int LEVEL_TAG_MAX_LENGTH = 50;
    private static final int SOURCE_NAME_MAX_LENGTH = 100;
    private static final int MAX_ERROR_ITEMS = 50;
    private static final long MAX_CSV_FILE_SIZE = 20L * 1024 * 1024;
    private static final int BATCH_SIZE = 200;

    private final PublicWordLibraryMapper publicWordLibraryMapper;
    private final WordCardMapper wordCardMapper;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Override
    public PageResult<PublicWordVO> page(PublicWordPageQuery query) {
        String levelTag = StringUtils.hasText(query.getLevelTag()) ? query.getLevelTag().trim() : null;
        Page<PublicWordLibrary> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PublicWordLibrary> wrapper = new LambdaQueryWrapper<PublicWordLibrary>()
                .eq(PublicWordLibrary::getStatus, 1)
                .eq(levelTag != null, PublicWordLibrary::getLevelTag, levelTag)
                .orderByDesc(PublicWordLibrary::getUpdatedAt)
                .orderByDesc(PublicWordLibrary::getId);

        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(condition -> condition
                    .like(PublicWordLibrary::getWord, keyword)
                    .or()
                    .like(PublicWordLibrary::getMeaning, keyword));
        }

        Page<PublicWordLibrary> result = publicWordLibraryMapper.selectPage(page, wrapper);
        List<PublicWordVO> list = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public PublicWordImportResultVO importToUserWordCard(Long userId, Long publicWordId) {
        PublicWordLibrary publicWord = requireEnabledPublicWord(publicWordId);

        WordCard existing = wordCardMapper.selectOne(new LambdaQueryWrapper<WordCard>()
                .eq(WordCard::getUserId, userId)
                .eq(WordCard::getWord, publicWord.getWord())
                .eq(WordCard::getMeaning, publicWord.getMeaning())
                .eq(WordCard::getIsDeleted, 0)
                .last("LIMIT 1"));

        if (existing != null) {
            PublicWordImportResultVO result = new PublicWordImportResultVO();
            result.setImported(false);
            result.setWordCardId(existing.getId());
            result.setWord(existing.getWord());
            return result;
        }

        WordCard wordCard = new WordCard();
        wordCard.setUserId(userId);
        wordCard.setWord(publicWord.getWord());
        wordCard.setPhonetic(trimToNull(publicWord.getPhonetic()));
        wordCard.setMeaning(publicWord.getMeaning());
        wordCard.setExampleSentence(trimToNull(publicWord.getExampleSentence()));
        wordCard.setTags(trimToNull(publicWord.getLevelTag()));
        wordCard.setSourceType(SOURCE_TYPE_PUBLIC);
        wordCard.setFamiliarityLevel(0);
        wordCard.setMemoryStatus(DEFAULT_MEMORY_STATUS);
        wordCard.setWrongCount(0);
        wordCard.setCorrectCount(0);
        wordCard.setIsPublic(0);
        wordCard.setIsDeleted(0);
        wordCardMapper.insert(wordCard);

        PublicWordImportResultVO result = new PublicWordImportResultVO();
        result.setImported(true);
        result.setWordCardId(wordCard.getId());
        result.setWord(wordCard.getWord());
        return result;
    }

    @Override
    @Transactional
    public PublicWordCsvImportResultVO importCsv(MultipartFile file) {
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
        int insertedCount = 0;
        int updatedCount = 0;
        List<PublicWordCsvImportErrorVO> errors = new ArrayList<>();
        Map<String, PendingPublicWordWrite> pendingInsertMap = new LinkedHashMap<>();
        Map<String, PendingPublicWordWrite> pendingUpdateMap = new LinkedHashMap<>();

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
                    ImportRow importRow = parseImportRow(columns);
                    String key = buildPublicWordKey(importRow.word, importRow.levelTag);

                    PendingPublicWordWrite pendingInsert = pendingInsertMap.get(key);
                    if (pendingInsert != null) {
                        applyRowToPublicWord(pendingInsert.entity, importRow);
                        pendingInsert.lineNumbers.add(lineNumber);
                        continue;
                    }

                    PendingPublicWordWrite pendingUpdate = pendingUpdateMap.get(key);
                    if (pendingUpdate != null) {
                        applyRowToPublicWord(pendingUpdate.entity, importRow);
                        pendingUpdate.lineNumbers.add(lineNumber);
                        continue;
                    }

                    PublicWordLibrary existing = findByWordAndLevelTag(importRow.word, importRow.levelTag);
                    if (existing == null) {
                        PendingPublicWordWrite write = new PendingPublicWordWrite();
                        write.entity = buildPublicWordEntity(importRow);
                        write.lineNumbers.add(lineNumber);
                        pendingInsertMap.put(key, write);
                    } else {
                        applyRowToPublicWord(existing, importRow);
                        PendingPublicWordWrite write = new PendingPublicWordWrite();
                        write.entity = existing;
                        write.lineNumbers.add(lineNumber);
                        pendingUpdateMap.put(key, write);
                    }
                } catch (BusinessException ex) {
                    appendError(errors, lineNumber, ex.getMessage());
                } catch (Exception ex) {
                    appendError(errors, lineNumber, "写入失败，请检查数据格式");
                }
            }
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "读取 CSV 文件失败");
        }

        insertedCount = persistPendingInsertWrites(new ArrayList<>(pendingInsertMap.values()), errors);
        updatedCount = persistPendingUpdateWrites(new ArrayList<>(pendingUpdateMap.values()), errors);

        PublicWordCsvImportResultVO result = new PublicWordCsvImportResultVO();
        result.setTotalRows(totalRows);
        result.setInsertedCount(insertedCount);
        result.setUpdatedCount(updatedCount);
        result.setFailedCount(totalRows - insertedCount - updatedCount);
        result.setErrors(errors);
        return result;
    }

    private int persistPendingInsertWrites(List<PendingPublicWordWrite> writes, List<PublicWordCsvImportErrorVO> errors) {
        if (writes.isEmpty()) {
            return 0;
        }

        if (jdbcTemplate == null) {
            return persistInsertWritesOneByOne(writes, errors);
        }

        int success = 0;
        for (int start = 0; start < writes.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, writes.size());
            List<PendingPublicWordWrite> chunk = writes.subList(start, end);
            try {
                batchInsertPublicWordsChunk(chunk);
                success += countSuccessRows(chunk);
            } catch (Exception ex) {
                success += persistInsertWritesOneByOne(chunk, errors);
            }
        }
        return success;
    }

    private int persistPendingUpdateWrites(List<PendingPublicWordWrite> writes, List<PublicWordCsvImportErrorVO> errors) {
        if (writes.isEmpty()) {
            return 0;
        }

        if (jdbcTemplate == null) {
            return persistUpdateWritesOneByOne(writes, errors);
        }

        int success = 0;
        for (int start = 0; start < writes.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, writes.size());
            List<PendingPublicWordWrite> chunk = writes.subList(start, end);
            try {
                batchUpdatePublicWordsChunk(chunk);
                success += countSuccessRows(chunk);
            } catch (Exception ex) {
                success += persistUpdateWritesOneByOne(chunk, errors);
            }
        }
        return success;
    }

    private int persistInsertWritesOneByOne(List<PendingPublicWordWrite> writes, List<PublicWordCsvImportErrorVO> errors) {
        int success = 0;
        for (PendingPublicWordWrite write : writes) {
            try {
                publicWordLibraryMapper.insert(write.entity);
                success += write.lineNumbers.size();
            } catch (BusinessException ex) {
                appendErrors(errors, write.lineNumbers, ex.getMessage());
            } catch (Exception ex) {
                appendErrors(errors, write.lineNumbers, "写入失败，请检查数据格式");
            }
        }
        return success;
    }

    private int persistUpdateWritesOneByOne(List<PendingPublicWordWrite> writes, List<PublicWordCsvImportErrorVO> errors) {
        int success = 0;
        for (PendingPublicWordWrite write : writes) {
            try {
                publicWordLibraryMapper.updateById(write.entity);
                success += write.lineNumbers.size();
            } catch (BusinessException ex) {
                appendErrors(errors, write.lineNumbers, ex.getMessage());
            } catch (Exception ex) {
                appendErrors(errors, write.lineNumbers, "写入失败，请检查数据格式");
            }
        }
        return success;
    }

    private void batchInsertPublicWordsChunk(List<PendingPublicWordWrite> chunk) {
        StringBuilder sql = new StringBuilder("INSERT INTO public_word_library ")
                .append("(word, phonetic, meaning, example_sentence, level_tag, source_name, status) VALUES ");
        Object[] args = new Object[chunk.size() * 7];
        int argIndex = 0;

        for (int i = 0; i < chunk.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append("(?,?,?,?,?,?,?)");

            PublicWordLibrary entity = chunk.get(i).entity;
            args[argIndex++] = entity.getWord();
            args[argIndex++] = entity.getPhonetic();
            args[argIndex++] = entity.getMeaning();
            args[argIndex++] = entity.getExampleSentence();
            args[argIndex++] = entity.getLevelTag();
            args[argIndex++] = entity.getSourceName();
            args[argIndex++] = entity.getStatus();
        }

        jdbcTemplate.update(sql.toString(), args);
    }

    private void batchUpdatePublicWordsChunk(List<PendingPublicWordWrite> chunk) {
        String sql = "UPDATE public_word_library SET word = ?, phonetic = ?, meaning = ?, "
                + "example_sentence = ?, level_tag = ?, source_name = ?, status = ? WHERE id = ?";

        List<Object[]> batchArgs = new ArrayList<>(chunk.size());
        for (PendingPublicWordWrite write : chunk) {
            PublicWordLibrary entity = write.entity;
            batchArgs.add(new Object[]{
                    entity.getWord(),
                    entity.getPhonetic(),
                    entity.getMeaning(),
                    entity.getExampleSentence(),
                    entity.getLevelTag(),
                    entity.getSourceName(),
                    entity.getStatus(),
                    entity.getId()
            });
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private int countSuccessRows(List<PendingPublicWordWrite> writes) {
        int total = 0;
        for (PendingPublicWordWrite write : writes) {
            total += write.lineNumbers.size();
        }
        return total;
    }

    private PublicWordLibrary requireEnabledPublicWord(Long id) {
        PublicWordLibrary publicWord = publicWordLibraryMapper.selectOne(new LambdaQueryWrapper<PublicWordLibrary>()
                .eq(PublicWordLibrary::getId, id)
                .eq(PublicWordLibrary::getStatus, 1)
                .last("LIMIT 1"));
        if (publicWord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公共词条不存在");
        }
        return publicWord;
    }

    private PublicWordLibrary findByWordAndLevelTag(String word, String levelTag) {
        LambdaQueryWrapper<PublicWordLibrary> wrapper = new LambdaQueryWrapper<PublicWordLibrary>()
                .eq(PublicWordLibrary::getWord, word)
                .last("LIMIT 1");
        if (levelTag == null) {
            wrapper.isNull(PublicWordLibrary::getLevelTag);
        } else {
            wrapper.eq(PublicWordLibrary::getLevelTag, levelTag);
        }
        return publicWordLibraryMapper.selectOne(wrapper);
    }

    private ImportRow parseImportRow(List<String> columns) {
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
        String levelTag = trimToNull(getColumn(columns, 4));
        String sourceName = trimToNull(getColumn(columns, 5));
        Integer status = parseStatus(getColumn(columns, 6));

        if (phonetic != null && phonetic.length() > PHONETIC_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "phonetic 长度不能超过 100");
        }
        if (exampleSentence != null && exampleSentence.length() > EXAMPLE_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "exampleSentence 长度不能超过 500");
        }
        if (levelTag != null && levelTag.length() > LEVEL_TAG_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "levelTag 长度不能超过 50");
        }
        if (sourceName != null && sourceName.length() > SOURCE_NAME_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "sourceName 长度不能超过 100");
        }

        ImportRow row = new ImportRow();
        row.word = word;
        row.meaning = meaning;
        row.phonetic = phonetic;
        row.exampleSentence = exampleSentence;
        row.levelTag = levelTag;
        row.sourceName = sourceName;
        row.status = status;
        return row;
    }

    private PublicWordLibrary buildPublicWordEntity(ImportRow row) {
        PublicWordLibrary entity = new PublicWordLibrary();
        entity.setWord(row.word);
        applyRowToPublicWord(entity, row);
        return entity;
    }

    private void applyRowToPublicWord(PublicWordLibrary entity, ImportRow row) {
        entity.setMeaning(row.meaning);
        entity.setPhonetic(row.phonetic);
        entity.setExampleSentence(row.exampleSentence);
        entity.setLevelTag(row.levelTag);
        entity.setSourceName(row.sourceName);
        entity.setStatus(row.status);
    }

    private String buildPublicWordKey(String word, String levelTag) {
        return word + "\u0001" + (levelTag == null ? "" : levelTag);
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

    private Integer parseStatus(String rawStatus) {
        if (!StringUtils.hasText(rawStatus)) {
            return 1;
        }
        String value = rawStatus.trim().toLowerCase();
        if ("1".equals(value) || "true".equals(value) || "yes".equals(value) || "y".equals(value)) {
            return 1;
        }
        if ("0".equals(value) || "false".equals(value) || "no".equals(value) || "n".equals(value)) {
            return 0;
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "status 仅支持 1/0/true/false");
    }

    private String getColumn(List<String> columns, int index) {
        if (index < 0 || index >= columns.size()) {
            return null;
        }
        return columns.get(index);
    }

    private void appendError(List<PublicWordCsvImportErrorVO> errors, int lineNumber, String reason) {
        if (errors.size() >= MAX_ERROR_ITEMS) {
            return;
        }
        PublicWordCsvImportErrorVO error = new PublicWordCsvImportErrorVO();
        error.setLineNumber(lineNumber);
        error.setReason(reason);
        errors.add(error);
    }

    private void appendErrors(List<PublicWordCsvImportErrorVO> errors, List<Integer> lineNumbers, String reason) {
        for (Integer lineNumber : lineNumbers) {
            appendError(errors, lineNumber, reason);
        }
    }

    private static class ImportRow {
        private String word;
        private String meaning;
        private String phonetic;
        private String exampleSentence;
        private String levelTag;
        private String sourceName;
        private Integer status;
    }

    private static class PendingPublicWordWrite {
        private PublicWordLibrary entity;
        private List<Integer> lineNumbers = new ArrayList<>();
    }

    private PublicWordVO toVO(PublicWordLibrary publicWord) {
        PublicWordVO vo = new PublicWordVO();
        vo.setId(publicWord.getId());
        vo.setWord(publicWord.getWord());
        vo.setPhonetic(publicWord.getPhonetic());
        vo.setMeaning(publicWord.getMeaning());
        vo.setExampleSentence(publicWord.getExampleSentence());
        vo.setLevelTag(publicWord.getLevelTag());
        vo.setSourceName(publicWord.getSourceName());
        return vo;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
