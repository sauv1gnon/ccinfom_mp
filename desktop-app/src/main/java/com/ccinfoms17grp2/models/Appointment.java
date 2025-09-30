package com.ccinfoms17grp2.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Appointment {

    private int appointmentId;
    private int patientId;
    private int doctorId;
    private int branchId;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private LocalDateTime createdAt;

    public Appointment() {
        this.status = AppointmentStatus.SCHEDULED;
    }

    public Appointment(int appointmentId, int patientId, int doctorId, int branchId,
                       LocalDateTime appointmentDateTime, AppointmentStatus status, LocalDateTime createdAt) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.branchId = branchId;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status == null ? AppointmentStatus.SCHEDULED : status;
        this.createdAt = createdAt;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Appointment)) {
            return false;
        }
        Appointment that = (Appointment) o;
        return appointmentId == that.appointmentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentId);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId=" + appointmentId +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", branchId=" + branchId +
                ", appointmentDateTime=" + appointmentDateTime +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
