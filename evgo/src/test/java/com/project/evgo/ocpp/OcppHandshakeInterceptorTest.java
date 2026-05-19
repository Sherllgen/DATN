package com.project.evgo.ocpp;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.ocpp.internal.OcppHandshakeInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.project.evgo.charger.response.ChargerResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * B1: Unit tests for OcppHandshakeInterceptor — covers charger existence validation logic.
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



    // ============================
    // Tests
    // ============================

    @Test
    @DisplayName("B1: Should ACCEPT handshake when charger exists")
    void beforeHandshake_ChargerExists_ReturnsTrue() throws Exception {
        mockUri("1");
        when(chargerService.findById(1L)).thenReturn(Optional.of(mock(ChargerResponse.class)));

        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isTrue();
        verify(chargerService).findById(1L);
    }

    @Test
    @DisplayName("B1: Should REJECT handshake when charger does not exist")
    void beforeHandshake_ChargerNotFound_ReturnsFalse() throws Exception {
        mockUri("1");
        when(chargerService.findById(1L)).thenReturn(Optional.empty());

        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isFalse();
        verify(chargerService).findById(1L);
    }

    @Test
    @DisplayName("B1: Should REJECT handshake when charge point ID is not a number")
    void beforeHandshake_InvalidChargePointId_ReturnsFalse() throws Exception {
        mockUri("not-a-number");

        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertThat(result).isFalse();
        verifyNoInteractions(chargerService);
    }
}
