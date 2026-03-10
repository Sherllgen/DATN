package com.project.evgo.ocpp.internal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe manager for tracking active OCPP WebSocket sessions.
 * <p>
 * Maps charge point IDs to their {@link WebSocketSession} instances using a
 * {@link ConcurrentHashMap} for thread safety.
 */
@Component
@Slf4j
public class OcppSessionManager {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * Registers a WebSocket session for the given charge point ID.
     *
     * @param chargePointId the unique identifier of the charge point
     * @param session       the WebSocket session to register
     */
    public void registerSession(String chargePointId, WebSocketSession session) {
        WebSocketSession previousSession = sessions.put(chargePointId, session);
        if (previousSession != null) {
            log.warn("Replaced existing session for charge point: {}", chargePointId);
        }
        log.info("Registered session for charge point: {} (total: {})", chargePointId, sessions.size());
    }

    /**
     * Removes the session for the given charge point ID.
     *
     * @param chargePointId the unique identifier of the charge point
     */
    public void removeSession(String chargePointId) {
        WebSocketSession removed = sessions.remove(chargePointId);
        if (removed != null) {
            log.info("Removed session for charge point: {} (total: {})", chargePointId, sessions.size());
        }
    }

    /**
     * Returns the active session for the given charge point ID, or {@code null} if
     * not connected.
     *
     * @param chargePointId the unique identifier of the charge point
     * @return the WebSocket session, or null
     */
    public WebSocketSession getSession(String chargePointId) {
        return sessions.get(chargePointId);
    }

    /**
     * Returns an unmodifiable set of all currently connected charge point IDs.
     *
     * @return set of charge point IDs
     */
    public Set<String> getAllChargePointIds() {
        return Collections.unmodifiableSet(sessions.keySet());
    }
}
