package dao;

import model.Business; // To link campaigns back to businesses
import model.Campaign;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampaignDAO {

    /**
     * Creates a new campaign in the database.
     * @param campaign The Campaign object containing data for the new campaign (campaignId should be 0).
     * @return The generated campaign_id if successful, -1 otherwise.
     */
    public int createCampaign(Campaign campaign) {
        String sql = "INSERT INTO campaigns (business_user_id, title, description, requirements, budget, start_date, end_date, product_image_path, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        int campaignId = -1;

        if (campaign == null || campaign.getBusinessUserId() <= 0) {
             System.err.println("Create campaign failed: Invalid Campaign object or Business User ID.");
             return -1;
        }

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, campaign.getBusinessUserId());
            pstmt.setString(2, campaign.getTitle());
            pstmt.setString(3, campaign.getDescription());
            pstmt.setString(4, campaign.getRequirements());
            // Handle null budget gracefully
            if (campaign.getBudget() != null) {
                 pstmt.setBigDecimal(5, campaign.getBudget());
            } else {
                 pstmt.setNull(5, Types.DECIMAL);
            }
            // Handle null dates gracefully
             pstmt.setDate(6, campaign.getStartDate()); // java.sql.Date can be null
             pstmt.setDate(7, campaign.getEndDate());   // java.sql.Date can be null

            pstmt.setString(8, campaign.getProductImagePath()); // Can be null if no image uploaded
            pstmt.setString(9, campaign.getStatus() != null ? campaign.getStatus() : Campaign.Status.ACTIVE.getStatusName()); // Default to active if not set

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    campaignId = generatedKeys.getInt(1);
                    System.out.println("Campaign created successfully: ID=" + campaignId + ", Title=" + campaign.getTitle());
                } else {
                    System.err.println("Campaign creation succeeded but failed to retrieve generated campaign ID. Title: " + campaign.getTitle());
                    campaignId = -1;
                }
            } else {
                 System.err.println("Campaign creation failed: No rows affected. Title: " + campaign.getTitle());
                 campaignId = -1;
            }

        } catch (SQLException e) {
            System.err.println("SQL Error creating campaign '" + campaign.getTitle() + "': " + e.getMessage());
            e.printStackTrace();
            campaignId = -1;
        } finally {
            DBUtil.closeResultSet(generatedKeys);
            DBUtil.closeResources(pstmt, conn);
        }
        return campaignId;
    }

    /**
     * Retrieves a specific campaign by its ID.
     * Optionally joins with the businesses table to populate the Business object.
     * @param campaignId The ID of the campaign to retrieve.
     * @param includeBusinessDetails If true, fetches associated business details.
     * @return Campaign object if found, null otherwise.
     */
    public Campaign getCampaignById(int campaignId, boolean includeBusinessDetails) {
         // Base query
         String baseSql = "SELECT c.*";
         String joinSql = "";
         String fromSql = " FROM campaigns c";

         if (includeBusinessDetails) {
             baseSql += ", b.company_name, b.website, b.industry, b.description as business_description, b.profile_updated_at as business_updated_at"; // Alias columns to avoid name conflicts
             joinSql = " LEFT JOIN businesses b ON c.business_user_id = b.user_id";
         }

         String sql = baseSql + fromSql + joinSql + " WHERE c.campaign_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Campaign campaign = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, campaignId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                campaign = mapResultSetToCampaign(rs);
                 if (includeBusinessDetails && campaign != null && campaign.getBusinessUserId() > 0) {
                     Business business = new Business();
                     business.setUserId(campaign.getBusinessUserId());
                     // Map aliased columns
                     business.setCompanyName(rs.getString("company_name"));
                     business.setWebsite(rs.getString("website"));
                     business.setIndustry(rs.getString("industry"));
                     business.setDescription(rs.getString("business_description"));
                     business.setProfileUpdatedAt(rs.getTimestamp("business_updated_at"));
                     campaign.setBusiness(business);
                 }
                 // System.out.println("Campaign found by ID: " + campaignId); // Debug
            } else {
                 // System.out.println("Campaign not found by ID: " + campaignId); // Debug
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting campaign by ID " + campaignId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return campaign;
    }

    /**
     * Updates an existing campaign.
     * @param campaign The Campaign object with updated data (must have a valid campaignId).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateCampaign(Campaign campaign) {
        // Note: productImagePath might be handled separately if it's optional on update
        String sql = "UPDATE campaigns SET title = ?, description = ?, requirements = ?, budget = ?, start_date = ?, end_date = ?, product_image_path = ?, status = ? WHERE campaign_id = ? AND business_user_id = ?"; // Add business_user_id check for security
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

         if (campaign == null || campaign.getCampaignId() <= 0 || campaign.getBusinessUserId() <= 0) {
             System.err.println("Update campaign failed: Invalid Campaign object, Campaign ID, or Business User ID.");
             return false;
         }

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, campaign.getTitle());
            pstmt.setString(2, campaign.getDescription());
            pstmt.setString(3, campaign.getRequirements());
             if (campaign.getBudget() != null) {
                 pstmt.setBigDecimal(4, campaign.getBudget());
             } else {
                 pstmt.setNull(4, Types.DECIMAL);
             }
             pstmt.setDate(5, campaign.getStartDate());
             pstmt.setDate(6, campaign.getEndDate());
             pstmt.setString(7, campaign.getProductImagePath()); // Update image path
            pstmt.setString(8, campaign.getStatus());
            pstmt.setInt(9, campaign.getCampaignId());
            pstmt.setInt(10, campaign.getBusinessUserId()); // Ensure only the owner can update

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Campaign updated successfully: ID=" + campaign.getCampaignId());
            } else {
                 System.err.println("Failed to update campaign ID: " + campaign.getCampaignId() + ". Campaign not found or not owned by user ID: " + campaign.getBusinessUserId());
            }
        } catch (SQLException e) {
            System.err.println("SQL Error updating campaign ID " + campaign.getCampaignId() + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

    /**
     * Deletes a campaign (sets status to 'deleted' - soft delete).
     * Requires campaign ID and the owning business user ID for verification.
     * @param campaignId The ID of the campaign to delete.
     * @param businessUserId The ID of the business user attempting the deletion.
     * @return true if the campaign status was set to 'deleted', false otherwise.
     */
    public boolean deleteCampaign(int campaignId, int businessUserId) {
        String sql = "UPDATE campaigns SET status = ? WHERE campaign_id = ? AND business_user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, Campaign.Status.DELETED.getStatusName()); // Set status to deleted
            pstmt.setInt(2, campaignId);
            pstmt.setInt(3, businessUserId); // Verify owner

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
             if (success) {
                 System.out.println("Campaign status set to 'deleted' for ID: " + campaignId);
             } else {
                  System.err.println("Failed to delete campaign ID: " + campaignId + ". Campaign not found or not owned by user ID: " + businessUserId);
             }
        } catch (SQLException e) {
            System.err.println("SQL Error deleting campaign ID " + campaignId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

    /**
     * Retrieves all campaigns created by a specific business user.
     * Filters out campaigns marked as 'deleted'.
     * @param businessUserId The ID of the business user.
     * @return List of Campaign objects, potentially empty.
     */
    public List<Campaign> getCampaignsByBusinessId(int businessUserId) {
        List<Campaign> campaigns = new ArrayList<>();
        // Exclude 'deleted' campaigns from the business owner's view
        String sql = "SELECT * FROM campaigns WHERE business_user_id = ? AND status != ? ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, businessUserId);
            pstmt.setString(2, Campaign.Status.DELETED.getStatusName()); // Exclude deleted
            rs = pstmt.executeQuery();
            while (rs.next()) {
                campaigns.add(mapResultSetToCampaign(rs));
            }
             System.out.println("Retrieved " + campaigns.size() + " campaigns for business user ID: " + businessUserId);
        } catch (SQLException e) {
            System.err.println("SQL Error getting campaigns for business ID " + businessUserId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return campaigns;
    }

    /**
     * Retrieves all ACTIVE campaigns available for creators to view and apply.
     * Excludes inactive, completed, deleted campaigns. Optionally filters by title/keyword.
     * Includes pagination.
     * @param searchTerm Optional keyword to search in title or description (can be null/empty).
     * @param limit Max number of campaigns per page.
     * @param offset Starting record index for pagination.
     * @return List of active Campaign objects matching the criteria.
     */
    public List<Campaign> getActiveCampaignsForCreators(String searchTerm, int limit, int offset) {
        List<Campaign> campaigns = new ArrayList<>();
         StringBuilder sqlBuilder = new StringBuilder(
            "SELECT c.*, b.company_name " + // Include company name for display
            "FROM campaigns c JOIN businesses b ON c.business_user_id = b.user_id " +
            "WHERE c.status = ?");
        List<Object> params = new ArrayList<>();
        params.add(Campaign.Status.ACTIVE.getStatusName());

         // Add search term filter if provided
         if (searchTerm != null && !searchTerm.trim().isEmpty()) {
             sqlBuilder.append(" AND (LOWER(c.title) LIKE LOWER(?) OR LOWER(c.description) LIKE LOWER(?))");
             String likeTerm = "%" + searchTerm.trim() + "%";
             params.add(likeTerm);
             params.add(likeTerm);
         }

        sqlBuilder.append(" ORDER BY c.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);


        String sql = sqlBuilder.toString();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                Campaign campaign = mapResultSetToCampaign(rs);
                 // Create a minimal business object just for the company name
                 Business business = new Business();
                 business.setUserId(campaign.getBusinessUserId()); // Set the ID
                 business.setCompanyName(rs.getString("company_name"));
                 campaign.setBusiness(business); // Attach business info
                campaigns.add(campaign);
            }
             System.out.println("Retrieved " + campaigns.size() + " active campaigns for creators.");
        } catch (SQLException e) {
            System.err.println("SQL Error getting active campaigns for creators: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return campaigns;
    }

    /**
     * Counts the total number of ACTIVE campaigns matching the search criteria (for pagination).
     * @param searchTerm Optional keyword to search in title or description.
     * @return Total count of matching active campaigns, or -1 on error.
     */
    public int countActiveCampaignsForCreators(String searchTerm) {
         StringBuilder sqlBuilder = new StringBuilder(
            "SELECT COUNT(c.campaign_id) " +
            "FROM campaigns c JOIN businesses b ON c.business_user_id = b.user_id " +
            "WHERE c.status = ?");
        List<Object> params = new ArrayList<>();
        params.add(Campaign.Status.ACTIVE.getStatusName());
         int count = -1;

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sqlBuilder.append(" AND (LOWER(c.title) LIKE LOWER(?) OR LOWER(c.description) LIKE LOWER(?))");
            String likeTerm = "%" + searchTerm.trim() + "%";
            params.add(likeTerm);
            params.add(likeTerm);
        }

        String sql = sqlBuilder.toString();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                 pstmt.setObject(i + 1, params.get(i));
             }
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error counting active campaigns for creators: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return count;
    }


    /**
     * Retrieves ALL campaigns, regardless of status (for Admin view).
     * Includes business company name for context.
     * @return List of all Campaign objects.
     */
    public List<Campaign> getAllCampaignsForAdmin() {
        List<Campaign> campaigns = new ArrayList<>();
        String sql = "SELECT c.*, b.company_name FROM campaigns c LEFT JOIN businesses b ON c.business_user_id = b.user_id ORDER BY c.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                 Campaign campaign = mapResultSetToCampaign(rs);
                 // Attach minimal business info
                 Business business = new Business();
                 business.setUserId(campaign.getBusinessUserId());
                 business.setCompanyName(rs.getString("company_name"));
                 campaign.setBusiness(business);
                 campaigns.add(campaign);
            }
             System.out.println("Retrieved " + campaigns.size() + " campaigns for admin view.");
        } catch (SQLException e) {
            System.err.println("SQL Error getting all campaigns for admin: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return campaigns;
    }

     /**
      * Counts the total number of campaigns in the system (for Admin dashboard).
      * @return Total campaign count, or -1 on error.
      */
      public long countTotalCampaigns() {
         String sql = "SELECT COUNT(*) FROM campaigns";
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
             System.err.println("SQL Error counting total campaigns: " + e.getMessage());
             e.printStackTrace();
         } finally {
             DBUtil.closeResources(rs, pstmt, conn);
         }
         return count;
     }


    /**
     * Helper method to map a ResultSet row to a Campaign object.
     * Does not map associated Business details by default (handle in calling method if needed).
     * @param rs The ResultSet containing campaign data.
     * @return A populated Campaign object.
     * @throws SQLException If a database access error occurs.
     */
    private Campaign mapResultSetToCampaign(ResultSet rs) throws SQLException {
        Campaign campaign = new Campaign();
        campaign.setCampaignId(rs.getInt("campaign_id"));
        campaign.setBusinessUserId(rs.getInt("business_user_id"));
        campaign.setTitle(rs.getString("title"));
        campaign.setDescription(rs.getString("description"));
        campaign.setRequirements(rs.getString("requirements"));
        campaign.setBudget(rs.getBigDecimal("budget")); // Can be null
        campaign.setStartDate(rs.getDate("start_date")); // Can be null
        campaign.setEndDate(rs.getDate("end_date"));     // Can be null
        campaign.setProductImagePath(rs.getString("product_image_path")); // Can be null
        campaign.setStatus(rs.getString("status"));
        campaign.setCreatedAt(rs.getTimestamp("created_at"));
        campaign.setUpdatedAt(rs.getTimestamp("updated_at"));
        return campaign;
    }
}