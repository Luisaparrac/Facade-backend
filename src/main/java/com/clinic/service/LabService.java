package com.clinic.service;

import com.clinic.model.ExamType;
import com.clinic.model.LabOrder;
import com.clinic.model.LabParameter;
import com.clinic.model.LabResult;
import com.clinic.repository.LabOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LabService {

    private final LabOrderRepository labOrderRepository;
    private final LabResultSimulator simulator;
    private final Clock clock;

    public LabService(LabOrderRepository labOrderRepository, LabResultSimulator simulator, Clock clock) {
        this.labOrderRepository = labOrderRepository;
        this.simulator = simulator;
        this.clock = clock;
    }

    @Transactional
    public LabOrder requestExams(Long patientId, List<ExamType> exams) {
        LocalDateTime now = LocalDateTime.now(clock);
        LabOrder order = new LabOrder(patientId, now);
        for (ExamType exam : exams.stream().distinct().toList()) {
            for (LabParameter parameter : exam.getParameters()) {
                order.addResult(new LabResult(exam, parameter, simulator.simulate(parameter)));
            }
        }
        order.complete(now);
        return labOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<LabOrder> findByPatient(Long patientId) {
        return labOrderRepository.findByPatientIdOrderByRequestedAtDesc(patientId);
    }

    public List<ExamType> getCatalog() {
        return List.of(ExamType.values());
    }
}
