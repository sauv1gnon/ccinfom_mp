package com.ccinfoms17grp2.dao.impl;

import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.dao.DoctorDAO;
import com.ccinfoms17grp2.models.Branch;
import com.ccinfoms17grp2.models.Doctor;
import com.ccinfoms17grp2.models.DoctorAvailabilityStatus;
import com.ccinfoms17grp2.utils.DateTimeUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

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

    private static final String BASE_SELECT = "SELECT doctor_records.doctor_id, doctor_records.last_name, doctor_records.first_name, doctor_records.email, doctor_records.specializations_list, doctor_records.availability_status, doctor_records.availability_datetime_ranges, doctor_records.created_at FROM doctor_records ";
    private static final String ORDER_BY = " ORDER BY doctor_records.last_name ASC, doctor_records.first_name ASC";

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
        final String sql = "INSERT INTO doctor_records (last_name, first_name, email, specializations_list, availability_status) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doctor.getLastName());
            ps.setString(2, doctor.getFirstName());
            ps.setString(3, doctor.getEmail());
            ps.setString(4, serializeSpecializations(doctor.getSpecializationIds()));
            ps.setString(5, doctor.getAvailabilityStatus().toDatabaseValue());
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
        final String sql = "UPDATE doctor_records SET last_name = ?, first_name = ?, email = ?, specializations_list = ?, availability_status = ? WHERE doctor_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, doctor.getLastName());
            ps.setString(2, doctor.getFirstName());
            ps.setString(3, doctor.getEmail());
            ps.setString(4, serializeSpecializations(doctor.getSpecializationIds()));
            ps.setString(5, doctor.getAvailabilityStatus().toDatabaseValue());
            ps.setInt(6, doctor.getDoctorId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update doctor with id=" + doctor.getDoctorId(), ex);
        }
    }

    @Override
    public boolean delete(Integer id) throws DaoException {
        final String sql = "DELETE FROM doctor_records WHERE doctor_id = ?";
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
        final String sql = BASE_SELECT + "WHERE JSON_CONTAINS(specializations_list, ?, '$')" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(specializationId));
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

    @Override
    public List<Doctor> findByBranchId(int branchId) throws DaoException {
        final String sql = BASE_SELECT + 
            "INNER JOIN doctor_branch_assignment dba ON doctor_records.doctor_id = dba.doctor_id " +
            "WHERE dba.branch_id = ?" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Doctor> doctors = new ArrayList<>();
                while (rs.next()) {
                    doctors.add(mapRow(rs));
                }
                return doctors;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to fetch doctors by branch", ex);
        }
    }

    @Override
    public List<Doctor> findByBranchAndSpecializations(int branchId, List<Integer> specializationIds) throws DaoException {
        if (specializationIds == null || specializationIds.isEmpty()) {
            return findByBranchId(branchId);
        }

        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("INNER JOIN doctor_branch_assignment dba ON doctor_records.doctor_id = dba.doctor_id ");
        sql.append("WHERE dba.branch_id = ? AND (");
        for (int i = 0; i < specializationIds.size(); i++) {
            if (i > 0) sql.append(" OR ");
            sql.append("JSON_CONTAINS(specializations_list, ?, '$')");
        }
        sql.append(")").append(ORDER_BY);

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setInt(1, branchId);
            for (int i = 0; i < specializationIds.size(); i++) {
                ps.setString(i + 2, String.valueOf(specializationIds.get(i)));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Doctor> doctors = new ArrayList<>();
                while (rs.next()) {
                    doctors.add(mapRow(rs));
                }
                return doctors;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to fetch doctors by branch and specializations", ex);
        }
    }

    @Override
    public List<Doctor> findByAvailabilityStatus(DoctorAvailabilityStatus status) throws DaoException {
        final String sql = BASE_SELECT + "WHERE availability_status = ?" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.toDatabaseValue());
            try (ResultSet rs = ps.executeQuery()) {
                List<Doctor> doctors = new ArrayList<>();
                while (rs.next()) {
                    doctors.add(mapRow(rs));
                }
                return doctors;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to fetch doctors by availability status", ex);
        }
    }

    @Override
    public List<Branch> findBranchesForDoctor(int doctorId) throws DaoException {
        final String sql = "SELECT b.branch_id, b.branch_name, b.address, b.latitude, b.longitude, " +
                "b.capacity, b.contact_number, b.created_at FROM branch_records b " +
                "INNER JOIN doctor_branch_assignment dba ON b.branch_id = dba.branch_id " +
                "WHERE dba.doctor_id = ? ORDER BY b.branch_name";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Branch> branches = new ArrayList<>();
                while (rs.next()) {
                    branches.add(mapBranch(rs));
                }
                return branches;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to fetch branches for doctor id=" + doctorId, ex);
        }
    }

    @Override
    public void updateBranchAssignments(int doctorId, List<Integer> branchIds) throws DaoException {
        final String deleteSql = "DELETE FROM doctor_branch_assignment WHERE doctor_id = ?";
        final String insertSql = "INSERT INTO doctor_branch_assignment (doctor_id, branch_id) VALUES (?, ?)";
        try (Connection connection = getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, doctorId);
                    deleteStmt.executeUpdate();
                }
                if (branchIds != null && !branchIds.isEmpty()) {
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        for (Integer branchId : branchIds) {
                            insertStmt.setInt(1, doctorId);
                            insertStmt.setInt(2, branchId);
                            insertStmt.addBatch();
                        }
                        insertStmt.executeBatch();
                    }
                }
                connection.commit();
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    throw translateException("Failed to rollback branch assignments for doctor id=" + doctorId, rollbackEx);
                }
                throw translateException("Failed to update branch assignments for doctor id=" + doctorId, ex);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException ex) {
            throw translateException("Failed to update branch assignments for doctor id=" + doctorId, ex);
        }
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        int doctorId = rs.getInt("doctor_id");
        String lastName = rs.getString("last_name");
        String firstName = rs.getString("first_name");
        String email = rs.getString("email");
        String specializationsJson = rs.getString("specializations_list");
        List<Integer> specializationIds = parseSpecializations(specializationsJson);
        String statusValue = rs.getString("availability_status");
        DoctorAvailabilityStatus status = DoctorAvailabilityStatus.fromDatabaseValue(statusValue);
        String availabilityRanges = rs.getString("availability_datetime_ranges");
        LocalDateTime createdAt = DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at"));
        Doctor doctor = new Doctor(doctorId, lastName, firstName, email, specializationIds, status, createdAt);
        doctor.setAvailabilityDatetimeRanges(availabilityRanges);
        return doctor;
    }

    private Branch mapBranch(ResultSet rs) throws SQLException {
        int id = rs.getInt("branch_id");
        String name = rs.getString("branch_name");
        String address = rs.getString("address");
        Double latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
        Double longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
        int capacity = rs.getInt("capacity");
        String contactNumber = rs.getString("contact_number");
        LocalDateTime createdAt = DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at"));
        return new Branch(id, name, address, latitude, longitude, capacity, contactNumber, createdAt);
    }

    private List<Integer> parseSpecializations(String jsonString) {
        List<Integer> ids = new ArrayList<>();
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return ids;
        }
        try {
            JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                ids.add(jsonArray.get(i).getAsInt());
            }
        } catch (Exception e) {
            System.err.println("Error parsing specializations JSON: " + e.getMessage());
        }
        return ids;
    }

    private String serializeSpecializations(List<Integer> specializationIds) {
        if (specializationIds == null || specializationIds.isEmpty()) {
            return "[]";
        }
        JsonArray jsonArray = new JsonArray();
        for (Integer id : specializationIds) {
            jsonArray.add(id);
        }
        return jsonArray.toString();
    }
}
