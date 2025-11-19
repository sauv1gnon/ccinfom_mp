package com.ccinfoms17grp2.dao.impl;

import com.ccinfoms17grp2.dao.AppointmentDAO;
import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.models.Appointment;
import com.ccinfoms17grp2.models.AppointmentStatus;
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

public class AppointmentJdbcDao extends AbstractJdbcDao implements AppointmentDAO {

    private static final String BASE_SELECT = 
        "SELECT appointment_id, patient_id, doctor_id, branch_id, appointment_datetime, status, created_at " +
        "FROM appointment_records ";

    @Override
    public List<Appointment> findAll() throws DaoException {
        final String sql = BASE_SELECT + "ORDER BY appointment_datetime DESC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Appointment> appointments = new ArrayList<>();
            while (rs.next()) {
                appointments.add(mapRow(rs));
            }
            return appointments;
        } catch (SQLException ex) {
            throw translateException("Failed to fetch appointments", ex);
        }
    }

    @Override
    public Optional<Appointment> findById(Integer id) throws DaoException {
        final String sql = BASE_SELECT + "WHERE appointment_id = ?";
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
            throw translateException("Failed to find appointment with id=" + id, ex);
        }
    }

    @Override
    public Appointment create(Appointment appointment) throws DaoException {
        final String sql = "INSERT INTO appointment_records (patient_id, doctor_id, branch_id, appointment_datetime, status) " +
                          "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setInt(3, appointment.getBranchId());
            ps.setTimestamp(4, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            ps.setString(5, appointment.getStatus().toDatabaseValue());
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    appointment.setAppointmentId(keys.getInt(1));
                }
            }
            return findById(appointment.getAppointmentId()).orElse(appointment);
        } catch (SQLException ex) {
            throw translateException("Failed to create appointment", ex);
        }
    }

    @Override
    public boolean update(Appointment appointment) throws DaoException {
        final String sql = "UPDATE appointment_records SET patient_id = ?, doctor_id = ?, branch_id = ?, " +
                          "appointment_datetime = ?, status = ? WHERE appointment_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setInt(3, appointment.getBranchId());
            ps.setTimestamp(4, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            ps.setString(5, appointment.getStatus().toDatabaseValue());
            ps.setInt(6, appointment.getAppointmentId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update appointment with id=" + appointment.getAppointmentId(), ex);
        }
    }

    @Override
    public boolean delete(Integer id) throws DaoException {
        final String sql = "DELETE FROM appointment_records WHERE appointment_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to delete appointment with id=" + id, ex);
        }
    }

    @Override
    public List<Appointment> findByPatientId(int patientId) throws DaoException {
        final String sql = BASE_SELECT + "WHERE patient_id = ? ORDER BY appointment_datetime DESC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> appointments = new ArrayList<>();
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
                return appointments;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find appointments for patient id=" + patientId, ex);
        }
    }

    @Override
    public List<Appointment> findByDoctorId(int doctorId) throws DaoException {
        final String sql = BASE_SELECT + "WHERE doctor_id = ? ORDER BY appointment_datetime DESC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> appointments = new ArrayList<>();
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
                return appointments;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find appointments for doctor id=" + doctorId, ex);
        }
    }

    @Override
    public List<Appointment> findByBranchId(int branchId) throws DaoException {
        final String sql = BASE_SELECT + "WHERE branch_id = ? ORDER BY appointment_datetime DESC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> appointments = new ArrayList<>();
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
                return appointments;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find appointments for branch id=" + branchId, ex);
        }
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) throws DaoException {
        final String sql = BASE_SELECT + "WHERE status = ? ORDER BY appointment_datetime DESC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.toDatabaseValue());
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> appointments = new ArrayList<>();
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
                return appointments;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find appointments with status=" + status, ex);
        }
    }

    @Override
    public List<Appointment> findByDoctorAndDateRange(int doctorId, LocalDateTime start, LocalDateTime end) throws DaoException {
        final String sql = BASE_SELECT + 
            "WHERE doctor_id = ? AND appointment_datetime BETWEEN ? AND ? ORDER BY appointment_datetime";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> appointments = new ArrayList<>();
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
                return appointments;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find appointments for doctor in date range", ex);
        }
    }

    @Override
    public boolean updateStatus(int appointmentId, AppointmentStatus status) throws DaoException {
        final String sql = "UPDATE appointment_records SET status = ? WHERE appointment_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.toDatabaseValue());
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update appointment status for id=" + appointmentId, ex);
        }
    }
    
    @Override
    public List<Appointment> findTodaysAppointmentsByDoctor(int doctorId) throws DaoException {
        final String sql = BASE_SELECT + 
            "WHERE doctor_id = ? AND DATE(appointment_datetime) = CURDATE() ORDER BY appointment_datetime";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appointment> appointments = new ArrayList<>();
                while (rs.next()) {
                    appointments.add(mapRow(rs));
                }
                return appointments;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find today's appointments for doctor id=" + doctorId, ex);
        }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        int appointmentId = rs.getInt("appointment_id");
        int patientId = rs.getInt("patient_id");
        int doctorId = rs.getInt("doctor_id");
        int branchId = rs.getInt("branch_id");
        LocalDateTime appointmentDateTime = DateTimeUtil.fromTimestamp(rs.getTimestamp("appointment_datetime"));
        String statusStr = rs.getString("status");
        AppointmentStatus status = AppointmentStatus.fromDatabaseValue(statusStr);
        LocalDateTime createdAt = DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at"));
        
        return new Appointment(appointmentId, patientId, doctorId, branchId, appointmentDateTime, status, createdAt);
    }
}
