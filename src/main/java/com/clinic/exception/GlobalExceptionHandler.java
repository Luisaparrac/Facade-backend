package com.clinic.exception;

import com.clinic.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getMessage(), request, Map.of(), ex.getDetails());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Some fields are invalid.", request, fieldErrors, List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "The request body is malformed or has an invalid value. Use ISO dates (2026-10-20T09:00:00) and uppercase codes (CARDIOLOGY).",
                request, Map.of(), List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'.";
        Class<?> type = ex.getRequiredType();
        if (type != null && type.isEnum()) {
            String allowed = Arrays.stream(type.getEnumConstants()).map(String::valueOf).collect(Collectors.joining(", "));
            message += " Allowed values: " + allowed + ".";
        }
        return build(HttpStatus.BAD_REQUEST, message, request, Map.of(), List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Missing required parameter '" + ex.getParameterName() + "'.",
                request, Map.of(), List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Endpoint not found.", request, Map.of(), List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method " + ex.getMethod() + " is not supported for this endpoint.",
                request, Map.of(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse springError) {
            HttpStatus status = HttpStatus.valueOf(springError.getStatusCode().value());
            String detail = springError.getBody().getDetail();
            return build(status, detail != null ? detail : status.getReasonPhrase(), request, Map.of(), List.of());
        }
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.",
                request, Map.of(), List.of());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest request,
                                                   Map<String, String> fieldErrors, List<String> details) {
        ApiErrorResponse body = new ApiErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI(), fieldErrors, details);
        return ResponseEntity.status(status).body(body);
    }
}
