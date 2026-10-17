package com.clinic.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MedicationRequest(
        @NotBlank(message = "is required") @Size(max = 100, message = "must have at most 100 characters") String name,
        @NotBlank(message = "is required") @Size(max = 60, message = "must have at most 60 characters") String dose,
        @NotBlank(message = "is required") @Size(max = 60, message = "must have at most 60 characters") String frequency,
        @NotNull(message = "is required") @Min(value = 1, message = "must be at least 1 day") @Max(value = 365, message = "must be at most 365 days") Integer durationDays,
        @Size(max = 200, message = "must have at most 200 characters") String instructions) {
}
