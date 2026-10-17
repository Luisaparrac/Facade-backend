package com.clinic.config;

import com.clinic.dto.request.MedicationRequest;
import com.clinic.dto.request.RegisterPatientRequest;
import com.clinic.dto.response.PatientResponse;
import com.clinic.facade.ClinicFacade;
import com.clinic.model.Doctor;
import com.clinic.model.DocumentType;
import com.clinic.model.ExamType;
import com.clinic.model.Specialty;
import com.clinic.service.LabService;
import com.clinic.service.MedicalHistoryService;
import com.clinic.service.PatientService;
import com.clinic.service.PrescriptionService;
import com.clinic.service.ScheduleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(2)
@ConditionalOnProperty(name = "clinic.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements CommandLineRunner {

    public static final String DEMO_DOCUMENT = "1000000001";
    public static final String DEMO_PASSWORD = "Demo1234!";

    private final ClinicFacade clinicFacade;
    private final PatientService patientService;
    private final ScheduleService scheduleService;
    private final MedicalHistoryService medicalHistoryService;
    private final PrescriptionService prescriptionService;
    private final LabService labService;
    private final Clock clock;

    public DemoDataSeeder(ClinicFacade clinicFacade, PatientService patientService, ScheduleService scheduleService,
                          MedicalHistoryService medicalHistoryService, PrescriptionService prescriptionService,
                          LabService labService, Clock clock) {
        this.clinicFacade = clinicFacade;
        this.patientService = patientService;
        this.scheduleService = scheduleService;
        this.medicalHistoryService = medicalHistoryService;
        this.prescriptionService = prescriptionService;
        this.labService = labService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (patientService.existsByDocument(DEMO_DOCUMENT)) {
            return;
        }
        PatientResponse demo = clinicFacade.registerPatient(new RegisterPatientRequest(DocumentType.CITIZEN_ID,
                DEMO_DOCUMENT, "Sofia", "Ramirez", LocalDate.of(1994, 5, 12), "sofia.ramirez@example.com",
                "3001234567", DEMO_PASSWORD, List.of("Penicillin")));
        Long patientId = demo.id();

        LocalDateTime now = LocalDateTime.now(clock);
        Doctor generalDoctor = scheduleService.findDoctors(Specialty.GENERAL_MEDICINE).get(0);
        Doctor cardiologist = scheduleService.findDoctors(Specialty.CARDIOLOGY).get(0);

        LocalDateTime firstVisit = now.minusMonths(3).withHour(9).withMinute(0).withSecond(0).withNano(0);
        scheduleService.registerCompletedAppointment(patientId, generalDoctor, firstVisit, "Persistent headache");
        medicalHistoryService.recordConsultation(patientId, generalDoctor.getFullName(), Specialty.GENERAL_MEDICINE,
                firstVisit, "Persistent headache", "Tension-type headache",
                "Hydration, rest and posture correction recommended.");
        prescriptionService.generate(patientId, generalDoctor.getFullName(), "Tension-type headache",
                List.of(new MedicationRequest("Acetaminophen", "500 mg", "Every 8 hours", 5, "Take after meals")),
                firstVisit.plusMinutes(30));

        LocalDateTime secondVisit = now.minusWeeks(3).withHour(10).withMinute(30).withSecond(0).withNano(0);
        scheduleService.registerCompletedAppointment(patientId, cardiologist, secondVisit, "Cardiovascular check-up");
        medicalHistoryService.recordConsultation(patientId, cardiologist.getFullName(), Specialty.CARDIOLOGY,
                secondVisit, "Cardiovascular check-up", "Borderline high cholesterol",
                "Diet changes and lipid profile follow-up.");

        labService.requestExams(patientId, List.of(ExamType.LIPID_PROFILE, ExamType.GLUCOSE));

        LocalDate followUpDay = scheduleService.nextBusinessDay(now.toLocalDate().plusDays(1));
        scheduleService.book(patientId, Specialty.GENERAL_MEDICINE, followUpDay.atTime(9, 0),
                generalDoctor.getId(), "Follow-up visit");
    }
}
