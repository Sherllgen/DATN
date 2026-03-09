package com.project.evgo.ocpp.internal;

import com.project.evgo.ocpp.OcppErrorCode;

/**
 * Runtime exception for OCPP protocol-level errors.
 * <p>
 * Thrown when an incoming message cannot be parsed or violates the OCPP 1.6J
 * message frame format.
 */
public class OcppProtocolException extends RuntimeException {

    private final OcppErrorCode ocppErrorCode;

    public OcppProtocolException(OcppErrorCode ocppErrorCode, String message) {
        super(message);
        this.ocppErrorCode = ocppErrorCode;
    }

    public OcppProtocolException(OcppErrorCode ocppErrorCode, String message, Throwable cause) {
        super(message, cause);
        this.ocppErrorCode = ocppErrorCode;
    }

    public OcppErrorCode getOcppErrorCode() {
        return ocppErrorCode;
    }
}
