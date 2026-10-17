package com.clinic.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ConflictException extends ApiException {

    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public ConflictException(String message, List<String> details) {
        super(HttpStatus.CONFLICT, message, details);
    }
}
