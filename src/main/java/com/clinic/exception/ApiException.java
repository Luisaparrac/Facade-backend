package com.clinic.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public abstract class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final transient List<String> details;

    protected ApiException(HttpStatus status, String message) {
        this(status, message, List.of());
    }

    protected ApiException(HttpStatus status, String message, List<String> details) {
        super(message);
        this.status = status;
        this.details = List.copyOf(details);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getDetails() {
        return details;
    }
}
