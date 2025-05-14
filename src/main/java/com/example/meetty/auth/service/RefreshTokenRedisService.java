package com.example.meetty.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "refresh_token:";

    public void saveRefreshToken(Long userId, String token, Duration ttl) {
        String key = PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public Optional<String> getRefreshTokenByUserId(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + userId));
    }

    public void deleteToken(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}
