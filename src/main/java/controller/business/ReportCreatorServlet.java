package controller.business;

import dao.ReportDAO;
import dao.UserDAO; // To verify creator exists
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Report;
import model.User;

import java.io.IOException;

@WebServlet("/business/report-creator")
public class ReportCreatorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReportDAO reportDAO;
    private UserDAO userDAO; // Needed to check if reported user exists and is a creator

    public void init() {
        reportDAO = new ReportDAO();
        userDAO = new UserDAO();
        System.out.println("ReportCreatorServlet Initialized");
    }

    // doGet might show a dedicated report form if needed, but often reports
    // are initiated from creator profiles or search results via POST.
    /*
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Example: Pre-fill reported user ID if provided
        String reportedIdParam = request.getParameter("reportedUserId");
        if (reportedIdParam != null) {
            try {
                int reportedId = Integer.parseInt(reportedIdParam);
                User reportedUser = userDAO.findUserById(reportedId);
                if (reportedUser != null && reportedUser.isCreator()) {
                    request.setAttribute("reportedUser", reportedUser);
                } else {
                    request.setAttribute("errorMessage", "Invalid creator specified for report.");
                }
            } catch (NumberFormatException e) {
                 request.setAttribute("errorMessage", "Invalid creator ID format.");
            }
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/report_creator_form.jsp"); // Assuming a dedicated form JSP
        dispatcher.forward(request, response);
    }
    */

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ReportCreatorServlet: doPost called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;
        String errorMessage = null;
        String successMessage = null;
        String reportedUserIdParam = request.getParameter("reportedUserId"); // ID of the creator user being reported
        String reason = request.getParameter("reason");
        String details = request.getParameter("details");
        String redirectBackUrl = request.getParameter("redirectBackUrl"); // URL to redirect to after reporting

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isBusiness()) { // Only businesses report creators here
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
            redirectBackUrl = request.getContextPath() + "/business/dashboard"; // Fallback redirect
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
            // Optional: Validate details length

            // 2. Verify Reported User Exists and is a Creator
            User reportedUser = userDAO.findUserById(reportedUserId);
            if (reportedUser == null) {
                 throw new IllegalArgumentException("The user you are trying to report does not exist.");
            }
            if (!reportedUser.isCreator()) { // Make sure they are reporting a creator
                 throw new IllegalArgumentException("You can only report creator accounts using this form.");
            }
            // Optional: Check if reported user is banned already? Maybe allow reporting anyway for record.


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
                System.out.println("Report submitted by Business " + reporterUserId + " against Creator " + reportedUserId);
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
        // Redirecting to avoid form resubmission. Pass status via query parameters.
        System.out.println("Redirecting back after report attempt to: " + redirectBackUrl);
        response.sendRedirect(redirectBackUrl);
    }
}