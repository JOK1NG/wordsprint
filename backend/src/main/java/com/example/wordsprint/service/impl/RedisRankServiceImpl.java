package com.example.wordsprint.service.impl;

import com.example.wordsprint.entity.User;
import com.example.wordsprint.entity.UserPoints;
import com.example.wordsprint.mapper.UserMapper;
import com.example.wordsprint.mapper.UserPointsMapper;
import com.example.wordsprint.service.RedisRankService;
import com.example.wordsprint.vo.RankItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisRankServiceImpl implements RedisRankService {

    private static final String KEY_PREFIX = "wordsprint:rank:";
    private static final String RANK_POINTS = "points";
    private static final String RANK_STREAK = "streak";

    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    private final UserPointsMapper userPointsMapper;

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

        List<RankItemVO> result = new ArrayList<>();
        long rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String userIdStr = tuple.getValue();
            Double score = tuple.getScore();

            if (userIdStr == null) {
                continue;
            }

            Long userId = Long.valueOf(userIdStr);
            User user = userMapper.selectById(userId);

            RankItemVO item = new RankItemVO();
            item.setUserId(userId);
            item.setNickname(user != null ? user.getNickname() : null);
            item.setScore(score.intValue());
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
