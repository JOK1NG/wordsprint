package com.example.wordsprint.service.impl;

import com.example.wordsprint.entity.User;
import com.example.wordsprint.entity.UserPoints;
import com.example.wordsprint.mapper.UserMapper;
import com.example.wordsprint.mapper.UserPointsMapper;
import com.example.wordsprint.service.RedisRankService;
import com.example.wordsprint.vo.RankItemVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class RedisRankServiceImpl implements RedisRankService {

    private static final String KEY_PREFIX = "wordsprint:rank:";
    private static final String RANK_POINTS = "points";
    private static final String RANK_STREAK = "streak";

    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    private final UserPointsMapper userPointsMapper;

    @PostConstruct
    public void initRankCache() {
        try {
            refreshRankFromDb();
            log.info("排行榜缓存已从数据库加载");
        } catch (Exception e) {
            log.warn("启动时加载排行榜缓存失败，将在首次答题时逐步重建: {}", e.getMessage());
        }
    }

    @Override
    public void updateUserPoints(Long userId, Integer points) {
        String key = KEY_PREFIX + RANK_POINTS;
        stringRedisTemplate.opsForZSet().add(key, userId.toString(), points);
    }

    @Override
    public void updateUserStreak(Long userId, Integer streakDays) {
        String key = KEY_PREFIX + RANK_STREAK;
        stringRedisTemplate.opsForZSet().add(key, userId.toString(), streakDays);
    }

    @Override
    public Long getUserRank(String rankType, Long userId) {
        String key = buildKey(rankType);
        Long rank = stringRedisTemplate.opsForZSet().reverseRank(key, userId.toString());
        return rank != null ? rank + 1 : null;
    }

    @Override
    public Integer getUserScore(String rankType, Long userId) {
        String key = buildKey(rankType);
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        return score != null ? score.intValue() : null;
    }

    @Override
    public List<RankItemVO> getTopUsers(String rankType, int limit) {
        String key = buildKey(rankType);
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, limit - 1);

        if (tuples == null || tuples.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> userIds = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() != null) {
                userIds.add(Long.valueOf(tuple.getValue()));
            }
        }

        Map<Long, User> userMap = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<RankItemVO> result = new ArrayList<>();
        long rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String userIdStr = tuple.getValue();
            Double score = tuple.getScore();

            if (userIdStr == null) {
                continue;
            }

            Long userId = Long.valueOf(userIdStr);
            User user = userMap.get(userId);

            RankItemVO item = new RankItemVO();
            item.setUserId(userId);
            item.setNickname(user != null ? user.getNickname() : null);
            item.setAvatar(user != null ? user.getAvatar() : null);
            item.setScore(score != null ? score.intValue() : 0);
            item.setRank((int) rank++);
            result.add(item);
        }

        return result;
    }

    @Override
    public void refreshRankFromDb() {
        List<UserPoints> userPointsList = userPointsMapper.selectList(null);
        if (userPointsList == null || userPointsList.isEmpty()) {
            return;
        }

        String pointsKey = KEY_PREFIX + RANK_POINTS;
        String streakKey = KEY_PREFIX + RANK_STREAK;

        for (UserPoints userPoints : userPointsList) {
            Long userId = userPoints.getUserId();
            if (userId == null) {
                continue;
            }

            Integer totalPoints = Objects.requireNonNullElse(userPoints.getTotalPoints(), 0);
            Integer streakDays = Objects.requireNonNullElse(userPoints.getStreakDays(), 0);

            stringRedisTemplate.opsForZSet().add(pointsKey, userId.toString(), totalPoints);
            stringRedisTemplate.opsForZSet().add(streakKey, userId.toString(), streakDays);
        }
    }

    private String buildKey(String rankType) {
        return KEY_PREFIX + rankType;
    }
}
