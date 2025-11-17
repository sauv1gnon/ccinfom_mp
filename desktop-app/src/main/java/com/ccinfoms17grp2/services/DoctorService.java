package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.DoctorDAO;
import com.ccinfoms17grp2.dao.SpecializationDAO;
import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DoctorService {

    private final DoctorDAO doctorDAO;
    private final SpecializationDAO specializationDAO;

    public DoctorService(DoctorDAO doctorDAO, SpecializationDAO specializationDAO) {
        this.doctorDAO = Objects.requireNonNull(doctorDAO);
        this.specializationDAO = Objects.requireNonNull(specializationDAO);
    }

    public List<Doctor> listDoctors() {
        return doctorDAO.findAll();
    }

    public Optional<Doctor> getDoctorById(int doctorId) {
        if (doctorId <= 0) {
            return Optional.empty();
        }
        return doctorDAO.findById(doctorId);
    }

    public Doctor createDoctor(Doctor doctor) {
        validate(doctor, false);
        return doctorDAO.create(doctor);
    }

    public Doctor updateDoctor(Doctor doctor) {
        validate(doctor, true);
        boolean updated = doctorDAO.update(doctor);
        if (!updated) {
            throw new ValidationException("Doctor record could not be updated.");
        }
        Optional<Doctor> refreshed = doctorDAO.findById(doctor.getDoctorId());
        return refreshed.orElse(doctor);
    }

    public void deleteDoctor(int doctorId) {
        boolean deleted = doctorDAO.delete(doctorId);
        if (!deleted) {
            throw new ValidationException("Doctor record could not be deleted. It may have scheduled appointments.");
        }
    }

    public List<Doctor> findBySpecialization(int specializationId) {
        return doctorDAO.findBySpecialization(specializationId);
    }

    private void validate(Doctor doctor, boolean requireId) {
        if (doctor == null) {
            throw new ValidationException("Doctor information is required.");
        }
        if (requireId && doctor.getDoctorId() <= 0) {
            throw new ValidationException("Doctor ID is invalid.");
        }
        if (doctor.getLastName() == null || doctor.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name is required.");
        }
        if (doctor.getFirstName() == null || doctor.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name is required.");
        }
        DoctorAvailabilityStatus status = doctor.getAvailabilityStatus();
        if (status == null) {
            throw new ValidationException("Availability status must be specified.");
        }
        if (doctor.getSpecializationIds() != null && !doctor.getSpecializationIds().isEmpty()) {
            for (Integer specId : doctor.getSpecializationIds()) {
                if (specializationDAO.findById(specId).isEmpty()) {
                    throw new ValidationException("Specialization with ID " + specId + " does not exist.");
                }
            }
        }
    }

    public List<Doctor> getDoctorsByBranch(int branchId) {
        return doctorDAO.findByBranchId(branchId);
    }

    public List<Doctor> getDoctorsByBranchAndSpecializations(int branchId, List<Integer> specializationIds) {
        return doctorDAO.findByBranchAndSpecializations(branchId, specializationIds);
    }

    public List<Doctor> getDoctorsByAvailabilityStatus(DoctorAvailabilityStatus status) {
        return doctorDAO.findByAvailabilityStatus(status);
    }

    public List<Doctor> findDoctorsByAvailability(DoctorAvailabilityStatus status) {
        if (status == null) {
            return listDoctors();
        }
        return getDoctorsByAvailabilityStatus(status);
    }

    public List<Doctor> searchDoctors(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listDoctors();
        }
        String normalized = keyword.trim().toLowerCase();
        List<Doctor> matches = new java.util.ArrayList<>();
        for (Doctor doctor : listDoctors()) {
            if (doctor.getLastName() != null && doctor.getLastName().toLowerCase().contains(normalized)) {
                matches.add(doctor);
                continue;
            }
            if (doctor.getFirstName() != null && doctor.getFirstName().toLowerCase().contains(normalized)) {
                matches.add(doctor);
                continue;
            }
            if (doctor.getEmail() != null && doctor.getEmail().toLowerCase().contains(normalized)) {
                matches.add(doctor);
                continue;
            }
            if (!doctor.getSpecializationIds().isEmpty() && doctor.getSpecializationIds().stream()
                .map(String::valueOf)
                .anyMatch(id -> id.contains(normalized))) {
                matches.add(doctor);
            }
        }
        return matches;
    }

    public List<Branch> getBranchesForDoctor(int doctorId) {
        if (doctorId <= 0) {
            return List.of();
        }
        return doctorDAO.findBranchesForDoctor(doctorId);
    }

    public void assignDoctorToBranches(int doctorId, List<Integer> branchIds) {
        if (doctorId <= 0) {
            throw new ValidationException("Doctor ID is invalid.");
        }
        List<Integer> assignments = branchIds == null ? List.of() : branchIds;
        Optional<Doctor> existing = doctorDAO.findById(doctorId);
        if (existing.isEmpty()) {
            throw new ValidationException("Doctor could not be found.");
        }
        doctorDAO.updateBranchAssignments(doctorId, assignments);
    }
}
