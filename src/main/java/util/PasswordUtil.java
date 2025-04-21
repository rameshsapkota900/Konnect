package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    private static final int SALT_LENGTH = 16; // bytes, 16 bytes = 128 bits
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Generates a cryptographically strong random salt.
     * @return Base64 encoded salt string.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes the given password using the provided salt and SHA-256.
     * @param password The plain text password.
     * @param salt Base64 encoded salt string.
     * @return Hexadecimal representation of the hashed password, or null if hashing fails.
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            System.err.println("Password or salt cannot be null for hashing.");
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            // Decode the Base64 salt back to bytes
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            // Prepend or append salt to the password bytes before hashing
            // Prepending is common: salt + password
            md.update(saltBytes); // Add salt first
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8)); // Hash the password

            // Convert the byte array hash to a hexadecimal string for storage
            return bytesToHex(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 should always be available in standard Java environments
            System.err.println("FATAL ERROR: " + HASH_ALGORITHM + " algorithm not found.");
            throw new RuntimeException(HASH_ALGORITHM + " algorithm not found", e);
        } catch (IllegalArgumentException e) {
             System.err.println("Error decoding Base64 salt: " + e.getMessage());
             return null; // Handle invalid salt format
        }
    }

    /**
     * Verifies if the entered password matches the stored hash and salt.
     * @param enteredPassword The password entered by the user during login.
     * @param storedHash The hexadecimal hash string stored in the database.
     * @param storedSalt The Base64 encoded salt string stored in the database.
     * @return true if the password matches, false otherwise.
     */
    public static boolean verifyPassword(String enteredPassword, String storedHash, String storedSalt) {
         if (enteredPassword == null || storedHash == null || storedSalt == null) {
             return false; // Cannot verify with null inputs
         }
        // Hash the entered password using the stored salt
        String newHash = hashPassword(enteredPassword, storedSalt);
        // Compare the newly generated hash with the stored hash (case-insensitive comparison for hex)
        return newHash != null && newHash.equalsIgnoreCase(storedHash);
    }

    /**
     * Helper method to convert a byte array to its hexadecimal string representation.
     * @param bytes The byte array to convert.
     * @return The hexadecimal string.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            // %02x formats the byte as two hexadecimal digits, padding with 0 if necessary
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // --- Example Usage (for generating initial admin hash/salt) ---
    public static void main(String[] args) {
        String passwordToHash = "admin123"; // Choose a strong default password!
        String salt = generateSalt();
        String hash = hashPassword(passwordToHash, salt);

        System.out.println("--- Password Hashing Example ---");
        System.out.println("Password: " + passwordToHash);
        System.out.println("Generated Salt (Base64): " + salt);
        System.out.println("Generated Hash (Hex): " + hash);
        System.out.println("Verification Check (Correct): " + verifyPassword(passwordToHash, hash, salt));
        System.out.println("Verification Check (Incorrect): " + verifyPassword("wrongpassword", hash, salt));
        System.out.println("---------------------------------");
        System.out.println("Use the Salt and Hash below for the initial admin user INSERT statement:");
        System.out.println("Salt Value: " + salt);
        System.out.println("Hash Value: " + hash);
         System.out.println("\nExample SQL:");
         System.out.println("INSERT INTO users (email, password_hash, salt, role) VALUES ('admin@konnect.com', '" + hash + "', '" + salt + "', 'admin');");
    }
}