package controller.admin;

import dao.ReportDAO;
import dao.UserDAO; // May need to ban user based on report
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

@WebServlet("/admin/reports")
public class ReportManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReportDAO reportDAO;
    private UserDAO userDAO; // To potentially ban users

    public void init() {
        reportDAO = new ReportDAO();
        userDAO = new UserDAO(); // Initialize UserDAO
        System.out.println("ReportManagementServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ReportManagementServlet: doGet called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        // --- End Authorization Check ---

        // Check for messages from POST actions
        if ("statusUpdated".equals(request.getParameter("success"))) request.setAttribute("successMessage", "Report status updated successfully.");
        if ("actionTaken".equals(request.getParameter("success"))) request.setAttribute("successMessage", "Report action taken successfully.");
        if ("updateFailed".equals(request.getParameter("error"))) request.setAttribute("errorMessage", "Failed to update report status.");
         if ("invalidId".equals(request.getParameter("error"))) request.setAttribute("errorMessage", "Invalid report or user specified.");


        try {
            System.out.println("Fetching all reports for admin view.");
            // Use DAO method that includes user emails
            List<Report> reports = reportDAO.getAllReports();

            request.setAttribute("reports", reports);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/admin/report_list.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error fetching reports for admin: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while loading reports.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ReportManagementServlet: doPost called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        // --- End Authorization Check ---

        String action = request.getParameter("action"); // e.g., "updateStatus", "banUser"
        String reportIdParam = request.getParameter("reportId");
        String redirectUrl = request.getContextPath() + "/admin/reports";

        if (reportIdParam != null && action != null) {
             System.out.println("Action: " + action + ", Report ID: " + reportIdParam);
            try {
                int reportId = Integer.parseInt(reportIdParam);
                boolean success = false;
                String successParam = "statusUpdated"; // Default success parameter

                if ("updateStatus".equals(action)) {
                    String newStatus = request.getParameter("newStatus");
                     if (newStatus != null && !newStatus.trim().isEmpty()) {
                          success = reportDAO.updateReportStatus(reportId, newStatus.trim());
                          if (!success) redirectUrl += "?error=updateFailed";
                     } else {
                          System.err.println("Missing newStatus parameter for updateStatus action.");
                           redirectUrl += "?error=missingStatus";
                     }

                } else if ("banUserBasedOnReport".equals(action)) {
                     // 1. Get the reported user ID from the report
                     Report report = reportDAO.getReportById(reportId, false);
                     if (report != null) {
                         int reportedUserId = report.getReportedUserId();
                          // Prevent admin self-ban via report
                          if (reportedUserId == loggedInUser.getUserId()) {
                               System.err.println("Admin attempted self-ban via report ID: " + reportId);
                               redirectUrl += "?error=selfBanAttempt";
                          } else {
                              // 2. Ban the user
                              boolean banSuccess = userDAO.updateUserBanStatus(reportedUserId, true);
                              if (banSuccess) {
                                   // 3. Update report status to 'action_taken'
                                   success = reportDAO.updateReportStatus(reportId, Report.Status.ACTION_TAKEN.getStatusName());
                                   successParam = "actionTaken"; // Specific success message
                                   if (!success) System.err.println("User banned, but failed to update report status for ID: " + reportId);
                              } else {
                                   System.err.println("Failed to ban user ID " + reportedUserId + " based on report " + reportId);
                                   redirectUrl += "?error=banFailed";
                              }
                          }
                     } else {
                          System.err.println("Report not found for ban action: ID=" + reportId);
                           redirectUrl += "?error=reportNotFound";
                     }
                      // Ensure success flag reflects the overall outcome for redirect parameter
                      success = success && (redirectUrl.contains("?error=") == false);


                } else if ("dismissReport".equals(action)) {
                      success = reportDAO.updateReportStatus(reportId, Report.Status.DISMISSED.getStatusName());
                      if (!success) redirectUrl += "?error=updateFailed";

                } else {
                     System.err.println("Invalid action received for report management: " + action);
                      redirectUrl += "?error=invalidAction";
                }

                if (success) {
                    redirectUrl += (redirectUrl.contains("?") ? "&" : "?") + "success=" + successParam;
                }
                // Error parameters are added directly within the logic blocks

            } catch (NumberFormatException e) {
                System.err.println("Invalid Report ID format: " + reportIdParam);
                 redirectUrl += "?error=invalidId";
            } catch (Exception e) {
                System.err.println("Error processing report action: " + e.getMessage());
                e.printStackTrace();
                 redirectUrl += "?error=serverError";
            }
        } else {
             System.err.println("Missing action or reportId for report management POST.");
             redirectUrl += "?error=badRequest";
        }

        response.sendRedirect(redirectUrl);
    }
}