package com.project.evgo.ocpp.internal;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallError;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.OcppMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Optional;

/**
 * WebSocket handler for OCPP 1.6J connections.
 * <p>
 * Accepts connections at {@code /ocpp/{chargePointId}}, tracks sessions,
 * parses incoming OCPP messages, and routes them to action handlers.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OcppWebSocketHandler extends TextWebSocketHandler {

    private final OcppSessionManager sessionManager;
    private final OcppMessageParser messageParser;
    private final OcppActionRouter actionRouter;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String chargePointId = extractChargePointId(session);
        sessionManager.registerSession(chargePointId, session);
        log.info("OCPP connection established: chargePointId={}", chargePointId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String chargePointId = extractChargePointId(session);
        String rawPayload = message.getPayload();
        log.debug("Received OCPP message from {}: {}", chargePointId, rawPayload);

        try {
            OcppMessage ocppMessage = messageParser.parse(rawPayload);

            if (ocppMessage instanceof OcppCall call) {
                log.info("Received CALL from {}: action={}, messageId={}", chargePointId, call.action(),
                        call.messageId());

                // Route to registered action handler
                Optional<OcppCallResult> result = actionRouter.route(chargePointId, call);

                if (result.isPresent()) {
                    String responseJson = messageParser.serialize(result.get());
                    session.sendMessage(new TextMessage(responseJson));
                    log.debug("Sent CALLRESULT to {}: {}", chargePointId, responseJson);
                } else {
                    // No handler registered — send NOT_IMPLEMENTED error
                    OcppCallError error = actionRouter.createNotImplementedError(call);
                    String errorJson = messageParser.serialize(error);
                    session.sendMessage(new TextMessage(errorJson));
                    log.warn("No handler for action '{}' from {}", call.action(), chargePointId);
                }
            } else {
                log.info("Received non-CALL message from {}: type={}", chargePointId,
                        ocppMessage.getMessageTypeId());
            }
        } catch (OcppProtocolException exception) {
            log.warn("Invalid OCPP message from {}: {}", chargePointId, exception.getMessage());
            OcppCallError errorResponse = new OcppCallError(
                    "",
                    exception.getOcppErrorCode().getValue(),
                    exception.getMessage(),
                    JsonNodeFactory.instance.objectNode());
            String errorJson = messageParser.serialize(errorResponse);
            session.sendMessage(new TextMessage(errorJson));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String chargePointId = extractChargePointId(session);
        sessionManager.removeSession(chargePointId);
        log.info("OCPP connection closed: chargePointId={}, status={}", chargePointId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String chargePointId = extractChargePointId(session);
        log.error("Transport error for charge point {}: {}", chargePointId, exception.getMessage());
        sessionManager.removeSession(chargePointId);
    }

    /**
     * Extracts the charge point ID from the WebSocket session URI path.
     * <p>
     * Expects a URI path like {@code /ocpp/{chargePointId}}.
     *
     * @param session the WebSocket session
     * @return the charge point ID
     */
    private String extractChargePointId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            throw new IllegalStateException("WebSocket session has no URI");
        }
        String path = uri.getPath();
        // Path format: /ocpp/{chargePointId}
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
