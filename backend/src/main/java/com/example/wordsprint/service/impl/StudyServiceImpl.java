package com.example.wordsprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.common.ErrorCode;
import com.example.wordsprint.dto.StudyRandomQuery;
import com.example.wordsprint.dto.StudyStatisticsQuery;
import com.example.wordsprint.dto.StudySubmitRequest;
import com.example.wordsprint.entity.DailyCheckin;
import com.example.wordsprint.entity.StudyRecord;
import com.example.wordsprint.entity.UserPoints;
import com.example.wordsprint.entity.WrongWord;
import com.example.wordsprint.entity.WordCard;
import com.example.wordsprint.mapper.DailyCheckinMapper;
import com.example.wordsprint.mapper.StudyRecordMapper;
import com.example.wordsprint.mapper.UserPointsMapper;
import com.example.wordsprint.mapper.WrongWordMapper;
import com.example.wordsprint.mapper.WordCardMapper;
import com.example.wordsprint.service.StudyService;
import com.example.wordsprint.service.RedisRankService;
import com.example.wordsprint.vo.StudyQuestionVO;
import com.example.wordsprint.vo.StudyRandomResponse;
import com.example.wordsprint.vo.StudyStatisticsPointVO;
import com.example.wordsprint.vo.StudyStatisticsVO;
import com.example.wordsprint.vo.StudySubmitResponse;
import com.example.wordsprint.vo.StudyTodaySummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {

    private static final String MODE_WORD_TO_MEANING = "WORD_TO_MEANING";
    private static final String MODE_MEANING_TO_WORD = "MEANING_TO_WORD";
    private static final String MODE_CHOICE = "CHOICE";
    private static final String MODE_WRONG_REVIEW = "WRONG_REVIEW";

    private static final Set<String> ALLOWED_MODES = Set.of(
            MODE_WORD_TO_MEANING,
            MODE_MEANING_TO_WORD,
            MODE_CHOICE,
            MODE_WRONG_REVIEW
    );

    private static final int MAX_FAMILIARITY = 5;
    private static final int POINTS_PER_CORRECT = 1;
    private static final int RESOLVED_STREAK_THRESHOLD = 3;
    private static final int RANDOM_POOL_LIMIT = 200;
    private static final int CHOICE_OPTION_COUNT = 4;
    private static final Duration SUBMIT_LOCK_TTL = Duration.ofSeconds(3);
    private static final String RANGE_TYPE_WEEK = "WEEK";

    private final WordCardMapper wordCardMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final WrongWordMapper wrongWordMapper;
    private final DailyCheckinMapper dailyCheckinMapper;
    private final UserPointsMapper userPointsMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisRankService redisRankService;

    @Override
    public StudyRandomResponse random(Long userId, StudyRandomQuery query) {
        String mode = normalizeMode(query.getMode());
        validateMode(mode);
        int size = query.getSize() == null ? 10 : query.getSize();

        List<WordCard> cards = fetchRandomCards(userId, mode, size);
        List<String> choiceMeaningPool = MODE_CHOICE.equals(mode)
                ? fetchChoiceMeaningPool(userId)
                : Collections.emptyList();

        String sessionId = UUID.randomUUID().toString();
        List<StudyQuestionVO> questions = cards.stream()
                .map(card -> toQuestion(sessionId, card, mode, choiceMeaningPool))
                .toList();

        StudyRandomResponse response = new StudyRandomResponse();
        response.setSessionId(sessionId);
        response.setMode(mode);
        response.setQuestions(questions);
        return response;
    }

    @Override
    @Transactional
    public StudySubmitResponse submit(Long userId, StudySubmitRequest request) {
        String mode = normalizeMode(request.getStudyMode());
        validateMode(mode);
        acquireSubmitLock(userId, request, mode);

        WordCard wordCard = requireOwnedWordCard(userId, request.getWordCardId());
        String correctAnswer = resolveCorrectAnswer(mode, wordCard);
        boolean isCorrect = isAnswerCorrect(request.getAnswerContent(), correctAnswer);

        recordStudy(userId, wordCard.getId(), mode, request, correctAnswer, isCorrect);
        updateWordCardAfterSubmit(wordCard, isCorrect);
        updateWrongWord(userId, wordCard.getId(), isCorrect);
        updateDailyCheckin(userId, request.getDurationSeconds(), isCorrect);
        updateUserPoints(userId, request.getDurationSeconds(), isCorrect);

        StudySubmitResponse response = new StudySubmitResponse();
        response.setIsCorrect(isCorrect);
        response.setCorrectAnswer(correctAnswer);
        response.setFamiliarityLevel(wordCard.getFamiliarityLevel());
        response.setMemoryStatus(wordCard.getMemoryStatus());
        return response;
    }

    @Override
    public StudyTodaySummaryVO todaySummary(Long userId) {
        LocalDate today = LocalDate.now();
        DailyCheckin todayCheckin = getDailyCheckin(userId, today);
        UserPoints userPoints = getUserPoints(userId);

        StudyTodaySummaryVO response = new StudyTodaySummaryVO();
        response.setDate(today);
        response.setStudyCount(todayCheckin == null ? 0 : nullSafe(todayCheckin.getStudyCount()));
        response.setCorrectCount(todayCheckin == null ? 0 : nullSafe(todayCheckin.getCorrectCount()));
        response.setAccuracyRate(calculateRate(response.getCorrectCount(), response.getStudyCount()));
        response.setDurationSeconds(todayCheckin == null ? 0 : nullSafe(todayCheckin.getTotalDurationSeconds()));
        response.setPointsEarned(todayCheckin == null ? 0 : nullSafe(todayCheckin.getPointsEarned()));
        response.setCheckedIn(todayCheckin != null && response.getStudyCount() > 0);
        response.setStreakDays(resolveCurrentStreakDays(userPoints));
        response.setTotalPoints(userPoints == null ? 0 : nullSafe(userPoints.getTotalPoints()));
        response.setPendingReviewCount(countWrongWords(userId, "ACTIVE"));
        response.setTotalStudied(userPoints == null ? 0 : nullSafe(userPoints.getTotalStudied()));
        response.setTotalCorrect(userPoints == null ? 0 : nullSafe(userPoints.getTotalCorrect()));
        return response;
    }

    @Override
    public StudyStatisticsVO statistics(Long userId, StudyStatisticsQuery query) {
        String rangeType = normalizeRangeType(query.getRangeType());
        if (!RANGE_TYPE_WEEK.equals(rangeType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "统计范围不合法");
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        List<DailyCheckin> checkins = dailyCheckinMapper.selectList(new LambdaQueryWrapper<DailyCheckin>()
                .eq(DailyCheckin::getUserId, userId)
                .between(DailyCheckin::getCheckinDate, startDate, endDate)
                .orderByAsc(DailyCheckin::getCheckinDate));
        Map<LocalDate, DailyCheckin> checkinMap = checkins.stream()
                .collect(Collectors.toMap(DailyCheckin::getCheckinDate, checkin -> checkin));

        List<StudyStatisticsPointVO> trend = new ArrayList<>();
        int rangeStudyCount = 0;
        int rangeCorrectCount = 0;
        int rangeDurationSeconds = 0;
        int rangePointsEarned = 0;

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        for (int i = 0; i <= days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            DailyCheckin checkin = checkinMap.get(currentDate);

            int studyCount = checkin == null ? 0 : nullSafe(checkin.getStudyCount());
            int correctCount = checkin == null ? 0 : nullSafe(checkin.getCorrectCount());
            int durationSeconds = checkin == null ? 0 : nullSafe(checkin.getTotalDurationSeconds());
            int pointsEarned = checkin == null ? 0 : nullSafe(checkin.getPointsEarned());

            StudyStatisticsPointVO point = new StudyStatisticsPointVO();
            point.setDate(currentDate);
            point.setStudyCount(studyCount);
            point.setCorrectCount(correctCount);
            point.setAccuracyRate(calculateRate(correctCount, studyCount));
            point.setDurationSeconds(durationSeconds);
            point.setPointsEarned(pointsEarned);
            trend.add(point);

            rangeStudyCount += studyCount;
            rangeCorrectCount += correctCount;
            rangeDurationSeconds += durationSeconds;
            rangePointsEarned += pointsEarned;
        }

        UserPoints userPoints = getUserPoints(userId);

        StudyStatisticsVO response = new StudyStatisticsVO();
        response.setRangeType(rangeType);
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setRangeStudyCount(rangeStudyCount);
        response.setRangeCorrectCount(rangeCorrectCount);
        response.setRangeAccuracyRate(calculateRate(rangeCorrectCount, rangeStudyCount));
        response.setRangeDurationSeconds(rangeDurationSeconds);
        response.setRangePointsEarned(rangePointsEarned);
        response.setTotalStudied(userPoints == null ? 0 : nullSafe(userPoints.getTotalStudied()));
        response.setTotalCorrect(userPoints == null ? 0 : nullSafe(userPoints.getTotalCorrect()));
        response.setTotalAccuracyRate(calculateRate(response.getTotalCorrect(), response.getTotalStudied()));
        response.setTotalPoints(userPoints == null ? 0 : nullSafe(userPoints.getTotalPoints()));
        response.setStreakDays(resolveCurrentStreakDays(userPoints));
        response.setMaxStreakDays(userPoints == null ? 0 : nullSafe(userPoints.getMaxStreakDays()));
        response.setPendingReviewCount(countWrongWords(userId, "ACTIVE"));
        response.setTotalWrongCount(countWrongWords(userId, null));
        response.setTrend(trend);
        return response;
    }

    private List<WordCard> fetchRandomCards(Long userId, String mode, int size) {
        if (MODE_WRONG_REVIEW.equals(mode)) {
            return fetchWrongReviewCards(userId, size);
        }

        List<WordCard> pool = wordCardMapper.selectList(new LambdaQueryWrapper<WordCard>()
                .eq(WordCard::getUserId, userId)
                .orderByDesc(WordCard::getUpdatedAt)
                .orderByDesc(WordCard::getId)
                .last("LIMIT " + RANDOM_POOL_LIMIT));

        if (pool.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(pool);
        return pool.subList(0, Math.min(size, pool.size()));
    }

    private List<WordCard> fetchWrongReviewCards(Long userId, int size) {
        List<WrongWord> wrongPool = wrongWordMapper.selectList(new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId)
                .eq(WrongWord::getStatus, "ACTIVE")
                .orderByDesc(WrongWord::getLastWrongAt)
                .last("LIMIT " + RANDOM_POOL_LIMIT));
        if (wrongPool.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(wrongPool);
        List<Long> ids = wrongPool.stream()
                .limit(size)
                .map(WrongWord::getWordCardId)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<WordCard> cards = wordCardMapper.selectList(new LambdaQueryWrapper<WordCard>()
                .eq(WordCard::getUserId, userId)
                .in(WordCard::getId, ids));
        if (cards.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, WordCard> cardMap = cards.stream().collect(Collectors.toMap(WordCard::getId, card -> card));
        List<WordCard> ordered = new ArrayList<>();
        for (Long id : ids) {
            WordCard card = cardMap.get(id);
            if (card != null) {
                ordered.add(card);
            }
        }
        return ordered;
    }

    private List<String> fetchChoiceMeaningPool(Long userId) {
        List<WordCard> cards = wordCardMapper.selectList(new LambdaQueryWrapper<WordCard>()
                .eq(WordCard::getUserId, userId)
                .isNotNull(WordCard::getMeaning)
                .orderByDesc(WordCard::getUpdatedAt)
                .orderByDesc(WordCard::getId)
                .last("LIMIT " + RANDOM_POOL_LIMIT));
        return cards.stream()
                .map(WordCard::getMeaning)
                .map(this::trimToNull)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private List<String> buildChoiceOptions(WordCard currentCard, List<String> choiceMeaningPool) {
        String correctMeaning = trimToNull(currentCard.getMeaning());
        if (!StringUtils.hasText(correctMeaning)) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> distractorSet = new LinkedHashSet<>();
        for (String meaning : choiceMeaningPool) {
            if (normalizeText(meaning).equals(normalizeText(correctMeaning))) {
                continue;
            }
            distractorSet.add(meaning);
            if (distractorSet.size() >= CHOICE_OPTION_COUNT - 1) {
                break;
            }
        }

        List<String> distractors = new ArrayList<>(distractorSet);
        Collections.shuffle(distractors);

        List<String> options = new ArrayList<>();
        options.add(correctMeaning);
        options.addAll(distractors.stream().limit(CHOICE_OPTION_COUNT - 1).toList());
        Collections.shuffle(options);
        return options;
    }

    private void acquireSubmitLock(Long userId, StudySubmitRequest request, String mode) {
        String answerFingerprint = Integer.toHexString(normalizeText(request.getAnswerContent()).hashCode());
        String lockKey = String.format("wordsprint:submit:lock:%d:%d:%s:%s", userId, request.getWordCardId(), mode, answerFingerprint);
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", SUBMIT_LOCK_TTL);
        if (!Boolean.TRUE.equals(success)) {
            throw new BusinessException(ErrorCode.CONFLICT, "请勿重复提交");
        }
    }

    private StudyQuestionVO toQuestion(String sessionId, WordCard card, String mode, List<String> choiceMeaningPool) {
        StudyQuestionVO question = new StudyQuestionVO();
        question.setQuestionId(sessionId + "-" + card.getId());
        question.setWordCardId(card.getId());
        question.setOptions(Collections.emptyList());

        if (MODE_MEANING_TO_WORD.equals(mode)) {
            question.setWord(null);
            question.setMeaning(card.getMeaning());
            return question;
        }

        if (MODE_CHOICE.equals(mode)) {
            question.setWord(card.getWord());
            question.setMeaning(null);
            question.setOptions(buildChoiceOptions(card, choiceMeaningPool));
            return question;
        }

        question.setWord(card.getWord());
        question.setMeaning(null);
        return question;
    }

    private void recordStudy(Long userId,
                             Long wordCardId,
                             String mode,
                             StudySubmitRequest request,
                             String correctAnswer,
                             boolean isCorrect) {
        StudyRecord record = new StudyRecord();
        record.setUserId(userId);
        record.setWordCardId(wordCardId);
        record.setStudyMode(mode);
        record.setAnswerContent(trimToNull(request.getAnswerContent()));
        record.setCorrectAnswer(correctAnswer);
        record.setIsCorrect(isCorrect ? 1 : 0);
        record.setDurationSeconds(Math.max(request.getDurationSeconds(), 0));
        record.setStudiedAt(LocalDateTime.now());
        studyRecordMapper.insert(record);
    }

    private void updateWordCardAfterSubmit(WordCard wordCard, boolean isCorrect) {
        int familiarity = wordCard.getFamiliarityLevel() == null ? 0 : wordCard.getFamiliarityLevel();
        int nextFamiliarity = clampFamiliarity(familiarity + (isCorrect ? 1 : -1));

        wordCard.setFamiliarityLevel(nextFamiliarity);
        wordCard.setMemoryStatus(resolveMemoryStatus(nextFamiliarity, isCorrect));
        wordCard.setLastStudiedAt(LocalDateTime.now());
        if (isCorrect) {
            wordCard.setCorrectCount((wordCard.getCorrectCount() == null ? 0 : wordCard.getCorrectCount()) + 1);
        } else {
            wordCard.setWrongCount((wordCard.getWrongCount() == null ? 0 : wordCard.getWrongCount()) + 1);
        }

        wordCardMapper.updateById(wordCard);
    }

    private void updateWrongWord(Long userId, Long wordCardId, boolean isCorrect) {
        WrongWord wrongWord = getWrongWord(userId, wordCardId);

        if (isCorrect) {
            if (wrongWord == null || !"ACTIVE".equals(wrongWord.getStatus())) {
                return;
            }
            int streak = wrongWord.getResolvedCorrectStreak() == null ? 0 : wrongWord.getResolvedCorrectStreak();
            wrongWord.setResolvedCorrectStreak(streak + 1);
            if (wrongWord.getResolvedCorrectStreak() >= RESOLVED_STREAK_THRESHOLD) {
                wrongWord.setStatus("RESOLVED");
            }
            wrongWordMapper.updateById(wrongWord);
            return;
        }

        if (wrongWord == null) {
            wrongWord = new WrongWord();
            wrongWord.setUserId(userId);
            wrongWord.setWordCardId(wordCardId);
            wrongWord.setWrongCount(1);
            wrongWord.setResolvedCorrectStreak(0);
            wrongWord.setLastWrongAt(LocalDateTime.now());
            wrongWord.setStatus("ACTIVE");
            try {
                wrongWordMapper.insert(wrongWord);
            } catch (DuplicateKeyException ex) {
                WrongWord existing = getWrongWord(userId, wordCardId);
                if (existing != null) {
                    existing.setWrongCount((existing.getWrongCount() == null ? 0 : existing.getWrongCount()) + 1);
                    existing.setResolvedCorrectStreak(0);
                    existing.setLastWrongAt(LocalDateTime.now());
                    existing.setStatus("ACTIVE");
                    wrongWordMapper.updateById(existing);
                }
            }
            return;
        }

        wrongWord.setWrongCount((wrongWord.getWrongCount() == null ? 0 : wrongWord.getWrongCount()) + 1);
        wrongWord.setResolvedCorrectStreak(0);
        wrongWord.setLastWrongAt(LocalDateTime.now());
        wrongWord.setStatus("ACTIVE");
        wrongWordMapper.updateById(wrongWord);
    }

    private void updateDailyCheckin(Long userId, int durationSeconds, boolean isCorrect) {
        LocalDate today = LocalDate.now();
        DailyCheckin checkin = getOrCreateDailyCheckin(userId, today);

        checkin.setStudyCount((checkin.getStudyCount() == null ? 0 : checkin.getStudyCount()) + 1);
        if (isCorrect) {
            checkin.setCorrectCount((checkin.getCorrectCount() == null ? 0 : checkin.getCorrectCount()) + 1);
            checkin.setPointsEarned((checkin.getPointsEarned() == null ? 0 : checkin.getPointsEarned()) + POINTS_PER_CORRECT);
        }
        checkin.setTotalDurationSeconds((checkin.getTotalDurationSeconds() == null ? 0 : checkin.getTotalDurationSeconds()) + Math.max(durationSeconds, 0));
        dailyCheckinMapper.updateById(checkin);
    }

    private void updateUserPoints(Long userId, int durationSeconds, boolean isCorrect) {
        UserPoints userPoints = getOrCreateUserPoints(userId);

        userPoints.setTotalStudied((userPoints.getTotalStudied() == null ? 0 : userPoints.getTotalStudied()) + 1);
        userPoints.setTotalDurationSeconds((userPoints.getTotalDurationSeconds() == null ? 0 : userPoints.getTotalDurationSeconds()) + Math.max(durationSeconds, 0));
        if (isCorrect) {
            userPoints.setTotalCorrect((userPoints.getTotalCorrect() == null ? 0 : userPoints.getTotalCorrect()) + 1);
            userPoints.setTotalPoints((userPoints.getTotalPoints() == null ? 0 : userPoints.getTotalPoints()) + POINTS_PER_CORRECT);
        }

        LocalDate today = LocalDate.now();
        LocalDate lastCheckinDate = userPoints.getLastCheckinDate();
        if (lastCheckinDate == null) {
            userPoints.setStreakDays(1);
        } else if (today.equals(lastCheckinDate)) {
            // keep today's streak unchanged
        } else if (today.minusDays(1).equals(lastCheckinDate)) {
            userPoints.setStreakDays((userPoints.getStreakDays() == null ? 0 : userPoints.getStreakDays()) + 1);
        } else {
            userPoints.setStreakDays(1);
        }
        userPoints.setMaxStreakDays(Math.max(userPoints.getMaxStreakDays() == null ? 0 : userPoints.getMaxStreakDays(), userPoints.getStreakDays() == null ? 0 : userPoints.getStreakDays()));
        userPoints.setLastCheckinDate(today);

        userPointsMapper.updateById(userPoints);

        // 同步更新 Redis 排行榜
        redisRankService.updateUserPoints(userId, userPoints.getTotalPoints());
        redisRankService.updateUserStreak(userId, userPoints.getStreakDays());
    }

    private WrongWord getWrongWord(Long userId, Long wordCardId) {
        return wrongWordMapper.selectOne(new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId)
                .eq(WrongWord::getWordCardId, wordCardId)
                .last("LIMIT 1"));
    }

    private DailyCheckin getDailyCheckin(Long userId, LocalDate checkinDate) {
        return dailyCheckinMapper.selectOne(new LambdaQueryWrapper<DailyCheckin>()
                .eq(DailyCheckin::getUserId, userId)
                .eq(DailyCheckin::getCheckinDate, checkinDate)
                .last("LIMIT 1"));
    }

    private UserPoints getUserPoints(Long userId) {
        return userPointsMapper.selectOne(new LambdaQueryWrapper<UserPoints>()
                .eq(UserPoints::getUserId, userId)
                .last("LIMIT 1"));
    }

    private Integer countWrongWords(Long userId, String status) {
        LambdaQueryWrapper<WrongWord> queryWrapper = new LambdaQueryWrapper<WrongWord>()
                .eq(WrongWord::getUserId, userId);
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(WrongWord::getStatus, status);
        }
        Long count = wrongWordMapper.selectCount(queryWrapper);
        return count == null ? 0 : count.intValue();
    }

    private DailyCheckin getOrCreateDailyCheckin(Long userId, LocalDate checkinDate) {
        DailyCheckin checkin = dailyCheckinMapper.selectOne(new LambdaQueryWrapper<DailyCheckin>()
                .eq(DailyCheckin::getUserId, userId)
                .eq(DailyCheckin::getCheckinDate, checkinDate)
                .last("LIMIT 1"));
        if (checkin != null) {
            return checkin;
        }

        DailyCheckin created = new DailyCheckin();
        created.setUserId(userId);
        created.setCheckinDate(checkinDate);
        created.setStudyCount(0);
        created.setCorrectCount(0);
        created.setTotalDurationSeconds(0);
        created.setPointsEarned(0);
        try {
            dailyCheckinMapper.insert(created);
            return created;
        } catch (DuplicateKeyException ex) {
            return dailyCheckinMapper.selectOne(new LambdaQueryWrapper<DailyCheckin>()
                    .eq(DailyCheckin::getUserId, userId)
                    .eq(DailyCheckin::getCheckinDate, checkinDate)
                    .last("LIMIT 1"));
        }
    }

    private UserPoints getOrCreateUserPoints(Long userId) {
        UserPoints userPoints = userPointsMapper.selectOne(new LambdaQueryWrapper<UserPoints>()
                .eq(UserPoints::getUserId, userId)
                .last("LIMIT 1"));
        if (userPoints != null) {
            return userPoints;
        }

        UserPoints created = new UserPoints();
        created.setUserId(userId);
        created.setTotalPoints(0);
        created.setStreakDays(0);
        created.setMaxStreakDays(0);
        created.setTotalStudied(0);
        created.setTotalCorrect(0);
        created.setTotalDurationSeconds(0);
        try {
            userPointsMapper.insert(created);
            return created;
        } catch (DuplicateKeyException ex) {
            return userPointsMapper.selectOne(new LambdaQueryWrapper<UserPoints>()
                    .eq(UserPoints::getUserId, userId)
                    .last("LIMIT 1"));
        }
    }

    private String resolveCorrectAnswer(String mode, WordCard wordCard) {
        if (MODE_MEANING_TO_WORD.equals(mode)) {
            return wordCard.getWord();
        }
        return wordCard.getMeaning();
    }

    private boolean isAnswerCorrect(String answerContent, String correctAnswer) {
        String normalizedAnswer = normalizeText(answerContent);
        String normalizedCorrect = normalizeText(correctAnswer);

        if (!StringUtils.hasText(normalizedAnswer) || !StringUtils.hasText(normalizedCorrect)) {
            return false;
        }

        if (normalizedAnswer.equals(normalizedCorrect)) {
            return true;
        }

        String[] candidates = normalizedCorrect.split("[,;；、/|]");
        for (String candidate : candidates) {
            if (normalizedAnswer.equals(candidate.trim())) {
                return true;
            }
        }
        return false;
    }

    private WordCard requireOwnedWordCard(Long userId, Long wordCardId) {
        WordCard wordCard = wordCardMapper.selectOne(new LambdaQueryWrapper<WordCard>()
                .eq(WordCard::getId, wordCardId)
                .eq(WordCard::getUserId, userId)
                .last("LIMIT 1"));
        if (wordCard == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "单词卡不存在");
        }
        return wordCard;
    }

    private void validateMode(String mode) {
        if (!ALLOWED_MODES.contains(mode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "学习模式不合法");
        }
    }

    private String normalizeMode(String mode) {
        if (!StringUtils.hasText(mode)) {
            return "";
        }
        return mode.trim().toUpperCase();
    }

    private String normalizeRangeType(String rangeType) {
        if (!StringUtils.hasText(rangeType)) {
            return "";
        }
        return rangeType.trim().toUpperCase();
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String resolveMemoryStatus(int familiarityLevel, boolean isCorrect) {
        if (familiarityLevel >= MAX_FAMILIARITY) {
            return "MASTERED";
        }
        if (!isCorrect && familiarityLevel <= 1) {
            return "REVIEWING";
        }
        if (familiarityLevel == 0) {
            return "NEW";
        }
        return "LEARNING";
    }

    private int clampFamiliarity(int familiarity) {
        if (familiarity < 0) {
            return 0;
        }
        return Math.min(familiarity, MAX_FAMILIARITY);
    }

    private int nullSafe(Integer value) {
        return value == null ? 0 : value;
    }

    private int resolveCurrentStreakDays(UserPoints userPoints) {
        if (userPoints == null || userPoints.getLastCheckinDate() == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        LocalDate lastCheckinDate = userPoints.getLastCheckinDate();
        if (lastCheckinDate.isBefore(today.minusDays(1))) {
            return 0;
        }
        return nullSafe(userPoints.getStreakDays());
    }

    private double calculateRate(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return Math.round((double) numerator * 10000 / denominator) / 100D;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
