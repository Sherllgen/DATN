package com.project.evgo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * B6: Per-user API rate limiting filter using Bucket4j (in-memory).
 * <p>
 * Applies a limit of <strong>10 requests per 60 seconds</strong> per authenticated user
 * to the following high-risk endpoints:
 * <ul>
 *   <li>{@code POST /api/v1/bookings/check-availability}</li>
 *   <li>{@code POST /api/v1/charging/start}</li>
 * </ul>
 * Unauthenticated requests to these paths are rejected immediately with HTTP 401,
 * since these endpoints require a logged-in user anyway.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int RATE_LIMIT_CAPACITY = 10;
    private static final long RATE_LIMIT_REFILL_SECONDS = 60L;

    private static final Set<String> RATE_LIMITED_PATHS = Set.of(
            "/api/v1/bookings/check-availability",
            "/api/v1/charging/start"
    );

    private final ObjectMapper objectMapper;

    /**
     * Per-user bucket registry.
     * Key: authenticated principal name (userId string).
     */
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Only apply rate limiting to targeted POST endpoints
        if (!"POST".equalsIgnoreCase(method) || !RATE_LIMITED_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Security layer will reject with 401; don't rate limit anonymous requests here
            filterChain.doFilter(request, response);
            return;
        }

        String userId = auth.getName();
        Bucket bucket = buckets.computeIfAbsent(userId, id -> buildBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for user {} on path {}", userId, path);
            sendRateLimitResponse(response, path);
        }
    }

    /**
     * Builds a new Bucket with 10 tokens refilled every 60 seconds (greedy).
     */
    private Bucket buildBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(RATE_LIMIT_CAPACITY)
                .refillGreedy(RATE_LIMIT_CAPACITY, Duration.ofSeconds(RATE_LIMIT_REFILL_SECONDS))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private void sendRateLimitResponse(HttpServletResponse response, String path) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of(
                "code", 429,
                "message", "Too many requests. Please wait a moment before trying again.",
                "path", path
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
