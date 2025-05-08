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

    public void saveRefreshToken(String email, String token, Duration ttl) {
        String key = PREFIX + email;
        redisTemplate.opsForValue().set(key, token, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public Optional<String> getRefreshTokenByEmail(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + email));
    }

    public void deleteToken(String email) {
        redisTemplate.delete(PREFIX + email);
    }
}
