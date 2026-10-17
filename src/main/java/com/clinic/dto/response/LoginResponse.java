package com.clinic.dto.response;

import java.time.LocalDateTime;

public record LoginResponse(String token, LocalDateTime expiresAt, PatientResponse patient) {
}
