package com.team.catchup.auth.service;

import com.team.catchup.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void addToBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;

        long expiration = jwtTokenProvider.parseClaims(token).getExpiration().getTime();
        long now = System.currentTimeMillis();
        long ttl = expiration - now;

        if(ttl > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }
}
