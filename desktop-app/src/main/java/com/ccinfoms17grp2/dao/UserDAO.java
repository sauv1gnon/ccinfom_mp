package com.ccinfoms17grp2.dao;

import com.ccinfoms17grp2.models.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entities
 */
public interface UserDAO extends CrudRepository<User, Integer> {

    /**
     * Find user by email address
     * @param email the email to search for
     * @return Optional containing the user if found
     * @throws DaoException if a database error occurs
     */
    Optional<User> findByEmail(String email) throws DaoException;

    /**
     * Find user by email and type (for security)
     * @param email the email to search for
     * @param userType the user type
     * @return Optional containing the user if found
     * @throws DaoException if a database error occurs
     */
    Optional<User> findByEmailAndType(String email, User.UserType userType) throws DaoException;

    /**
     * Find users by type
     * @param userType the user type to filter by
     * @return List of users of the specified type
     * @throws DaoException if a database error occurs
     */
    List<User> findByUserType(User.UserType userType) throws DaoException;

    /**
     * Find users by person ID (patient_id or doctor_id)
     * @param personId the person ID to search for
     * @return Optional containing the user if found
     * @throws DaoException if a database error occurs
     */
    Optional<User> findByPersonId(int personId) throws DaoException;

    /**
     * Update last login timestamp
     * @param userId the user ID
     * @param loginTime the login timestamp
     * @return true if update was successful
     * @throws DaoException if a database error occurs
     */
    boolean updateLastLogin(int userId, java.time.LocalDateTime loginTime) throws DaoException;

    /**
     * Activate or deactivate user account
     * @param userId the user ID
     * @param active the active status
     * @return true if update was successful
     * @throws DaoException if a database error occurs
     */
    boolean updateActiveStatus(int userId, boolean active) throws DaoException;

    /**
     * Update password hash
     * @param userId the user ID
     * @param newPasswordHash the new password hash
     * @return true if update was successful
     * @throws DaoException if a database error occurs
     */
    boolean updatePassword(int userId, String newPasswordHash) throws DaoException;
}