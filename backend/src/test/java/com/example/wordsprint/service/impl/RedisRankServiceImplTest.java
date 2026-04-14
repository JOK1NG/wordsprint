package com.example.wordsprint.service.impl;

import com.example.wordsprint.entity.User;
import com.example.wordsprint.mapper.UserMapper;
import com.example.wordsprint.mapper.UserPointsMapper;
import com.example.wordsprint.vo.RankItemVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRankServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserPointsMapper userPointsMapper;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private RedisRankServiceImpl redisRankService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void shouldIncludeAvatarWhenBuildingRankItems() {
        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("1", 120D));

        User user = new User();
        user.setId(1L);
        user.setNickname("Alice");
        user.setAvatar("https://example.com/avatar-alice.png");

        when(zSetOperations.reverseRangeWithScores("wordsprint:rank:points", 0, 19))
                .thenReturn(tuples);
        when(userMapper.selectByIds(List.of(1L))).thenReturn(List.of(user));

        List<RankItemVO> result = redisRankService.getTopUsers("points", 20);

        assertEquals(1, result.size());
        RankItemVO item = result.get(0);
        assertEquals(1, item.getRank());
        assertEquals(1L, item.getUserId());
        assertEquals("Alice", item.getNickname());
        assertEquals("https://example.com/avatar-alice.png", item.getAvatar());
        assertEquals(120, item.getScore());
        assertNotNull(item.getAvatar());
    }
}
