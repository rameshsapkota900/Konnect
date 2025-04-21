package controller.auth;

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

@WebServlet("/login") // Map URL /login to this servlet
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    public void init() {
        userDAO = new UserDAO(); // Initialize DAO
    }

    // Show login page
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check if user is already logged in, redirect if so
        HttpSession session = request.getSession(false); // Don't create if not exists
         if (session != null && session.getAttribute("user") != null) {
             User loggedInUser = (User) session.getAttribute("user");
             // *** FIX: Pass request object ***
              redirectToDashboard(loggedInUser, request, response);
              return;
         }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/login.jsp");
        dispatcher.forward(request, response);
    }

    // Process login attempt
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String errorMessage = null;

        User user = null;
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            errorMessage = "Email and password cannot be empty.";
        } else {
            user = userDAO.validateLogin(email.trim(), password);
        }

        if (user != null) {
            // Check if banned
            if (user.isBanned()) {
                errorMessage = "Your account has been banned. Please contact support.";
                request.setAttribute("errorMessage", errorMessage);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/login.jsp");
                dispatcher.forward(request, response);
            } else {
                // Login successful
                HttpSession session = request.getSession(); // Create session if not exists
                session.setAttribute("user", user); // Store user object in session
                 System.out.println("Login successful for user: " + user.getEmail() + " Role: " + user.getRole());
                 // *** FIX: Pass request object ***
                 redirectToDashboard(user, request, response); // Redirect based on role
            }
        } else {
             if (errorMessage == null) { // If no specific error message yet
                 errorMessage = "Invalid email or password.";
             }
            // Login failed
            request.setAttribute("errorMessage", errorMessage);
             System.err.println("Login failed for email: " + email);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/login.jsp");
            dispatcher.forward(request, response);
        }
    }

     // Helper method to redirect based on user role
     // *** FIX: Add HttpServletRequest request parameter ***
     private void redirectToDashboard(User user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String targetUrl = switch (user.getRole().toLowerCase()) {
             case "admin" -> "/admin/dashboard"; // Maps to AdminDashboardServlet
             case "creator" -> "/creator/dashboard"; // Maps to CreatorDashboardServlet
             case "business" -> "/business/dashboard"; // Maps to BusinessDashboardServlet
             default -> "/login"; // Should not happen, fallback to login
         };
         // *** FIX: Now 'request' is available in this scope ***
         String contextPath = request.getContextPath();
         System.out.println("Redirecting to: " + contextPath + targetUrl);
         response.sendRedirect(contextPath + targetUrl);
     }
}