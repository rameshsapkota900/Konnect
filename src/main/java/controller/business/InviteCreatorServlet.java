package controller.business;

import dao.CampaignDAO;
import dao.CreatorDAO;
import dao.InviteDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Campaign;
import model.Creator;
import model.Invite;
import model.User;

import java.io.IOException;

@WebServlet("/business/invite")
public class InviteCreatorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private InviteDAO inviteDAO;
    private CampaignDAO campaignDAO; // To verify campaign ownership/status
    private CreatorDAO creatorDAO;   // To verify creator exists

    public void init() {
        inviteDAO = new InviteDAO();
        campaignDAO = new CampaignDAO();
        creatorDAO = new CreatorDAO();
        System.out.println("InviteCreatorServlet Initialized");
    }

    // doGet could potentially display a confirmation page or pre-fill form,
    // but typically the invite is triggered by a button on search results or profile.
    // We focus on doPost to handle the invite creation.

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("InviteCreatorServlet: doPost called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;
        String errorMessage = null;
        String successMessage = null;

        // Parameters expected from the form/button click
        String creatorIdParam = request.getParameter("creatorId");
        String campaignIdParam = request.getParameter("campaignId");
        String inviteMessage = request.getParameter("inviteMessage"); // Optional message
         String redirectBackUrl = request.getParameter("redirectBackUrl"); // Where to go after invite

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isBusiness()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        if (loggedInUser.isBanned()) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?error=banned");
            return;
        }
        // --- End Authorization Check ---

        // Default redirect if not specified (e.g., back to search results)
         if (redirectBackUrl == null || redirectBackUrl.trim().isEmpty()) {
             redirectBackUrl = request.getContextPath() + "/business/search"; // Fallback redirect
         }

        try {
            // 1. Validate Inputs
            if (creatorIdParam == null || campaignIdParam == null) {
                 throw new IllegalArgumentException("Missing creator ID or campaign ID for invite.");
            }
            int creatorId = Integer.parseInt(creatorIdParam.trim());
            int campaignId = Integer.parseInt(campaignIdParam.trim());
            int businessId = loggedInUser.getUserId();

             if (inviteMessage != null && inviteMessage.length() > 1000) { // Limit message length
                  throw new IllegalArgumentException("Invite message is too long (max 1000 characters).");
             }

            // 2. Verify Campaign exists, is active, and owned by the business
            Campaign campaign = campaignDAO.getCampaignById(campaignId, false);
             if (campaign == null) {
                 throw new IllegalArgumentException("The selected campaign does not exist.");
             }
             if (campaign.getBusinessUserId() != businessId) {
                  throw new SecurityException("You do not own this campaign.");
             }
             if (!Campaign.Status.ACTIVE.getStatusName().equalsIgnoreCase(campaign.getStatus())) {
                 throw new IllegalArgumentException("You can only invite creators to active campaigns.");
             }

            // 3. Verify Creator exists (and is not banned - handled by DAO potentially)
             Creator creator = creatorDAO.getCreatorByUserId(creatorId); // Check if creator exists
             if (creator == null) {
                  // Also check if the *user* record exists and isn't banned, in case creator profile missing
                  User creatorUser = new UserDAO().findUserById(creatorId); // Need UserDAO instance
                  if (creatorUser == null || !creatorUser.isCreator() || creatorUser.isBanned()) {
                      throw new IllegalArgumentException("The creator you are trying to invite does not exist or cannot be invited.");
                  }
                  // Allow inviting even if creator profile is sparse, but log a warning?
                   System.out.println("Warning: Inviting Creator ID " + creatorId + " who might have an incomplete profile.");
             }


            // 4. Create Invite Object
            Invite newInvite = new Invite();
            newInvite.setCampaignId(campaignId);
            newInvite.setBusinessUserId(businessId);
            newInvite.setCreatorUserId(creatorId);
            newInvite.setInviteMessage(inviteMessage != null ? inviteMessage.trim() : ""); // Default empty message
            // Status set to PENDING by DAO

            // 5. Call DAO to create invite (DAO checks for duplicates)
            boolean created = inviteDAO.createInvite(newInvite);

            if (created) {
                successMessage = "Invite sent successfully!";
                System.out.println("Invite sent by Business " + businessId + " to Creator " + creatorId + " for Campaign " + campaignId);
                 redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "inviteStatus=success";
            } else {
                 // Check if it failed because invite already exists
                 if (inviteDAO.getInviteByBusinessCreatorCampaign(businessId, creatorId, campaignId) != null) {
                      errorMessage = "You have already invited this creator to this campaign.";
                      redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "inviteStatus=duplicate";
                 } else {
                      errorMessage = "Failed to send invite due to a server error.";
                       redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "inviteStatus=failed";
                       System.err.println("InviteDAO.createInvite returned false.");
                 }
            }

        } catch (NumberFormatException e) {
            System.err.println("Invalid ID format for invite: Creator=" + creatorIdParam + ", Campaign=" + campaignIdParam);
            errorMessage = "Invalid creator or campaign specified.";
             redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "inviteStatus=invalidId";
        } catch (IllegalArgumentException | SecurityException e) {
             System.err.println("Invite validation error: " + e.getMessage());
             errorMessage = e.getMessage();
              redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "inviteStatus=validationError&message=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");
        } catch (Exception e) {
            System.err.println("Unexpected error sending invite: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "An unexpected error occurred.";
            redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "inviteStatus=serverError";
        }

        // --- Redirect Back ---
         // Pass status via query parameters
         System.out.println("Redirecting back after invite attempt to: " + redirectBackUrl);
         response.sendRedirect(redirectBackUrl);
    }
}