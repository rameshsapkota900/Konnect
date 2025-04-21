package controller.creator;

import dao.ApplicationDAO;
import dao.CampaignDAO; // To check if campaign exists and is active
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Application;
import model.Campaign;
import model.User;

import java.io.IOException;

@WebServlet("/creator/apply")
public class ApplyToCampaignServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ApplicationDAO applicationDAO;
    private CampaignDAO campaignDAO; // To verify campaign status

    public void init() {
        applicationDAO = new ApplicationDAO();
        campaignDAO = new CampaignDAO();
        System.out.println("ApplyToCampaignServlet Initialized");
    }

    // doGet could potentially show the application form again if there was an error on POST,
    // but typically the application form is part of the campaign detail page.
    // We'll primarily use doPost here.
    /*
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect to campaign view or show an error if accessed directly?
        response.sendRedirect(request.getContextPath() + "/creator/campaigns");
    }
    */

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ApplyToCampaignServlet: doPost called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;
        String errorMessage = null;
        String successMessage = null;
        String campaignIdParam = request.getParameter("campaignId");
        int campaignId = -1;


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

        try {
            // 1. Get and Validate Campaign ID
            if (campaignIdParam == null || campaignIdParam.trim().isEmpty()) {
                 throw new IllegalArgumentException("Campaign ID is missing.");
            }
             campaignId = Integer.parseInt(campaignIdParam.trim());

             // 2. Get Pitch Message
             String pitchMessage = request.getParameter("pitchMessage");
             if (pitchMessage == null || pitchMessage.trim().isEmpty()) {
                 throw new IllegalArgumentException("Your pitch message cannot be empty.");
             }
              // Optional: Add length validation for pitch message

             // 3. Verify Campaign Exists and is Active
             Campaign campaign = campaignDAO.getCampaignById(campaignId, false); // Don't need full details here
             if (campaign == null) {
                  throw new IllegalArgumentException("The campaign you are trying to apply to does not exist.");
             }
             if (!Campaign.Status.ACTIVE.getStatusName().equalsIgnoreCase(campaign.getStatus())) {
                  throw new IllegalArgumentException("This campaign is no longer active and cannot be applied to.");
             }

             // 4. Check if Already Applied (redundant check, DAO also checks, but good practice)
             if (applicationDAO.getApplicationByCreatorAndCampaign(loggedInUser.getUserId(), campaignId) != null) {
                 throw new IllegalArgumentException("You have already applied to this campaign.");
             }

             // 5. Create Application Object
             Application newApplication = new Application();
             newApplication.setCampaignId(campaignId);
             newApplication.setCreatorUserId(loggedInUser.getUserId());
             newApplication.setPitchMessage(pitchMessage.trim());
             // Status will be set to PENDING by DAO

             // 6. Call DAO to create application
             boolean created = applicationDAO.createApplication(newApplication);

             if (created) {
                 successMessage = "Application submitted successfully!";
                 System.out.println("Application submitted by Creator ID " + loggedInUser.getUserId() + " for Campaign ID " + campaignId);
                 // Redirect to 'My Applications' page after success
                  response.sendRedirect(request.getContextPath() + "/creator/applications?success=applied");
                  return; // Important: Stop further processing after redirect
             } else {
                 // Check again if it failed because they already applied (race condition?)
                 if (applicationDAO.getApplicationByCreatorAndCampaign(loggedInUser.getUserId(), campaignId) != null) {
                    errorMessage = "You have already applied to this campaign.";
                 } else {
                    errorMessage = "Failed to submit application due to a server error. Please try again.";
                 }
                  System.err.println("ApplicationDAO.createApplication returned false for Creator " + loggedInUser.getUserId() + ", Campaign " + campaignId);
             }

        } catch (NumberFormatException e) {
            System.err.println("Invalid Campaign ID format on application submission: " + campaignIdParam);
            errorMessage = "Invalid campaign specified.";
        } catch (IllegalArgumentException e) {
             System.err.println("Application submission validation error: " + e.getMessage());
             errorMessage = e.getMessage(); // Use the specific validation error message
        } catch (Exception e) {
            System.err.println("Unexpected error applying to campaign: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "An unexpected error occurred. Please try again later.";
        }

        // --- Error Handling ---
        // If we reach here, something went wrong (validation error or DB error)
        // Forward back to the campaign detail page with the error message
         System.out.println("Application failed, forwarding back to campaign detail page (ID: " + campaignId + ") with error: " + errorMessage);
        request.setAttribute("errorMessage", errorMessage);
         // We need the campaign object again to display the detail page correctly
         if (campaignId > 0) {
             Campaign campaign = campaignDAO.getCampaignById(campaignId, true); // Get full details for display
             request.setAttribute("campaign", campaign);
             // Also keep the pitch message the user entered
             request.setAttribute("pitchMessage", request.getParameter("pitchMessage"));
         } else {
              // If campaign ID was invalid, maybe redirect to list view?
              response.sendRedirect(request.getContextPath() + "/creator/campaigns?error=applyFailedInvalidId");
              return;
         }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/creator/campaign_detail.jsp");
        dispatcher.forward(request, response);
    }
}