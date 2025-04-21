package controller.creator;

import dao.ReportDAO;
import dao.UserDAO; // To verify business exists
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Report;
import model.User;

import java.io.IOException;

@WebServlet("/creator/report-business")
public class ReportBusinessServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReportDAO reportDAO;
    private UserDAO userDAO; // Needed to check if reported user exists and is a business

    public void init() {
        reportDAO = new ReportDAO();
        userDAO = new UserDAO();
        System.out.println("ReportBusinessServlet Initialized");
    }

    // doGet could display the report form if needed separately,
    // but often it's a link/button on a profile or campaign page.
    // We'll focus on doPost to handle the submission.

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ReportBusinessServlet: doPost called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;
        String errorMessage = null;
        String successMessage = null;
        String reportedUserIdParam = request.getParameter("reportedUserId"); // ID of the business user being reported
        String reason = request.getParameter("reason");
        String details = request.getParameter("details");
         String redirectBackUrl = request.getParameter("redirectBackUrl"); // URL to redirect to after reporting


        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isCreator()) { // Only creators report businesses here
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        if (loggedInUser.isBanned()) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?error=banned");
            return;
        }
        // --- End Authorization Check ---

        // Default redirect if not specified
         if (redirectBackUrl == null || redirectBackUrl.trim().isEmpty()) {
             redirectBackUrl = request.getContextPath() + "/creator/dashboard"; // Fallback redirect
         }

        try {
            // 1. Validate Inputs
            if (reportedUserIdParam == null || reportedUserIdParam.trim().isEmpty()) {
                 throw new IllegalArgumentException("Missing reported user ID.");
            }
            int reportedUserId = Integer.parseInt(reportedUserIdParam.trim());
            int reporterUserId = loggedInUser.getUserId();

            if (reporterUserId == reportedUserId) {
                 throw new IllegalArgumentException("You cannot report yourself.");
            }
             if (reason == null || reason.trim().isEmpty() || reason.length() > 255) {
                 throw new IllegalArgumentException("Please provide a reason for the report (max 255 characters).");
             }
             // Details are optional, but might have length limit if needed

             // 2. Verify Reported User Exists and is a Business
             User reportedUser = userDAO.findUserById(reportedUserId);
             if (reportedUser == null) {
                  throw new IllegalArgumentException("The user you are trying to report does not exist.");
             }
             if (!reportedUser.isBusiness()) { // Make sure they are reporting a business
                  throw new IllegalArgumentException("You can only report business accounts using this form.");
             }


             // 3. Create Report Object
             Report newReport = new Report();
             newReport.setReporterUserId(reporterUserId);
             newReport.setReportedUserId(reportedUserId);
             newReport.setReason(reason.trim());
             newReport.setDetails(details != null ? details.trim() : null);
             // Status set to PENDING by DAO

             // 4. Call DAO to create report
             boolean created = reportDAO.createReport(newReport);

             if (created) {
                 successMessage = "Report submitted successfully. Our team will review it.";
                 System.out.println("Report submitted by Creator " + reporterUserId + " against Business " + reportedUserId);
                 // Add success message to session flash attributes for redirect? Simpler just to use query param.
                 redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "reportStatus=success";
             } else {
                 errorMessage = "Failed to submit report due to a server error. Please try again.";
                 System.err.println("ReportDAO.createReport returned false for Reporter " + reporterUserId + ", Reported " + reportedUserId);
                  redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "reportStatus=failed";
             }

        } catch (NumberFormatException e) {
            System.err.println("Invalid Reported User ID format: " + reportedUserIdParam);
            errorMessage = "Invalid user specified for report.";
             redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "reportStatus=invalidUser";
        } catch (IllegalArgumentException e) {
             System.err.println("Report submission validation error: " + e.getMessage());
             errorMessage = e.getMessage(); // Use specific validation error
              redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "reportStatus=validationError&message=" + java.net.URLEncoder.encode(errorMessage, "UTF-8");
        } catch (Exception e) {
            System.err.println("Unexpected error submitting report: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "An unexpected error occurred. Please try again later.";
             redirectBackUrl += (redirectBackUrl.contains("?") ? "&" : "?") + "reportStatus=serverError";
        }

         // --- Redirect Back ---
         // Note: We are redirecting instead of forwarding to avoid issues with form resubmission on refresh.
         // Success/error messages are passed via query parameters (or could use session flash attributes).
         System.out.println("Redirecting back after report attempt to: " + redirectBackUrl);
         response.sendRedirect(redirectBackUrl);
    }
}