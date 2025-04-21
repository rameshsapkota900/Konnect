package model;

import java.sql.Timestamp;
import java.util.Objects;

public class Creator {
    private int userId; // Foreign Key linking to User table
    private String displayName;
    private String bio;
    private String socialMediaLinks; // Store as JSON string, parse when needed
    private String niche;
    private int followerCount;
    private String pricingInfo;
    private String mediaKitPath; // Relative path to the uploaded file
    private Timestamp profileUpdatedAt;

    // Link to the User object (optional, useful for accessing email etc.)
    private User user;

    // Constructors
    public Creator() {}

    public Creator(int userId, String displayName, String bio, String socialMediaLinks, String niche, int followerCount, String pricingInfo, String mediaKitPath, Timestamp profileUpdatedAt) {
        this.userId = userId;
        this.displayName = displayName;
        this.bio = bio;
        this.socialMediaLinks = socialMediaLinks; // Assume it's already a JSON string or handle parsing
        this.niche = niche;
        this.followerCount = followerCount;
        this.pricingInfo = pricingInfo;
        this.mediaKitPath = mediaKitPath;
        this.profileUpdatedAt = profileUpdatedAt;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getBio() { return bio; }
    public String getSocialMediaLinks() { return socialMediaLinks; } // Returns JSON string
    public String getNiche() { return niche; }
    public int getFollowerCount() { return followerCount; }
    public String getPricingInfo() { return pricingInfo; }
    public String getMediaKitPath() { return mediaKitPath; }
    public Timestamp getProfileUpdatedAt() { return profileUpdatedAt; }
    public User getUser() { return user; } // Getter for associated User

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setBio(String bio) { this.bio = bio; }
    public void setSocialMediaLinks(String socialMediaLinks) { this.socialMediaLinks = socialMediaLinks; } // Expects JSON string
    public void setNiche(String niche) { this.niche = niche; }
    public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }
    public void setPricingInfo(String pricingInfo) { this.pricingInfo = pricingInfo; }
    public void setMediaKitPath(String mediaKitPath) { this.mediaKitPath = mediaKitPath; }
    public void setProfileUpdatedAt(Timestamp profileUpdatedAt) { this.profileUpdatedAt = profileUpdatedAt; }
    public void setUser(User user) { this.user = user; } // Setter for associated User

    // --- Convenience methods ---

    // Example: Method to parse social media links (requires a JSON library like Gson or Jackson)
    // If you add a JSON library to WEB-INF/lib:
    /*
    import com.google.gson.Gson;
    import com.google.gson.reflect.TypeToken;
    import java.lang.reflect.Type;
    import java.util.Map;

    public Map<String, String> getSocialMediaLinksMap() {
        if (socialMediaLinks == null || socialMediaLinks.trim().isEmpty() || socialMediaLinks.equals("{}")) {
            return new java.util.HashMap<>(); // Return empty map
        }
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> map = gson.fromJson(socialMediaLinks, type);
            return map != null ? map : new java.util.HashMap<>();
        } catch (Exception e) {
            System.err.println("Error parsing socialMediaLinks JSON for creator " + userId + ": " + e.getMessage());
            return new java.util.HashMap<>(); // Return empty map on error
        }
    }
    */

    // toString, equals, hashCode
    @Override
    public String toString() {
        return "Creator{" +
               "userId=" + userId +
               ", displayName='" + displayName + '\'' +
               ", niche='" + niche + '\'' +
               ", followerCount=" + followerCount +
               ", mediaKitPath='" + mediaKitPath + '\'' +
               ", profileUpdatedAt=" + profileUpdatedAt +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Creator creator = (Creator) o;
        return userId == creator.userId; // Primary key is sufficient for equality
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}