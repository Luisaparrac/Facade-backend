package com.clinic.dto.response;

import com.clinic.model.Consultation;
import com.clinic.model.Specialty;

import java.time.LocalDateTime;

public record ConsultationResponse(
        Long id,
        String doctorName,
        Specialty specialty,
        String specialtyName,
        LocalDateTime date,
        String reason,
        String diagnosis,
        String notes) {

    public static ConsultationResponse from(Consultation consultation) {
        return new ConsultationResponse(consultation.getId(), consultation.getDoctorName(),
                consultation.getSpecialty(), consultation.getSpecialty().getDisplayName(),
                consultation.getConsultationDate(), consultation.getReason(), consultation.getDiagnosis(),
                consultation.getNotes());
    }
}
