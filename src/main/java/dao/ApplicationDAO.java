package dao;

import model.Application;
import model.Campaign; // To link application back to campaign
import model.Creator;  // To link application back to creator
import model.User;     // To potentially link creator user details
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    /**
     * Creates a new application for a creator applying to a campaign.
     * Checks if an application already exists for this creator/campaign pair.
     * @param application Application object containing campaignId, creatorUserId, and pitchMessage.
     * @return true if the application was created successfully, false otherwise (e.g., already applied).
     */
    public boolean createApplication(Application application) {
         // 1. Check if already applied
         if (getApplicationByCreatorAndCampaign(application.getCreatorUserId(), application.getCampaignId()) != null) {
             System.err.println("Application creation failed: Creator ID " + application.getCreatorUserId() +
                                " has already applied to Campaign ID " + application.getCampaignId());
             return false;
         }

         // 2. Insert the new application
        String sql = "INSERT INTO applications (campaign_id, creator_user_id, pitch_message, status) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, application.getCampaignId());
            pstmt.setInt(2, application.getCreatorUserId());
            pstmt.setString(3, application.getPitchMessage());
            pstmt.setString(4, Application.Status.PENDING.getStatusName()); // Initial status is pending

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Application created successfully: CreatorID=" + application.getCreatorUserId() + ", CampaignID=" + application.getCampaignId());
            } else {
                 System.err.println("Application creation failed: No rows affected for CreatorID=" + application.getCreatorUserId() + ", CampaignID=" + application.getCampaignId());
            }
        } catch (SQLException e) {
            System.err.println("SQL Error creating application for CreatorID=" + application.getCreatorUserId() + ", CampaignID=" + application.getCampaignId() + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

    /**
     * Retrieves a specific application by its ID.
     * @param applicationId The ID of the application.
     * @return Application object if found, null otherwise.
     */
    public Application getApplicationById(int applicationId) {
        String sql = "SELECT * FROM applications WHERE application_id = ?";
        // Consider joining other tables here if needed often
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Application application = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, applicationId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                application = mapResultSetToApplication(rs);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting application by ID " + applicationId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return application;
    }


    /**
     * Retrieves a specific application for a given creator and campaign.
     * Used to check if a creator has already applied.
     * @param creatorUserId The ID of the creator.
     * @param campaignId The ID of the campaign.
     * @return Application object if found, null otherwise.
     */
    public Application getApplicationByCreatorAndCampaign(int creatorUserId, int campaignId) {
        String sql = "SELECT * FROM applications WHERE creator_user_id = ? AND campaign_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Application application = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, creatorUserId);
            pstmt.setInt(2, campaignId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                application = mapResultSetToApplication(rs);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting application by creator " + creatorUserId + " and campaign " + campaignId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return application;
    }


    /**
     * Retrieves all applications submitted by a specific creator.
     * Joins with the campaigns table to include campaign titles.
     * @param creatorUserId The ID of the creator.
     * @return List of Application objects, including basic Campaign info.
     */
    public List<Application> getApplicationsByCreatorId(int creatorUserId) {
        List<Application> applications = new ArrayList<>();
        // Join with campaigns to get title, exclude deleted campaigns maybe?
        String sql = "SELECT app.*, camp.title as campaign_title, camp.business_user_id " +
                     "FROM applications app " +
                     "JOIN campaigns camp ON app.campaign_id = camp.campaign_id " +
                     "WHERE app.creator_user_id = ? " +
                     //"AND camp.status != 'deleted' " + // Optional: Hide apps for deleted campaigns?
                     "ORDER BY app.applied_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, creatorUserId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Application app = mapResultSetToApplication(rs);
                // Attach basic campaign info
                Campaign camp = new Campaign();
                camp.setCampaignId(app.getCampaignId());
                camp.setTitle(rs.getString("campaign_title"));
                camp.setBusinessUserId(rs.getInt("business_user_id")); // Useful for linking to business
                app.setCampaign(camp);
                applications.add(app);
            }
            System.out.println("Retrieved " + applications.size() + " applications for creator ID: " + creatorUserId);
        } catch (SQLException e) {
            System.err.println("SQL Error getting applications for creator ID " + creatorUserId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return applications;
    }

    /**
     * Retrieves all applications submitted for a specific campaign.
     * Joins with creators and users tables to include creator display name and email.
     * Filters out applications from banned creators.
     * @param campaignId The ID of the campaign.
     * @return List of Application objects, including basic Creator and User info.
     */
    public List<Application> getApplicationsByCampaignId(int campaignId) {
        List<Application> applications = new ArrayList<>();
         // Join applications with creators and users to get creator info and check ban status
         String sql = "SELECT app.*, cr.display_name, u.email as creator_email, u.is_banned " +
                      "FROM applications app " +
                      "JOIN creators cr ON app.creator_user_id = cr.user_id " +
                      "JOIN users u ON app.creator_user_id = u.user_id " +
                      "WHERE app.campaign_id = ? AND u.is_banned = FALSE " + // Exclude banned applicants
                      "ORDER BY app.applied_at ASC"; // Show oldest first
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, campaignId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Application app = mapResultSetToApplication(rs);
                // Attach basic creator and user info
                Creator creator = new Creator();
                creator.setUserId(app.getCreatorUserId());
                creator.setDisplayName(rs.getString("display_name"));

                User user = new User();
                user.setUserId(app.getCreatorUserId());
                user.setEmail(rs.getString("creator_email"));
                 user.setBanned(rs.getBoolean("is_banned")); // Should always be false due to WHERE clause

                app.setCreator(creator);
                app.setCreatorUser(user); // Attach user object as well
                applications.add(app);
            }
             System.out.println("Retrieved " + applications.size() + " applications for campaign ID: " + campaignId);
        } catch (SQLException e) {
            System.err.println("SQL Error getting applications for campaign ID " + campaignId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return applications;
    }

    /**
     * Updates the status of an application (e.g., accepted, rejected).
     * Typically done by the business owner.
     * @param applicationId The ID of the application to update.
     * @param newStatus The new status string (use Application.Status enum constants).
     * @param businessUserId The ID of the business user making the change (for verification).
     * @return true if the update was successful, false otherwise (e.g., application not found, or not owned by business).
     */
    public boolean updateApplicationStatus(int applicationId, String newStatus, int businessUserId) {
         // Verify the business user owns the campaign associated with this application
         String sql = "UPDATE applications app " +
                      "JOIN campaigns camp ON app.campaign_id = camp.campaign_id " +
                      "SET app.status = ? " +
                      "WHERE app.application_id = ? AND camp.business_user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        // Basic validation of status string
        if (!isValidStatus(newStatus)) {
            System.err.println("Update application status failed: Invalid status value '" + newStatus + "' for application ID " + applicationId);
            return false;
        }

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, applicationId);
            pstmt.setInt(3, businessUserId); // Check ownership

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Application status updated successfully for ID: " + applicationId + " to " + newStatus);
            } else {
                 System.err.println("Failed to update application status for ID: " + applicationId + ". Application not found or campaign not owned by user ID: " + businessUserId);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error updating application status for ID " + applicationId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }


     /**
      * Allows a creator to withdraw their application.
      * @param applicationId The ID of the application to withdraw.
      * @param creatorUserId The ID of the creator withdrawing (for verification).
      * @return true if successful, false otherwise.
      */
     public boolean withdrawApplication(int applicationId, int creatorUserId) {
         String sql = "UPDATE applications SET status = ? WHERE application_id = ? AND creator_user_id = ?";
         Connection conn = null;
         PreparedStatement pstmt = null;
         boolean success = false;

         try {
             conn = DBUtil.getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setString(1, Application.Status.WITHDRAWN.getStatusName());
             pstmt.setInt(2, applicationId);
             pstmt.setInt(3, creatorUserId); // Verify creator owns the application

             int rowsAffected = pstmt.executeUpdate();
             success = rowsAffected > 0;
             if (success) {
                 System.out.println("Application ID " + applicationId + " withdrawn successfully by creator ID " + creatorUserId);
             } else {
                 System.err.println("Failed to withdraw application ID " + applicationId + ". Application not found or not owned by creator ID " + creatorUserId);
             }
         } catch (SQLException e) {
             System.err.println("SQL Error withdrawing application ID " + applicationId + ": " + e.getMessage());
             e.printStackTrace();
         } finally {
             DBUtil.closeResources(pstmt, conn);
         }
         return success;
     }


    /**
     * Deletes an application. Use with caution, usually updating status is preferred.
     * Consider if creators or businesses should be allowed to delete, or only admins.
     * Let's restrict this to the creator who made it, for example.
     * @param applicationId The ID of the application to delete.
      * @param creatorUserId The ID of the creator attempting deletion (for verification).
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteApplication(int applicationId, int creatorUserId) {
        String sql = "DELETE FROM applications WHERE application_id = ? AND creator_user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, applicationId);
            pstmt.setInt(2, creatorUserId); // Verify ownership

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
             if (success) {
                 System.out.println("Application deleted successfully: ID=" + applicationId);
             } else {
                  System.err.println("Failed to delete application ID: " + applicationId + ". Application not found or not owned by creator ID: " + creatorUserId);
             }
        } catch (SQLException e) {
            System.err.println("SQL Error deleting application ID " + applicationId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }


    /**
     * Helper method to map a ResultSet row to an Application object.
     * Does not map associated Campaign/Creator/User objects by default.
     * @param rs The ResultSet containing application data.
     * @return A populated Application object.
     * @throws SQLException If a database access error occurs.
     */
    private Application mapResultSetToApplication(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setApplicationId(rs.getInt("application_id"));
        app.setCampaignId(rs.getInt("campaign_id"));
        app.setCreatorUserId(rs.getInt("creator_user_id"));
        app.setPitchMessage(rs.getString("pitch_message"));
        app.setStatus(rs.getString("status"));
        app.setAppliedAt(rs.getTimestamp("applied_at"));
        return app;
    }

     /**
      * Helper to validate status strings against the enum.
      * @param status The status string to check.
      * @return true if the status is valid, false otherwise.
      */
     private boolean isValidStatus(String status) {
         if (status == null) return false;
         for (Application.Status s : Application.Status.values()) {
             if (s.getStatusName().equalsIgnoreCase(status)) {
                 return true;
             }
         }
         return false;
     }
}