package com.project.evgo.sharedkernel.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // General errors
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    INVALID_REQUEST(400, HttpStatus.BAD_REQUEST, "Invalid request"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "Access denied"),
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "Resource not found"),
    ENDPOINT_NOT_FOUND(4041, HttpStatus.NOT_FOUND, "Endpoint not found"),
    METHOD_NOT_ALLOWED(4051, HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),

    // Authentication errors
    EMAIL_ALREADY_EXISTS(1001, HttpStatus.CONFLICT, "Email already exists"),
    PHONE_ALREADY_EXISTS(1002, HttpStatus.CONFLICT, "Phone number already exists"),
    INVALID_CREDENTIALS(1003, HttpStatus.UNAUTHORIZED, "Invalid email/phone or password"),
    INVALID_TOKEN(1004, HttpStatus.UNAUTHORIZED, "Invalid token"),
    TOKEN_EXPIRED(1005, HttpStatus.UNAUTHORIZED, "Token has expired"),
    ACCOUNT_NOT_VERIFIED(1006, HttpStatus.FORBIDDEN, "Account not verified"),
    ACCOUNT_DISABLED(1007, HttpStatus.FORBIDDEN, "Account is disabled"),
    ROLE_NOT_FOUND(1008, HttpStatus.INTERNAL_SERVER_ERROR, "Role not found"),
    EMAIL_OR_PHONE_REQUIRED(1009, HttpStatus.BAD_REQUEST, "Email or phone number is required"),

    // User Profile errors
    USER_NOT_FOUND(2001, HttpStatus.NOT_FOUND, "User not found"),
    PASSWORD_MISMATCH(2002, HttpStatus.BAD_REQUEST, "Password does not match"),
    CURRENT_PASSWORD_INCORRECT(2003, HttpStatus.BAD_REQUEST, "Current password is incorrect"),
    AVATAR_UPLOAD_FAILED(2004, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload avatar"),
    ;

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
