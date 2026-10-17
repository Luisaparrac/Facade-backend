package com.clinic.dto.response;

import java.time.LocalDateTime;

public record HistorySummaryResponse(
        int upcomingAppointments,
        int pastAppointments,
        int consultations,
        int prescriptions,
        int labOrders,
        long outOfRangeResults,
        LocalDateTime nextAppointment) {
}
