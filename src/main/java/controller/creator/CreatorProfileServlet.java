package controller.creator;

import dao.CreatorDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.Creator;
import model.User;
import util.FileUploadUtil;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;

// Enable multipart config for file uploads
@WebServlet("/creator/profile")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1,  // 1 MB threshold
    maxFileSize = 1024 * 1024 * 10, // 10 MB limit per file
    maxRequestSize = 1024 * 1024 * 15 // 15 MB total request size limit
)
public class CreatorProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CreatorDAO creatorDAO;

    public void init() {
        creatorDAO = new CreatorDAO();
        System.out.println("CreatorProfileServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CreatorProfileServlet: doGet called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isCreator()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
         if (loggedInUser.isBanned()) {
             session.invalidate();
             response.sendRedirect(request.getContextPath() + "/login?error=banned");
             return;
         }
        // --- End Authorization Check ---

        String action = request.getParameter("action");
        int creatorId = loggedInUser.getUserId();

        try {
            Creator creatorProfile = creatorDAO.getCreatorByUserId(creatorId);

            if (creatorProfile == null) {
                 // Handle case where profile doesn't exist yet (might happen after registration issues)
                 System.out.println("Creator profile not found for User ID: " + creatorId + ". Displaying empty form.");
                 // Create a new empty Creator object to avoid nulls in JSP
                 creatorProfile = new Creator();
                 creatorProfile.setUserId(creatorId);
                 // Set a flag or message indicating the profile is new/incomplete
                 request.setAttribute("isNewProfile", true);
                 request.setAttribute("infoMessage", "Please complete your creator profile.");
            }

            request.setAttribute("creatorProfile", creatorProfile);
            // If action is "edit", show the form. Otherwise (or null action), default to showing the form.
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/creator/profile.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error loading creator profile page for User ID " + creatorId + ": " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error loading profile.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CreatorProfileServlet: doPost called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;
        String errorMessage = null;
        String successMessage = null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isCreator()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
         if (loggedInUser.isBanned()) {
            // Technically should not happen if checked in doGet, but good practice
             session.invalidate();
             response.sendRedirect(request.getContextPath() + "/login?error=banned");
             return;
         }
        // --- End Authorization Check ---

        int creatorId = loggedInUser.getUserId();
        Creator currentProfile = creatorDAO.getCreatorByUserId(creatorId); // Get current profile for comparison/updates

        if (currentProfile == null) {
             // Should ideally not happen if doGet handled it, but as a safeguard
             System.err.println("CRITICAL: Attempting to POST profile update, but no existing profile found for User ID: " + creatorId);
             errorMessage = "Could not find your profile to update. Please contact support.";
              request.setAttribute("errorMessage", errorMessage);
              RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/creator/profile.jsp"); // Show form again
              dispatcher.forward(request, response);
             return;
        }

        try {
            // 1. Retrieve Form Data
            String displayName = request.getParameter("displayName");
            String bio = request.getParameter("bio");
            String niche = request.getParameter("niche");
            String pricingInfo = request.getParameter("pricingInfo");
            String followerCountStr = request.getParameter("followerCount");

            // Social Links - simple example assuming direct input for now
            // For robust implementation, use multiple fields or JavaScript to build JSON
            String instagramLink = request.getParameter("social_instagram");
            String youtubeLink = request.getParameter("social_youtube");
            String tiktokLink = request.getParameter("social_tiktok");
             // Construct JSON string (requires careful handling of quotes and nulls)
             // A JSON library would be much better here.
             StringBuilder socialJson = new StringBuilder("{");
             if (instagramLink != null && !instagramLink.trim().isEmpty()) socialJson.append("\"instagram\":\"").append(escapeJson(instagramLink.trim())).append("\",");
             if (youtubeLink != null && !youtubeLink.trim().isEmpty()) socialJson.append("\"youtube\":\"").append(escapeJson(youtubeLink.trim())).append("\",");
             if (tiktokLink != null && !tiktokLink.trim().isEmpty()) socialJson.append("\"tiktok\":\"").append(escapeJson(tiktokLink.trim())).append("\",");
             // Remove trailing comma if any fields were added
             if (socialJson.length() > 1) socialJson.deleteCharAt(socialJson.length() - 1);
             socialJson.append("}");
             String socialMediaLinksJson = socialJson.toString();


             // Validate and parse follower count
             int followerCount = 0; // Default
             if (followerCountStr != null && !followerCountStr.trim().isEmpty()) {
                 try {
                     followerCount = Integer.parseInt(followerCountStr.trim());
                     if (followerCount < 0) followerCount = 0; // Ensure non-negative
                 } catch (NumberFormatException e) {
                     errorMessage = "Invalid follower count. Please enter a valid number.";
                     // Keep existing value? Or set to 0? Let's keep existing for now.
                     followerCount = currentProfile.getFollowerCount();
                 }
             } else {
                  // If empty, keep existing value or set to 0? Let's keep existing.
                  followerCount = currentProfile.getFollowerCount();
             }


            // 2. Handle File Upload (Media Kit)
            Part filePart = request.getPart("mediaKitFile"); // Matches the name attribute in the form's file input
             String uploadedFilePath = null; // Relative path to store in DB
             String oldMediaPath = currentProfile.getMediaKitPath(); // Get path of the old file

             if (filePart != null && filePart.getSize() > 0) {
                 System.out.println("Media kit file received: " + filePart.getSubmittedFileName() + ", Size: " + filePart.getSize());
                 // Attempt to save the file
                 uploadedFilePath = FileUploadUtil.saveFile(request, filePart, "media_kits");

                 if (uploadedFilePath != null) {
                     System.out.println("New media kit saved successfully: " + uploadedFilePath);
                     // If upload successful and there was an old file, delete the old one
                     if (oldMediaPath != null && !oldMediaPath.isEmpty()) {
                         System.out.println("Attempting to delete old media kit: " + oldMediaPath);
                         boolean deleted = FileUploadUtil.deleteFile(request, oldMediaPath);
                         if (!deleted) {
                             System.err.println("Warning: Failed to delete old media kit file: " + oldMediaPath);
                             // Log this, but don't necessarily fail the whole profile update
                         } else {
                              System.out.println("Old media kit deleted successfully.");
                         }
                     }
                 } else {
                     // File upload failed (error message should be set in request by FileUploadUtil)
                     errorMessage = (String) request.getAttribute("fileError");
                      if (errorMessage == null) errorMessage = "Failed to upload media kit."; // Default error
                      System.err.println("Media kit upload failed for User ID: " + creatorId);
                      // Keep the old file path if upload fails
                      uploadedFilePath = oldMediaPath;
                 }
             } else {
                  System.out.println("No new media kit file uploaded or file was empty.");
                  // No new file uploaded, keep the existing path
                 uploadedFilePath = oldMediaPath;
             }


            // 3. Update Creator Object (only if no critical errors so far)
            if (errorMessage == null) {
                 currentProfile.setDisplayName(displayName != null ? displayName.trim() : "");
                 currentProfile.setBio(bio != null ? bio.trim() : "");
                 currentProfile.setNiche(niche != null ? niche.trim() : "");
                 currentProfile.setPricingInfo(pricingInfo != null ? pricingInfo.trim() : "");
                 currentProfile.setFollowerCount(followerCount);
                 currentProfile.setSocialMediaLinks(socialMediaLinksJson);
                 // Only set the path if it changed or was newly uploaded
                 currentProfile.setMediaKitPath(uploadedFilePath);
                 // The DAO update method handles the timestamp automatically

                 // 4. Call DAO to update the database
                 boolean updated = creatorDAO.updateCreatorProfile(currentProfile);

                 if (updated) {
                     successMessage = "Profile updated successfully!";
                     System.out.println("Creator profile database update successful for User ID: " + creatorId);
                 } else {
                     errorMessage = "Failed to update profile in database. Please try again.";
                      System.err.println("CreatorDAO.updateCreatorProfile returned false for User ID: " + creatorId);
                 }
            }


            // 5. Set attributes and forward back to profile page
            request.setAttribute("creatorProfile", currentProfile); // Send the updated (or attempted update) profile back
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
            }
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/creator/profile.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error processing creator profile update for User ID " + creatorId + ": " + e.getMessage());
            e.printStackTrace();
             request.setAttribute("creatorProfile", currentProfile); // Send back the last known profile state
            request.setAttribute("errorMessage", "An unexpected error occurred while updating your profile.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/creator/profile.jsp");
            dispatcher.forward(request, response);
        }
    }

     /**
      * Basic JSON string escaping. Replace with a library for robustness.
      */
     private String escapeJson(String str) {
         if (str == null) return "";
         return str.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
     }
}