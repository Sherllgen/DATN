package com.project.evgo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * B6: Unit tests for RateLimitingFilter.
 * Verifies that the 429 response is returned after exceeding 10 requests per 60 seconds.
 */
@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter(new ObjectMapper());
    }

    private void setAuthenticatedUser(String name) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(name, null, Collections.emptyList());
        SecurityContext context = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("B6: Should pass through non-rate-limited paths")
    void nonTargetedPath_ShouldPassThrough() throws Exception {
        setAuthenticatedUser("user1");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("B6: Should return 429 after 10 requests to rate-limited endpoint")
    void rateLimitedEndpoint_ExceedsLimit_Returns429() throws Exception {
        setAuthenticatedUser("user2");
        String path = "/api/v1/bookings/check-availability";

        // First 10 requests should pass
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            filter.doFilterInternal(request, response, chain);
            assertThat(response.getStatus()).as("Request %d should pass", i + 1).isEqualTo(200);
        }

        // 11th request should be rate limited
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("B6: GET requests to rate-limited paths should NOT be rate limited")
    void getRequestToRateLimitedPath_ShouldPassThrough() throws Exception {
        setAuthenticatedUser("user3");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/bookings/check-availability");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("B6: Anonymous requests to rate-limited paths should bypass rate limiting")
    void anonymousRequest_ShouldNotBeRateLimited() throws Exception {
        SecurityContextHolder.clearContext(); // no auth set
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/charging/start");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        // Security layer will reject with 401, rate limiter should not interfere
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
