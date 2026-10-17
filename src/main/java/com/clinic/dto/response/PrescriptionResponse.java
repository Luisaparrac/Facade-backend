package com.clinic.dto.response;

import com.clinic.model.Prescription;

import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionResponse(
        Long id,
        Long patientId,
        String doctorName,
        String diagnosis,
        LocalDateTime issuedAt,
        List<MedicationResponse> medications) {

    public static PrescriptionResponse from(Prescription prescription) {
        return new PrescriptionResponse(prescription.getId(), prescription.getPatientId(),
                prescription.getDoctorName(), prescription.getDiagnosis(), prescription.getIssuedAt(),
                prescription.getItems().stream().map(MedicationResponse::from).toList());
    }
}
