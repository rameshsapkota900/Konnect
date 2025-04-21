package controller.creator;

import dao.CampaignDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Campaign;
import model.User;

import java.io.IOException;
import java.util.List;

@WebServlet("/creator/campaigns")
public class ViewCampaignsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CampaignDAO campaignDAO;
    private static final int CAMPAIGNS_PER_PAGE = 9; // Number of campaigns per page

    public void init() {
        campaignDAO = new CampaignDAO();
        System.out.println("ViewCampaignsServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ViewCampaignsServlet: doGet called");
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
        String campaignIdParam = request.getParameter("id");

        try {
            if ("viewDetail".equals(action) && campaignIdParam != null) {
                 // --- View Campaign Detail ---
                int campaignId = Integer.parseInt(campaignIdParam);
                System.out.println("Action: viewDetail, Campaign ID: " + campaignId);
                Campaign campaign = campaignDAO.getCampaignById(campaignId, true); // Include business details

                if (campaign != null && Campaign.Status.ACTIVE.getStatusName().equalsIgnoreCase(campaign.getStatus())) {
                    request.setAttribute("campaign", campaign);
                    // Check if creator has already applied (using ApplicationDAO - needs to be added if not already)
                    // ApplicationDAO applicationDAO = new ApplicationDAO();
                    // Application existingApp = applicationDAO.getApplicationByCreatorAndCampaign(loggedInUser.getUserId(), campaignId);
                    // request.setAttribute("existingApplication", existingApp);
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/creator/campaign_detail.jsp");
                    dispatcher.forward(request, response);
                } else {
                    System.out.println("Campaign not found or not active for ID: " + campaignId);
                    request.setAttribute("errorMessage", "Campaign not found or is no longer active.");
                    // Redirect back to the campaign list
                    response.sendRedirect(request.getContextPath() + "/creator/campaigns?error=notFound");
                }
            } else {
                 // --- List Available Campaigns (Default Action) ---
                 System.out.println("Action: list campaigns");
                 String searchTerm = request.getParameter("search");
                 String pageParam = request.getParameter("page");
                 int currentPage = 1;
                 if (pageParam != null) {
                     try {
                         currentPage = Integer.parseInt(pageParam);
                         if (currentPage < 1) currentPage = 1;
                     } catch (NumberFormatException e) {
                         currentPage = 1; // Default to page 1 if param is invalid
                     }
                 }

                 int offset = (currentPage - 1) * CAMPAIGNS_PER_PAGE;

                 // Fetch campaigns for the current page
                 List<Campaign> campaigns = campaignDAO.getActiveCampaignsForCreators(searchTerm, CAMPAIGNS_PER_PAGE, offset);
                 // Fetch total count for pagination
                 int totalCampaigns = campaignDAO.countActiveCampaignsForCreators(searchTerm);
                 int totalPages = (int) Math.ceil((double) totalCampaigns / CAMPAIGNS_PER_PAGE);

                 request.setAttribute("campaigns", campaigns);
                 request.setAttribute("currentPage", currentPage);
                 request.setAttribute("totalPages", totalPages);
                 request.setAttribute("totalCampaigns", totalCampaigns);
                 request.setAttribute("searchTerm", searchTerm); // Send search term back to JSP

                 System.out.println("Displaying campaign list: Page " + currentPage + "/" + totalPages + ", Total Found: " + totalCampaigns);

                 RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/creator/view_campaigns.jsp");
                 dispatcher.forward(request, response);
            }

        } catch (NumberFormatException e) {
             System.err.println("Invalid Campaign ID format: " + campaignIdParam);
             request.setAttribute("errorMessage", "Invalid campaign ID.");
             response.sendRedirect(request.getContextPath() + "/creator/campaigns?error=invalidId");
        } catch (Exception e) {
            System.err.println("Error in ViewCampaignsServlet: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while retrieving campaign data.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
            dispatcher.forward(request, response);
        }
    }

     // No doPost needed typically for just viewing, unless adding search form submission via POST
     // protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     //      // Handle POST search if needed, then call doGet or forward
     //      doGet(request, response);
     // }
}