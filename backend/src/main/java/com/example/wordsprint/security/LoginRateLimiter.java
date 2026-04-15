package com.example.wordsprint.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LoginRateLimiter {

    private static final String RATE_LIMIT_KEY_PREFIX = "wordsprint:rate:login:";
    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Check if the given IP is allowed to attempt login.
     * Returns true if allowed, false if rate-limited.
     */
    public boolean isAllowed(String clientIp) {
        String key = RATE_LIMIT_KEY_PREFIX + clientIp;
        String countStr = stringRedisTemplate.opsForValue().get(key);
        if (countStr != null && Integer.parseInt(countStr) >= MAX_ATTEMPTS) {
            return false;
        }
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        return true;
    }
}
