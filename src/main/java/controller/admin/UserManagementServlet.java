package controller.admin;

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
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/admin/users")
public class UserManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    public void init() {
        userDAO = new UserDAO();
        System.out.println("UserManagementServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("UserManagementServlet: doGet called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        // --- End Authorization Check ---

         // Check for messages from POST actions
        if ("banned".equals(request.getParameter("success"))) request.setAttribute("successMessage", "User banned successfully.");
        if ("unbanned".equals(request.getParameter("success"))) request.setAttribute("successMessage", "User unbanned successfully.");
        if ("failed".equals(request.getParameter("error"))) request.setAttribute("errorMessage", "Failed to update user ban status.");
        if ("invalidId".equals(request.getParameter("error"))) request.setAttribute("errorMessage", "Invalid user specified.");
         if ("selfBan".equals(request.getParameter("error"))) request.setAttribute("errorMessage", "Admins cannot ban themselves.");


        try {
            System.out.println("Fetching all users for admin view.");
            List<User> users = userDAO.getAllUsers();

            // Optional: Filter out the current admin from the list?
             // users = users.stream()
             //              .filter(u -> u.getUserId() != loggedInUser.getUserId())
             //              .collect(Collectors.toList());

            request.setAttribute("users", users);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/admin/user_list.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error fetching users for admin: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while loading user data.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("UserManagementServlet: doPost called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        // --- End Authorization Check ---

        String action = request.getParameter("action"); // "ban" or "unban"
        String userIdParam = request.getParameter("userId");
        String redirectUrl = request.getContextPath() + "/admin/users"; // Redirect back to user list

        if (userIdParam != null && (action.equals("ban") || action.equals("unban"))) {
             System.out.println("Action: " + action + ", User ID: " + userIdParam);
            try {
                int userId = Integer.parseInt(userIdParam);

                // Prevent admin from banning themselves
                if (userId == loggedInUser.getUserId()) {
                     System.err.println("Admin " + loggedInUser.getEmail() + " attempted to ban self.");
                     redirectUrl += "?error=selfBan";
                } else {
                     boolean newBanStatus = action.equals("ban"); // true for ban, false for unban
                     boolean success = userDAO.updateUserBanStatus(userId, newBanStatus);

                     if (success) {
                          System.out.println("Ban status update successful for User ID: " + userId);
                         redirectUrl += "?success=" + action; // "ban" or "unban"
                     } else {
                          System.err.println("Ban status update failed for User ID: " + userId);
                         redirectUrl += "?error=failed";
                     }
                }
            } catch (NumberFormatException e) {
                 System.err.println("Invalid User ID format for ban/unban: " + userIdParam);
                 redirectUrl += "?error=invalidId";
            } catch (Exception e) {
                 System.err.println("Error processing user ban/unban: " + e.getMessage());
                 e.printStackTrace();
                 redirectUrl += "?error=serverError";
            }
        } else {
            System.err.println("Invalid action or missing parameters for user management POST.");
            redirectUrl += "?error=badRequest";
        }

        // Redirect back to the user list
         response.sendRedirect(redirectUrl);
    }
}