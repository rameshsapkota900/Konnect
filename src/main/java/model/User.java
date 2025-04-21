package model;

import java.sql.Timestamp;
import java.util.Objects;

public class User {
    private int userId;
    private String email;
    private String passwordHash; // Never expose this via getters in sensitive contexts if possible
    private String salt;         // Never expose this either
    private String role; // "admin", "creator", "business"
    private boolean isBanned;
    private Timestamp createdAt;

    // Enum for Roles for better type safety (Optional but recommended)
    public enum Role {
        ADMIN("admin"),
        CREATOR("creator"),
        BUSINESS("business");

        private final String roleName;

        Role(String roleName) {
            this.roleName = roleName;
        }

        public String getRoleName() {
            return roleName;
        }

        public static Role fromString(String text) {
            for (Role r : Role.values()) {
                if (r.roleName.equalsIgnoreCase(text)) {
                    return r;
                }
            }
            return null; // Or throw an exception for unknown roles
        }
    }

    // Constructors
    public User() {}

    public User(int userId, String email, String passwordHash, String salt, String role, boolean isBanned, Timestamp createdAt) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        // Basic validation for role string
        if (role != null && (role.equals("admin") || role.equals("creator") || role.equals("business"))) {
             this.role = role;
        } else {
             this.role = null; // Or throw exception for invalid role?
             System.err.println("Warning: Invalid role string set for User ID " + userId + ": " + role);
        }
        this.isBanned = isBanned;
        this.createdAt = createdAt;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; } // Use with caution
    public String getSalt() { return salt; }                 // Use with caution
    public String getRole() { return role; }
    public boolean isBanned() { return isBanned; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setSalt(String salt) { this.salt = salt; }
    public void setRole(String role) {
         // Optional: Add validation here too
         if (role != null && (role.equals("admin") || role.equals("creator") || role.equals("business"))) {
             this.role = role;
         } else {
              System.err.println("Warning: Attempted to set invalid role: " + role);
              // Decide: Keep old value, set to null, or throw exception?
              // this.role = null; // Example: Set to null
         }
    }
    public void setBanned(boolean banned) { isBanned = banned; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // --- Utility methods ---
    public boolean isAdmin() {
        return Role.ADMIN.getRoleName().equalsIgnoreCase(this.role);
    }
    public boolean isCreator() {
        return Role.CREATOR.getRoleName().equalsIgnoreCase(this.role);
    }
     public boolean isBusiness() {
        return Role.BUSINESS.getRoleName().equalsIgnoreCase(this.role);
    }


    // toString, equals, hashCode
    @Override
    public String toString() {
        return "User{" +
               "userId=" + userId +
               ", email='" + email + '\'' +
               // DO NOT include passwordHash or salt in standard toString for security logs
               ", role='" + role + '\'' +
               ", isBanned=" + isBanned +
               ", createdAt=" + createdAt +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId &&
               isBanned == user.isBanned &&
               Objects.equals(email, user.email) &&
               Objects.equals(role, user.role); // Don't compare hash/salt/timestamp for logical equality
    }

    @Override
    public int hashCode() {
        // Use fields that define logical equality
        return Objects.hash(userId, email, role, isBanned);
    }
}
