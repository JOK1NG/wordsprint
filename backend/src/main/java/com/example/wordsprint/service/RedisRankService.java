package com.example.wordsprint.service;

import com.example.wordsprint.vo.RankItemVO;

import java.util.List;

public interface RedisRankService {

    void updateUserPoints(Long userId, Integer points);

    void updateUserStreak(Long userId, Integer streakDays);

    Long getUserRank(String rankType, Long userId);

    Integer getUserScore(String rankType, Long userId);

    List<RankItemVO> getTopUsers(String rankType, int limit);

    void refreshRankFromDb();
}
