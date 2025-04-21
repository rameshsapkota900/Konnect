package model;

import java.sql.Timestamp;
import java.util.Objects;

public class Application {
    private int applicationId;
    private int campaignId;
    private int creatorUserId;
    private String pitchMessage;
    private String status; // "pending", "accepted", "rejected", "withdrawn"
    private Timestamp appliedAt;

    // Optional: Links to the associated Campaign and Creator
    private Campaign campaign;
    private Creator creator;
    // Optional: Link to the associated User (for email etc. without joining Creator)
    private User creatorUser;


     // Enum for Status (Recommended)
     public enum Status {
         PENDING("pending"),
         ACCEPTED("accepted"),
         REJECTED("rejected"),
         WITHDRAWN("withdrawn"); // Creator withdrew

         private final String statusName;
         Status(String name) { this.statusName = name; }
         public String getStatusName() { return statusName; }

         public static Status fromString(String text) {
             for (Status s : Status.values()) {
                 if (s.statusName.equalsIgnoreCase(text)) return s;
             }
             return PENDING; // Default or throw error
         }
     }

    // Constructors
    public Application() {}

    public Application(int applicationId, int campaignId, int creatorUserId, String pitchMessage, String status, Timestamp appliedAt) {
        this.applicationId = applicationId;
        this.campaignId = campaignId;
        this.creatorUserId = creatorUserId;
        this.pitchMessage = pitchMessage;
        this.status = status; // Add validation if needed
        this.appliedAt = appliedAt;
    }

    // Getters
    public int getApplicationId() { return applicationId; }
    public int getCampaignId() { return campaignId; }
    public int getCreatorUserId() { return creatorUserId; }
    public String getPitchMessage() { return pitchMessage; }
    public String getStatus() { return status; }
    public Timestamp getAppliedAt() { return appliedAt; }
    public Campaign getCampaign() { return campaign; }
    public Creator getCreator() { return creator; }
    public User getCreatorUser() { return creatorUser; }


    // Setters
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }
    public void setCreatorUserId(int creatorUserId) { this.creatorUserId = creatorUserId; }
    public void setPitchMessage(String pitchMessage) { this.pitchMessage = pitchMessage; }
    public void setStatus(String status) { this.status = status; } // Add validation if needed
    public void setAppliedAt(Timestamp appliedAt) { this.appliedAt = appliedAt; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
    public void setCreator(Creator creator) { this.creator = creator; }
     public void setCreatorUser(User creatorUser) { this.creatorUser = creatorUser; }

    // toString, equals, hashCode
    @Override
    public String toString() {
        return "Application{" +
               "applicationId=" + applicationId +
               ", campaignId=" + campaignId +
               ", creatorUserId=" + creatorUserId +
               ", status='" + status + '\'' +
               ", appliedAt=" + appliedAt +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        // Use composite key for equality if application ID is not yet assigned
        if (applicationId > 0) {
             return applicationId == that.applicationId;
        } else {
             return campaignId == that.campaignId && creatorUserId == that.creatorUserId;
        }
    }

    @Override
    public int hashCode() {
         // Use composite key if application ID might be 0
        if (applicationId > 0) {
            return Objects.hash(applicationId);
        } else {
             return Objects.hash(campaignId, creatorUserId);
        }
    }
}