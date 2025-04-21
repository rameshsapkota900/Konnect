package dao;

import model.Invite;
import model.Campaign; // To link invite to campaign details
import model.Creator;  // To link invite to creator details
import model.Business; // To link invite to business details
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InviteDAO {

    /**
     * Creates a new invite from a business to a creator for a specific campaign.
     * Checks if an invite already exists for this combination.
     * @param invite Invite object containing campaignId, businessUserId, creatorUserId, and message.
     * @return true if the invite was created successfully, false otherwise (e.g., already invited).
     */
    public boolean createInvite(Invite invite) {
        // 1. Check if already invited for this campaign
        if (getInviteByBusinessCreatorCampaign(invite.getBusinessUserId(), invite.getCreatorUserId(), invite.getCampaignId()) != null) {
            System.err.println("Invite creation failed: Creator ID " + invite.getCreatorUserId() +
                               " has already been invited to Campaign ID " + invite.getCampaignId() +
                               " by Business ID " + invite.getBusinessUserId());
            return false;
        }

        // 2. Insert the new invite
        String sql = "INSERT INTO invites (campaign_id, business_user_id, creator_user_id, invite_message, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, invite.getCampaignId());
            pstmt.setInt(2, invite.getBusinessUserId());
            pstmt.setInt(3, invite.getCreatorUserId());
            pstmt.setString(4, invite.getInviteMessage());
            pstmt.setString(5, Invite.Status.PENDING.getStatusName()); // Initial status

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Invite created successfully: BusinessID=" + invite.getBusinessUserId() +
                                   ", CreatorID=" + invite.getCreatorUserId() + ", CampaignID=" + invite.getCampaignId());
            } else {
                 System.err.println("Invite creation failed: No rows affected for BusinessID=" + invite.getBusinessUserId() +
                                   ", CreatorID=" + invite.getCreatorUserId() + ", CampaignID=" + invite.getCampaignId());
            }
        } catch (SQLException e) {
            // Catch potential foreign key constraint violations if IDs are invalid
             if (e.getSQLState().startsWith("23")) { // SQLState for integrity constraint violation
                 System.err.println("Invite creation failed: Invalid Campaign ID, Business ID, or Creator ID.");
             } else {
                System.err.println("SQL Error creating invite: " + e.getMessage());
             }
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

     /**
      * Retrieves a specific invite by its ID.
      * @param inviteId The ID of the invite.
      * @return Invite object if found, null otherwise.
      */
     public Invite getInviteById(int inviteId) {
         String sql = "SELECT * FROM invites WHERE invite_id = ?";
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         Invite invite = null;

         try {
             conn = DBUtil.getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setInt(1, inviteId);
             rs = pstmt.executeQuery();

             if (rs.next()) {
                 invite = mapResultSetToInvite(rs);
             }
         } catch (SQLException e) {
             System.err.println("SQL Error getting invite by ID " + inviteId + ": " + e.getMessage());
             e.printStackTrace();
         } finally {
             DBUtil.closeResources(rs, pstmt, conn);
         }
         return invite;
     }


     /**
      * Retrieves a specific invite for a given business, creator, and campaign combination.
      * Used to check if an invite already exists before creating a new one.
      * @param businessUserId The ID of the business sender.
      * @param creatorUserId The ID of the creator recipient.
      * @param campaignId The ID of the campaign.
      * @return Invite object if found, null otherwise.
      */
     public Invite getInviteByBusinessCreatorCampaign(int businessUserId, int creatorUserId, int campaignId) {
         String sql = "SELECT * FROM invites WHERE business_user_id = ? AND creator_user_id = ? AND campaign_id = ?";
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         Invite invite = null;

         try {
             conn = DBUtil.getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setInt(1, businessUserId);
             pstmt.setInt(2, creatorUserId);
             pstmt.setInt(3, campaignId);
             rs = pstmt.executeQuery();

             if (rs.next()) {
                 invite = mapResultSetToInvite(rs);
             }
         } catch (SQLException e) {
             System.err.println("SQL Error checking for existing invite: " + e.getMessage());
             e.printStackTrace();
         } finally {
             DBUtil.closeResources(rs, pstmt, conn);
         }
         return invite;
     }


    /**
     * Retrieves all invites received by a specific creator.
     * Joins with campaigns and businesses tables to include relevant details.
     * @param creatorUserId The ID of the creator.
     * @return List of Invite objects received by the creator.
     */
    public List<Invite> getInvitesForCreator(int creatorUserId) {
        List<Invite> invites = new ArrayList<>();
        String sql = "SELECT inv.*, camp.title as campaign_title, biz.company_name " +
                     "FROM invites inv " +
                     "JOIN campaigns camp ON inv.campaign_id = camp.campaign_id " +
                     "JOIN businesses biz ON inv.business_user_id = biz.user_id " +
                     "WHERE inv.creator_user_id = ? " +
                     "AND camp.status = ? " + // Only show invites for active campaigns? Or all? Let's show for active.
                     "ORDER BY inv.sent_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, creatorUserId);
            pstmt.setString(2, Campaign.Status.ACTIVE.getStatusName()); // Filter for active campaigns

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Invite invite = mapResultSetToInvite(rs);
                // Attach campaign title and business name
                Campaign campaign = new Campaign();
                campaign.setCampaignId(invite.getCampaignId());
                campaign.setTitle(rs.getString("campaign_title"));
                invite.setCampaign(campaign);

                Business business = new Business();
                business.setUserId(invite.getBusinessUserId());
                business.setCompanyName(rs.getString("company_name"));
                invite.setBusiness(business);

                invites.add(invite);
            }
            System.out.println("Retrieved " + invites.size() + " invites for creator ID: " + creatorUserId);
        } catch (SQLException e) {
            System.err.println("SQL Error getting invites for creator ID " + creatorUserId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return invites;
    }

    /**
     * Retrieves all invites sent by a specific business for a given campaign.
     * Joins with creators table to include creator display name.
     * @param businessUserId The ID of the business.
     * @param campaignId The ID of the campaign.
     * @return List of Invite objects sent by the business for this campaign.
     */
    public List<Invite> getInvitesSentByBusinessForCampaign(int businessUserId, int campaignId) {
        List<Invite> invites = new ArrayList<>();
         String sql = "SELECT inv.*, cr.display_name as creator_name " +
                      "FROM invites inv " +
                      "JOIN creators cr ON inv.creator_user_id = cr.user_id " +
                      "WHERE inv.business_user_id = ? AND inv.campaign_id = ? " +
                      "ORDER BY inv.sent_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, businessUserId);
            pstmt.setInt(2, campaignId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                 Invite invite = mapResultSetToInvite(rs);
                 // Attach creator name
                 Creator creator = new Creator();
                 creator.setUserId(invite.getCreatorUserId());
                 creator.setDisplayName(rs.getString("creator_name"));
                 invite.setCreator(creator);
                 invites.add(invite);
            }
             System.out.println("Retrieved " + invites.size() + " invites sent by business ID " + businessUserId + " for campaign ID " + campaignId);
        } catch (SQLException e) {
            System.err.println("SQL Error getting sent invites for business ID " + businessUserId + ", campaign ID " + campaignId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return invites;
    }


    /**
     * Updates the status of an invite (accepted, rejected) by the creator.
     * Sets the responded_at timestamp.
     * @param inviteId The ID of the invite to update.
     * @param newStatus The new status (must be "accepted" or "rejected").
     * @param creatorUserId The ID of the creator responding (for verification).
     * @return true if the update was successful, false otherwise.
     */
    public boolean respondToInvite(int inviteId, String newStatus, int creatorUserId) {
        // Validate status
        if (!Invite.Status.ACCEPTED.getStatusName().equalsIgnoreCase(newStatus) &&
            !Invite.Status.REJECTED.getStatusName().equalsIgnoreCase(newStatus)) {
            System.err.println("Respond to invite failed: Invalid status value '" + newStatus + "' for invite ID " + inviteId);
            return false;
        }

        String sql = "UPDATE invites SET status = ?, responded_at = CURRENT_TIMESTAMP WHERE invite_id = ? AND creator_user_id = ? AND status = ?"; // Ensure it's still pending
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, inviteId);
            pstmt.setInt(3, creatorUserId); // Verify ownership
            pstmt.setString(4, Invite.Status.PENDING.getStatusName()); // Only update if pending

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Invite ID " + inviteId + " responded to successfully by creator ID " + creatorUserId + " with status: " + newStatus);
                // Optional: If accepted, automatically create an 'accepted' application?
                // This depends on the desired workflow. Could call ApplicationDAO here.
            } else {
                 System.err.println("Failed to respond to invite ID: " + inviteId + ". Invite not found, not owned by creator ID " + creatorUserId + ", or not pending.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error responding to invite ID " + inviteId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

    /**
     * Deletes an invite. Usually done by the business sender if they change their mind
     * before it's accepted/rejected, or by an admin.
     * @param inviteId The ID of the invite to delete.
     * @param businessUserId The ID of the business user attempting deletion (for verification).
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteInvite(int inviteId, int businessUserId) {
        String sql = "DELETE FROM invites WHERE invite_id = ? AND business_user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, inviteId);
             pstmt.setInt(2, businessUserId); // Verify sender

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
             if (success) {
                 System.out.println("Invite deleted successfully: ID=" + inviteId);
             } else {
                  System.err.println("Failed to delete invite ID: " + inviteId + ". Invite not found or not owned by business ID: " + businessUserId);
             }
        } catch (SQLException e) {
            System.err.println("SQL Error deleting invite ID " + inviteId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }


    /**
     * Helper method to map a ResultSet row to an Invite object.
     * Does not map associated objects by default.
     * @param rs The ResultSet containing invite data.
     * @return A populated Invite object.
     * @throws SQLException If a database access error occurs.
     */
    private Invite mapResultSetToInvite(ResultSet rs) throws SQLException {
        Invite invite = new Invite();
        invite.setInviteId(rs.getInt("invite_id"));
        invite.setCampaignId(rs.getInt("campaign_id"));
        invite.setBusinessUserId(rs.getInt("business_user_id"));
        invite.setCreatorUserId(rs.getInt("creator_user_id"));
        invite.setInviteMessage(rs.getString("invite_message"));
        invite.setStatus(rs.getString("status"));
        invite.setSentAt(rs.getTimestamp("sent_at"));
        invite.setRespondedAt(rs.getTimestamp("responded_at")); // Can be null
        return invite;
    }
}