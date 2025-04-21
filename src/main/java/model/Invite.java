package model;

import java.sql.Timestamp;
import java.util.Objects;

public class Invite {
    private int inviteId;
    private int campaignId;
    private int businessUserId;
    private int creatorUserId;
    private String inviteMessage;
    private String status; // "pending", "accepted", "rejected"
    private Timestamp sentAt;
    private Timestamp respondedAt;

    // Optional: Links to associated objects
    private Campaign campaign;
    private Business business; // The sender (business)
    private Creator creator;   // The recipient (creator)

    // Enum for Status
    public enum Status {
        PENDING("pending"),
        ACCEPTED("accepted"),
        REJECTED("rejected");

        private final String statusName;
        Status(String name) { this.statusName = name; }
        public String getStatusName() { return statusName; }

        public static Status fromString(String text) {
            for (Status s : Status.values()) {
                if (s.statusName.equalsIgnoreCase(text)) return s;
            }
            return PENDING; // Default
        }
    }

    // Constructors
    public Invite() {}

    public Invite(int inviteId, int campaignId, int businessUserId, int creatorUserId, String inviteMessage, String status, Timestamp sentAt, Timestamp respondedAt) {
        this.inviteId = inviteId;
        this.campaignId = campaignId;
        this.businessUserId = businessUserId;
        this.creatorUserId = creatorUserId;
        this.inviteMessage = inviteMessage;
        this.status = status; // Add validation if needed
        this.sentAt = sentAt;
        this.respondedAt = respondedAt;
    }

    // Getters
    public int getInviteId() { return inviteId; }
    public int getCampaignId() { return campaignId; }
    public int getBusinessUserId() { return businessUserId; }
    public int getCreatorUserId() { return creatorUserId; }
    public String getInviteMessage() { return inviteMessage; }
    public String getStatus() { return status; }
    public Timestamp getSentAt() { return sentAt; }
    public Timestamp getRespondedAt() { return respondedAt; }
    public Campaign getCampaign() { return campaign; }
    public Business getBusiness() { return business; }
    public Creator getCreator() { return creator; }

    // Setters
    public void setInviteId(int inviteId) { this.inviteId = inviteId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }
    public void setBusinessUserId(int businessUserId) { this.businessUserId = businessUserId; }
    public void setCreatorUserId(int creatorUserId) { this.creatorUserId = creatorUserId; }
    public void setInviteMessage(String inviteMessage) { this.inviteMessage = inviteMessage; }
    public void setStatus(String status) { this.status = status; } // Add validation if needed
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }
    public void setRespondedAt(Timestamp respondedAt) { this.respondedAt = respondedAt; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
    public void setBusiness(Business business) { this.business = business; }
    public void setCreator(Creator creator) { this.creator = creator; }

    // toString, equals, hashCode
    @Override
    public String toString() {
        return "Invite{" +
               "inviteId=" + inviteId +
               ", campaignId=" + campaignId +
               ", businessUserId=" + businessUserId +
               ", creatorUserId=" + creatorUserId +
               ", status='" + status + '\'' +
               ", sentAt=" + sentAt +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invite invite = (Invite) o;
        // Use composite key if invite ID is not yet assigned
         if (inviteId > 0) {
             return inviteId == invite.inviteId;
         } else {
             return campaignId == invite.campaignId && businessUserId == invite.businessUserId && creatorUserId == invite.creatorUserId;
         }
    }

    @Override
    public int hashCode() {
         if (inviteId > 0) {
             return Objects.hash(inviteId);
         } else {
             return Objects.hash(campaignId, businessUserId, creatorUserId);
         }
    }
}