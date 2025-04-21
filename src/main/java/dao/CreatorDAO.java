package dao;

import model.Creator;
import model.User; // To potentially join and get email etc.
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreatorDAO {

    /**
     * Creates the initial profile entry for a newly registered creator.
     * Called right after user registration.
     * @param userId The user ID of the new creator.
     * @param displayName The initial display name provided during registration.
     * @return true if creation was successful, false otherwise.
     */
    public boolean createCreatorProfile(int userId, String displayName) {
        String sql = "INSERT INTO creators (user_id, display_name, bio, social_media_links, niche, follower_count, pricing_info, media_kit_path) VALUES (?, ?, '', '{}', '', 0, '', NULL)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, displayName); // Set the initial display name

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Initial creator profile created successfully for User ID: " + userId);
            } else {
                 System.err.println("Failed to create initial creator profile for User ID: " + userId);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
             System.err.println("Error creating creator profile: User ID " + userId + " likely already has a profile. " + e.getMessage());
             // This might happen if registration logic calls this incorrectly multiple times
             success = false; // Or true if profile existing is acceptable here? Depends on logic.
        } catch (SQLException e) {
            System.err.println("SQL Error creating initial creator profile for User ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

    /**
     * Retrieves a creator's profile by their user ID.
     * @param userId The ID of the user whose creator profile is needed.
     * @return Creator object if found, null otherwise.
     */
    public Creator getCreatorByUserId(int userId) {
        String sql = "SELECT * FROM creators WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Creator creator = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                creator = mapResultSetToCreator(rs);
                 // System.out.println("Creator profile found for User ID: " + userId); // Debug
            } else {
                 // System.out.println("No creator profile found for User ID: " + userId); // Debug
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting creator profile for User ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return creator;
    }

    /**
     * Updates an existing creator's profile information.
     * Does not update the user_id (PK).
     * Handles null mediaKitPath to avoid overwriting if not changed.
     * @param creator The Creator object containing updated information.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateCreatorProfile(Creator creator) {
        // Build the SQL query dynamically based on whether media kit is updated
        StringBuilder sqlBuilder = new StringBuilder("UPDATE creators SET display_name = ?, bio = ?, social_media_links = ?, niche = ?, follower_count = ?, pricing_info = ?");
        if (creator.getMediaKitPath() != null) {
             sqlBuilder.append(", media_kit_path = ?");
        }
         sqlBuilder.append(" WHERE user_id = ?");
         String sql = sqlBuilder.toString();

        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        if (creator == null || creator.getUserId() <= 0) {
            System.err.println("Update creator profile failed: Invalid Creator object or User ID.");
            return false;
        }

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, creator.getDisplayName());
            pstmt.setString(2, creator.getBio());
            pstmt.setString(3, creator.getSocialMediaLinks()); // Assume valid JSON or handle validation elsewhere
            pstmt.setString(4, creator.getNiche());
            pstmt.setInt(5, creator.getFollowerCount());
            pstmt.setString(6, creator.getPricingInfo());

            int parameterIndex = 7;
             if (creator.getMediaKitPath() != null) {
                 pstmt.setString(parameterIndex++, creator.getMediaKitPath());
             }
            pstmt.setInt(parameterIndex, creator.getUserId());


            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
             if (success) {
                 System.out.println("Creator profile updated successfully for User ID: " + creator.getUserId());
             } else {
                  System.err.println("Failed to update creator profile for User ID: " + creator.getUserId() + ". Profile might not exist or data unchanged.");
             }
        } catch (SQLException e) {
            System.err.println("SQL Error updating creator profile for User ID " + creator.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }


    /**
     * Searches for creators based on filters (e.g., niche, follower count).
     * Includes pagination support (limit, offset).
     * Joins with the users table to exclude banned users.
     * @param niche Niche to filter by (can be null or empty to ignore).
     * @param minFollowers Minimum follower count (can be 0 or null to ignore).
     * @param limit Max number of results per page.
     * @param offset Starting record number for pagination (0-based).
     * @return List of matching Creator objects, potentially empty.
     */
    public List<Creator> searchCreators(String niche, Integer minFollowers, int limit, int offset) {
        List<Creator> creators = new ArrayList<>();
        // Base query joining creators and users, filtering out banned users
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT c.* FROM creators c JOIN users u ON c.user_id = u.user_id WHERE u.is_banned = FALSE");
        List<Object> params = new ArrayList<>();

        // Append filters dynamically
        if (niche != null && !niche.trim().isEmpty()) {
            sqlBuilder.append(" AND LOWER(c.niche) LIKE LOWER(?)");
            params.add("%" + niche.trim() + "%"); // Use LIKE for partial match
        }
        if (minFollowers != null && minFollowers > 0) {
            sqlBuilder.append(" AND c.follower_count >= ?");
            params.add(minFollowers);
        }

        // Add ordering and pagination
        sqlBuilder.append(" ORDER BY c.follower_count DESC, c.display_name ASC"); // Example order
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        String sql = sqlBuilder.toString();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        System.out.println("Executing Creator Search SQL: " + sql); // Debugging SQL
        System.out.println("With Params: " + params); // Debugging parameters

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                creators.add(mapResultSetToCreator(rs));
            }
             System.out.println("Found " + creators.size() + " creators matching search criteria.");

        } catch (SQLException e) {
            System.err.println("SQL Error searching creators: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return creators;
    }

     /**
      * Counts the total number of creators matching the search criteria (for pagination).
      * Excludes banned users.
      * @param niche Niche filter (can be null/empty).
      * @param minFollowers Follower filter (can be null/0).
      * @return Total count of matching creators, or -1 on error.
      */
     public int countSearchedCreators(String niche, Integer minFollowers) {
         StringBuilder sqlBuilder = new StringBuilder(
             "SELECT COUNT(c.user_id) FROM creators c JOIN users u ON c.user_id = u.user_id WHERE u.is_banned = FALSE");
         List<Object> params = new ArrayList<>();
         int count = -1;

         if (niche != null && !niche.trim().isEmpty()) {
             sqlBuilder.append(" AND LOWER(c.niche) LIKE LOWER(?)");
             params.add("%" + niche.trim() + "%");
         }
         if (minFollowers != null && minFollowers > 0) {
             sqlBuilder.append(" AND c.follower_count >= ?");
             params.add(minFollowers);
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
             System.err.println("SQL Error counting searched creators: " + e.getMessage());
             e.printStackTrace();
         } finally {
             DBUtil.closeResources(rs, pstmt, conn);
         }
         return count;
     }


    /**
     * Retrieves a list of all creators (potentially for admin use).
     * Joins with users table to filter out banned users.
     * @return List of all non-banned Creator objects.
     */
     public List<Creator> getAllCreators() {
        List<Creator> creators = new ArrayList<>();
        String sql = "SELECT c.* FROM creators c JOIN users u ON c.user_id = u.user_id WHERE u.is_banned = FALSE ORDER BY c.display_name";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                creators.add(mapResultSetToCreator(rs));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting all creators: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return creators;
    }


    /**
     * Helper method to map a ResultSet row to a Creator object.
     * @param rs The ResultSet containing creator data.
     * @return A populated Creator object.
     * @throws SQLException If a database access error occurs.
     */
    private Creator mapResultSetToCreator(ResultSet rs) throws SQLException {
        Creator creator = new Creator();
        creator.setUserId(rs.getInt("user_id"));
        creator.setDisplayName(rs.getString("display_name"));
        creator.setBio(rs.getString("bio"));
        creator.setSocialMediaLinks(rs.getString("social_media_links")); // Raw JSON string
        creator.setNiche(rs.getString("niche"));
        creator.setFollowerCount(rs.getInt("follower_count"));
        creator.setPricingInfo(rs.getString("pricing_info"));
        creator.setMediaKitPath(rs.getString("media_kit_path"));
        creator.setProfileUpdatedAt(rs.getTimestamp("profile_updated_at"));
        return creator;
    }

    // --- Potential future methods ---
    // deleteCreatorProfile(int userId) - Usually handled by UserDAO delete via CASCADE
    // getTopCreators(int limit)
    // getCreatorsByNiche(String niche)
}