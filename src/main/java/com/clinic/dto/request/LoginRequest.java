package com.clinic.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "is required") String documentNumber,
        @NotBlank(message = "is required") String password) {
}
