package com.project.evgo.sharedkernel.exceptions;

import com.project.evgo.sharedkernel.enums.ErrorCode;

public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage); // message custom
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
