package com.ccinfoms17grp2.services;

import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.dao.UserDAO;
import com.ccinfoms17grp2.models.User;
import com.ccinfoms17grp2.utils.PasswordUtil;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Authentication Service for handling login, password validation, and security
 */
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = Objects.requireNonNull(userDAO, "UserDAO is required");
    }

    /**
     * Authenticate user with email and password
     * @param email the user's email
     * @param password the plain text password
     * @return User object if authentication successful
     * @throws AuthenticationException if authentication fails
     */
    public User authenticate(String email, String password) {
        validateEmail(email);
        validatePassword(password);

        Optional<User> userOptional;
        try {
            userOptional = userDAO.findByEmail(email);
        } catch (DaoException ex) {
            throw new AuthenticationException("Database error during authentication", ex);
        }

        if (!userOptional.isPresent()) {
            throw new AuthenticationException("Invalid email or password");
        }

        User user = userOptional.get();

        if (!user.isActive()) {
            throw new AuthenticationException("Account is deactivated. Please contact support.");
        }

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }

        try {
            userDAO.updateLastLogin(user.getUserId(), LocalDateTime.now());
        } catch (DaoException ex) {
            System.err.println("Failed to update last login time: " + ex.getMessage());
        }

        return user;
    }

    /**
     * Register a new patient user
     * @param email the email address
     * @param password the plain text password
     * @param personId the patient_id from patient_records
     * @return created User object
     * @throws AuthenticationException if registration fails
     */
    public User registerPatient(String email, String password, int personId) {
        return registerUser(email, password, User.UserType.PATIENT, personId);
    }

    /**
     * Register a new doctor user
     * @param email the email address
     * @param password the plain text password
     * @param personId the doctor_id from doctor_records
     * @return created User object
     * @throws AuthenticationException if registration fails
     */
    public User registerDoctor(String email, String password, int personId) {
        return registerUser(email, password, User.UserType.DOCTOR, personId);
    }

    /**
     * Change user password
     * @param userId the user ID
     * @param currentPassword the current password
     * @param newPassword the new password
     * @throws AuthenticationException if password change fails
     */
    public void changePassword(int userId, String currentPassword, String newPassword) {
        validatePassword(newPassword);

        Optional<User> userOptional;
        try {
            userOptional = userDAO.findById(userId);
        } catch (DaoException ex) {
            throw new AuthenticationException("User not found", ex);
        }

        if (!userOptional.isPresent()) {
            throw new AuthenticationException("User not found");
        }

        User user = userOptional.get();

        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        String newPasswordHash = PasswordUtil.hashPassword(newPassword);
        try {
            boolean updated = userDAO.updatePassword(userId, newPasswordHash);
            if (!updated) {
                throw new AuthenticationException("Failed to update password");
            }
        } catch (DaoException ex) {
            throw new AuthenticationException("Database error during password update", ex);
        }
    }

    /**
     * Reset password using token
     * @param token the reset token
     * @param newPassword the new password
     * @throws AuthenticationException if reset fails
     */
    public void resetPassword(String token, String newPassword) {
        validatePassword(newPassword);
        throw new UnsupportedOperationException("Password reset functionality not implemented yet");
    }

    /**
     * Activate user account
     * @param userId the user ID
     * @throws AuthenticationException if activation fails
     */
    public void activateAccount(int userId) {
        try {
            boolean updated = userDAO.updateActiveStatus(userId, true);
            if (!updated) {
                throw new AuthenticationException("Failed to activate account");
            }
        } catch (DaoException ex) {
            throw new AuthenticationException("Database error during account activation", ex);
        }
    }

    /**
     * Deactivate user account
     * @param userId the user ID
     * @throws AuthenticationException if deactivation fails
     */
    public void deactivateAccount(int userId) {
        try {
            boolean updated = userDAO.updateActiveStatus(userId, false);
            if (!updated) {
                throw new AuthenticationException("Failed to deactivate account");
            }
        } catch (DaoException ex) {
            throw new AuthenticationException("Database error during account deactivation", ex);
        }
    }

    /**
     * Find user by email
     * @param email the email to search for
     * @return Optional containing the user if found
     * @throws AuthenticationException if database error occurs
     */
    public Optional<User> findUserByEmail(String email) {
        try {
            return userDAO.findByEmail(email);
        } catch (DaoException ex) {
            throw new AuthenticationException("Database error while searching for user", ex);
        }
    }

    private User registerUser(String email, String password, User.UserType userType, int personId) {
        validateEmail(email);
        validatePassword(password);

        if (personId <= 0) {
            throw new AuthenticationException("Invalid person ID");
        }

        try {
            Optional<User> existingUser = userDAO.findByEmail(email);
            if (existingUser.isPresent()) {
                throw new AuthenticationException("Email address is already registered");
            }

            Optional<User> existingPersonUser = userDAO.findByPersonId(personId);
            if (existingPersonUser.isPresent()) {
                throw new AuthenticationException("This person already has a user account");
            }
        } catch (DaoException ex) {
            throw new AuthenticationException("Database error during registration", ex);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setUserType(userType);
        user.setPersonId(personId);
        user.setActive(true);

        try {
            return userDAO.create(user);
        } catch (DaoException ex) {
            throw new AuthenticationException("Failed to create user account", ex);
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new AuthenticationException("Email address is required");
        }
        String trimmedEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new AuthenticationException("Invalid email address format");
        }
        if (trimmedEmail.length() > 255) {
            throw new AuthenticationException("Email address is too long");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Password is required");
        }
        if (password.length() < 6) {
            throw new AuthenticationException("Password must be at least 6 characters long");
        }
        if (password.length() > 128) {
            throw new AuthenticationException("Password is too long");
        }
    }
}