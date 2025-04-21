package controller.auth;

import dao.BusinessDAO;
import dao.CreatorDAO;
import dao.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private CreatorDAO creatorDAO;
    private BusinessDAO businessDAO;


    public void init() {
        userDAO = new UserDAO();
        creatorDAO = new CreatorDAO();
        businessDAO = new BusinessDAO();
        System.out.println("RegisterServlet Initialized"); // Log initialization
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
         System.out.println("RegisterServlet: doGet called"); // Log request
         // Check if user is already logged in, redirect if so
          HttpSession session = request.getSession(false);
         if (session != null && session.getAttribute("user") != null) {
             User loggedInUser = (User) session.getAttribute("user");
             System.out.println("User already logged in (" + loggedInUser.getEmail() + "), redirecting to dashboard.");
             redirectToDashboard(loggedInUser, request, response);
             return;
         }

         // Set default role selection if needed, or let JSP handle it
         // request.setAttribute("selectedRole", "creator"); // Example default

        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/register.jsp");
        dispatcher.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
         System.out.println("RegisterServlet: doPost called"); // Log request

        // Retrieve all parameters
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = request.getParameter("role"); // "creator" or "business"
        String displayName = request.getParameter("displayName"); // For creator
        String companyName = request.getParameter("companyName"); // For business

        // Trim inputs
        email = (email != null) ? email.trim() : null;
        displayName = (displayName != null) ? displayName.trim() : null;
        companyName = (companyName != null) ? companyName.trim() : null;
        // Don't trim passwords

        String errorMessage = null;
        String successMessage = null;

        // --- Input Validation ---
        if (email == null || email.isEmpty() || !email.contains("@") || email.length() > 255) { // Add length check
            errorMessage = "Please enter a valid email address (max 255 characters).";
        } else if (password == null || password.isEmpty()) {
             errorMessage = "Password cannot be empty.";
        } else if (password.length() < 8) { // Enforce minimum length
            errorMessage = "Password must be at least 8 characters long.";
        } else if (!password.equals(confirmPassword)) {
            errorMessage = "Passwords do not match.";
        } else if (role == null || (!role.equals("creator") && !role.equals("business"))) {
            errorMessage = "Please select a valid user role (Creator or Business).";
        } else if (role.equals("creator") && (displayName == null || displayName.isEmpty() || displayName.length() > 100)) { // Add length check
            errorMessage = "Display Name is required for creators (max 100 characters).";
        } else if (role.equals("business") && (companyName == null || companyName.isEmpty() || companyName.length() > 255)) { // Add length check
             errorMessage = "Company Name is required for businesses (max 255 characters).";
        } else if (userDAO.findUserByEmail(email) != null) {
             // Check for existing email *before* attempting registration
             errorMessage = "This email address is already registered. Please login or use a different email.";
             System.out.println("Registration failed: Email already exists - " + email);
        }

        // --- Registration Logic ---
        if (errorMessage == null) {
            System.out.println("Validation passed for email: " + email + ", role: " + role);
            // Attempt to register the user in the 'users' table
            int newUserId = userDAO.registerUser(email, password, role);

            if (newUserId > 0) { // Check if user registration returned a valid ID
                 System.out.println("User record created successfully, User ID: " + newUserId);
                 boolean profileCreated = false;
                 // Now create the corresponding profile entry
                 if ("creator".equals(role)) {
                     profileCreated = creatorDAO.createCreatorProfile(newUserId, displayName);
                     System.out.println("Attempting to create creator profile for User ID: " + newUserId + (profileCreated ? " - Success" : " - Failed"));
                 } else if ("business".equals(role)) {
                     profileCreated = businessDAO.createBusinessProfile(newUserId, companyName);
                      System.out.println("Attempting to create business profile for User ID: " + newUserId + (profileCreated ? " - Success" : " - Failed"));
                 }

                 if(profileCreated) {
                    successMessage = "Registration successful! Please login to continue.";
                    System.out.println("Registration and profile creation complete for User ID: " + newUserId + ", Email: " + email);
                 } else {
                     // CRITICAL: User created but profile failed. This indicates an inconsistency.
                     // In a production system, use database transactions to roll back the user creation
                     // or implement a cleanup/retry mechanism. Here, we set an error message.
                     errorMessage = "Account created, but profile setup failed. Please contact support.";
                     System.err.println("CRITICAL ERROR: User record created (ID: " + newUserId + "), but failed to create corresponding " + role + " profile.");
                     // Optionally: Attempt to delete the inconsistent user record
                     // userDAO.deleteUser(newUserId); // Be careful with automatic deletes
                 }
            } else {
                // User registration failed (e.g., DB error, duplicate email caught again by DB constraint)
                 errorMessage = "Registration failed. An unexpected error occurred. Please try again later.";
                 if (userDAO.findUserByEmail(email) != null) { // Re-check if it was a duplicate issue caught by DB
                     errorMessage = "This email address is already registered.";
                 }
                 System.err.println("UserDAO.registerUser failed for email: " + email + " (Returned ID: " + newUserId + ")");
            }
        } else {
             System.out.println("Validation failed: " + errorMessage);
        }

        // --- Response Handling ---
        if (successMessage != null) {
             // Redirect to login page with a success message
             System.out.println("Registration successful, redirecting to login.");
              response.sendRedirect(request.getContextPath() + "/login?registration=success&email=" + java.net.URLEncoder.encode(email, "UTF-8"));

        } else {
            // Forward back to registration page with error message and pre-filled values
            System.out.println("Registration failed, forwarding back to register.jsp with error: " + errorMessage);
            request.setAttribute("errorMessage", errorMessage);
            // Retain form values to avoid re-typing
            request.setAttribute("email", email);
            request.setAttribute("displayName", displayName);
            request.setAttribute("companyName", companyName);
            request.setAttribute("selectedRole", role); // Keep the selected role

            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/register.jsp");
            dispatcher.forward(request, response);
        }
    }

     // Helper method to redirect based on user role (copied from LoginServlet for consistency)
     private void redirectToDashboard(User user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String targetUrl = switch (user.getRole().toLowerCase()) {
             case "admin" -> "/admin/dashboard";
             case "creator" -> "/creator/dashboard";
             case "business" -> "/business/dashboard";
             default -> "/login"; // Fallback
         };
         System.out.println("Redirecting logged-in user to: " + request.getContextPath() + targetUrl);
         response.sendRedirect(request.getContextPath() + targetUrl);
     }
}