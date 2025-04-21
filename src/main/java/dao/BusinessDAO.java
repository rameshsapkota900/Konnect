package dao;

import model.Business;
import model.User; // To potentially join and get email etc.
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusinessDAO {

    /**
     * Creates the initial profile entry for a newly registered business.
     * Called right after user registration.
     * @param userId The user ID of the new business user.
     * @param companyName The initial company name provided during registration.
     * @return true if creation was successful, false otherwise.
     */
    public boolean createBusinessProfile(int userId, String companyName) {
        String sql = "INSERT INTO businesses (user_id, company_name, website, industry, description) VALUES (?, ?, '', '', '')";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, companyName); // Set the initial company name

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Initial business profile created successfully for User ID: " + userId);
            } else {
                 System.err.println("Failed to create initial business profile for User ID: " + userId);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
             System.err.println("Error creating business profile: User ID " + userId + " likely already has a profile. " + e.getMessage());
             success = false;
        } catch (SQLException e) {
            System.err.println("SQL Error creating initial business profile for User ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

    /**
     * Retrieves a business's profile by their user ID.
     * @param userId The ID of the user whose business profile is needed.
     * @return Business object if found, null otherwise.
     */
    public Business getBusinessByUserId(int userId) {
        String sql = "SELECT * FROM businesses WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Business business = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                business = mapResultSetToBusiness(rs);
                // System.out.println("Business profile found for User ID: " + userId); // Debug
            } else {
                // System.out.println("No business profile found for User ID: " + userId); // Debug
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting business profile for User ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return business;
    }

     /**
      * Retrieves a business's profile by their company name (case-insensitive search).
      * Primarily useful if searching businesses directly, though less common than by ID.
      * Joins with users table to exclude banned users.
      * @param companyName The company name to search for.
      * @return Business object if found and not banned, null otherwise.
      */
     public Business getBusinessByCompanyName(String companyName) {
         String sql = "SELECT b.* FROM businesses b JOIN users u ON b.user_id = u.user_id WHERE LOWER(b.company_name) = LOWER(?) AND u.is_banned = FALSE";
         Connection conn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         Business business = null;

         try {
             conn = DBUtil.getConnection();
             pstmt = conn.prepareStatement(sql);
             pstmt.setString(1, companyName);
             rs = pstmt.executeQuery();

             if (rs.next()) {
                 business = mapResultSetToBusiness(rs);
             }
         } catch (SQLException e) {
             System.err.println("SQL Error getting business profile by Company Name '" + companyName + "': " + e.getMessage());
             e.printStackTrace();
         } finally {
             DBUtil.closeResources(rs, pstmt, conn);
         }
         return business;
     }


    /**
     * Updates an existing business's profile information.
     * Does not update the user_id (PK).
     * @param business The Business object containing updated information.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateBusinessProfile(Business business) {
        String sql = "UPDATE businesses SET company_name = ?, website = ?, industry = ?, description = ? WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

         if (business == null || business.getUserId() <= 0) {
            System.err.println("Update business profile failed: Invalid Business object or User ID.");
            return false;
        }

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, business.getCompanyName());
            pstmt.setString(2, business.getWebsite());
            pstmt.setString(3, business.getIndustry());
            pstmt.setString(4, business.getDescription());
            pstmt.setInt(5, business.getUserId());

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Business profile updated successfully for User ID: " + business.getUserId());
            } else {
                 System.err.println("Failed to update business profile for User ID: " + business.getUserId() + ". Profile might not exist or data unchanged.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error updating business profile for User ID " + business.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

    /**
     * Retrieves a list of all businesses (potentially for admin use).
     * Joins with users table to filter out banned users.
     * @return List of all non-banned Business objects.
     */
     public List<Business> getAllBusinesses() {
        List<Business> businesses = new ArrayList<>();
         String sql = "SELECT b.* FROM businesses b JOIN users u ON b.user_id = u.user_id WHERE u.is_banned = FALSE ORDER BY b.company_name";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                businesses.add(mapResultSetToBusiness(rs));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting all businesses: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return businesses;
    }


    /**
     * Helper method to map a ResultSet row to a Business object.
     * @param rs The ResultSet containing business data.
     * @return A populated Business object.
     * @throws SQLException If a database access error occurs.
     */
    private Business mapResultSetToBusiness(ResultSet rs) throws SQLException {
        Business business = new Business();
        business.setUserId(rs.getInt("user_id"));
        business.setCompanyName(rs.getString("company_name"));
        business.setWebsite(rs.getString("website"));
        business.setIndustry(rs.getString("industry"));
        business.setDescription(rs.getString("description"));
        business.setProfileUpdatedAt(rs.getTimestamp("profile_updated_at"));
        return business;
    }

    // --- Potential future methods ---
    // deleteBusinessProfile(int userId) - Usually handled by UserDAO delete via CASCADE
    // searchBusinessesByIndustry(String industry)
}