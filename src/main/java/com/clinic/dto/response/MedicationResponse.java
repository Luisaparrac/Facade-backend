package com.clinic.dto.response;

import com.clinic.model.PrescriptionItem;

public record MedicationResponse(String medication, String dose, String frequency, int durationDays, String instructions) {

    public static MedicationResponse from(PrescriptionItem item) {
        return new MedicationResponse(item.getMedication(), item.getDose(), item.getFrequency(),
                item.getDurationDays(), item.getInstructions());
    }
}
