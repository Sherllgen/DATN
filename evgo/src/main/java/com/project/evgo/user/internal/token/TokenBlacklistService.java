package com.project.evgo.user.internal.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for blacklisting access tokens on logout.
 * Tokens are stored in Redis with TTL matching remaining token lifetime.
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Add a token to the blacklist.
     *
     * @param token    The JWT token to blacklist
     * @param expiryMs Time until the token would naturally expire (in milliseconds)
     */
    public void blacklist(String token, long expiryMs) {
        if (expiryMs > 0) {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "blacklisted", expiryMs, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Check if a token is blacklisted.
     *
     * @param token The JWT token to check
     * @return true if the token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
