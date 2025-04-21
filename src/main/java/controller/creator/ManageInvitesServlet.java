package controller.creator;

import dao.InviteDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Invite;
import model.User;

import java.io.IOException;
import java.util.List;

@WebServlet("/creator/invites")
public class ManageInvitesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private InviteDAO inviteDAO;

    public void init() {
        inviteDAO = new InviteDAO();
        System.out.println("ManageInvitesServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ManageInvitesServlet: doGet called");
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

         // Check for messages from POST actions
        if ("accepted".equals(request.getParameter("success"))) {
            request.setAttribute("successMessage", "Invite accepted successfully!");
        } else if ("rejected".equals(request.getParameter("success"))) {
            request.setAttribute("successMessage", "Invite rejected successfully.");
        } else if ("failedResponse".equals(request.getParameter("error"))) {
             request.setAttribute("errorMessage", "Failed to respond to invite. It might no longer be valid.");
        } else if ("invalidId".equals(request.getParameter("error"))) {
             request.setAttribute("errorMessage", "Invalid invite specified.");
        } else if ("serverError".equals(request.getParameter("error"))) {
             request.setAttribute("errorMessage", "An unexpected server error occurred.");
        }


        try {
            int creatorId = loggedInUser.getUserId();
            System.out.println("Fetching invites for Creator ID: " + creatorId);

            // Get invites from DAO (includes basic campaign and business info)
            List<Invite> invites = inviteDAO.getInvitesForCreator(creatorId);

            request.setAttribute("invites", invites);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/creator/manage_invites.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error fetching creator invites for User ID " + loggedInUser.getUserId() + ": " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while loading your invites.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ManageInvitesServlet: doPost called");
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

        String action = request.getParameter("action"); // "accept" or "reject"
        String inviteIdParam = request.getParameter("inviteId");
        String redirectUrl = request.getContextPath() + "/creator/invites"; // Default redirect

        if (inviteIdParam != null && (action.equals("accept") || action.equals("reject"))) {
            System.out.println("Action: " + action + ", Invite ID: " + inviteIdParam);
            try {
                int inviteId = Integer.parseInt(inviteIdParam);
                int creatorId = loggedInUser.getUserId();
                String newStatus = action.equals("accept") ? Invite.Status.ACCEPTED.getStatusName() : Invite.Status.REJECTED.getStatusName();

                // Attempt to respond to the invite
                boolean responded = inviteDAO.respondToInvite(inviteId, newStatus, creatorId);

                if (responded) {
                    System.out.println("Response successful for Invite ID: " + inviteId + ", Status: " + newStatus);
                    redirectUrl += "?success=" + newStatus; // Add success parameter
                } else {
                    System.err.println("Response failed for Invite ID: " + inviteId + " by Creator ID: " + creatorId);
                    redirectUrl += "?error=failedResponse";
                }
            } catch (NumberFormatException e) {
                 System.err.println("Invalid Invite ID format for response: " + inviteIdParam);
                 redirectUrl += "?error=invalidId";
            } catch (Exception e) {
                System.err.println("Error processing invite response for ID " + inviteIdParam + ": " + e.getMessage());
                e.printStackTrace();
                redirectUrl += "?error=serverError";
            }
        } else {
            System.out.println("Invalid action or missing parameters for POST request.");
             redirectUrl += "?error=badRequest"; // Indicate bad request parameters
        }

         // Redirect back to the invites list page
         response.sendRedirect(redirectUrl);
    }
}