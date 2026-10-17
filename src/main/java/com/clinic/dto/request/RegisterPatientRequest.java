package com.clinic.dto.request;

import com.clinic.model.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record RegisterPatientRequest(
        @NotNull(message = "is required")
        DocumentType documentType,

        @NotBlank(message = "is required")
        @Pattern(regexp = "[A-Za-z0-9]{5,15}", message = "must have 5 to 15 letters or digits")
        String documentNumber,

        @NotBlank(message = "is required")
        @Size(max = 60, message = "must have at most 60 characters")
        String firstName,

        @NotBlank(message = "is required")
        @Size(max = 60, message = "must have at most 60 characters")
        String lastName,

        @NotNull(message = "is required")
        @Past(message = "must be a date in the past")
        LocalDate birthDate,

        @NotBlank(message = "is required")
        @Email(message = "must be a valid email address")
        @Size(max = 120, message = "must have at most 120 characters")
        String email,

        @Pattern(regexp = "\\+?[0-9]{7,15}", message = "must have 7 to 15 digits")
        String phone,

        @NotBlank(message = "is required")
        @Size(min = 8, max = 72, message = "must have 8 to 72 characters")
        String password,

        List<@NotBlank(message = "cannot be blank") @Size(min = 3, max = 80, message = "must have 3 to 80 characters") String> allergies) {
}
