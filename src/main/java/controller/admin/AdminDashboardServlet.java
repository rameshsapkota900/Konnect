package controller.admin;

import dao.CampaignDAO;
import dao.ReportDAO;
import dao.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Report;
import model.User;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private CampaignDAO campaignDAO;
    private ReportDAO reportDAO;

    public void init() {
        userDAO = new UserDAO();
        campaignDAO = new CampaignDAO();
        reportDAO = new ReportDAO();
        System.out.println("AdminDashboardServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("AdminDashboardServlet: doGet called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            System.out.println("User not logged in or not an admin. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        // Admins cannot be banned through the normal UI, but check anyway might be good
        // if (loggedInUser.isBanned()) { ... }
        // --- End Authorization Check ---

        try {
            System.out.println("Fetching dashboard data for Admin: " + loggedInUser.getEmail());

            // 1. Get User Counts
            long totalUsers = userDAO.countTotalUsers();
            long creatorCount = userDAO.countUsersByRole(User.Role.CREATOR.getRoleName());
            long businessCount = userDAO.countUsersByRole(User.Role.BUSINESS.getRoleName());
             // Optional: Count banned users
             List<User> allUsers = userDAO.getAllUsers(); // Less efficient but needed if no dedicated count method
             long bannedCount = allUsers.stream().filter(User::isBanned).count();


            request.setAttribute("totalUserCount", totalUsers);
            request.setAttribute("creatorCount", creatorCount);
            request.setAttribute("businessCount", businessCount);
             request.setAttribute("bannedUserCount", bannedCount);

            System.out.println("User Counts: Total=" + totalUsers + ", Creators=" + creatorCount + ", Businesses=" + businessCount + ", Banned=" + bannedCount);


            // 2. Get Campaign Count
            long totalCampaigns = campaignDAO.countTotalCampaigns();
            request.setAttribute("totalCampaignCount", totalCampaigns);
             System.out.println("Total Campaign Count: " + totalCampaigns);

            // 3. Get Pending Reports Count
             List<Report> allReports = reportDAO.getAllReports(); // Get all reports
             long pendingReportCount = allReports.stream()
                 .filter(r -> Report.Status.PENDING.getStatusName().equalsIgnoreCase(r.getStatus()))
                 .count();
             request.setAttribute("pendingReportCount", pendingReportCount);
             System.out.println("Pending Report Count: " + pendingReportCount);

            // Optional: Fetch recent users, recent campaigns, etc. for display

            // 4. Forward to the JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/admin/dashboard.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error loading admin dashboard for User ID " + loggedInUser.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An unexpected error occurred while loading the admin dashboard.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    // No POST actions expected for the main dashboard view
    // protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //     doGet(request, response);
    // }
}