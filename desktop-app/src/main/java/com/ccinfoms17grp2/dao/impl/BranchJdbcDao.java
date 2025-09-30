package com.ccinfoms17grp2.dao.impl;

import com.ccinfoms17grp2.dao.BranchDAO;
import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.models.Branch;
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

public class BranchJdbcDao extends AbstractJdbcDao implements BranchDAO {

    private static final String BASE_SELECT = "SELECT branch_id, branch_name, address, capacity, created_at FROM Branch ";
    private static final String ORDER_BY = " ORDER BY branch_name";

    @Override
    public List<Branch> findAll() throws DaoException {
        final String sql = BASE_SELECT + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Branch> branches = new ArrayList<>();
            while (rs.next()) {
                branches.add(mapRow(rs));
            }
            return branches;
        } catch (SQLException ex) {
            throw translateException("Failed to fetch branches", ex);
        }
    }

    @Override
    public Optional<Branch> findById(Integer id) throws DaoException {
        final String sql = BASE_SELECT + "WHERE branch_id = ?";
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
            throw translateException("Failed to find branch with id=" + id, ex);
        }
    }

    @Override
    public Branch create(Branch branch) throws DaoException {
        final String sql = "INSERT INTO Branch (branch_name, address, capacity) VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, branch.getBranchName());
            ps.setString(2, branch.getAddress());
            ps.setInt(3, branch.getCapacity());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    branch.setBranchId(keys.getInt(1));
                }
            }
            return findById(branch.getBranchId()).orElse(branch);
        } catch (SQLException ex) {
            throw translateException("Failed to create branch", ex);
        }
    }

    @Override
    public boolean update(Branch branch) throws DaoException {
        final String sql = "UPDATE Branch SET branch_name = ?, address = ?, capacity = ? WHERE branch_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, branch.getBranchName());
            ps.setString(2, branch.getAddress());
            ps.setInt(3, branch.getCapacity());
            ps.setInt(4, branch.getBranchId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update branch with id=" + branch.getBranchId(), ex);
        }
    }

    @Override
    public boolean delete(Integer id) throws DaoException {
        final String sql = "DELETE FROM Branch WHERE branch_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to delete branch with id=" + id, ex);
        }
    }

    @Override
    public boolean existsByName(String name) throws DaoException {
        final String sql = "SELECT 1 FROM Branch WHERE branch_name = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw translateException("Failed to check branch name", ex);
        }
    }

    private Branch mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("branch_id");
        String name = rs.getString("branch_name");
        String address = rs.getString("address");
        int capacity = rs.getInt("capacity");
        LocalDateTime createdAt = DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at"));
        return new Branch(id, name, address, capacity, createdAt);
    }
}
