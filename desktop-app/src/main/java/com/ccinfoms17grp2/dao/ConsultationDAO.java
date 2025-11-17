package com.ccinfoms17grp2.dao;

import com.ccinfoms17grp2.models.Consultation;

import java.util.List;

public interface ConsultationDAO extends CrudRepository<Consultation, Integer> {

    List<Consultation> findByAppointmentId(int appointmentId) throws DaoException;

    List<Consultation> findByPatientId(int patientId) throws DaoException;

    List<Consultation> findByDoctorId(int doctorId) throws DaoException;
}
