package com.project.evgo.ocpp;

/**
 * OCPP 1.6 standard error codes as defined in the specification.
 * <p>
 * These are used in CALLERROR messages to indicate the type of error
 * encountered.
 */
public enum OcppErrorCode {

    NOT_IMPLEMENTED("NotImplemented"),
    NOT_SUPPORTED("NotSupported"),
    INTERNAL_ERROR("InternalError"),
    PROTOCOL_ERROR("ProtocolError"),
    SECURITY_ERROR("SecurityError"),
    FORMATION_VIOLATION("FormationViolation"),
    PROPERTY_CONSTRAINT_VIOLATION("PropertyConstraintViolation"),
    OCCURENCE_CONSTRAINT_VIOLATION("OccurenceConstraintViolation"),
    TYPE_CONSTRAINT_VIOLATION("TypeConstraintViolation"),
    GENERIC_ERROR("GenericError");

    private final String value;

    OcppErrorCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
