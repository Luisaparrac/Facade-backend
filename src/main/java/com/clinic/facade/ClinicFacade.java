package com.clinic.facade;

import com.clinic.dto.request.MedicationRequest;
import com.clinic.dto.request.RegisterPatientRequest;
import com.clinic.dto.response.AllergyResponse;
import com.clinic.dto.response.AppointmentConfirmationResponse;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.dto.response.ConsultationResponse;
import com.clinic.dto.response.DoctorResponse;
import com.clinic.dto.response.ExamTypeResponse;
import com.clinic.dto.response.HistorySummaryResponse;
import com.clinic.dto.response.LabOrderResponse;
import com.clinic.dto.response.MedicalHistoryResponse;
import com.clinic.dto.response.PatientResponse;
import com.clinic.dto.response.PrescriptionResponse;
import com.clinic.dto.response.SpecialtyResponse;
import com.clinic.exception.AllergyConflictException;
import com.clinic.model.Allergy;
import com.clinic.model.Appointment;
import com.clinic.model.Doctor;
import com.clinic.model.ExamType;
import com.clinic.model.LabOrder;
import com.clinic.model.Patient;
import com.clinic.model.Prescription;
import com.clinic.model.Specialty;
import com.clinic.service.LabService;
import com.clinic.service.MedicalHistoryService;
import com.clinic.service.PatientService;
import com.clinic.service.PrescriptionService;
import com.clinic.service.ScheduleService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class ClinicFacade {

    private static final String DEFAULT_PRESCRIBER = "On-duty physician";

    private final PatientService patientService;
    private final ScheduleService scheduleService;
    private final MedicalHistoryService medicalHistoryService;
    private final PrescriptionService prescriptionService;
    private final LabService labService;
    private final Clock clock;

    public ClinicFacade(PatientService patientService, ScheduleService scheduleService,
                        MedicalHistoryService medicalHistoryService, PrescriptionService prescriptionService,
                        LabService labService, Clock clock) {
        this.patientService = patientService;
        this.scheduleService = scheduleService;
        this.medicalHistoryService = medicalHistoryService;
        this.prescriptionService = prescriptionService;
        this.labService = labService;
        this.clock = clock;
    }

    @Transactional
    public PatientResponse registerPatient(RegisterPatientRequest request) {
        Patient patient = patientService.register(request);
        medicalHistoryService.registerAllergies(patient.getId(), request.allergies());
        return toPatientResponse(patient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientProfile(Long patientId) {
        return toPatientResponse(patientService.getProfile(patientId));
    }

    public List<SpecialtyResponse> getSpecialties() {
        return Arrays.stream(Specialty.values()).map(SpecialtyResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> findDoctors(Specialty specialty, LocalDate date) {
        LocalDate day = date != null ? date : scheduleService.nextBusinessDay(LocalDate.now(clock).plusDays(1));
        return scheduleService.findDoctors(specialty).stream()
                .map(doctor -> DoctorResponse.from(doctor, day, scheduleService.findAvailableSlots(doctor, day)))
                .toList();
    }

    @Transactional
    public AppointmentConfirmationResponse scheduleAppointment(Long patientId, Specialty specialty, LocalDateTime dateTime) {
        return scheduleAppointment(patientId, specialty, dateTime, null, null);
    }

    @Transactional
    public AppointmentConfirmationResponse scheduleAppointment(Long patientId, Specialty specialty, LocalDateTime dateTime,
                                                               Long doctorId, String reason) {
        Patient patient = patientService.getProfile(patientId);
        Appointment appointment = scheduleService.book(patient.getId(), specialty, dateTime, doctorId, reason);

        List<String> reminders = new ArrayList<>(scheduleService.buildReminders(appointment));
        List<String> allergies = medicalHistoryService.getAllergySubstances(patientId);
        if (!allergies.isEmpty()) {
            reminders.add("Remind the doctor about your allergies: " + String.join(", ", allergies) + ".");
        }
        return new AppointmentConfirmationResponse(AppointmentResponse.from(appointment), reminders,
                scheduleService.reminderTime(appointment));
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long patientId, Long appointmentId) {
        patientService.getProfile(patientId);
        return AppointmentResponse.from(scheduleService.cancel(patientId, appointmentId));
    }

    @Transactional(readOnly = true)
    public MedicalHistoryResponse getFullHistory(Long patientId) {
        Patient patient = patientService.getProfile(patientId);
        LocalDateTime now = LocalDateTime.now(clock);

        List<Allergy> allergies = medicalHistoryService.getAllergies(patientId);
        List<Appointment> appointments = scheduleService.findByPatient(patientId);
        List<AppointmentResponse> upcoming = appointments.stream()
                .filter(appointment -> appointment.isUpcoming(now))
                .map(AppointmentResponse::from)
                .toList();
        List<AppointmentResponse> past = appointments.stream()
                .filter(appointment -> appointment.isPast(now))
                .sorted(Comparator.comparing(Appointment::getDateTime, Comparator.reverseOrder()))
                .map(AppointmentResponse::from)
                .toList();
        List<ConsultationResponse> consultations = medicalHistoryService.getConsultations(patientId).stream()
                .map(ConsultationResponse::from)
                .toList();
        List<PrescriptionResponse> prescriptions = prescriptionService.findByPatient(patientId).stream()
                .map(PrescriptionResponse::from)
                .toList();
        List<LabOrder> labOrders = labService.findByPatient(patientId);

        HistorySummaryResponse summary = new HistorySummaryResponse(upcoming.size(), past.size(),
                consultations.size(), prescriptions.size(), labOrders.size(),
                labOrders.stream().mapToLong(LabOrder::countOutOfRange).sum(),
                upcoming.isEmpty() ? null : upcoming.get(0).dateTime());

        return new MedicalHistoryResponse(
                PatientResponse.from(patient, allergies.stream().map(Allergy::getSubstance).toList(), now.toLocalDate()),
                allergies.stream().map(AllergyResponse::from).toList(),
                upcoming, past, consultations, prescriptions,
                labOrders.stream().map(LabOrderResponse::from).toList(),
                summary, now);
    }

    @Transactional
    public PrescriptionResponse generatePrescription(Long patientId, List<MedicationRequest> medications) {
        return generatePrescription(patientId, medications, null, null);
    }

    @Transactional
    public PrescriptionResponse generatePrescription(Long patientId, List<MedicationRequest> medications,
                                                     String diagnosis, Long doctorId) {
        patientService.getProfile(patientId);

        List<String> medicationNames = medications.stream().map(MedicationRequest::name).toList();
        List<String> conflicts = prescriptionService.findAllergyConflicts(medicationNames,
                medicalHistoryService.getAllergySubstances(patientId));
        if (!conflicts.isEmpty()) {
            throw new AllergyConflictException(conflicts);
        }

        Doctor doctor = doctorId == null ? null : scheduleService.findDoctor(doctorId);
        String doctorName = doctor == null ? DEFAULT_PRESCRIBER : doctor.getFullName();
        Prescription prescription = prescriptionService.generate(patientId, doctorName, diagnosis, medications);

        if (diagnosis != null && !diagnosis.isBlank()) {
            Specialty specialty = doctor == null ? Specialty.GENERAL_MEDICINE : doctor.getSpecialty();
            medicalHistoryService.recordConsultation(patientId, doctorName, specialty, prescription.getIssuedAt(),
                    "Consultation with prescription", diagnosis.trim(),
                    "Prescribed: " + String.join(", ", medicationNames) + ".");
        }
        return PrescriptionResponse.from(prescription);
    }

    @Transactional
    public LabOrderResponse requestExams(Long patientId, List<ExamType> exams) {
        patientService.getProfile(patientId);
        return LabOrderResponse.from(labService.requestExams(patientId, exams));
    }

    public List<ExamTypeResponse> getExamCatalog() {
        return labService.getCatalog().stream().map(ExamTypeResponse::from).toList();
    }

    private PatientResponse toPatientResponse(Patient patient) {
        return PatientResponse.from(patient, medicalHistoryService.getAllergySubstances(patient.getId()),
                LocalDate.now(clock));
    }
}
