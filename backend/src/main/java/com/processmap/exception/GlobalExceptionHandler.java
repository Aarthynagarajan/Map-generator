package com.processmap.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        logStructuredException(ex, request, "WARN");
        ErrorResponse response = new ErrorResponse(
            ex.getErrorCode().name(),
            ex.getMessage(),
            ex.getDetails(),
            OffsetDateTime.now()
        );
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logStructuredException(ex, request, "WARN");
        Map<String, Object> details = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponse response = new ErrorResponse(
            ErrorCode.VALIDATION_FAILED.name(),
            "Validation failed for one or more fields",
            details,
            OffsetDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        logStructuredException(ex, request, "ERROR");
        ErrorResponse response = new ErrorResponse(
            ErrorCode.INTERNAL_ERROR.name(),
            "An unexpected internal server error occurred",
            null,
            OffsetDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logStructuredException(Exception ex, HttpServletRequest request, String level) {
        String uri = request != null ? request.getRequestURI() : "unknown";
        String method = request != null ? request.getMethod() : "unknown";
        UUID userId = getAuthenticatedUserId();
        Throwable rootCause = getRootCause(ex);
        String exceptionType = ex.getClass().getName();
        String message = ex.getMessage();

        String logOutput = String.format(
            "\nRequest Path: %s\nHTTP Method: %s\nAuthenticated User: %s\nException: %s\nRoot Cause: %s\nMessage: %s",
            uri,
            method,
            userId != null ? userId.toString() : "Anonymous",
            exceptionType,
            rootCause != null ? rootCause.getClass().getName() + ": " + rootCause.getMessage() : "None",
            message
        );

        if ("ERROR".equalsIgnoreCase(level)) {
            log.error("Exception diagnostics: {}", logOutput, ex);
        } else {
            log.warn("Exception diagnostics: {}", logOutput);
        }
    }

    private UUID getAuthenticatedUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UUID) {
                return (UUID) auth.getPrincipal();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
