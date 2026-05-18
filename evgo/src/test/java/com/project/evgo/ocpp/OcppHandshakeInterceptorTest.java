package com.project.evgo.ocpp;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.ocpp.internal.OcppHandshakeInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * B1: Unit tests for OcppHandshakeInterceptor — covers Basic Auth validation logic.
 */
@ExtendWith(MockitoExtension.class)
class OcppHandshakeInterceptorTest {

    @Mock
    private ChargerService chargerService;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private WebSocketHandler wsHandler;

    @InjectMocks
    private OcppHandshakeInterceptor interceptor;

    private final Map<String, Object> attributes = new HashMap<>();

    // ============================
    // Helper
    // ============================

    private void mockUri(String chargePointId) throws Exception {
        when(request.getURI()).thenReturn(new URI("ws://localhost:8080/ocpp/" + chargePointId));
    }

    private HttpHeaders headersWithBasicAuth(String chargePointId, String password) {
        String credentials = chargePointId + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        return headers;
    }

    // ============================
    // Tests
    // ============================

    @Test
    @DisplayName("B1: Should ACCEPT handshake when charger ID and password are valid")
    void beforeHandshake_ValidCredentials_ReturnsTrue() throws Exception {
        mockUri("1");
        when(request.getHeaders()).thenReturn(headersWithBasicAuth("1", "evgo123"));
        when(chargerService.validateOcppPassword(1L, "evgo123")).thenReturn(true);

        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isTrue();
        verify(chargerService).validateOcppPassword(1L, "evgo123");
    }

    @Test
    @DisplayName("B1: Should REJECT handshake when password is wrong")
    void beforeHandshake_WrongPassword_ReturnsFalse() throws Exception {
        mockUri("1");
        when(request.getHeaders()).thenReturn(headersWithBasicAuth("1", "wrongpassword"));
        when(chargerService.validateOcppPassword(1L, "wrongpassword")).thenReturn(false);

        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("B1: Should REJECT handshake when Authorization header is missing")
    void beforeHandshake_MissingAuthHeader_ReturnsFalse() throws Exception {
        mockUri("2");
        HttpHeaders emptyHeaders = new HttpHeaders();
        when(request.getHeaders()).thenReturn(emptyHeaders);

        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isFalse();
        verifyNoInteractions(chargerService);
    }

    @Test
    @DisplayName("B1: Should REJECT handshake when charge point ID is not a number")
    void beforeHandshake_InvalidChargePointId_ReturnsFalse() throws Exception {
        mockUri("not-a-number");

        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isFalse();
        verifyNoInteractions(chargerService);
    }

    @Test
    @DisplayName("B1: Should REJECT handshake when Authorization is non-Basic scheme")
    void beforeHandshake_BearerScheme_ReturnsFalse() throws Exception {
        mockUri("3");
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer sometoken");
        when(request.getHeaders()).thenReturn(headers);

        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isFalse();
        verifyNoInteractions(chargerService);
    }
}
