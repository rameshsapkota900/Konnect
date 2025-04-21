package controller.business;

import dao.ApplicationDAO;
import dao.BusinessDAO;
import dao.CampaignDAO;
import dao.InviteDAO; // May need for dashboard counts
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Business;
import model.Campaign;
import model.User;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import model.Application; // Import Application model

@WebServlet("/business/dashboard")
public class BusinessDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private BusinessDAO businessDAO;
    private CampaignDAO campaignDAO;
    private ApplicationDAO applicationDAO; // To count applicants

    public void init() {
        businessDAO = new BusinessDAO();
        campaignDAO = new CampaignDAO();
        applicationDAO = new ApplicationDAO();
        System.out.println("BusinessDashboardServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("BusinessDashboardServlet: doGet called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isBusiness()) {
            System.out.println("User not logged in or not a business. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
         if (loggedInUser.isBanned()) {
            System.out.println("Banned business accessing dashboard. Redirecting to login.");
             session.invalidate();
             response.sendRedirect(request.getContextPath() + "/login?error=banned");
             return;
         }
        // --- End Authorization Check ---

        try {
            int businessId = loggedInUser.getUserId();
            System.out.println("Fetching dashboard data for Business ID: " + businessId);

            // 1. Get Business Profile
            Business businessProfile = businessDAO.getBusinessByUserId(businessId);
             if (businessProfile == null) {
                 System.err.println("CRITICAL: Business profile not found for User ID: " + businessId + ". Redirecting to profile creation/edit.");
                 request.setAttribute("errorMessage", "Your business profile is missing. Please complete it.");
                 response.sendRedirect(request.getContextPath() + "/business/profile?action=edit");
                 return;
            }
            request.setAttribute("businessProfile", businessProfile);
             System.out.println("Business profile loaded: " + businessProfile.getCompanyName());

            // 2. Get Business's Campaigns (Active ones for summary)
            List<Campaign> allCampaigns = campaignDAO.getCampaignsByBusinessId(businessId); // Gets non-deleted

             // Filter for active campaigns
             List<Campaign> activeCampaigns = allCampaigns.stream()
                 .filter(c -> Campaign.Status.ACTIVE.getStatusName().equalsIgnoreCase(c.getStatus()))
                 .collect(Collectors.toList());
                 request.setAttribute("activeCampaignCount", (long) activeCampaigns.size());
             System.out.println("Active Campaign Count: " + activeCampaigns.size());

             // 3. Get Total Pending Applicants Count across all active campaigns
             long totalPendingApplicants = 0;
             for (Campaign campaign : activeCampaigns) {
                 List<Application> applicants = applicationDAO.getApplicationsByCampaignId(campaign.getCampaignId());
                 totalPendingApplicants += applicants.stream()
                     .filter(app -> Application.Status.PENDING.getStatusName().equalsIgnoreCase(app.getStatus()))
                     .count();
             }
             request.setAttribute("pendingApplicantCount", totalPendingApplicants);
              System.out.println("Total Pending Applicant Count: " + totalPendingApplicants);


             // 4. Check Profile Completeness (Simple Example)
             boolean profileComplete = businessProfile.getWebsite() != null && !businessProfile.getWebsite().trim().isEmpty() &&
                                       businessProfile.getIndustry() != null && !businessProfile.getIndustry().trim().isEmpty() &&
                                       businessProfile.getDescription() != null && !businessProfile.getDescription().trim().isEmpty();
             request.setAttribute("isProfileComplete", profileComplete);
              System.out.println("Profile Complete Status: " + profileComplete);


            // 5. Forward to the JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/dashboard.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error loading business dashboard for User ID " + loggedInUser.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An unexpected error occurred while loading your dashboard.");
             RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
             dispatcher.forward(request, response);
        }
    }

    // doPost not typically used for dashboard view
    // protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //     doGet(request, response);
    // }
}