package com.ccinfoms17grp2.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Doctor {

    private int doctorId;
    private String lastName;
    private String firstName;
    private Integer specializationId;
    private DoctorAvailabilityStatus availabilityStatus;
    private LocalDateTime createdAt;

    public Doctor() {
        this.availabilityStatus = DoctorAvailabilityStatus.AVAILABLE;
    }

    public Doctor(int doctorId, String lastName, String firstName, Integer specializationId, DoctorAvailabilityStatus availabilityStatus, LocalDateTime createdAt) {
        this.doctorId = doctorId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.specializationId = specializationId;
        this.availabilityStatus = availabilityStatus == null ? DoctorAvailabilityStatus.AVAILABLE : availabilityStatus;
        this.createdAt = createdAt;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Integer getSpecializationId() {
        return specializationId;
    }

    public void setSpecializationId(Integer specializationId) {
        this.specializationId = specializationId;
    }

    public DoctorAvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(DoctorAvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
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
        if (!(o instanceof Doctor)) {
            return false;
        }
        Doctor doctor = (Doctor) o;
        return doctorId == doctor.doctorId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctorId);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId=" + doctorId +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", specializationId=" + specializationId +
                ", availabilityStatus=" + availabilityStatus +
                ", createdAt=" + createdAt +
                '}';
    }
}
