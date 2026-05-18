package com.project.evgo.ocpp.internal;

import com.project.evgo.charger.ChargerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Validates OCPP WebSocket handshake requests.
 * <p>
 * Performs two checks before allowing the connection:
 * <ol>
 *   <li>The charger ID extracted from the URL path must exist in the database.</li>
 *   <li>A valid HTTP Basic Auth header must be present with credentials {@code <chargerId>:<password>}.</li>
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

        // Step 1: Validate numeric charger ID
        Long chargerId;
        try {
            chargerId = Long.parseLong(chargePointId);
        } catch (NumberFormatException e) {
            log.warn("OCPP handshake rejected: invalid charge point ID '{}'", chargePointId);
            return false;
        }

        // Step 2: Validate Basic Auth header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            log.warn("OCPP handshake rejected: missing or non-Basic Authorization header for charger {}", chargerId);
            return false;
        }

        String rawPassword = extractPasswordFromBasicAuth(authHeader, chargerId);
        if (rawPassword == null) {
            return false;
        }

        // Step 3: Validate password against the DB
        if (!chargerService.validateOcppPassword(chargerId, rawPassword)) {
            log.warn("OCPP handshake rejected: invalid password for charger {}", chargerId);
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

    /**
     * Decodes the Base64 Basic Auth credential string and returns the password part.
     * Expects the format {@code <chargerId>:<password>}.
     * Returns {@code null} and logs a warning if the header is malformed.
     */
    private String extractPasswordFromBasicAuth(String authHeader, Long chargerId) {
        try {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String decoded = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            int colonIndex = decoded.indexOf(':');
            if (colonIndex < 0) {
                log.warn("OCPP handshake rejected: malformed Basic Auth credentials for charger {}", chargerId);
                return null;
            }
            return decoded.substring(colonIndex + 1);
        } catch (IllegalArgumentException e) {
            log.warn("OCPP handshake rejected: could not decode Basic Auth header for charger {}", chargerId);
            return null;
        }
    }
}
