package com.clinic.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AppointmentConfirmationResponse(
        AppointmentResponse appointment,
        List<String> reminders,
        LocalDateTime reminderAt) {
}
