package controller.business;

import dao.BusinessDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Business;
import model.User;

import java.io.IOException;

@WebServlet("/business/profile")
public class BusinessProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private BusinessDAO businessDAO;

    public void init() {
        businessDAO = new BusinessDAO();
        System.out.println("BusinessProfileServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("BusinessProfileServlet: doGet called");
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

        String action = request.getParameter("action");
        int businessId = loggedInUser.getUserId();

        try {
            Business businessProfile = businessDAO.getBusinessByUserId(businessId);

            if (businessProfile == null) {
                 System.out.println("Business profile not found for User ID: " + businessId + ". Displaying empty form.");
                 businessProfile = new Business();
                 businessProfile.setUserId(businessId);
                 request.setAttribute("isNewProfile", true);
                 request.setAttribute("infoMessage", "Please complete your business profile.");
            }

            request.setAttribute("businessProfile", businessProfile);
            // Default to showing the form for viewing/editing
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/profile.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error loading business profile page for User ID " + businessId + ": " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error loading profile.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("BusinessProfileServlet: doPost called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;
        String errorMessage = null;
        String successMessage = null;

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

        int businessId = loggedInUser.getUserId();
        Business currentProfile = businessDAO.getBusinessByUserId(businessId); // Get current profile for updates

        if (currentProfile == null) {
             System.err.println("CRITICAL: Attempting to POST profile update, but no existing profile found for User ID: " + businessId);
             errorMessage = "Could not find your profile to update. Please contact support.";
              request.setAttribute("errorMessage", errorMessage);
              RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/profile.jsp");
              dispatcher.forward(request, response);
             return;
        }

        try {
            // 1. Retrieve Form Data
            String companyName = request.getParameter("companyName");
            String website = request.getParameter("website");
            String industry = request.getParameter("industry");
            String description = request.getParameter("description");

            // Basic validation
             if (companyName == null || companyName.trim().isEmpty() || companyName.length() > 255) {
                  errorMessage = "Company Name is required (max 255 characters).";
             }
             // Optional: Add validation for website format (e.g., starts with http/https)
              if (website != null && !website.trim().isEmpty() && website.length() > 255) {
                  // Basic check - could use regex for better validation
                  if (!website.startsWith("http://") && !website.startsWith("https://")) {
                      errorMessage = "Website URL should start with http:// or https://";
                  } else if (website.length() > 255) {
                       errorMessage = "Website URL is too long (max 255 characters).";
                  }
             } else if (website != null && website.length() > 255) { // Check length even if empty logic is removed
                  errorMessage = "Website URL is too long (max 255 characters).";
             }

            // 2. Update Business Object (if no validation errors)
            if (errorMessage == null) {
                 currentProfile.setCompanyName(companyName.trim());
                 currentProfile.setWebsite(website != null ? website.trim() : null); // Allow empty website
                 currentProfile.setIndustry(industry != null ? industry.trim() : "");
                 currentProfile.setDescription(description != null ? description.trim() : "");
                 // Timestamp updated by DB automatically

                 // 3. Call DAO to update
                 boolean updated = businessDAO.updateBusinessProfile(currentProfile);

                 if (updated) {
                     successMessage = "Business profile updated successfully!";
                     System.out.println("Business profile updated successfully for User ID: " + businessId);
                 } else {
                     errorMessage = "Failed to update profile in database. Please try again.";
                      System.err.println("BusinessDAO.updateBusinessProfile returned false for User ID: " + businessId);
                 }
            } else {
                 System.out.println("Validation failed for business profile update: " + errorMessage);
                 // Keep the entered (invalid) values to show back to the user
                 currentProfile.setCompanyName(companyName);
                 currentProfile.setWebsite(website);
                 currentProfile.setIndustry(industry);
                 currentProfile.setDescription(description);
            }

            // 4. Set attributes and forward back
            request.setAttribute("businessProfile", currentProfile); // Send updated or current-with-error profile
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
            }
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/profile.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error processing business profile update for User ID " + businessId + ": " + e.getMessage());
            e.printStackTrace();
             request.setAttribute("businessProfile", currentProfile); // Send back last known state
            request.setAttribute("errorMessage", "An unexpected error occurred while updating your profile.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/profile.jsp");
            dispatcher.forward(request, response);
        }
    }
}