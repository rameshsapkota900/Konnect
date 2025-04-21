package dao;

import model.User;
import util.DBUtil;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Registers a new user (Creator or Business) in the database.
     * Hashes the password using PasswordUtil.
     * @param email User's email (must be unique).
     * @param password User's plain text password.
     * @param role User's role ("creator" or "business").
     * @return The generated user_id if successful, -1 otherwise.
     */
    public int registerUser(String email, String password, String role) {
        // 1. Check if email already exists
        if (findUserByEmail(email) != null) {
             System.err.println("Registration failed: Email already exists - " + email);
             return -1; // Indicate failure due to existing email
        }

        // 2. Generate salt and hash password
        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(password, salt);
        if (hashedPassword == null) {
             System.err.println("Registration failed: Password hashing error for email " + email);
             return -1; // Indicate hashing failure
        }

        // 3. Insert into database
        String sql = "INSERT INTO users (email, password_hash, salt, role) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        int userId = -1;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); // Request generated keys

            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, salt);
            pstmt.setString(4, role);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Retrieve the generated user_id
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1);
                    System.out.println("User registered successfully: Email=" + email + ", Role=" + role + ", UserID=" + userId);
                } else {
                     System.err.println("Registration succeeded but failed to retrieve generated user ID for email: " + email);
                     // This case is problematic, might need manual check or rollback logic in a real app
                     userId = -1; // Indicate failure to get ID
                }
            } else {
                 System.err.println("Registration failed: No rows affected for email: " + email);
                 userId = -1;
            }

        } catch (SQLException e) {
            System.err.println("SQL Error registering user '" + email + "': " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            userId = -1;
        } finally {
            DBUtil.closeResultSet(generatedKeys); // Close generatedKeys ResultSet
            DBUtil.closeResources(pstmt, conn);   // Close statement and connection
        }
        return userId; // Return the generated ID or -1 on failure
    }

    /**
     * Finds a user by their email address.
     * @param email The email to search for.
     * @return User object if found, null otherwise.
     */
    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUser(rs);
                 // System.out.println("User found by email: " + email); // Debugging
            } else {
                // System.out.println("User not found by email: " + email); // Debugging
            }
        } catch (SQLException e) {
             System.err.println("SQL Error finding user by email '" + email + "': " + e.getMessage());
             e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return user;
    }

     /**
      * Finds a user by their unique user ID.
      * @param userId The ID of the user to find.
      * @return User object if found, null otherwise.
      */
    public User findUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUser(rs);
                 // System.out.println("User found by ID: " + userId); // Debugging
            } else {
                 // System.out.println("User not found by ID: " + userId); // Debugging
            }
        } catch (SQLException e) {
             System.err.println("SQL Error finding user by ID " + userId + ": " + e.getMessage());
             e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return user;
    }


    /**
     * Validates user login credentials.
     * Fetches the user by email and verifies the password using PasswordUtil.
     * Checks if the user is banned.
     * @param email User's email.
     * @param password User's plain text password.
     * @return User object if login is valid and user is not banned, null otherwise.
     */
    public User validateLogin(String email, String password) {
        User user = findUserByEmail(email);

        if (user == null) {
             System.out.println("Login validation failed: User not found for email " + email);
            return null; // User not found
        }

        if (user.isBanned()) {
             System.out.println("Login validation failed: User is banned. Email: " + email);
            return null; // User is banned
        }

        // Verify the password
        if (PasswordUtil.verifyPassword(password, user.getPasswordHash(), user.getSalt())) {
             System.out.println("Login validation successful for email: " + email);
            return user; // Password matches
        } else {
             System.out.println("Login validation failed: Incorrect password for email " + email);
            return null; // Password incorrect
        }
    }

    /**
     * Retrieves a list of all users from the database.
     * Primarily used by the Admin.
     * @return List of User objects, or an empty list if none found or on error.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        // Exclude the admin performing the action? Maybe not necessary here.
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null; // Use PreparedStatement even without parameters for consistency
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
             System.out.println("Retrieved " + users.size() + " users from database.");
        } catch (SQLException e) {
             System.err.println("SQL Error getting all users: " + e.getMessage());
              e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return users;
    }

    /**
     * Updates the ban status of a specific user.
     * @param userId The ID of the user to update.
     * @param isBanned true to ban the user, false to unban.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateUserBanStatus(int userId, boolean isBanned) {
        String sql = "UPDATE users SET is_banned = ? WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, isBanned);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if(success) {
                System.out.println("Successfully updated ban status for user ID " + userId + " to " + isBanned);
            } else {
                 System.err.println("Failed to update ban status for user ID " + userId + ". User might not exist.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error updating ban status for user ID " + userId + ": " + e.getMessage());
             e.printStackTrace();
             success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

     /**
      * Deletes a user and associated profile data (uses database CASCADE DELETE).
      * Be very careful using this!
      * @param userId The ID of the user to delete.
      * @return true if deletion was successful, false otherwise.
      */
     public boolean deleteUser(int userId) {
         String sql = "DELETE FROM users WHERE user_id = ?";
         Connection conn = null;
         PreparedStatement pstmt = null;
         boolean success = false;

         // Optional: Add checks - e.g., don't allow deleting the last admin.

         try {
             conn = DBUtil.getConnection();
             // Ensure cascading delete is set up correctly in the DB schema
             // for creators, businesses, campaigns, applications, messages, reports tables.
             pstmt = conn.prepareStatement(sql);
             pstmt.setInt(1, userId);

             int rowsAffected = pstmt.executeUpdate();
             success = (rowsAffected > 0);

             if (success) {
                 System.out.println("Successfully deleted user ID: " + userId);
             } else {
                 System.err.println("Failed to delete user ID: " + userId + ". User might not exist.");
             }
         } catch (SQLException e) {
             System.err.println("SQL Error deleting user ID " + userId + ": " + e.getMessage());
             e.printStackTrace();
             success = false;
         } finally {
             DBUtil.closeResources(pstmt, conn);
         }
         return success;
     }


    /**
     * Helper method to map a row from the ResultSet to a User object.
     * @param rs The ResultSet containing user data.
     * @return A populated User object.
     * @throws SQLException If a database access error occurs.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("salt"),
                rs.getString("role"),
                rs.getBoolean("is_banned"),
                rs.getTimestamp("created_at")
        );
    }

    // --- Add other potential methods as needed ---
    // Example: updateUserPassword(int userId, String newPassword)
    // Example: countUsersByRole() for admin reports
    // Example: getUsersForChat(int currentUserId) - fetch users excluding self

    /**
     * Counts the total number of registered users.
     * @return The count of users, or -1 on error.
     */
    public long countTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long count = -1;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error counting total users: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return count;
    }

    /**
     * Counts users by their role.
     * @param role The role to count ("admin", "creator", "business").
     * @return The count for that role, or -1 on error.
     */
     public long countUsersByRole(String role) {
         String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         long count = -1;

         try {
             conn = DBUtil.getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setString(1, role);
             rs = pstmt.executeQuery();
             if (rs.next()) {
                 count = rs.getLong(1);
             }
         } catch (SQLException e) {
             System.err.println("SQL Error counting users by role '" + role + "': " + e.getMessage());
             e.printStackTrace();
         } finally {
             DBUtil.closeResources(rs, pstmt, conn);
         }
         return count;
     }
}