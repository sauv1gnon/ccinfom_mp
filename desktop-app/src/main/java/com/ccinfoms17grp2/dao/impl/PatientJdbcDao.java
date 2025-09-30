package com.ccinfoms17grp2.dao.impl;

import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.dao.PatientDAO;
import com.ccinfoms17grp2.models.Patient;
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

public class PatientJdbcDao extends AbstractJdbcDao implements PatientDAO {

    private static final String BASE_SELECT = "SELECT patient_id, last_name, first_name, contact_number, email, created_at FROM Patient ";
    private static final String ORDER_BY = " ORDER BY last_name ASC, first_name ASC";

    @Override
    public List<Patient> findAll() throws DaoException {
        final String sql = BASE_SELECT + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Patient> patients = new ArrayList<>();
            while (rs.next()) {
                patients.add(mapRow(rs));
            }
            return patients;
        } catch (SQLException ex) {
            throw translateException("Failed to fetch patients", ex);
        }
    }

    @Override
    public Optional<Patient> findById(Integer id) throws DaoException {
        final String sql = BASE_SELECT + "WHERE patient_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find patient with id=" + id, ex);
        }
    }

    @Override
    public Patient create(Patient patient) throws DaoException {
        final String sql = "INSERT INTO Patient (last_name, first_name, contact_number, email) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, patient.getLastName());
            statement.setString(2, patient.getFirstName());
            statement.setString(3, patient.getContactNumber());
            statement.setString(4, patient.getEmail());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    patient.setPatientId(generatedId);
                }
            }
            // fetch created_at from database
            return findById(patient.getPatientId()).orElse(patient);
        } catch (SQLException ex) {
            throw translateException("Failed to create patient", ex);
        }
    }

    @Override
    public boolean update(Patient patient) throws DaoException {
        final String sql = "UPDATE Patient SET last_name = ?, first_name = ?, contact_number = ?, email = ? WHERE patient_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patient.getLastName());
            statement.setString(2, patient.getFirstName());
            statement.setString(3, patient.getContactNumber());
            statement.setString(4, patient.getEmail());
            statement.setInt(5, patient.getPatientId());
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update patient with id=" + patient.getPatientId(), ex);
        }
    }

    @Override
    public boolean delete(Integer id) throws DaoException {
        final String sql = "DELETE FROM Patient WHERE patient_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to delete patient with id=" + id, ex);
        }
    }

    @Override
    public List<Patient> searchByName(String keyword) throws DaoException {
        final String sql = BASE_SELECT + "WHERE CONCAT(last_name, ' ', first_name) LIKE ?" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + keyword + "%");
            try (ResultSet rs = statement.executeQuery()) {
                List<Patient> patients = new ArrayList<>();
                while (rs.next()) {
                    patients.add(mapRow(rs));
                }
                return patients;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to search patients by keyword", ex);
        }
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("patient_id");
        String lastName = rs.getString("last_name");
        String firstName = rs.getString("first_name");
        String contactNumber = rs.getString("contact_number");
        String email = rs.getString("email");
        LocalDateTime createdAt = DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at"));
        return new Patient(id, lastName, firstName, contactNumber, email, createdAt);
    }
}
