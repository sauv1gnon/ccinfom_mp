package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.AppointmentDAO;
import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AppointmentService {

    private final AppointmentDAO appointmentDAO;

    public AppointmentService(AppointmentDAO appointmentDAO) {
        this.appointmentDAO = Objects.requireNonNull(appointmentDAO);
    }

    public List<Appointment> listAppointments() {
        return appointmentDAO.findAll();
    }

    public Optional<Appointment> getAppointmentById(int appointmentId) {
        return appointmentDAO.findById(appointmentId);
    }

    public List<Appointment> getAppointmentsByPatientId(int patientId) {
        return appointmentDAO.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctorId(int doctorId) {
        return appointmentDAO.findByDoctorId(doctorId);
    }

    public List<Appointment> getAppointmentsByBranchId(int branchId) {
        return appointmentDAO.findByBranchId(branchId);
    }

    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentDAO.findByStatus(status);
    }

    public List<Appointment> getAppointmentsByDoctorAndDateRange(int doctorId, LocalDateTime start, LocalDateTime end) {
        return appointmentDAO.findByDoctorAndDateRange(doctorId, start, end);
    }
    
    public List<Appointment> getTodaysAppointmentsByDoctor(int doctorId) {
        if (doctorId <= 0) {
            throw new ValidationException("Doctor ID is invalid.");
        }
        return appointmentDAO.findTodaysAppointmentsByDoctor(doctorId);
    }

    public Appointment createAppointment(Appointment appointment) {
        validate(appointment, false);
        return appointmentDAO.create(appointment);
    }

    public Appointment updateAppointment(Appointment appointment) {
        validate(appointment, true);
        Optional<Appointment> existing = appointmentDAO.findById(appointment.getAppointmentId());
        if (existing.isEmpty()) {
            throw new ValidationException("Appointment could not be found.");
        }
        boolean updated = appointmentDAO.update(appointment);
        if (!updated) {
            throw new ValidationException("Appointment record could not be updated.");
        }
        return appointmentDAO.findById(appointment.getAppointmentId()).orElse(appointment);
    }

    public boolean cancelAppointment(int appointmentId) {
        Optional<Appointment> existing = appointmentDAO.findById(appointmentId);
        if (existing.isEmpty()) {
            throw new ValidationException("Appointment could not be found.");
        }
        return appointmentDAO.updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }

    public boolean updateAppointmentStatus(int appointmentId, AppointmentStatus status) {
        Optional<Appointment> existing = appointmentDAO.findById(appointmentId);
        if (existing.isEmpty()) {
            throw new ValidationException("Appointment could not be found.");
        }
        return appointmentDAO.updateStatus(appointmentId, status);
    }

    public void deleteAppointment(int appointmentId) {
        boolean deleted = appointmentDAO.delete(appointmentId);
        if (!deleted) {
            throw new ValidationException("Appointment could not be deleted.");
        }
    }

    private void validate(Appointment appointment, boolean requireId) {
        if (appointment == null) {
            throw new ValidationException("Appointment information is required.");
        }
        if (requireId && appointment.getAppointmentId() <= 0) {
            throw new ValidationException("Appointment ID is invalid.");
        }
        if (appointment.getPatientId() <= 0) {
            throw new ValidationException("Patient ID is required.");
        }
        if (appointment.getDoctorId() <= 0) {
            throw new ValidationException("Doctor ID is required.");
        }
        if (appointment.getBranchId() <= 0) {
            throw new ValidationException("Branch ID is required.");
        }
        if (appointment.getAppointmentDateTime() == null) {
            throw new ValidationException("Appointment date and time are required.");
        }
        if (appointment.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Appointment date must be in the future.");
        }
    }
}
