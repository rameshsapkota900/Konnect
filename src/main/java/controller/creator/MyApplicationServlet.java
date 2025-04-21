package controller.creator;

import dao.ApplicationDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Application;
import model.User;

import java.io.IOException;
import java.util.List;

@WebServlet("/creator/applications")
public class MyApplicationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ApplicationDAO applicationDAO;

    public void init() {
        applicationDAO = new ApplicationDAO();
        System.out.println("MyApplicationServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("MyApplicationServlet: doGet called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isCreator()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        if (loggedInUser.isBanned()) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?error=banned");
            return;
        }
        // --- End Authorization Check ---

        // Check for success messages from redirects
        if ("applied".equals(request.getParameter("success"))) {
            request.setAttribute("successMessage", "Application submitted successfully!");
        }
         if ("withdrawn".equals(request.getParameter("success"))) {
            request.setAttribute("successMessage", "Application withdrawn successfully.");
        }
         if ("failedWithdraw".equals(request.getParameter("error"))) {
            request.setAttribute("errorMessage", "Failed to withdraw application.");
        }


        try {
            int creatorId = loggedInUser.getUserId();
            System.out.println("Fetching applications for Creator ID: " + creatorId);

            // Get applications from DAO (includes basic campaign info like title)
            List<Application> applications = applicationDAO.getApplicationsByCreatorId(creatorId);

            request.setAttribute("applications", applications);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/creator/my_applications.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error fetching creator applications for User ID " + loggedInUser.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while loading your applications.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
            dispatcher.forward(request, response);
        }
    }

     // Handle Withdraw Action via POST (could also be GET with careful parameter checking)
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
         System.out.println("MyApplicationServlet: doPost called");
         HttpSession session = request.getSession(false);
         User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

         // --- Authorization Check ---
         if (loggedInUser == null || !loggedInUser.isCreator()) {
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
         String applicationIdParam = request.getParameter("applicationId");

         if ("withdraw".equals(action) && applicationIdParam != null) {
              System.out.println("Action: withdraw, Application ID: " + applicationIdParam);
             try {
                 int applicationId = Integer.parseInt(applicationIdParam);
                 int creatorId = loggedInUser.getUserId();

                 // Attempt to withdraw
                 boolean withdrawn = applicationDAO.withdrawApplication(applicationId, creatorId);

                 if (withdrawn) {
                      System.out.println("Withdrawal successful for Application ID: " + applicationId);
                     response.sendRedirect(request.getContextPath() + "/creator/applications?success=withdrawn");
                 } else {
                      System.err.println("Withdrawal failed for Application ID: " + applicationId + " by Creator ID: " + creatorId);
                     response.sendRedirect(request.getContextPath() + "/creator/applications?error=failedWithdraw");
                 }
             } catch (NumberFormatException e) {
                  System.err.println("Invalid Application ID format for withdrawal: " + applicationIdParam);
                  response.sendRedirect(request.getContextPath() + "/creator/applications?error=invalidId");
             } catch (Exception e) {
                 System.err.println("Error processing withdrawal for Application ID " + applicationIdParam + ": " + e.getMessage());
                 e.printStackTrace();
                 response.sendRedirect(request.getContextPath() + "/creator/applications?error=serverError");
             }
         } else {
              System.out.println("Invalid action or missing parameters for POST request.");
             // If POST is used for anything else, handle it here. Otherwise, redirect to GET view.
             doGet(request, response);
         }
     }

}