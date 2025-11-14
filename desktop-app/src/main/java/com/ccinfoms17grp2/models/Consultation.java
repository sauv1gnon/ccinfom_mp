package com.ccinfoms17grp2.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Consultation {

    private int consultationId;
    private int appointmentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String diagnosis;
    private String treatmentPlan;
    private String prescription;
    private LocalDateTime followUpDate;

    public Consultation() {
    }

    public Consultation(int consultationId, int appointmentId, LocalDateTime startTime, LocalDateTime endTime,
                       String diagnosis, String treatmentPlan, String prescription, LocalDateTime followUpDate) {
        this.consultationId = consultationId;
        this.appointmentId = appointmentId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.diagnosis = diagnosis;
        this.treatmentPlan = treatmentPlan;
        this.prescription = prescription;
        this.followUpDate = followUpDate;
    }

    public int getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(int consultationId) {
        this.consultationId = consultationId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public LocalDateTime getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDateTime followUpDate) {
        this.followUpDate = followUpDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Consultation)) {
            return false;
        }
        Consultation that = (Consultation) o;
        return consultationId == that.consultationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(consultationId);
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "consultationId=" + consultationId +
                ", appointmentId=" + appointmentId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", diagnosis='" + diagnosis + '\'' +
                ", treatmentPlan='" + treatmentPlan + '\'' +
                ", prescription='" + prescription + '\'' +
                ", followUpDate=" + followUpDate +
                '}';
    }
}