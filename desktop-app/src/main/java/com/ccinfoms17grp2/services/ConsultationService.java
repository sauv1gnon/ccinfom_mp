package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.ConsultationDAO;
import com.ccinfoms17grp2.models.Consultation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ConsultationService {

    private final ConsultationDAO consultationDAO;

    public ConsultationService(ConsultationDAO consultationDAO) {
        this.consultationDAO = Objects.requireNonNull(consultationDAO);
    }

    public List<Consultation> listConsultations() {
        return consultationDAO.findAll();
    }

    public Optional<Consultation> getConsultationById(int consultationId) {
        return consultationDAO.findById(consultationId);
    }

    public List<Consultation> getConsultationsByAppointmentId(int appointmentId) {
        return consultationDAO.findByAppointmentId(appointmentId);
    }

    public List<Consultation> getConsultationsByPatientId(int patientId) {
        return consultationDAO.findByPatientId(patientId);
    }

    public List<Consultation> getConsultationsByDoctorId(int doctorId) {
        return consultationDAO.findByDoctorId(doctorId);
    }

    public Consultation createConsultation(Consultation consultation) {
        validate(consultation, false);
        return consultationDAO.create(consultation);
    }

    public Consultation updateConsultation(Consultation consultation) {
        validate(consultation, true);
        Optional<Consultation> existing = consultationDAO.findById(consultation.getConsultationId());
        if (existing.isEmpty()) {
            throw new ValidationException("Consultation could not be found.");
        }
        boolean updated = consultationDAO.update(consultation);
        if (!updated) {
            throw new ValidationException("Consultation record could not be updated.");
        }
        return consultationDAO.findById(consultation.getConsultationId()).orElse(consultation);
    }

    public void deleteConsultation(int consultationId) {
        boolean deleted = consultationDAO.delete(consultationId);
        if (!deleted) {
            throw new ValidationException("Consultation could not be deleted.");
        }
    }

    private void validate(Consultation consultation, boolean requireId) {
        if (consultation == null) {
            throw new ValidationException("Consultation information is required.");
        }
        if (requireId && consultation.getConsultationId() <= 0) {
            throw new ValidationException("Consultation ID is invalid.");
        }
        if (consultation.getAppointmentId() <= 0) {
            throw new ValidationException("Appointment ID is required.");
        }
    }
}
