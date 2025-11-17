package com.ccinfoms17grp2.models;

import java.util.ArrayList;
import java.util.List;

public class BranchWithDoctors {
    private final Branch branch;
    private final double distanceKm;
    private final List<DoctorAvailabilityInfo> doctors;

    public BranchWithDoctors(Branch branch, double distanceKm) {
        this.branch = branch;
        this.distanceKm = distanceKm;
        this.doctors = new ArrayList<>();
    }

    public Branch getBranch() {
        return branch;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public List<DoctorAvailabilityInfo> getDoctors() {
        return doctors;
    }

    public void addDoctor(DoctorAvailabilityInfo doctor) {
        this.doctors.add(doctor);
    }

    public static class DoctorAvailabilityInfo {
        private final Doctor doctor;
        private final AvailabilityColor color;

        public DoctorAvailabilityInfo(Doctor doctor, AvailabilityColor color) {
            this.doctor = doctor;
            this.color = color;
        }

        public Doctor getDoctor() {
            return doctor;
        }

        public AvailabilityColor getColor() {
            return color;
        }

        public String getDisplayName() {
            return doctor.getFirstName() + " " + doctor.getLastName();
        }
    }

    public enum AvailabilityColor {
        GREEN,
        YELLOW,
        RED
    }
}
