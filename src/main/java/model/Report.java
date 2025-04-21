package model;

import java.sql.Timestamp;
import java.util.Objects;

public class Report {
    private int reportId;
    private int reporterUserId;
    private int reportedUserId;
    private String reason;
    private String details;
    private String status; // "pending", "reviewed", "action_taken", "dismissed"
    private Timestamp reportedAt;
    private Timestamp reviewedAt;

    // Optional: Links to User objects
    private User reporterUser;
    private User reportedUser;

    // Enum for Status
     public enum Status {
         PENDING("pending"),
         REVIEWED("reviewed"), // Admin has looked at it
         ACTION_TAKEN("action_taken"), // e.g., user banned
         DISMISSED("dismissed"); // No action needed

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
    public Report() {}

    public Report(int reportId, int reporterUserId, int reportedUserId, String reason, String details, String status, Timestamp reportedAt, Timestamp reviewedAt) {
        this.reportId = reportId;
        this.reporterUserId = reporterUserId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.details = details;
        this.status = status; // Add validation if needed
        this.reportedAt = reportedAt;
        this.reviewedAt = reviewedAt;
    }

    // Getters
    public int getReportId() { return reportId; }
    public int getReporterUserId() { return reporterUserId; }
    public int getReportedUserId() { return reportedUserId; }
    public String getReason() { return reason; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
    public Timestamp getReportedAt() { return reportedAt; }
    public Timestamp getReviewedAt() { return reviewedAt; }
    public User getReporterUser() { return reporterUser; }
    public User getReportedUser() { return reportedUser; }

    // Setters
    public void setReportId(int reportId) { this.reportId = reportId; }
    public void setReporterUserId(int reporterUserId) { this.reporterUserId = reporterUserId; }
    public void setReportedUserId(int reportedUserId) { this.reportedUserId = reportedUserId; }
    public void setReason(String reason) { this.reason = reason; }
    public void setDetails(String details) { this.details = details; }
    public void setStatus(String status) { this.status = status; } // Add validation if needed
    public void setReportedAt(Timestamp reportedAt) { this.reportedAt = reportedAt; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setReporterUser(User reporterUser) { this.reporterUser = reporterUser; }
    public void setReportedUser(User reportedUser) { this.reportedUser = reportedUser; }

    // toString, equals, hashCode
    @Override
    public String toString() {
        return "Report{" +
               "reportId=" + reportId +
               ", reporterUserId=" + reporterUserId +
               ", reportedUserId=" + reportedUserId +
               ", reason='" + reason + '\'' +
               ", status='" + status + '\'' +
               ", reportedAt=" + reportedAt +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return reportId == report.reportId; // Primary key
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }
}