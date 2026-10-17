package com.clinic.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrescriptionServiceTest {

    private final PrescriptionService service = new PrescriptionService(null, Clock.systemUTC());

    @Test
    void detectsDirectMatchIgnoringCaseAndAccents() {
        List<String> conflicts = service.findAllergyConflicts(List.of("Ibuprofen 400 mg"), List.of("IBUPROFÉN"));
        assertEquals(1, conflicts.size());
    }

    @Test
    void blocksTheWholeDrugFamilyInEnglishOrSpanish() {
        assertEquals(1, service.findAllergyConflicts(List.of("Amoxicilina"), List.of("Penicillin")).size());
        assertEquals(1, service.findAllergyConflicts(List.of("Naproxen"), List.of("Ibuprofeno")).size());
    }

    @Test
    void doesNotMatchPartsOfOtherWords() {
        assertTrue(service.findAllergyConflicts(List.of("Ferrous sulfate"), List.of("Sulfa drugs")).isEmpty());
    }

    @Test
    void reportsOnlyTheConflictingMedications() {
        List<String> conflicts = service.findAllergyConflicts(List.of("Acetaminophen", "Amoxicillin"), List.of("Penicillin"));
        assertEquals(1, conflicts.size());
        assertTrue(conflicts.get(0).startsWith("Amoxicillin"));
    }
}
