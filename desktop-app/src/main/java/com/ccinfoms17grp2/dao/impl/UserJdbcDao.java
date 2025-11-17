package com.ccinfoms17grp2.dao.impl;

import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.dao.UserDAO;
import com.ccinfoms17grp2.models.User;
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

/**
 * JDBC implementation of UserDAO
 */
public class UserJdbcDao extends AbstractJdbcDao implements UserDAO {

    private static final String BASE_SELECT = "SELECT user_id, email, password_hash, user_type, person_id, is_active, last_login_at, created_at, updated_at FROM users ";
    private static final String ORDER_BY = " ORDER BY email ASC";

    @Override
    public List<User> findAll() throws DaoException {
        final String sql = BASE_SELECT + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (SQLException ex) {
            throw translateException("Failed to fetch users", ex);
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws DaoException {
        final String sql = BASE_SELECT + "WHERE user_id = ?";
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
            throw translateException("Failed to find user with id=" + id, ex);
        }
    }

    @Override
    public User create(User user) throws DaoException {
        final String sql = "INSERT INTO users (email, password_hash, user_type, person_id, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getUserType().name());
            statement.setInt(4, user.getPersonId());
            statement.setBoolean(5, user.isActive());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    user.setUserId(generatedId);
                }
            }
            return findById(user.getUserId()).orElse(user);
        } catch (SQLException ex) {
            throw translateException("Failed to create user", ex);
        }
    }

    @Override
    public boolean update(User user) throws DaoException {
        final String sql = "UPDATE users SET email = ?, password_hash = ?, user_type = ?, person_id = ?, is_active = ? WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getUserType().name());
            statement.setInt(4, user.getPersonId());
            statement.setBoolean(5, user.isActive());
            statement.setInt(6, user.getUserId());
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update user with id=" + user.getUserId(), ex);
        }
    }

    @Override
    public boolean delete(Integer id) throws DaoException {
        final String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to delete user with id=" + id, ex);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DaoException {
        final String sql = BASE_SELECT + "WHERE email = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find user with email=" + email, ex);
        }
    }

    @Override
    public Optional<User> findByEmailAndType(String email, User.UserType userType) throws DaoException {
        final String sql = BASE_SELECT + "WHERE email = ? AND user_type = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, userType.name());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find user with email=" + email + " and type=" + userType, ex);
        }
    }

    @Override
    public List<User> findByUserType(User.UserType userType) throws DaoException {
        final String sql = BASE_SELECT + "WHERE user_type = ?" + ORDER_BY;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userType.name());
            try (ResultSet rs = statement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
                return users;
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find users with type=" + userType, ex);
        }
    }

    @Override
    public Optional<User> findByPersonId(int personId) throws DaoException {
        final String sql = BASE_SELECT + "WHERE person_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, personId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw translateException("Failed to find user with person_id=" + personId, ex);
        }
    }

    @Override
    public boolean updateLastLogin(int userId, LocalDateTime loginTime) throws DaoException {
        final String sql = "UPDATE users SET last_login_at = ? WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, java.sql.Timestamp.valueOf(loginTime));
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update last login for user_id=" + userId, ex);
        }
    }

    @Override
    public boolean updateActiveStatus(int userId, boolean active) throws DaoException {
        final String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update active status for user_id=" + userId, ex);
        }
    }

    @Override
    public boolean updatePassword(int userId, String newPasswordHash) throws DaoException {
        final String sql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPasswordHash);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw translateException("Failed to update password for user_id=" + userId, ex);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        User.UserType userType = User.UserType.valueOf(rs.getString("user_type"));
        int personId = rs.getInt("person_id");
        boolean isActive = rs.getBoolean("is_active");
        LocalDateTime lastLoginAt = rs.getTimestamp("last_login_at") != null ? 
            DateTimeUtil.fromTimestamp(rs.getTimestamp("last_login_at")) : null;
        LocalDateTime createdAt = rs.getTimestamp("created_at") != null ? 
            DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")) : null;
        LocalDateTime updatedAt = rs.getTimestamp("updated_at") != null ? 
            DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at")) : null;

        return new User(userId, email, passwordHash, userType, personId, 
                       isActive, lastLoginAt, createdAt, updatedAt);
    }
}