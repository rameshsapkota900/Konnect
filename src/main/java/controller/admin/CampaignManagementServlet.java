package controller.admin;

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

@WebServlet("/admin/campaigns")
public class CampaignManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CampaignDAO campaignDAO;

    public void init() {
        campaignDAO = new CampaignDAO();
        System.out.println("CampaignManagementServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CampaignManagementServlet: doGet called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        // --- End Authorization Check ---

        // No specific success/error messages needed from POST for simple view

        try {
            System.out.println("Fetching all campaigns for admin view.");
            // Use the specific DAO method that includes business name
            List<Campaign> campaigns = campaignDAO.getAllCampaignsForAdmin();

            request.setAttribute("campaigns", campaigns);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/admin/campaign_list.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error fetching campaigns for admin: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while loading campaign data.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
            dispatcher.forward(request, response);
        }
    }
}