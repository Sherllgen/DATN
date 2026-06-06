package com.project.evgo.ocpp.internal;

import com.project.evgo.charger.ChargerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * Validates OCPP WebSocket handshake requests.
 * <p>
 * Performs check before allowing the connection:
 * <ol>
 *   <li>The charger ID extracted from the URL path must exist in the database.</li>
 * </ol>
 * Rejects with HTTP 403 on any failure (no leak of whether the ID exists).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OcppHandshakeInterceptor implements HandshakeInterceptor {

    private final ChargerService chargerService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {

        URI uri = request.getURI();
        String path = uri.getPath();
        String chargePointId = path.substring(path.lastIndexOf('/') + 1);

        Long chargerId;
        try {
            chargerId = Long.parseLong(chargePointId);
        } catch (NumberFormatException e) {
            log.warn("OCPP handshake rejected: invalid charge point ID '{}'", chargePointId);
            return false;
        }
        if (chargerService.findById(chargerId).isEmpty()) {
            log.warn("OCPP handshake rejected: charger {} not found", chargerId);
            return false;
        }

        log.info("OCPP handshake accepted for charge point ID: {}", chargerId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}
