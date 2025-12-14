package com.project.evgo.sharedkernel.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // General errors
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    INVALID_REQUEST(400, HttpStatus.BAD_REQUEST, "Invalid request"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "Access denied"),
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "Resource not found"),

    // Authentication errors
    EMAIL_ALREADY_EXISTS(1001, HttpStatus.CONFLICT, "Email already exists"),
    PHONE_ALREADY_EXISTS(1002, HttpStatus.CONFLICT, "Phone number already exists"),
    INVALID_CREDENTIALS(1003, HttpStatus.UNAUTHORIZED, "Invalid email/phone or password"),
    INVALID_TOKEN(1004, HttpStatus.UNAUTHORIZED, "Invalid token"),
    TOKEN_EXPIRED(1005, HttpStatus.UNAUTHORIZED, "Token has expired"),
    ACCOUNT_NOT_VERIFIED(1006, HttpStatus.FORBIDDEN, "Account not verified"),
    ACCOUNT_DISABLED(1007, HttpStatus.FORBIDDEN, "Account is disabled"),
    ROLE_NOT_FOUND(1008, HttpStatus.INTERNAL_SERVER_ERROR, "Role not found"),
    EMAIL_OR_PHONE_REQUIRED(1009, HttpStatus.BAD_REQUEST, "Email or phone number is required");

    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
