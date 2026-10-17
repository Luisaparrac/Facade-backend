package com.clinic.dto.response;

import com.clinic.model.DocumentType;
import com.clinic.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PatientResponse(
        Long id,
        DocumentType documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String fullName,
        LocalDate birthDate,
        int age,
        String email,
        String phone,
        List<String> allergies,
        LocalDateTime createdAt) {

    public static PatientResponse from(Patient patient, List<String> allergies, LocalDate today) {
        return new PatientResponse(patient.getId(), patient.getDocumentType(), patient.getDocumentNumber(),
                patient.getFirstName(), patient.getLastName(), patient.getFullName(), patient.getBirthDate(),
                patient.getAge(today), patient.getEmail(), patient.getPhone(), allergies, patient.getCreatedAt());
    }
}
