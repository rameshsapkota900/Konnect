package controller.business;

import dao.ApplicationDAO;
import dao.CampaignDAO; // To verify campaign ownership
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
import java.util.List;

@WebServlet("/business/applicants")
public class ViewApplicantsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ApplicationDAO applicationDAO;
    private CampaignDAO campaignDAO;

    public void init() {
        applicationDAO = new ApplicationDAO();
        campaignDAO = new CampaignDAO();
        System.out.println("ViewApplicantsServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ViewApplicantsServlet: doGet called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;
        String campaignIdParam = request.getParameter("campaignId");

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

         // Check for messages from POST actions
        if ("statusUpdated".equals(request.getParameter("success"))) {
             request.setAttribute("successMessage", "Applicant status updated successfully.");
        } else if ("updateFailed".equals(request.getParameter("error"))) {
             request.setAttribute("errorMessage", "Failed to update applicant status.");
        } else if ("invalidId".equals(request.getParameter("error"))) {
             request.setAttribute("errorMessage", "Invalid application or campaign specified.");
        }

        if (campaignIdParam == null || campaignIdParam.trim().isEmpty()) {
             System.err.println("Missing campaignId parameter for viewing applicants.");
             request.setAttribute("errorMessage", "Please select a campaign to view applicants.");
             response.sendRedirect(request.getContextPath() + "/business/campaigns?error=missingId");
             return;
         }

        try {
            int campaignId = Integer.parseInt(campaignIdParam.trim());
            int businessId = loggedInUser.getUserId();

            // 1. Verify Business Owns the Campaign
            Campaign campaign = campaignDAO.getCampaignById(campaignId, false);
            if (campaign == null || campaign.getBusinessUserId() != businessId) {
                System.err.println("Permission denied: Business " + businessId + " attempting to view applicants for Campaign " + campaignId);
                request.setAttribute("errorMessage", "You do not have permission to view applicants for this campaign.");
                 response.sendRedirect(request.getContextPath() + "/business/campaigns?error=permission");
                 return;
            }
             request.setAttribute("campaign", campaign); // Send campaign info to JSP

            // 2. Fetch Applicants for the Campaign
            List<Application> applicants = applicationDAO.getApplicationsByCampaignId(campaignId);
            request.setAttribute("applicants", applicants);
            System.out.println("Fetched " + applicants.size() + " applicants for Campaign ID: " + campaignId);

            // 3. Forward to JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/view_applicants.jsp");
            dispatcher.forward(request, response);

        } catch (NumberFormatException e) {
             System.err.println("Invalid Campaign ID format: " + campaignIdParam);
             request.setAttribute("errorMessage", "Invalid campaign specified.");
             response.sendRedirect(request.getContextPath() + "/business/campaigns?error=invalidIdFormat");
        } catch (Exception e) {
            System.err.println("Error fetching applicants for Campaign ID " + campaignIdParam + ": " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while loading applicants.");
            // Redirect back to campaign list might be safer than error page here
             response.sendRedirect(request.getContextPath() + "/business/campaigns?error=serverError");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ViewApplicantsServlet: doPost called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

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

         String action = request.getParameter("action"); // e.g., "accept", "reject"
         String applicationIdParam = request.getParameter("applicationId");
         String campaignIdParam = request.getParameter("campaignId"); // Need this to redirect back correctly
         String redirectUrl = request.getContextPath() + "/business/applicants?campaignId=" + campaignIdParam; // Base redirect

         if (applicationIdParam != null && campaignIdParam != null && (action.equals("accept") || action.equals("reject"))) {
              System.out.println("Action: " + action + ", Application ID: " + applicationIdParam + ", Campaign ID: " + campaignIdParam);
             try {
                 int applicationId = Integer.parseInt(applicationIdParam);
                 int campaignId = Integer.parseInt(campaignIdParam); // For verification
                 int businessId = loggedInUser.getUserId();
                 String newStatus = action.equals("accept") ? Application.Status.ACCEPTED.getStatusName() : Application.Status.REJECTED.getStatusName();

                  // DAO method verifies business ownership of the campaign linked to the application
                 boolean updated = applicationDAO.updateApplicationStatus(applicationId, newStatus, businessId);

                 if (updated) {
                      System.out.println("Applicant status update successful for Application ID: " + applicationId);
                     redirectUrl += "&success=statusUpdated";
                 } else {
                      System.err.println("Applicant status update failed for Application ID: " + applicationId);
                     redirectUrl += "&error=updateFailed";
                 }

             } catch (NumberFormatException e) {
                  System.err.println("Invalid ID format for status update: AppID=" + applicationIdParam + ", CampID=" + campaignIdParam);
                  redirectUrl += "&error=invalidId";
             } catch (Exception e) {
                  System.err.println("Error processing applicant status update: " + e.getMessage());
                  e.printStackTrace();
                  redirectUrl += "&error=serverError";
             }
         } else {
              System.err.println("Invalid action or missing parameters for applicant status update.");
               // Adjust redirect if campaignIdParam was also missing
               if (campaignIdParam == null || campaignIdParam.trim().isEmpty()) {
                    redirectUrl = request.getContextPath() + "/business/campaigns?error=missingParams";
               } else {
                   redirectUrl += "&error=badRequest";
               }
         }

          // Redirect back to the applicants list page
          response.sendRedirect(redirectUrl);
    }
}