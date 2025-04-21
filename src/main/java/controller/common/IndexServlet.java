package controller.common; // Or just controller if preferred

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User; // To check if user is logged in

import java.io.IOException;

/**
 * Handles requests to the root context path ("/") and the "/index" path.
 * If the user is logged in, it redirects them to their appropriate dashboard.
 * Otherwise, it forwards to the landing page (index.jsp).
 */
@WebServlet(urlPatterns = {"", "/"}) // Maps to root and /index
public class IndexServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public void init() {
        System.out.println("IndexServlet Initialized - Handling root path");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("IndexServlet: doGet called for path: " + request.getServletPath());
        HttpSession session = request.getSession(false); // Don't create session if not exists
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (loggedInUser != null) {
            // User is already logged in, redirect to their dashboard
            System.out.println("User already logged in (" + loggedInUser.getEmail() + "), redirecting from IndexServlet to dashboard.");
            redirectToDashboard(loggedInUser, request, response);
        } else {
            // User is not logged in, show the landing page
            System.out.println("User not logged in, forwarding to index.jsp");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp"); // Forward to the root index.jsp
            dispatcher.forward(request, response);
        }
    }

    // doPost could potentially handle a form on the index page, but likely not needed here.
    // protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //     doGet(request, response); // Or handle specific actions
    // }

     // Helper method to redirect based on user role (copied for consistency)
     private void redirectToDashboard(User user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String targetUrl;
        String contextPath = request.getContextPath();

        switch (user.getRole().toLowerCase()) {
             case "admin":
                 targetUrl = contextPath + "/admin/dashboard";
                 break;
             case "creator":
                 targetUrl = contextPath + "/creator/dashboard";
                 break;
             case "business":
                 targetUrl = contextPath + "/business/dashboard";
                 break;
             default:
                 targetUrl = contextPath + "/login"; // Fallback if role is unknown
                 break;
         }
         System.out.println("IndexServlet redirecting logged-in user to: " + targetUrl);
         response.sendRedirect(targetUrl);
     }
}