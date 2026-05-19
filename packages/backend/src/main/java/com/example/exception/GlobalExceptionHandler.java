package com.example.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Business logic errors mapped to the appropriate HTTP status code. */
    @ExceptionHandler(WalletException.class)
    public ResponseEntity<ErrorResponse> handleWalletException(WalletException exception) {
        HttpStatus status = resolveHttpStatus(exception.getErrorCode());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(exception.getErrorCode().getCode(), exception.getMessage()));
    }

    /** Bean Validation errors on @RequestBody fields (@Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) message = "Validation failed";

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(WalletErrorCode.INVALID_REQUEST.getCode(), message));
    }

    /** Bean Validation errors on method parameters (@Validated, @Min, @Max, etc.). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(constraintViolation -> {
                    String path = constraintViolation.getPropertyPath().toString();
                    // Strip the method name prefix (e.g. "getTransactions.size" → "size")
                    int dotIndex = path.lastIndexOf('.');
                    return (dotIndex >= 0 ? path.substring(dotIndex + 1) : path) + ": " + constraintViolation.getMessage();
                })
                .collect(Collectors.joining("; "));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(WalletErrorCode.INVALID_REQUEST.getCode(), message));
    }

    /** Access denied (403) – normally intercepted by the security filter before reaching here. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(WalletErrorCode.UNAUTHORIZED.getCode(), "Access denied"));
    }

    /** Fallback for unexpected errors — logs the full stack trace and returns a generic 500 response. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
        log.error("Unexpected error", exception);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("5000", "Internal server error"));
    }

    private HttpStatus resolveHttpStatus(WalletErrorCode code) {
        return switch (code) {
            case UNAUTHORIZED, INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case RESOURCE_NOT_FOUND, RECIPIENT_NOT_FOUND, TRANSACTION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INSUFFICIENT_FUNDS -> HttpStatus.PAYMENT_REQUIRED;
            case EMAIL_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}