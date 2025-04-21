package controller.creator;

import dao.ApplicationDAO;
import dao.CreatorDAO;
import dao.InviteDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Creator;
import model.User;

import java.io.IOException;
import java.util.List;
import model.Application; // Import Application model
import model.Invite; // Import Invite model


@WebServlet("/creator/dashboard")
public class CreatorDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CreatorDAO creatorDAO;
    private ApplicationDAO applicationDAO; // To count applications
    private InviteDAO inviteDAO;         // To count invites

    public void init() {
        creatorDAO = new CreatorDAO();
        applicationDAO = new ApplicationDAO();
        inviteDAO = new InviteDAO();
        System.out.println("CreatorDashboardServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CreatorDashboardServlet: doGet called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isCreator()) {
            System.out.println("User not logged in or not a creator. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
         if (loggedInUser.isBanned()) {
            System.out.println("Banned creator accessing dashboard. Redirecting to login.");
             session.invalidate(); // Log out banned user
             response.sendRedirect(request.getContextPath() + "/login?error=banned");
             return;
         }
        // --- End Authorization Check ---

        try {
            int creatorId = loggedInUser.getUserId();
            System.out.println("Fetching dashboard data for Creator ID: " + creatorId);

            // 1. Get Creator Profile (for welcome message & profile status)
            Creator creatorProfile = creatorDAO.getCreatorByUserId(creatorId);
            if (creatorProfile == null) {
                 // This might happen if the creator profile wasn't created correctly during registration
                 System.err.println("CRITICAL: Creator profile not found for User ID: " + creatorId + ". Redirecting to profile creation/edit.");
                 // Optionally set an error message
                 request.setAttribute("errorMessage", "Your creator profile is missing. Please complete it.");
                 // Redirect to profile page might be better than showing an empty dashboard
                 response.sendRedirect(request.getContextPath() + "/creator/profile?action=edit"); // Redirect to edit mode
                 return;
            }
            request.setAttribute("creatorProfile", creatorProfile);
            System.out.println("Creator profile loaded: " + creatorProfile.getDisplayName());

            // 2. Get counts for summary cards (Example implementation)
            // Note: These counts could be optimized with dedicated COUNT(*) queries in DAOs

            // Count pending/accepted applications
            List<Application> applications = applicationDAO.getApplicationsByCreatorId(creatorId);
            long activeApplicationCount = applications.stream()
                 .filter(app -> Application.Status.PENDING.getStatusName().equalsIgnoreCase(app.getStatus()) ||
                               Application.Status.ACCEPTED.getStatusName().equalsIgnoreCase(app.getStatus()))
                 .count();
            request.setAttribute("activeApplicationCount", activeApplicationCount);
             System.out.println("Active Application Count: " + activeApplicationCount);


            // Count pending invites
            List<Invite> invites = inviteDAO.getInvitesForCreator(creatorId);
            long pendingInviteCount = invites.stream()
                 .filter(inv -> Invite.Status.PENDING.getStatusName().equalsIgnoreCase(inv.getStatus()))
                 .count();
            request.setAttribute("pendingInviteCount", pendingInviteCount);
             System.out.println("Pending Invite Count: " + pendingInviteCount);

            // You might add counts for active campaigns, total earnings (if tracked), etc.

             // 3. Check Profile Completeness (Simple Example)
             boolean profileComplete = creatorProfile.getBio() != null && !creatorProfile.getBio().trim().isEmpty() &&
                                      creatorProfile.getNiche() != null && !creatorProfile.getNiche().trim().isEmpty() &&
                                      creatorProfile.getSocialMediaLinks() != null && !creatorProfile.getSocialMediaLinks().equals("{}") && // Check if JSON is not empty
                                      creatorProfile.getMediaKitPath() != null && !creatorProfile.getMediaKitPath().trim().isEmpty();
             request.setAttribute("isProfileComplete", profileComplete);
              System.out.println("Profile Complete Status: " + profileComplete);


            // 4. Forward to the JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/creator/dashboard.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error loading creator dashboard for User ID " + loggedInUser.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
            // Forward to a generic error page
            request.setAttribute("errorMessage", "An unexpected error occurred while loading your dashboard.");
             RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
             dispatcher.forward(request, response);
        }
    }

     // doPost is not typically used for a dashboard view unless handling actions directly on the dashboard
     // protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     //     doGet(request, response); // Or handle specific POST actions
     // }
}