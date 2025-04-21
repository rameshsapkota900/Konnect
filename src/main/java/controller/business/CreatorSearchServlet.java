package controller.business;

import dao.CreatorDAO;
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

@WebServlet("/business/search")
public class CreatorSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CreatorDAO creatorDAO;
    private static final int CREATORS_PER_PAGE = 10; // Results per page

    public void init() {
        creatorDAO = new CreatorDAO();
        System.out.println("CreatorSearchServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CreatorSearchServlet: doGet called");
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

        try {
            // --- Get Search Parameters ---
            String niche = request.getParameter("niche");
            String minFollowersStr = request.getParameter("minFollowers");
            String pageParam = request.getParameter("page");

            // --- Parse Parameters ---
             niche = (niche != null) ? niche.trim() : null;
             Integer minFollowers = null;
             if (minFollowersStr != null && !minFollowersStr.trim().isEmpty()) {
                 try {
                     minFollowers = Integer.parseInt(minFollowersStr.trim());
                     if (minFollowers < 0) minFollowers = 0; // Ensure non-negative
                 } catch (NumberFormatException e) {
                     minFollowers = null; // Ignore invalid input
                     request.setAttribute("searchError", "Invalid minimum follower count.");
                 }
             }

             int currentPage = 1;
             if (pageParam != null) {
                 try {
                     currentPage = Integer.parseInt(pageParam);
                     if (currentPage < 1) currentPage = 1;
                 } catch (NumberFormatException e) { currentPage = 1; }
             }

             int offset = (currentPage - 1) * CREATORS_PER_PAGE;


            // --- Perform Search ---
             // Only perform search if criteria are provided or it's not the initial load?
             // Let's always perform search, even if criteria are null (shows all creators paginated)
            System.out.println("Searching creators: Niche=" + niche + ", MinFollowers=" + minFollowers + ", Page=" + currentPage);
            List<Creator> creators = creatorDAO.searchCreators(niche, minFollowers, CREATORS_PER_PAGE, offset);
            int totalCreators = creatorDAO.countSearchedCreators(niche, minFollowers);
            int totalPages = (int) Math.ceil((double) totalCreators / CREATORS_PER_PAGE);


            // --- Set Attributes for JSP ---
             request.setAttribute("creators", creators);
             request.setAttribute("currentPage", currentPage);
             request.setAttribute("totalPages", totalPages);
             request.setAttribute("totalCreators", totalCreators);
             // Send search parameters back to pre-fill the form
             request.setAttribute("searchNiche", niche);
             request.setAttribute("searchMinFollowers", minFollowers != null ? minFollowers : "");


            // --- Forward to JSP ---
            RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/search_creators.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            System.err.println("Error during creator search: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while searching for creators.");
            // Forward to search page with error or maybe dashboard? Let's go back to search page.
             RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/search_creators.jsp");
             dispatcher.forward(request, response);
        }
    }

     // doPost could be used if the search form method is POST
     // protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     //     doGet(request, response); // Simple implementation: process POST as GET
     // }
}