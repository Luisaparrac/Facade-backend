package com.clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lab_results")
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 30)
    private ExamType examType;

    @Column(name = "parameter_name", nullable = false, length = 60)
    private String parameterName;

    @Column(name = "result_value", nullable = false)
    private double resultValue;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "reference_min", nullable = false)
    private double referenceMin;

    @Column(name = "reference_max", nullable = false)
    private double referenceMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ResultStatus status;

    protected LabResult() {
    }

    public LabResult(ExamType examType, LabParameter parameter, double resultValue) {
        this.examType = examType;
        this.parameterName = parameter.name();
        this.unit = parameter.unit();
        this.referenceMin = parameter.referenceMin();
        this.referenceMax = parameter.referenceMax();
        this.resultValue = resultValue;
        this.status = ResultStatus.evaluate(resultValue, referenceMin, referenceMax);
    }

    public Long getId() { return id; }
    public ExamType getExamType() { return examType; }
    public String getParameterName() { return parameterName; }
    public double getResultValue() { return resultValue; }
    public String getUnit() { return unit; }
    public double getReferenceMin() { return referenceMin; }
    public double getReferenceMax() { return referenceMax; }
    public ResultStatus getStatus() { return status; }
}
