package com.ccinfoms17grp2.dao;

import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentDAO extends CrudRepository<Appointment, Integer> {

    List<Appointment> findByPatientId(int patientId) throws DaoException;

    List<Appointment> findByDoctorId(int doctorId) throws DaoException;

    List<Appointment> findByBranchId(int branchId) throws DaoException;

    List<Appointment> findByStatus(AppointmentStatus status) throws DaoException;

    List<Appointment> findByDoctorAndDateRange(int doctorId, LocalDateTime start, LocalDateTime end) throws DaoException;

    boolean updateStatus(int appointmentId, AppointmentStatus status) throws DaoException;
    
    List<Appointment> findTodaysAppointmentsByDoctor(int doctorId) throws DaoException;
}
