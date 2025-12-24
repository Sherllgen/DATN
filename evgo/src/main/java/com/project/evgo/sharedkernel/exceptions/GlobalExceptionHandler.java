package com.project.evgo.sharedkernel.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Xử lý AppException
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Object> response = new ApiResponse<>(
                errorCode.getCode(),
                ex.getMessage(),
                null);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // Xử lý Validation lỗi (Bean Validation @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiResponse<Object> response = new ApiResponse<>(
                ErrorCode.INVALID_REQUEST.getCode(),
                errorMessage,
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Xử lý lỗi JSON deserialization (bao gồm invalid enum values)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String errorMessage = "Invalid input format";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatEx) {
            if (invalidFormatEx.getTargetType().isEnum()) {
                String[] enumValues = java.util.Arrays.stream(invalidFormatEx.getTargetType().getEnumConstants())
                        .map(Object::toString)
                        .toArray(String[]::new);
                errorMessage = String.format("Invalid value '%s' for field '%s'. Accepted values are: %s",
                        invalidFormatEx.getValue(),
                        invalidFormatEx.getPath().get(0).getFieldName(),
                        String.join(", ", enumValues));
            }
        }

        ApiResponse<Object> response = new ApiResponse<>(
                ErrorCode.INVALID_REQUEST.getCode(),
                errorMessage,
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NoHandlerFoundException ex) {
        ApiResponse<Object> response = new ApiResponse<>(
                ErrorCode.ENDPOINT_NOT_FOUND.getCode(),
                "Endpoint not found",
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {

        String message = "Method " + ex.getMethod() +
                " is not supported for this endpoint";

        ApiResponse<Object> response = new ApiResponse<>(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                message,
                null
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // Xử lý lỗi ngoài dự kiến
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ApiResponse<Object> response = new ApiResponse<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
