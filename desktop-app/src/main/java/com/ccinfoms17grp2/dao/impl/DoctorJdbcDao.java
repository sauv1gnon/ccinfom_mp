package com.ccinfoms17grp2.dao.impl;

import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.dao.DoctorDAO;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;
import com.ccinfoms17grp2.utils.DateTimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorJdbcDao extends AbstractJdbcDao implements DoctorDAO {

    private static final String BASE_SELECT = "SELECT doctor_id, last_name, first_name, specialization_id, availability_status, created_at FROM Doctor ";
    private static final String ORDER_BY = " ORDER BY last_name ASC, first_name ASC";

    @Override
    public List<Doctor> findAll() throws DaoException {
        final String sql = BASE_SELECT + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Doctor> doctors = new ArrayList<>();
            while (rs.next()) {
                doctors.add(mapRow(rs));
            }
            return doctors;
        } catch (SQLException ex) {
            throw translateException("Failed to fetch doctors", ex);
        }
    }

    @Override
    public Optional<Doctor> findById(Integer id) throws DaoException {
        final String sql = BASE_SELECT + "WHERE doctor_id = ?";
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
            throw translateException("Failed to find doctor with id=" + id, ex);
        }
    }

    @Override
    public Doctor create(Doctor doctor) throws DaoException {
        final String sql = "INSERT INTO Doctor (last_name, first_name, specialization_id, availability_status) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doctor.getLastName());
            ps.setString(2, doctor.getFirstName());
            if (doctor.getSpecializationId() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, doctor.getSpecializationId());
            }
            ps.setString(4, doctor.getAvailabilityStatus().toDatabaseValue());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    doctor.setDoctorId(keys.getInt(1));
                }
            }
            return findById(doctor.getDoctorId()).orElse(doctor);
        } catch (SQLException ex) {
            throw translateException("Failed to create doctor", ex);
        }
    }

    @Override
    public boolean update(Doctor doctor) throws DaoException {
        final String sql = "UPDATE Doctor SET last_name = ?, first_name = ?, specialization_id = ?, availability_status = ? WHERE doctor_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, doctor.getLastName());
            ps.setString(2, doctor.getFirstName());
            if (doctor.getSpecializationId() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, doctor.getSpecializationId());
            }
            ps.setString(4, doctor.getAvailabilityStatus().toDatabaseValue());
            ps.setInt(5, doctor.getDoctorId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update doctor with id=" + doctor.getDoctorId(), ex);
        }
    }

    @Override
    public boolean delete(Integer id) throws DaoException {
        final String sql = "DELETE FROM Doctor WHERE doctor_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to delete doctor with id=" + id, ex);
        }
    }

    @Override
    public List<Doctor> findBySpecialization(int specializationId) throws DaoException {
        final String sql = BASE_SELECT + "WHERE specialization_id = ?" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, specializationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Doctor> doctors = new ArrayList<>();
                while (rs.next()) {
                    doctors.add(mapRow(rs));
                }
                return doctors;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to fetch doctors by specialization", ex);
        }
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        int doctorId = rs.getInt("doctor_id");
        String lastName = rs.getString("last_name");
        String firstName = rs.getString("first_name");
        Integer specializationId = rs.getObject("specialization_id") == null ? null : rs.getInt("specialization_id");
        String statusValue = rs.getString("availability_status");
        DoctorAvailabilityStatus status = DoctorAvailabilityStatus.fromDatabaseValue(statusValue);
        LocalDateTime createdAt = DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at"));
        return new Doctor(doctorId, lastName, firstName, specializationId, status, createdAt);
    }
}
