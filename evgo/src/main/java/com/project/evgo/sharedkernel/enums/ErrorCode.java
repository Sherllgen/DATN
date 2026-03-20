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

    // PDF Parsing errors
    PDF_PARSING_FAILED(3001, HttpStatus.BAD_REQUEST, "Failed to parse PDF file"),
    INVALID_INPUT(3002, HttpStatus.BAD_REQUEST, "Invalid input data"),
    FILE_TOO_LARGE(3003, HttpStatus.BAD_REQUEST, "Uploaded file is too large"),
    FILE_UPLOAD_FAILED(3004, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file"),

    // Station Owner Profile errors
    PROFILE_NOT_FOUND(4001, HttpStatus.NOT_FOUND, "Station owner profile not found"),
    INVALID_STATUS(4002, HttpStatus.BAD_REQUEST, "Invalid station owner status"),
    TOKEN_ALREADY_USED(4003, HttpStatus.BAD_REQUEST, "This activation token has already been used"),
    RESOURCE_ALREADY_EXISTS(4004, HttpStatus.BAD_REQUEST, "The resource is already in use"),

    // Account Management errors
    CANNOT_MODIFY_OWN_ACCOUNT(2010, HttpStatus.FORBIDDEN, "Cannot modify your own account"),
    ACCOUNT_ALREADY_LOCKED(2011, HttpStatus.BAD_REQUEST, "Account is already locked"),
    ACCOUNT_NOT_LOCKED(2012, HttpStatus.BAD_REQUEST, "Account is not locked"),
    ACCOUNT_ALREADY_DELETED(2013, HttpStatus.BAD_REQUEST, "Account is already deleted"),

    // Station errors (5xxx)
    STATION_NOT_FOUND(5001, HttpStatus.NOT_FOUND, "Station not found"),
    STATION_NOT_OWNED(5002, HttpStatus.FORBIDDEN, "You are not the owner of this station"),
    STATION_NAME_ALREADY_EXISTS(5003, HttpStatus.CONFLICT, "Station name already exists for this owner"),
    STATION_NOT_APPROVED(5004, HttpStatus.FORBIDDEN, "Station not yet approved by admin"),
    STATION_ALREADY_APPROVED(5005, HttpStatus.BAD_REQUEST, "Station is already approved"),
    STATION_ALREADY_SUSPENDED(5006, HttpStatus.BAD_REQUEST, "Station is already suspended"),
    STATION_INVALID_STATUS_CHANGE(5007, HttpStatus.BAD_REQUEST, "Invalid station status change"),

    // Booking errors
    BOOKING_SLOT_UNAVAILABLE(5008, HttpStatus.CONFLICT, "Booking slot is unavailable"),
    BOOKING_CANCELLATION_NOT_ALLOWED(5009, HttpStatus.BAD_REQUEST, "Cannot cancel booking less than 2 hours before start time"),

    // Station Photo errors (501x)
    STATION_PHOTO_NOT_FOUND(5010, HttpStatus.NOT_FOUND, "Station photo not found"),
    STATION_PHOTO_LIMIT_EXCEEDED(5011, HttpStatus.BAD_REQUEST, "Maximum number of photos exceeded"),

    // Price Setting errors (502x)
    PRICE_SETTING_NOT_FOUND(5020, HttpStatus.NOT_FOUND, "Price setting not found"),
    INVALID_PRICE_VALUE(5021, HttpStatus.BAD_REQUEST, "Price value must be positive"),

    // Charger errors (6xxx)
    CHARGER_NOT_FOUND(6001, HttpStatus.NOT_FOUND, "Charger not found"),
    CHARGER_NOT_OWNED(6002, HttpStatus.FORBIDDEN, "You don't own this charger's station"),

    // Port errors (7xxx)
    PORT_NOT_FOUND(7001, HttpStatus.NOT_FOUND, "Port not found"),

    // Search errors (8xxx)
    INVALID_SEARCH_QUERY(8001, HttpStatus.BAD_REQUEST, "Search query cannot be empty"),
    INVALID_COORDINATES(8002, HttpStatus.BAD_REQUEST, "Invalid GPS coordinates"),

    // Navigation errors (9xxx)
    NAVIGATION_SERVICE_UNAVAILABLE(9001, HttpStatus.SERVICE_UNAVAILABLE, "Navigation service unavailable"),
    ROUTE_CALCULATION_FAILED(9002, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to calculate route"),

    // Payment / ZaloPay errors (10xxx)
    ZALOPAY_ORDER_CREATION_FAILED(10001, HttpStatus.BAD_GATEWAY, "Failed to create ZaloPay order"),
    ZALOPAY_INVALID_CALLBACK_MAC(10002, HttpStatus.UNAUTHORIZED, "Invalid ZaloPay callback MAC signature"),
    ZALOPAY_ORDER_NOT_FOUND(10003, HttpStatus.NOT_FOUND, "ZaloPay order not found"),
    ZALOPAY_QUERY_FAILED(10004, HttpStatus.BAD_GATEWAY, "Failed to query ZaloPay order status"),
    INVOICE_NOT_FOUND(10005, HttpStatus.NOT_FOUND, "Invoice not found"),
    INVOICE_ALREADY_PAID(10006, HttpStatus.CONFLICT, "Invoice has already been paid"),

    // OCPP errors (11xxx)
    OCPP_INVALID_MESSAGE(11001, HttpStatus.BAD_REQUEST, "Invalid OCPP message format"),
    OCPP_CHARGE_POINT_NOT_CONNECTED(11002, HttpStatus.NOT_FOUND, "Charge point is not connected"),
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
