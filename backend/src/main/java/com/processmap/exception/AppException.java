package com.processmap.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final Map<String, Object> details;

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = getStatusFromErrorCode(errorCode);
        this.details = null;
    }

    public AppException(ErrorCode errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    public AppException(ErrorCode errorCode, String message, HttpStatus httpStatus, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    private static HttpStatus getStatusFromErrorCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case LLM_TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case LAYOUT_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
            case SYMBOL_NOT_FOUND -> HttpStatus.UNPROCESSABLE_ENTITY;
            case DUPLICATE_EMAIL -> HttpStatus.CONFLICT;
            case SHARE_LINK_EXPIRED, SHARE_LINK_REVOKED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
