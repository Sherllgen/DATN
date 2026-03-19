package com.project.evgo.ocpp.internal;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks outgoing OCPP commands so their CALLRESULTs can be processed with context.
 */
@Component
public class PendingCommandManager {
    
    private final Map<String, PendingCommand> pendingCommands = new ConcurrentHashMap<>();

    public void track(String messageId, String action, String chargePointId, Integer connectorId) {
        pendingCommands.put(messageId, new PendingCommand(action, chargePointId, connectorId));
    }

    public PendingCommand pop(String messageId) {
        return pendingCommands.remove(messageId);
    }
    
    public record PendingCommand(String action, String chargePointId, Integer connectorId) {}
}
