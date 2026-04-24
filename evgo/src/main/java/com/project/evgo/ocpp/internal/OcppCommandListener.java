package com.project.evgo.ocpp.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.evgo.booking.SendReserveNowCommandEvent;
import com.project.evgo.sharedkernel.events.SendRemoteStopCommandEvent;
import com.project.evgo.sharedkernel.events.SendRemoteStartCommandEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Listens for booking scheduler events and dispatches OCPP 1.6-J CALL messages
 * to the charge point via the WebSocket session manager.
 * 
 * @see <a href="https://www.openchargealliance.org/protocols/ocpp-16/">OCPP 1.6 Specification</a>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OcppCommandListener {

    private final OcppSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final PendingCommandManager pendingCommandManager;

    private static final DateTimeFormatter OCPP_DATE_FORMAT = 
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(java.time.ZoneId.of("UTC"));

    /**
     * Handles {@link SendRemoteStopCommandEvent} by sending a
     * {@code RemoteStopTransaction.req} OCPP CALL to the charge point.
     * <p>
     * OCPP 1.6 §6.12 — Payload: {@code { "transactionId": <int> }}
     */
    @EventListener
    public void onRemoteStop(SendRemoteStopCommandEvent event) {
        log.info("Received SendRemoteStopCommandEvent: sessionId={}, chargePointId={}, transactionId={}, reason={}",
                event.sessionId(), event.chargePointId(), event.transactionId(), event.reason());

        if (event.transactionId() == null) {
            log.warn("Cannot send RemoteStopTransaction: transactionId is null. " +
                    "Session may still be PREPARING. chargePointId={}, sessionId={}",
                    event.chargePointId(), event.sessionId());
            return;
        }

        WebSocketSession session = sessionManager.getSession(event.chargePointId());
        if (session == null || !session.isOpen()) {
            log.error("Cannot send RemoteStopTransaction: no active session for chargePointId={}",
                    event.chargePointId());
            return;
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("transactionId", event.transactionId());

            String messageId = UUID.randomUUID().toString();
            List<Object> callArray = List.of(2, messageId, "RemoteStopTransaction", payload);

            String jsonString = objectMapper.writeValueAsString(callArray);
            session.sendMessage(new TextMessage(jsonString));

            log.info("Sent RemoteStopTransaction.req to CP {}: messageId={}, transactionId={}, reason={}",
                    event.chargePointId(), messageId, event.transactionId(), event.reason());
        } catch (IOException e) {
            log.error("Failed to send RemoteStopTransaction to CP {}: {}",
                    event.chargePointId(), e.getMessage(), e);
        }
    }

    /**
     * Handles {@link SendReserveNowCommandEvent} by sending a
     * {@code ReserveNow.req} OCPP CALL to the charge point.
     * <p>
     * OCPP 1.6 §6.11 — Payload:
     * <pre>
     * {
     *   "connectorId":   &lt;int&gt;,
     *   "expiryDate":    "&lt;ISO-8601&gt;",
     *   "idTag":         "&lt;string max20&gt;",
     *   "reservationId": &lt;int&gt;
     * }
     * </pre>
     */
    @EventListener
    public void onReserveNow(SendReserveNowCommandEvent event) {
        log.info("Received SendReserveNowCommandEvent: chargePointId={}, connectorId={}, idTag={}, expiry={}, reservationId={}",
                event.chargePointId(), event.connectorId(), event.idTag(),
                event.expiryDate(), event.reservationId());

        WebSocketSession session = sessionManager.getSession(event.chargePointId());
        if (session == null || !session.isOpen()) {
            log.error("Cannot send ReserveNow: no active session for chargePointId={}",
                    event.chargePointId());
            return;
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("connectorId", event.connectorId());
            java.time.Instant expiryInstant = event.expiryDate()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant();
            payload.put("expiryDate", OCPP_DATE_FORMAT.format(expiryInstant));
            payload.put("idTag", event.idTag());
            payload.put("reservationId", event.reservationId());

            String messageId = UUID.randomUUID().toString();
            List<Object> callArray = List.of(2, messageId, "ReserveNow", payload);
            
            pendingCommandManager.track(messageId, "ReserveNow", event.chargePointId(), event.connectorId());

            String jsonString = objectMapper.writeValueAsString(callArray);
            session.sendMessage(new TextMessage(jsonString));

            log.info("Sent ReserveNow.req to CP {}: messageId={}, connectorId={}, idTag={}, expiry={}, reservationId={}",
                    event.chargePointId(), messageId, event.connectorId(),
                    event.idTag(), event.expiryDate(), event.reservationId());
        } catch (IOException e) {
            log.error("Failed to send ReserveNow to CP {}: {}",
                    event.chargePointId(), e.getMessage(), e);
        }
    }

    /**
     * Handles {@link com.project.evgo.sharedkernel.events.SendRemoteStartCommandEvent} from the Charging module.
     * Sends a {@code RemoteStartTransaction.req} to the charge point.
     * <p>
     * Per OCPP 1.6 §6.5:
     * <pre>{ "idTag": "&lt;string max20&gt;", "connectorId": &lt;int&gt; }</pre>
     */
    @EventListener
    public void onChargingRemoteStart(SendRemoteStartCommandEvent event) {
        log.info("Received charging.SendRemoteStartCommandEvent: sessionId={}, chargePointId={}, connectorId={}, idTag={}",
                event.sessionId(), event.chargePointId(), event.connectorId(), event.idTag());

        WebSocketSession session = sessionManager.getSession(event.chargePointId());
        if (session == null || !session.isOpen()) {
            log.error("Cannot send RemoteStartTransaction: no active session for chargePointId={}",
                    event.chargePointId());
            return;
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("idTag", event.idTag());
            if (event.connectorId() != null) {
                payload.put("connectorId", event.connectorId());
            }

            String messageId = UUID.randomUUID().toString();
            List<Object> callArray = List.of(2, messageId, "RemoteStartTransaction", payload);

            String jsonString = objectMapper.writeValueAsString(callArray);
            session.sendMessage(new TextMessage(jsonString));

            log.info("Sent RemoteStartTransaction.req to CP {}: messageId={}, connectorId={}, idTag={}",
                    event.chargePointId(), messageId, event.connectorId(), event.idTag());
        } catch (IOException e) {
            log.error("Failed to send RemoteStartTransaction to CP {}: {}",
                    event.chargePointId(), e.getMessage(), e);
        }
    }

}
