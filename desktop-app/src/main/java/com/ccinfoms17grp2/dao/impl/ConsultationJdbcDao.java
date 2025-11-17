package com.ccinfoms17grp2.dao.impl;

import com.ccinfoms17grp2.dao.ConsultationDAO;
import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.models.Consultation;
import com.ccinfoms17grp2.utils.DateTimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConsultationJdbcDao extends AbstractJdbcDao implements ConsultationDAO {

    private static final String BASE_SELECT = 
        "SELECT consultation_id, appointment_id, start_time, end_time, diagnosis, treatment_plan, prescription, follow_up_date " +
        "FROM consultation_records ";

    @Override
    public List<Consultation> findAll() throws DaoException {
        final String sql = BASE_SELECT + "ORDER BY start_time DESC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Consultation> consultations = new ArrayList<>();
            while (rs.next()) {
                consultations.add(mapRow(rs));
            }
            return consultations;
        } catch (SQLException ex) {
            throw translateException("Failed to fetch consultations", ex);
        }
    }

    @Override
    public Optional<Consultation> findById(Integer id) throws DaoException {
        final String sql = BASE_SELECT + "WHERE consultation_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find consultation with id=" + id, ex);
        }
    }

    @Override
    public Consultation create(Consultation consultation) throws DaoException {
        final String sql = "INSERT INTO consultation_records (appointment_id, start_time, end_time, diagnosis, treatment_plan, prescription, follow_up_date) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, consultation.getAppointmentId());
            ps.setTimestamp(2, consultation.getStartTime() != null ? Timestamp.valueOf(consultation.getStartTime()) : null);
            ps.setTimestamp(3, consultation.getEndTime() != null ? Timestamp.valueOf(consultation.getEndTime()) : null);
            ps.setString(4, consultation.getDiagnosis());
            ps.setString(5, consultation.getTreatmentPlan());
            ps.setString(6, consultation.getPrescription());
            ps.setTimestamp(7, consultation.getFollowUpDate() != null ? Timestamp.valueOf(consultation.getFollowUpDate()) : null);
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    consultation.setConsultationId(keys.getInt(1));
                }
            }
            return findById(consultation.getConsultationId()).orElse(consultation);
        } catch (SQLException ex) {
            throw translateException("Failed to create consultation", ex);
        }
    }

    @Override
    public boolean update(Consultation consultation) throws DaoException {
        final String sql = "UPDATE consultation_records SET appointment_id = ?, start_time = ?, end_time = ?, " +
                          "diagnosis = ?, treatment_plan = ?, prescription = ?, follow_up_date = ? WHERE consultation_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, consultation.getAppointmentId());
            ps.setTimestamp(2, consultation.getStartTime() != null ? Timestamp.valueOf(consultation.getStartTime()) : null);
            ps.setTimestamp(3, consultation.getEndTime() != null ? Timestamp.valueOf(consultation.getEndTime()) : null);
            ps.setString(4, consultation.getDiagnosis());
            ps.setString(5, consultation.getTreatmentPlan());
            ps.setString(6, consultation.getPrescription());
            ps.setTimestamp(7, consultation.getFollowUpDate() != null ? Timestamp.valueOf(consultation.getFollowUpDate()) : null);
            ps.setInt(8, consultation.getConsultationId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update consultation with id=" + consultation.getConsultationId(), ex);
        }
    }

    @Override
    public boolean delete(Integer id) throws DaoException {
        final String sql = "DELETE FROM consultation_records WHERE consultation_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to delete consultation with id=" + id, ex);
        }
    }

    @Override
    public List<Consultation> findByAppointmentId(int appointmentId) throws DaoException {
        final String sql = BASE_SELECT + "WHERE appointment_id = ? ORDER BY start_time DESC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Consultation> consultations = new ArrayList<>();
                while (rs.next()) {
                    consultations.add(mapRow(rs));
                }
                return consultations;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find consultations for appointment id=" + appointmentId, ex);
        }
    }

    @Override
    public List<Consultation> findByPatientId(int patientId) throws DaoException {
        final String sql = BASE_SELECT + 
            "INNER JOIN appointment_records a ON consultation_records.appointment_id = a.appointment_id " +
            "WHERE a.patient_id = ? ORDER BY start_time DESC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Consultation> consultations = new ArrayList<>();
                while (rs.next()) {
                    consultations.add(mapRow(rs));
                }
                return consultations;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find consultations for patient id=" + patientId, ex);
        }
    }

    @Override
    public List<Consultation> findByDoctorId(int doctorId) throws DaoException {
        final String sql = BASE_SELECT + 
            "INNER JOIN appointment_records a ON consultation_records.appointment_id = a.appointment_id " +
            "WHERE a.doctor_id = ? ORDER BY start_time DESC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Consultation> consultations = new ArrayList<>();
                while (rs.next()) {
                    consultations.add(mapRow(rs));
                }
                return consultations;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find consultations for doctor id=" + doctorId, ex);
        }
    }

    private Consultation mapRow(ResultSet rs) throws SQLException {
        int consultationId = rs.getInt("consultation_id");
        int appointmentId = rs.getInt("appointment_id");
        LocalDateTime startTime = DateTimeUtil.fromTimestamp(rs.getTimestamp("start_time"));
        LocalDateTime endTime = DateTimeUtil.fromTimestamp(rs.getTimestamp("end_time"));
        String diagnosis = rs.getString("diagnosis");
        String treatmentPlan = rs.getString("treatment_plan");
        String prescription = rs.getString("prescription");
        LocalDateTime followUpDate = DateTimeUtil.fromTimestamp(rs.getTimestamp("follow_up_date"));
        
        return new Consultation(consultationId, appointmentId, startTime, endTime, 
                               diagnosis, treatmentPlan, prescription, followUpDate);
    }
}
