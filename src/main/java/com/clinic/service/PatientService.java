package com.clinic.service;

import com.clinic.dto.request.RegisterPatientRequest;
import com.clinic.exception.ConflictException;
import com.clinic.exception.NotFoundException;
import com.clinic.exception.UnauthorizedException;
import com.clinic.model.Patient;
import com.clinic.repository.PatientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public PatientService(PatientRepository patientRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public Patient register(RegisterPatientRequest request) {
        String documentNumber = request.documentNumber().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (patientRepository.existsByDocumentNumber(documentNumber)) {
            throw new ConflictException("A patient with document number " + documentNumber + " is already registered.");
        }
        if (patientRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("The email " + email + " is already registered.");
        }

        Patient patient = new Patient(request.documentType(), documentNumber, request.firstName().trim(),
                request.lastName().trim(), request.birthDate(), email, blankToNull(request.phone()),
                passwordEncoder.encode(request.password()), LocalDateTime.now(clock));
        return patientRepository.save(patient);
    }

    @Transactional(readOnly = true)
    public Patient getProfile(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient " + patientId + " was not found."));
    }

    @Transactional(readOnly = true)
    public Patient authenticate(String documentNumber, String password) {
        return patientRepository.findByDocumentNumber(documentNumber.trim())
                .filter(patient -> passwordEncoder.matches(password, patient.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid document number or password."));
    }

    @Transactional(readOnly = true)
    public boolean existsByDocument(String documentNumber) {
        return patientRepository.existsByDocumentNumber(documentNumber);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
