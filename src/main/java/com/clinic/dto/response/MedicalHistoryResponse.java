package com.clinic.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MedicalHistoryResponse(
        PatientResponse patient,
        List<AllergyResponse> allergies,
        List<AppointmentResponse> upcomingAppointments,
        List<AppointmentResponse> pastAppointments,
        List<ConsultationResponse> consultations,
        List<PrescriptionResponse> prescriptions,
        List<LabOrderResponse> labOrders,
        HistorySummaryResponse summary,
        LocalDateTime generatedAt) {
}
