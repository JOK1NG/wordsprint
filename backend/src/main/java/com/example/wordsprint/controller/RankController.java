package com.example.wordsprint.controller;

import com.example.wordsprint.common.Result;
import com.example.wordsprint.service.RedisRankService;
import com.example.wordsprint.security.CurrentUserProvider;
import com.example.wordsprint.vo.RankItemVO;
import com.example.wordsprint.vo.RankResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rank")
public class RankController {

    private final RedisRankService redisRankService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/points")
    public Result<RankResponse> getPointsRank(@RequestParam(defaultValue = "20") Integer limit) {
        if (limit > 100) {
            limit = 100;
        }

        Long userId = currentUserProvider.getCurrentUserId();
        String rankType = "points";

        Long myRank = redisRankService.getUserRank(rankType, userId);
        Integer myScore = redisRankService.getUserScore(rankType, userId);
        List<RankItemVO> list = redisRankService.getTopUsers(rankType, limit);

        RankResponse response = new RankResponse();
        response.setMyRank(myRank != null ? myRank.intValue() : null);
        response.setMyScore(myScore);
        response.setList(list);

        return Result.success(response);
    }

    @GetMapping("/streak")
    public Result<RankResponse> getStreakRank(@RequestParam(defaultValue = "20") Integer limit) {
        if (limit > 100) {
            limit = 100;
        }

        Long userId = currentUserProvider.getCurrentUserId();
        String rankType = "streak";

        Long myRank = redisRankService.getUserRank(rankType, userId);
        Integer myScore = redisRankService.getUserScore(rankType, userId);
        List<RankItemVO> list = redisRankService.getTopUsers(rankType, limit);

        RankResponse response = new RankResponse();
        response.setMyRank(myRank != null ? myRank.intValue() : null);
        response.setMyScore(myScore);
        response.setList(list);

        return Result.success(response);
    }
}
