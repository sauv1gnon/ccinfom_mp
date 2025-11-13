package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.dao.UserDAO;
import com.ccinfoms17grp2.models.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service for user management operations
 */
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = Objects.requireNonNull(userDAO, "UserDAO is required");
    }

    /**
     * List all users
     * @return List of all users
     * @throws ValidationException if a database error occurs
     */
    public List<User> listUsers() {
        try {
            return userDAO.findAll();
        } catch (DaoException ex) {
            throw new ValidationException("Failed to retrieve users");
        }
    }

    /**
     * Get user by ID
     * @param userId the user ID
     * @return User object if found
     * @throws ValidationException if user not found or database error
     */
    public User getUser(int userId) {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID");
        }

        Optional<User> userOptional;
        try {
            userOptional = userDAO.findById(userId);
        } catch (DaoException ex) {
            throw new ValidationException("Database error while retrieving user");
        }

        return userOptional.orElseThrow(() -> 
            new ValidationException("User not found with ID: " + userId));
    }

    /**
     * Find user by email
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return userDAO.findByEmail(email.trim());
        } catch (DaoException ex) {
            throw new ValidationException("Database error while searching for user");
        }
    }

    /**
     * Find users by type
     * @param userType the user type
     * @return List of users of the specified type
     * @throws ValidationException if a database error occurs
     */
    public List<User> findUsersByType(User.UserType userType) {
        if (userType == null) {
            throw new ValidationException("User type is required");
        }

        try {
            return userDAO.findByUserType(userType);
        } catch (DaoException ex) {
            throw new ValidationException("Failed to retrieve users of type: " + userType);
        }
    }

    /**
     * Find users by person ID (patient_id or doctor_id)
     * @param personId the person ID
     * @return Optional containing the user if found
     */
    public Optional<User> findUserByPersonId(int personId) {
        if (personId <= 0) {
            return Optional.empty();
        }

        try {
            return userDAO.findByPersonId(personId);
        } catch (DaoException ex) {
            throw new ValidationException("Database error while searching for user");
        }
    }

    /**
     * Update user basic information (email, type, active status)
     * @param user the user object with updated information
     * @return Updated user object
     * @throws ValidationException if update fails
     */
    public User updateUser(User user) {
        if (user == null) {
            throw new ValidationException("User information is required");
        }
        if (user.getUserId() <= 0) {
            throw new ValidationException("Valid user ID is required");
        }

        validateUserData(user, false);

        try {
            boolean updated = userDAO.update(user);
            if (!updated) {
                throw new ValidationException("User could not be updated. It may have been removed by another user.");
            }
            Optional<User> refreshed = userDAO.findById(user.getUserId());
            return refreshed.orElse(user);
        } catch (DaoException ex) {
            throw new ValidationException("Database error during user update");
        }
    }

    /**
     * Update user email address
     * @param userId the user ID
     * @param newEmail the new email address
     * @return Updated user object
     * @throws ValidationException if update fails
     */
    public User updateEmail(int userId, String newEmail) {
        validateEmail(newEmail);

        Optional<User> userOptional;
        try {
            userOptional = userDAO.findById(userId);
        } catch (DaoException ex) {
            throw new ValidationException("User not found");
        }

        if (!userOptional.isPresent()) {
            throw new ValidationException("User not found with ID: " + userId);
        }

        User user = userOptional.get();

        // Check if email is already taken by another user
        try {
            Optional<User> existingUser = userDAO.findByEmail(newEmail);
            if (existingUser.isPresent() && existingUser.get().getUserId() != userId) {
                throw new ValidationException("Email address is already in use by another user");
            }
        } catch (DaoException ex) {
            throw new ValidationException("Database error while checking email availability");
        }

        // Update email
        user.setEmail(newEmail);
        return updateUser(user);
    }

    /**
     * Update user active status
     * @param userId the user ID
     * @param active the active status
     * @return true if update was successful
     * @throws ValidationException if update fails
     */
    public boolean updateActiveStatus(int userId, boolean active) {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID");
        }

        try {
            return userDAO.updateActiveStatus(userId, active);
        } catch (DaoException ex) {
            throw new ValidationException("Failed to update user active status");
        }
    }

    /**
     * Update last login time
     * @param userId the user ID
     * @param loginTime the login time
     * @return true if update was successful
     * @throws ValidationException if update fails
     */
    public boolean updateLastLogin(int userId, java.time.LocalDateTime loginTime) {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID");
        }
        if (loginTime == null) {
            throw new ValidationException("Login time is required");
        }

        try {
            return userDAO.updateLastLogin(userId, loginTime);
        } catch (DaoException ex) {
            throw new ValidationException("Failed to update last login time");
        }
    }

    /**
     * Delete user account
     * @param userId the user ID to delete
     * @throws ValidationException if deletion fails
     */
    public void deleteUser(int userId) {
        if (userId <= 0) {
            throw new ValidationException("Invalid user ID");
        }

        try {
            boolean deleted = userDAO.delete(userId);
            if (!deleted) {
                throw new ValidationException("User could not be deleted. It may have dependencies.");
            }
        } catch (DaoException ex) {
            throw new ValidationException("Failed to delete user");
        }
    }

    /**
     * Get users count by type
     * @param userType optional user type filter
     * @return Number of users
     */
    public long getUserCount(User.UserType userType) {
        try {
            if (userType == null) {
                return listUsers().size();
            } else {
                return findUsersByType(userType).size();
            }
        } catch (ValidationException ex) {
            return 0;
        }
    }

    private void validateUserData(User user, boolean requireId) {
        if (user == null) {
            throw new ValidationException("User information is required.");
        }
        if (requireId && user.getUserId() <= 0) {
            throw new ValidationException("User ID is invalid.");
        }
        validateEmail(user.getEmail());
        if (user.getUserType() == null) {
            throw new ValidationException("User type is required.");
        }
        if (user.getPersonId() <= 0) {
            throw new ValidationException("Valid person ID is required.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email address is required");
        }
        String trimmedEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new ValidationException("Invalid email address format");
        }
        if (trimmedEmail.length() > 255) {
            throw new ValidationException("Email address is too long");
        }
    }
}