package com.clinic.service;

import com.clinic.model.Allergy;
import com.clinic.model.Consultation;
import com.clinic.model.Specialty;
import com.clinic.repository.AllergyRepository;
import com.clinic.repository.ConsultationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class MedicalHistoryService {

    private final ConsultationRepository consultationRepository;
    private final AllergyRepository allergyRepository;

    public MedicalHistoryService(ConsultationRepository consultationRepository, AllergyRepository allergyRepository) {
        this.consultationRepository = consultationRepository;
        this.allergyRepository = allergyRepository;
    }

    @Transactional
    public void registerAllergies(Long patientId, List<String> substances) {
        if (substances == null) {
            return;
        }
        Set<String> seen = new HashSet<>();
        substances.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(substance -> !substance.isEmpty())
                .filter(substance -> seen.add(substance.toLowerCase(Locale.ROOT)))
                .forEach(substance -> addAllergy(patientId, substance, null));
    }

    @Transactional
    public void addAllergy(Long patientId, String substance, String reaction) {
        if (!allergyRepository.existsByPatientIdAndSubstanceIgnoreCase(patientId, substance)) {
            allergyRepository.save(new Allergy(patientId, substance, reaction));
        }
    }

    @Transactional(readOnly = true)
    public List<Allergy> getAllergies(Long patientId) {
        return allergyRepository.findByPatientIdOrderBySubstanceAsc(patientId);
    }

    @Transactional(readOnly = true)
    public List<String> getAllergySubstances(Long patientId) {
        return getAllergies(patientId).stream().map(Allergy::getSubstance).toList();
    }

    @Transactional
    public Consultation recordConsultation(Long patientId, String doctorName, Specialty specialty, LocalDateTime date,
                                           String reason, String diagnosis, String notes) {
        return consultationRepository.save(new Consultation(patientId, doctorName, specialty, date, reason, diagnosis, notes));
    }

    @Transactional(readOnly = true)
    public List<Consultation> getConsultations(Long patientId) {
        return consultationRepository.findByPatientIdOrderByConsultationDateDesc(patientId);
    }
}
