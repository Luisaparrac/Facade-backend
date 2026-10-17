package com.clinic.controller;

import com.clinic.dto.request.AppointmentRequest;
import com.clinic.dto.request.LabRequest;
import com.clinic.dto.request.PrescriptionRequest;
import com.clinic.dto.request.RegisterPatientRequest;
import com.clinic.dto.response.AppointmentConfirmationResponse;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.dto.response.DoctorResponse;
import com.clinic.dto.response.ExamTypeResponse;
import com.clinic.dto.response.LabOrderResponse;
import com.clinic.dto.response.MedicalHistoryResponse;
import com.clinic.dto.response.PatientResponse;
import com.clinic.dto.response.PrescriptionResponse;
import com.clinic.dto.response.SpecialtyResponse;
import com.clinic.facade.ClinicFacade;
import com.clinic.model.Specialty;
import com.clinic.security.AuthenticatedPatient;
import com.clinic.security.PublicEndpoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/clinic")
public class ClinicController {

    private final ClinicFacade clinicFacade;

    public ClinicController(ClinicFacade clinicFacade) {
        this.clinicFacade = clinicFacade;
    }

    @PublicEndpoint
    @PostMapping("/patient")
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody RegisterPatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clinicFacade.registerPatient(request));
    }

    @GetMapping("/patient/{patientId}")
    public PatientResponse getPatient(@PathVariable("patientId") Long patientId, HttpServletRequest http) {
        AuthenticatedPatient.requireSelf(http, patientId);
        return clinicFacade.getPatientProfile(patientId);
    }

    @PublicEndpoint
    @GetMapping("/specialties")
    public List<SpecialtyResponse> getSpecialties() {
        return clinicFacade.getSpecialties();
    }

    @PublicEndpoint
    @GetMapping("/doctors")
    public List<DoctorResponse> getDoctors(
            @RequestParam(name = "specialty", required = false) Specialty specialty,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return clinicFacade.findDoctors(specialty, date);
    }

    @PostMapping("/appointment")
    public ResponseEntity<AppointmentConfirmationResponse> scheduleAppointment(
            @Valid @RequestBody AppointmentRequest request, HttpServletRequest http) {
        AuthenticatedPatient.requireSelf(http, request.patientId());
        AppointmentConfirmationResponse confirmation = clinicFacade.scheduleAppointment(request.patientId(),
                request.specialty(), request.dateTime(), request.doctorId(), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED).body(confirmation);
    }

    @DeleteMapping("/appointment/{appointmentId}")
    public AppointmentResponse cancelAppointment(@PathVariable("appointmentId") Long appointmentId, HttpServletRequest http) {
        return clinicFacade.cancelAppointment(AuthenticatedPatient.get(http), appointmentId);
    }

    @GetMapping("/history/{patientId}")
    public MedicalHistoryResponse getHistory(@PathVariable("patientId") Long patientId, HttpServletRequest http) {
        AuthenticatedPatient.requireSelf(http, patientId);
        return clinicFacade.getFullHistory(patientId);
    }

    @PostMapping("/prescription")
    public ResponseEntity<PrescriptionResponse> generatePrescription(
            @Valid @RequestBody PrescriptionRequest request, HttpServletRequest http) {
        AuthenticatedPatient.requireSelf(http, request.patientId());
        PrescriptionResponse prescription = clinicFacade.generatePrescription(request.patientId(),
                request.medications(), request.diagnosis(), request.doctorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(prescription);
    }

    @PostMapping("/lab")
    public ResponseEntity<LabOrderResponse> requestExams(@Valid @RequestBody LabRequest request, HttpServletRequest http) {
        AuthenticatedPatient.requireSelf(http, request.patientId());
        return ResponseEntity.status(HttpStatus.CREATED).body(clinicFacade.requestExams(request.patientId(), request.exams()));
    }

    @PublicEndpoint
    @GetMapping("/lab/exams")
    public List<ExamTypeResponse> getExamCatalog() {
        return clinicFacade.getExamCatalog();
    }
}
