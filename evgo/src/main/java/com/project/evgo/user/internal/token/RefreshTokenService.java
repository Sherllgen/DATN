package com.project.evgo.user.internal.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing refresh tokens in Redis.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";
    private static final String USER_TOKENS_PREFIX = "user:tokens:";

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Store a refresh token for a user.
     *
     * @param userId       User ID
     * @param refreshToken The refresh token
     * @param expiryMs     Time until expiry (in milliseconds)
     */
    public void store(Long userId, String refreshToken, long expiryMs) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userTokensKey = USER_TOKENS_PREFIX + userId;

        // Store token -> userId mapping
        redisTemplate.opsForValue().set(tokenKey, String.valueOf(userId), expiryMs, TimeUnit.MILLISECONDS);

        // Add token to user's token set (for logout all sessions)
        redisTemplate.opsForSet().add(userTokensKey, refreshToken);
        redisTemplate.expire(userTokensKey, expiryMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Validate a refresh token and return associated user ID.
     *
     * @param refreshToken The refresh token to validate
     * @return User ID if valid, null otherwise
     */
    public Long validate(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(tokenKey);
        return userId != null ? Long.parseLong(userId) : null;
    }

    /**
     * Delete a specific refresh token.
     *
     * @param refreshToken The refresh token to delete
     */
    public void delete(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(tokenKey);

        redisTemplate.delete(tokenKey);

        // Remove from user's token set
        if (userId != null) {
            String userTokensKey = USER_TOKENS_PREFIX + userId;
            redisTemplate.opsForSet().remove(userTokensKey, refreshToken);
        }
    }

    /**
     * Delete all refresh tokens for a user (logout all sessions).
     *
     * @param userId User ID
     */
    public void deleteAllForUser(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);

        if (tokens != null) {
            for (String token : tokens) {
                redisTemplate.delete(REFRESH_TOKEN_PREFIX + token);
            }
        }

        redisTemplate.delete(userTokensKey);
    }
}
