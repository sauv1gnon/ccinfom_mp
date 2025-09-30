package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.DoctorDAO;
import com.ccinfoms17grp2.dao.SpecializationDAO;
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
        if (doctor.getSpecializationId() != null && specializationDAO.findById(doctor.getSpecializationId()).isEmpty()) {
            throw new ValidationException("Selected specialization does not exist.");
        }
    }
}
