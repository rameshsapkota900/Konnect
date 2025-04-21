package controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false); // Get existing session, don't create new
        if (session != null) {
            System.out.println("Logging out user: " + session.getAttribute("user"));
            session.invalidate(); // Invalidate the session
        }
        // Redirect to login page after logout
         System.out.println("Redirecting to login page after logout.");
        response.sendRedirect(request.getContextPath() + "/login?logout=success");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Logout should ideally only be a GET request for simplicity here
        doGet(request, response);
    }
}