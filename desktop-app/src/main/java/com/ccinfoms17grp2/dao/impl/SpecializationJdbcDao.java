package com.ccinfoms17grp2.dao.impl;

import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.dao.SpecializationDAO;
import com.ccinfoms17grp2.models.Specialization;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpecializationJdbcDao extends AbstractJdbcDao implements SpecializationDAO {

    private static final String BASE_SELECT = "SELECT specialization_id, specialization_name, specialization_code FROM Specialization ";
    private static final String ORDER_BY = " ORDER BY specialization_name";

    @Override
    public List<Specialization> findAll() throws DaoException {
        final String sql = BASE_SELECT + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Specialization> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw translateException("Failed to fetch specializations", ex);
        }
    }

    @Override
    public Optional<Specialization> findById(Integer id) throws DaoException {
        final String sql = BASE_SELECT + "WHERE specialization_id = ?";
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
            throw translateException("Failed to find specialization with id=" + id, ex);
        }
    }

    @Override
    public Specialization create(Specialization specialization) throws DaoException {
        final String sql = "INSERT INTO Specialization (specialization_name, specialization_code) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, specialization.getSpecializationName());
            ps.setString(2, specialization.getSpecializationCode());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    specialization.setSpecializationId(keys.getInt(1));
                }
            }
            return findById(specialization.getSpecializationId()).orElse(specialization);
        } catch (SQLException ex) {
            throw translateException("Failed to create specialization", ex);
        }
    }

    @Override
    public boolean update(Specialization specialization) throws DaoException {
        final String sql = "UPDATE Specialization SET specialization_name = ?, specialization_code = ? WHERE specialization_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, specialization.getSpecializationName());
            ps.setString(2, specialization.getSpecializationCode());
            ps.setInt(3, specialization.getSpecializationId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update specialization with id=" + specialization.getSpecializationId(), ex);
        }
    }

    @Override
    public boolean delete(Integer id) throws DaoException {
        final String sql = "DELETE FROM Specialization WHERE specialization_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to delete specialization with id=" + id, ex);
        }
    }

    @Override
    public boolean existsByCode(String code) throws DaoException {
        final String sql = "SELECT 1 FROM Specialization WHERE specialization_code = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw translateException("Failed to check specialization code", ex);
        }
    }

    private Specialization mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("specialization_id");
        String name = rs.getString("specialization_name");
        String code = rs.getString("specialization_code");
        return new Specialization(id, name, code);
    }
}
