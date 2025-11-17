package com.ccinfoms17grp2.dao.impl;

import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.dao.QueueDAO;
import com.ccinfoms17grp2.models.Queue;
import com.ccinfoms17grp2.models.QueueStatus;
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

public class QueueJdbcDao extends AbstractJdbcDao implements QueueDAO {

    private static final String BASE_SELECT = "SELECT queue_id, patient_id, branch_id, queue_number, status, created_at FROM queue_records ";
    private static final String ORDER_BY = " ORDER BY created_at DESC";

    @Override
    public List<Queue> findAll() throws DaoException {
        final String sql = BASE_SELECT + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Queue> queues = new ArrayList<>();
            while (rs.next()) {
                queues.add(mapRow(rs));
            }
            return queues;
        } catch (SQLException ex) {
            throw translateException("Failed to fetch queues", ex);
        }
    }

    @Override
    public Optional<Queue> findById(Integer id) throws DaoException {
        final String sql = BASE_SELECT + "WHERE queue_id = ?";
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
            throw translateException("Failed to find queue with id=" + id, ex);
        }
    }

    @Override
    public Queue create(Queue queue) throws DaoException {
        final String sql = "INSERT INTO queue_records (patient_id, branch_id, queue_number, status) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, queue.getPatientId());
            statement.setInt(2, queue.getBranchId());
            statement.setInt(3, queue.getQueueNumber());
            statement.setString(4, queue.getStatus().getValue());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    queue.setQueueId(generatedId);
                }
            }
            return findById(queue.getQueueId()).orElse(queue);
        } catch (SQLException ex) {
            throw translateException("Failed to create queue", ex);
        }
    }

    @Override
    public boolean update(Queue queue) throws DaoException {
        final String sql = "UPDATE queue_records SET patient_id = ?, branch_id = ?, queue_number = ?, status = ? WHERE queue_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, queue.getPatientId());
            statement.setInt(2, queue.getBranchId());
            statement.setInt(3, queue.getQueueNumber());
            statement.setString(4, queue.getStatus().getValue());
            statement.setInt(5, queue.getQueueId());
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update queue with id=" + queue.getQueueId(), ex);
        }
    }

    @Override
    public boolean delete(Integer id) throws DaoException {
        final String sql = "DELETE FROM queue_records WHERE queue_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to delete queue with id=" + id, ex);
        }
    }

    @Override
    public List<Queue> findByBranchId(int branchId) throws DaoException {
        final String sql = BASE_SELECT + "WHERE branch_id = ?" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Queue> queues = new ArrayList<>();
                while (rs.next()) {
                    queues.add(mapRow(rs));
                }
                return queues;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find queues for branch id=" + branchId, ex);
        }
    }

    @Override
    public List<Queue> findByPatientId(int patientId) throws DaoException {
        final String sql = BASE_SELECT + "WHERE patient_id = ?" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, patientId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Queue> queues = new ArrayList<>();
                while (rs.next()) {
                    queues.add(mapRow(rs));
                }
                return queues;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find queues for patient id=" + patientId, ex);
        }
    }

    @Override
    public List<Queue> findByBranchIdAndStatus(int branchId, String status) throws DaoException {
        final String sql = BASE_SELECT + "WHERE branch_id = ? AND status = ?" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setString(2, status);
            try (ResultSet rs = statement.executeQuery()) {
                List<Queue> queues = new ArrayList<>();
                while (rs.next()) {
                    queues.add(mapRow(rs));
                }
                return queues;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find queues for branch id=" + branchId + " and status=" + status, ex);
        }
    }

    @Override
    public int getNextQueueNumber(int branchId) throws DaoException {
        final String sql = "SELECT COALESCE(MAX(queue_number), 0) + 1 AS next_number FROM queue_records WHERE branch_id = ? AND DATE(created_at) = CURDATE()";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("next_number");
                }
                return 1;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to get next queue number for branch id=" + branchId, ex);
        }
    }

    @Override
    public List<Queue> findTodaysQueueByBranch(int branchId) throws DaoException {
        final String sql = BASE_SELECT + "WHERE branch_id = ? AND DATE(created_at) = CURDATE()" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Queue> queues = new ArrayList<>();
                while (rs.next()) {
                    queues.add(mapRow(rs));
                }
                return queues;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find today's queues for branch id=" + branchId, ex);
        }
    }
    
    @Override
    public List<Queue> findTodaysQueue() throws DaoException {
        final String sql = BASE_SELECT + "WHERE DATE(created_at) = CURDATE()" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Queue> queues = new ArrayList<>();
            while (rs.next()) {
                queues.add(mapRow(rs));
            }
            return queues;
        } catch (SQLException ex) {
            throw translateException("Failed to find today's queues", ex);
        }
    }

    private Queue mapRow(ResultSet rs) throws SQLException {
        int queueId = rs.getInt("queue_id");
        int patientId = rs.getInt("patient_id");
        int branchId = rs.getInt("branch_id");
        int queueNumber = rs.getInt("queue_number");
        String statusValue = rs.getString("status");
        QueueStatus status = QueueStatus.fromValue(statusValue);
        LocalDateTime createdAt = DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at"));
        return new Queue(queueId, patientId, branchId, queueNumber, status, createdAt);
    }
}
