package com.ccinfoms17grp2.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Doctor {

    private int doctorId;
    private String lastName;
    private String firstName;
    private String email;
    private List<Integer> specializationIds;
    private DoctorAvailabilityStatus availabilityStatus;
    private LocalDateTime createdAt;
    private String availabilityDatetimeRanges;

    public Doctor() {
        this.availabilityStatus = DoctorAvailabilityStatus.AVAILABLE;
        this.specializationIds = new ArrayList<>();
        this.availabilityDatetimeRanges = null;
    }

    public Doctor(int doctorId, String lastName, String firstName, String email, List<Integer> specializationIds, DoctorAvailabilityStatus availabilityStatus, LocalDateTime createdAt) {
        this.doctorId = doctorId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.specializationIds = specializationIds != null ? specializationIds : new ArrayList<>();
        this.availabilityStatus = availabilityStatus == null ? DoctorAvailabilityStatus.AVAILABLE : availabilityStatus;
        this.createdAt = createdAt;
        this.availabilityDatetimeRanges = null;
    }

    public Doctor(int doctorId, String lastName, String firstName, List<Integer> specializationIds,
                  DoctorAvailabilityStatus availabilityStatus, LocalDateTime createdAt) {
        this(doctorId, lastName, firstName, null, specializationIds, availabilityStatus, createdAt);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Integer> getSpecializationIds() {
        return specializationIds;
    }

    public void setSpecializationIds(List<Integer> specializationIds) {
        this.specializationIds = specializationIds != null ? specializationIds : new ArrayList<>();
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

    public String getAvailabilityDatetimeRanges() {
        return availabilityDatetimeRanges;
    }

    public void setAvailabilityDatetimeRanges(String availabilityDatetimeRanges) {
        this.availabilityDatetimeRanges = availabilityDatetimeRanges;
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
                ", email='" + email + '\'' +
                ", specializationIds=" + specializationIds +
                ", availabilityStatus=" + availabilityStatus +
                ", createdAt=" + createdAt +
                '}';
    }
}
