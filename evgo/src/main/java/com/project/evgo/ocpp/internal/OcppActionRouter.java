package com.project.evgo.ocpp.internal;

import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallError;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.OcppErrorCode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Routes incoming OCPP CALL messages to the appropriate
 * {@link OcppActionHandler}.
 * <p>
 * Collects all registered handlers via Spring injection and indexes them by
 * action name.
 * Returns a NOT_IMPLEMENTED error for unknown actions.
 */
@Component
@Slf4j
public class OcppActionRouter {

    private final Map<String, OcppActionHandler> handlers;

    public OcppActionRouter(List<OcppActionHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(OcppActionHandler::getAction, Function.identity()));
        log.info("Registered OCPP action handlers: {}", handlers.keySet());
    }

    /**
     * Route a CALL to the appropriate handler.
     *
     * @param chargePointId the charge point identity
     * @param call          the incoming CALL message
     * @return the response (CallResult or null if unhandled)
     */
    public Optional<OcppCallResult> route(String chargePointId, OcppCall call) {
        OcppActionHandler handler = handlers.get(call.action());

        if (handler == null) {
            log.warn("No handler registered for action '{}' from {}", call.action(), chargePointId);
            return Optional.empty();
        }

        log.debug("Routing action '{}' from {} to {}", call.action(), chargePointId,
                handler.getClass().getSimpleName());
        return Optional.of(handler.handle(chargePointId, call));
    }

    /**
     * Create a NOT_IMPLEMENTED error response for unsupported actions.
     */
    public OcppCallError createNotImplementedError(OcppCall call) {
        return new OcppCallError(
                call.messageId(),
                OcppErrorCode.NOT_IMPLEMENTED.getValue(),
                "Action '" + call.action() + "' is not supported",
                JsonNodeFactory.instance.objectNode());
    }
}
