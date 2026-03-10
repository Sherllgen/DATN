package com.project.evgo.ocpp;

/**
 * Sealed interface representing the three OCPP 1.6J message types.
 *
 * <ul>
 * <li>{@link OcppCall} — CALL (messageTypeId = 2)</li>
 * <li>{@link OcppCallResult} — CALLRESULT (messageTypeId = 3)</li>
 * <li>{@link OcppCallError} — CALLERROR (messageTypeId = 4)</li>
 * </ul>
 */
public sealed interface OcppMessage permits OcppCall, OcppCallResult, OcppCallError {

    /**
     * The OCPP message type identifier (2, 3, or 4).
     */
    int getMessageTypeId();

    /**
     * The unique message ID correlating a CALL with its CALLRESULT or CALLERROR.
     */
    String messageId();
}
