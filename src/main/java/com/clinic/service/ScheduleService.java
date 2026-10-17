package com.clinic.service;

import com.clinic.exception.BadRequestException;
import com.clinic.exception.ConflictException;
import com.clinic.exception.NotFoundException;
import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.Doctor;
import com.clinic.model.Specialty;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final List<LocalTime> DAILY_SLOTS = buildDailySlots();
    private static final int BOOKING_HORIZON_DAYS = 90;
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' HH:mm", Locale.ENGLISH);

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    public ScheduleService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository, Clock clock) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Doctor> findDoctors(Specialty specialty) {
        return specialty == null
                ? doctorRepository.findAllByOrderBySpecialtyAscFullNameAsc()
                : doctorRepository.findBySpecialtyOrderByFullNameAsc(specialty);
    }

    @Transactional(readOnly = true)
    public Doctor findDoctor(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor " + doctorId + " was not found."));
    }

    @Transactional(readOnly = true)
    public List<LocalTime> findAvailableSlots(Doctor doctor, LocalDate date) {
        if (isWeekend(date)) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now(clock);
        Set<LocalTime> booked = appointmentRepository
                .findByDoctor_IdAndStatusAndDateTimeBetween(doctor.getId(), AppointmentStatus.SCHEDULED,
                        date.atStartOfDay(), date.atTime(23, 59))
                .stream()
                .map(appointment -> appointment.getDateTime().toLocalTime())
                .collect(Collectors.toSet());
        return DAILY_SLOTS.stream()
                .filter(slot -> !booked.contains(slot))
                .filter(slot -> date.atTime(slot).isAfter(now))
                .toList();
    }

    @Transactional
    public Appointment book(Long patientId, Specialty specialty, LocalDateTime dateTime, Long doctorId, String reason) {
        validateSlot(dateTime);
        if (appointmentRepository.existsByPatientIdAndDateTimeAndStatus(patientId, dateTime, AppointmentStatus.SCHEDULED)) {
            throw new ConflictException("You already have an appointment on " + format(dateTime) + ".");
        }
        Doctor doctor = doctorId == null
                ? findFirstAvailableDoctor(specialty, dateTime)
                : requireAvailableDoctor(doctorId, specialty, dateTime);
        Appointment appointment = new Appointment(patientId, doctor, dateTime, blankToNull(reason),
                AppointmentStatus.SCHEDULED, LocalDateTime.now(clock));
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment registerCompletedAppointment(Long patientId, Doctor doctor, LocalDateTime dateTime, String reason) {
        Appointment appointment = new Appointment(patientId, doctor, dateTime, reason,
                AppointmentStatus.COMPLETED, dateTime.minusDays(7));
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancel(Long patientId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .filter(found -> found.getPatientId().equals(patientId))
                .orElseThrow(() -> new NotFoundException("Appointment " + appointmentId + " was not found."));
        LocalDateTime now = LocalDateTime.now(clock);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ConflictException("Only scheduled appointments can be cancelled.");
        }
        if (!appointment.getDateTime().isAfter(now)) {
            throw new ConflictException("Past appointments cannot be cancelled.");
        }
        appointment.cancel(now);
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByDateTimeAsc(patientId);
    }

    public List<String> buildReminders(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        List<String> reminders = new ArrayList<>();
        reminders.add("Your " + doctor.getSpecialty().getDisplayName() + " appointment with " + doctor.getFullName()
                + " is on " + format(appointment.getDateTime()) + ".");
        reminders.add("Please arrive 15 minutes early and bring your ID document.");
        reminders.add(specialtyReminder(doctor.getSpecialty()));
        reminders.add("If you cannot attend, cancel at least 24 hours in advance so another patient can use the slot.");
        return reminders;
    }

    public LocalDateTime reminderTime(Appointment appointment) {
        LocalDateTime dayBefore = appointment.getDateTime().minusHours(24);
        LocalDateTime now = LocalDateTime.now(clock);
        return dayBefore.isAfter(now) ? dayBefore : now;
    }

    public LocalDate nextBusinessDay(LocalDate from) {
        LocalDate day = from;
        while (isWeekend(day)) {
            day = day.plusDays(1);
        }
        return day;
    }

    private void validateSlot(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (!dateTime.isAfter(now)) {
            throw new BadRequestException("The appointment date must be in the future.");
        }
        if (dateTime.isAfter(now.plusDays(BOOKING_HORIZON_DAYS))) {
            throw new BadRequestException("Appointments can be booked up to " + BOOKING_HORIZON_DAYS + " days in advance.");
        }
        if (isWeekend(dateTime.toLocalDate())) {
            throw new BadRequestException("The clinic only schedules appointments from Monday to Friday.");
        }
        if (!DAILY_SLOTS.contains(dateTime.toLocalTime())) {
            throw new BadRequestException("Appointments start every 30 minutes from 08:00 to 11:30 and from 14:00 to 16:30.");
        }
    }

    private Doctor requireAvailableDoctor(Long doctorId, Specialty specialty, LocalDateTime dateTime) {
        Doctor doctor = findDoctor(doctorId);
        if (doctor.getSpecialty() != specialty) {
            throw new BadRequestException(doctor.getFullName() + " does not practice " + specialty.getDisplayName() + ".");
        }
        if (!isFree(doctor, dateTime)) {
            throw new ConflictException(doctor.getFullName() + " is not available on " + format(dateTime) + ".");
        }
        return doctor;
    }

    private Doctor findFirstAvailableDoctor(Specialty specialty, LocalDateTime dateTime) {
        return doctorRepository.findBySpecialtyOrderByFullNameAsc(specialty).stream()
                .filter(doctor -> isFree(doctor, dateTime))
                .findFirst()
                .orElseThrow(() -> new ConflictException("No " + specialty.getDisplayName()
                        + " doctor is available on " + format(dateTime) + "."));
    }

    private boolean isFree(Doctor doctor, LocalDateTime dateTime) {
        return !appointmentRepository.existsByDoctor_IdAndDateTimeAndStatus(doctor.getId(), dateTime, AppointmentStatus.SCHEDULED);
    }

    private static String specialtyReminder(Specialty specialty) {
        return switch (specialty) {
            case GENERAL_MEDICINE -> "Bring a list of the medications you currently take.";
            case CARDIOLOGY -> "Bring your latest electrocardiogram and blood pressure records, if you have them.";
            case DERMATOLOGY -> "Do not apply makeup or creams on the area that will be examined.";
            case PEDIATRICS -> "Bring the child's vaccination card.";
        };
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private static String format(LocalDateTime dateTime) {
        return DATE_TIME_FORMAT.format(dateTime);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static List<LocalTime> buildDailySlots() {
        List<LocalTime> slots = new ArrayList<>();
        addSlots(slots, LocalTime.of(8, 0), LocalTime.of(12, 0));
        addSlots(slots, LocalTime.of(14, 0), LocalTime.of(17, 0));
        return List.copyOf(slots);
    }

    private static void addSlots(List<LocalTime> slots, LocalTime start, LocalTime end) {
        for (LocalTime slot = start; slot.isBefore(end); slot = slot.plusMinutes(30)) {
            slots.add(slot);
        }
    }
}
