package model;

import java.sql.Timestamp;
import java.util.Objects;

public class Business {
    private int userId; // Foreign Key linking to User table
    private String companyName;
    private String website;
    private String industry;
    private String description;
    private Timestamp profileUpdatedAt;

    // Link to the User object (optional)
    private User user;

    // Constructors
    public Business() {}

    public Business(int userId, String companyName, String website, String industry, String description, Timestamp profileUpdatedAt) {
        this.userId = userId;
        this.companyName = companyName;
        this.website = website;
        this.industry = industry;
        this.description = description;
        this.profileUpdatedAt = profileUpdatedAt;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getCompanyName() { return companyName; }
    public String getWebsite() { return website; }
    public String getIndustry() { return industry; }
    public String getDescription() { return description; }
    public Timestamp getProfileUpdatedAt() { return profileUpdatedAt; }
    public User getUser() { return user; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setWebsite(String website) { this.website = website; }
    public void setIndustry(String industry) { this.industry = industry; }
    public void setDescription(String description) { this.description = description; }
    public void setProfileUpdatedAt(Timestamp profileUpdatedAt) { this.profileUpdatedAt = profileUpdatedAt; }
    public void setUser(User user) { this.user = user; }

    // toString, equals, hashCode
    @Override
    public String toString() {
        return "Business{" +
               "userId=" + userId +
               ", companyName='" + companyName + '\'' +
               ", website='" + website + '\'' +
               ", industry='" + industry + '\'' +
               ", profileUpdatedAt=" + profileUpdatedAt +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Business business = (Business) o;
        return userId == business.userId; // Primary key is sufficient
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}