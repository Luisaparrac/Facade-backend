package com.clinic.service;

import com.clinic.dto.request.MedicationRequest;
import com.clinic.exception.BadRequestException;
import com.clinic.model.Prescription;
import com.clinic.model.PrescriptionItem;
import com.clinic.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class PrescriptionService {

    private static final Map<String, Set<String>> DRUG_FAMILIES = Map.of(
            "penicillins", Set.of("penicillin", "penicilina", "amoxicillin", "amoxicilina", "ampicillin",
                    "ampicilina", "dicloxacillin", "dicloxacilina"),
            "cephalosporins", Set.of("cephalosporin", "cefalosporina", "cephalexin", "cefalexina",
                    "ceftriaxone", "ceftriaxona"),
            "nsaids", Set.of("nsaid", "nsaids", "aine", "aines", "ibuprofen", "ibuprofeno", "naproxen",
                    "naproxeno", "aspirin", "aspirina", "diclofenac", "diclofenaco"),
            "sulfonamides", Set.of("sulfa", "sulfonamide", "sulfonamida", "sulfamethoxazole", "sulfametoxazol"));

    private final PrescriptionRepository prescriptionRepository;
    private final Clock clock;

    public PrescriptionService(PrescriptionRepository prescriptionRepository, Clock clock) {
        this.prescriptionRepository = prescriptionRepository;
        this.clock = clock;
    }

    public List<String> findAllergyConflicts(List<String> medications, List<String> allergies) {
        List<String> conflicts = new ArrayList<>();
        for (String medication : medications) {
            String normalizedMedication = normalize(medication);
            allergies.stream()
                    .filter(allergy -> relatedTerms(normalize(allergy)).stream()
                            .anyMatch(term -> containsWord(normalizedMedication, term)))
                    .findFirst()
                    .ifPresent(allergy -> conflicts.add(medication + " cannot be prescribed: the patient is allergic to " + allergy + "."));
        }
        return conflicts;
    }

    @Transactional
    public Prescription generate(Long patientId, String doctorName, String diagnosis, List<MedicationRequest> medications) {
        return generate(patientId, doctorName, diagnosis, medications, LocalDateTime.now(clock));
    }

    @Transactional
    public Prescription generate(Long patientId, String doctorName, String diagnosis,
                                 List<MedicationRequest> medications, LocalDateTime issuedAt) {
        rejectDuplicates(medications);
        Prescription prescription = new Prescription(patientId, doctorName, blankToNull(diagnosis), issuedAt);
        for (MedicationRequest medication : medications) {
            prescription.addItem(new PrescriptionItem(medication.name().trim(), medication.dose().trim(),
                    medication.frequency().trim(), medication.durationDays(), blankToNull(medication.instructions())));
        }
        return prescriptionRepository.save(prescription);
    }

    @Transactional(readOnly = true)
    public List<Prescription> findByPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByIssuedAtDesc(patientId);
    }

    private static Set<String> relatedTerms(String allergy) {
        Set<String> terms = new HashSet<>();
        terms.add(allergy);
        for (Set<String> family : DRUG_FAMILIES.values()) {
            boolean allergyBelongsToFamily = family.stream().anyMatch(member -> containsWord(allergy, member));
            if (allergyBelongsToFamily) {
                terms.addAll(family);
            }
        }
        return terms;
    }

    private static boolean containsWord(String text, String word) {
        return Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
    }

    private static void rejectDuplicates(List<MedicationRequest> medications) {
        Set<String> seen = new HashSet<>();
        for (MedicationRequest medication : medications) {
            if (!seen.add(normalize(medication.name()))) {
                throw new BadRequestException(medication.name().trim() + " is listed more than once.");
            }
        }
    }

    static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
