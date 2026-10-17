package com.clinic.config;

import com.clinic.model.Doctor;
import com.clinic.model.Specialty;
import com.clinic.repository.DoctorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class DoctorSeeder implements CommandLineRunner {

    private final DoctorRepository doctorRepository;

    public DoctorSeeder(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public void run(String... args) {
        if (doctorRepository.count() > 0) {
            return;
        }
        doctorRepository.saveAll(List.of(
                new Doctor("Dr. Laura Martinez", Specialty.GENERAL_MEDICINE, "RM-10234"),
                new Doctor("Dr. Andres Gomez", Specialty.GENERAL_MEDICINE, "RM-10871"),
                new Doctor("Dr. Camila Rojas", Specialty.CARDIOLOGY, "RM-20415"),
                new Doctor("Dr. Felipe Herrera", Specialty.DERMATOLOGY, "RM-30562"),
                new Doctor("Dr. Valentina Ruiz", Specialty.PEDIATRICS, "RM-40198")));
    }
}
