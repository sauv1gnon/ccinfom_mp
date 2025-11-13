package com.ccinfoms17grp2.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for password hashing and validation using SHA256
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final String HEX_PREFIX = "0x";
    
    private PasswordUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Hash a password using SHA256 with salt
     * @param password the plain text password
     * @return the hexadecimal representation of the hash
     * @throws RuntimeException if hashing algorithm is not available
     */
    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            // Generate a random salt (16 bytes)
            byte[] salt = generateSalt();
            
            // Combine password and salt
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            byte[] saltedPassword = new byte[passwordBytes.length + salt.length];
            System.arraycopy(passwordBytes, 0, saltedPassword, 0, passwordBytes.length);
            System.arraycopy(salt, 0, saltedPassword, passwordBytes.length, salt.length);
            
            // Hash the salted password
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(saltedPassword);
            
            // Combine salt and hash for storage
            byte[] saltedHash = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, saltedHash, 0, salt.length);
            System.arraycopy(hash, 0, saltedHash, salt.length, hash.length);
            
            // Convert to hexadecimal string
            return bytesToHex(saltedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verify a password against its hash
     * @param password the plain text password to verify
     * @param hash the stored hash (including salt)
     * @return true if password matches the hash
     */
    public static boolean verifyPassword(String password, String hash) {
        if (password == null || password.trim().isEmpty() || 
            hash == null || hash.trim().isEmpty()) {
            return false;
        }

        try {
            // Convert hex string back to bytes
            byte[] saltedHash = hexToBytes(hash);
            if (saltedHash.length < 16) { // Minimum salt size
                return false;
            }

            // Extract salt (first 16 bytes)
            byte[] salt = new byte[16];
            System.arraycopy(saltedHash, 0, salt, 0, 16);

            // Extract hash (remaining bytes)
            byte[] storedHash = new byte[saltedHash.length - 16];
            System.arraycopy(saltedHash, 16, storedHash, 0, storedHash.length);

            // Hash the input password with the extracted salt
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            byte[] saltedPassword = new byte[passwordBytes.length + salt.length];
            System.arraycopy(passwordBytes, 0, saltedPassword, 0, passwordBytes.length);
            System.arraycopy(salt, 0, saltedPassword, passwordBytes.length, salt.length);

            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] computedHash = digest.digest(saltedPassword);

            // Compare the hashes
            return MessageDigest.isEqual(storedHash, computedHash);
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Password verification error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate a simple SHA256 hash without salt (for backward compatibility)
     * @param password the plain text password
     * @return the hexadecimal representation of the hash
     * @throws RuntimeException if hashing algorithm is not available
     * @deprecated Use hashPassword(String) instead for better security
     */
    @Deprecated
    public static String simpleHash(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Convert byte array to hexadecimal string
     * @param bytes the byte array to convert
     * @return hexadecimal string representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Convert hexadecimal string to byte array
     * @param hexString the hexadecimal string to convert
     * @return byte array representation
     * @throws IllegalArgumentException if hex string is invalid
     */
    private static byte[] hexToBytes(String hexString) {
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Hexadecimal string must have even length");
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
        }
        return bytes;
    }

    /**
     * Generate a random salt for password hashing
     * @return byte array containing random salt
     */
    private static byte[] generateSalt() {
        byte[] salt = new byte[16]; // 128 bits
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Check if a password hash follows the expected format
     * @param hash the hash to validate
     * @return true if hash format is valid
     */
    public static boolean isValidHashFormat(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return false;
        }
        // Check if it's a valid hex string and minimum length for salted hash
        try {
            byte[] bytes = hexToBytes(hash);
            return bytes.length >= 32; // Minimum 256 bits for SHA256 + 128 bits for salt
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}