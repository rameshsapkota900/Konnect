package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

public class Campaign {
    private int campaignId;
    private int businessUserId; // FK to users table (specifically a business user)
    private String title;
    private String description;
    private String requirements;
    private BigDecimal budget;
    private Date startDate;
    private Date endDate;
    private String productImagePath; // Relative path
    private String status; // "active", "inactive", "completed", "deleted"
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Optional: Link to the Business object that created it
    private Business business;

    // Enum for Status (Recommended)
    public enum Status {
        ACTIVE("active"),
        INACTIVE("inactive"),
        COMPLETED("completed"),
        DELETED("deleted");

        private final String statusName;
        Status(String name) { this.statusName = name; }
        public String getStatusName() { return statusName; }

        public static Status fromString(String text) {
            for (Status s : Status.values()) {
                if (s.statusName.equalsIgnoreCase(text)) return s;
            }
            return INACTIVE; // Default or throw error
        }
    }


    // Constructors
    public Campaign() {}

    public Campaign(int campaignId, int businessUserId, String title, String description, String requirements, BigDecimal budget, Date startDate, Date endDate, String productImagePath, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.campaignId = campaignId;
        this.businessUserId = businessUserId;
        this.title = title;
        this.description = description;
        this.requirements = requirements;
        this.budget = budget;
        this.startDate = startDate;
        this.endDate = endDate;
        this.productImagePath = productImagePath;
        this.status = status; // Add validation if needed
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getCampaignId() { return campaignId; }
    public int getBusinessUserId() { return businessUserId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRequirements() { return requirements; }
    public BigDecimal getBudget() { return budget; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public String getProductImagePath() { return productImagePath; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public Business getBusiness() { return business; }

    // Setters
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }
    public void setBusinessUserId(int businessUserId) { this.businessUserId = businessUserId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public void setProductImagePath(String productImagePath) { this.productImagePath = productImagePath; }
    public void setStatus(String status) { this.status = status; } // Add validation if needed
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public void setBusiness(Business business) { this.business = business; }

    // toString, equals, hashCode
    @Override
    public String toString() {
        return "Campaign{" +
               "campaignId=" + campaignId +
               ", businessUserId=" + businessUserId +
               ", title='" + title + '\'' +
               ", status='" + status + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Campaign campaign = (Campaign) o;
        return campaignId == campaign.campaignId; // Primary key
    }

    @Override
    public int hashCode() {
        return Objects.hash(campaignId);
    }
}