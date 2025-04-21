<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="model.Creator" %>
<%@ page import="model.Campaign" %> <%-- Needed for invite dropdown --%>
<%@ page import="dao.CampaignDAO" %> <%-- Needed for invite dropdown --%>
<%@ page import="model.User" %> <%-- For header & getting business ID --%>
<% request.setAttribute("pageTitle", "Find Creators"); %>
<%@ include file="../common/header.jsp" %>

<%
    // Get search results and pagination data
    List<Creator> creators = (List<Creator>) request.getAttribute("creators");
    int currentPage = (request.getAttribute("currentPage") != null) ? (Integer) request.getAttribute("currentPage") : 1;
    int totalPages = (request.getAttribute("totalPages") != null) ? (Integer) request.getAttribute("totalPages") : 1;
    int totalCreators = (request.getAttribute("totalCreators") != null) ? (Integer) request.getAttribute("totalCreators") : 0;

    // Get search form parameters back for pre-filling
    String searchNiche = (request.getAttribute("searchNiche") != null) ? (String) request.getAttribute("searchNiche") : "";
    String searchMinFollowers = (request.getAttribute("searchMinFollowers") != null) ? request.getAttribute("searchMinFollowers").toString() : "";

    // Get business's active campaigns for the invite dropdown
    CampaignDAO campaignDAO = new CampaignDAO();
    List<Campaign> myActiveCampaigns = null;
    if (loggedInUser != null && loggedInUser.isBusiness()) {
        // Fetch only active campaigns
         myActiveCampaigns = campaignDAO.getCampaignsByBusinessId(loggedInUser.getUserId())
                                        .stream()
                                        .filter(c -> "active".equalsIgnoreCase(c.getStatus()))
                                        .collect(java.util.stream.Collectors.toList());
    } else {
         myActiveCampaigns = new java.util.ArrayList<>(); // Empty list if not logged in or not business
    }


    // Get status messages from invite attempts (passed via redirect query params)
    String inviteStatus = request.getParameter("inviteStatus");
    String inviteMessageParam = request.getParameter("message"); // Optional message from validation error

     if ("success".equals(inviteStatus)) {
         request.setAttribute("successMessage", "Invite sent successfully!");
     } else if ("duplicate".equals(inviteStatus)) {
         request.setAttribute("errorMessage", "You have already invited this creator to the selected campaign.");
     } else if ("failed".equals(inviteStatus)) {
         request.setAttribute("errorMessage", "Failed to send invite due to a server error.");
     } else if ("invalidId".equals(inviteStatus)) {
         request.setAttribute("errorMessage", "Invalid creator or campaign specified for invite.");
     } else if ("validationError".equals(inviteStatus)) {
         request.setAttribute("errorMessage", "Invite failed: " + (inviteMessageParam != null ? inviteMessageParam : "Invalid data."));
     } else if ("serverError".equals(inviteStatus)) {
         request.setAttribute("errorMessage", "An unexpected server error occurred while sending the invite.");
     }


     NumberFormat numberFormatter = NumberFormat.getInstance(); // For formatting follower count
%>

<h2>Find Content Creators</h2>

<%-- Search Form --%>
<div class="search-filter-form" style="margin-bottom: 2rem; background: var(--card-bg); padding: 1.5rem; border-radius: 8px;">
    <form action="<%= contextPath %>/business/search" method="GET">
         <div style="display: flex; gap: 1rem; flex-wrap: wrap; align-items: flex-end;">
             <div class="form-group" style="flex: 2; min-width: 200px; margin-bottom: 0;">
                  <label for="niche">Search by Niche:</label>
                  <input type="search" id="niche" name="niche" value="<%= searchNiche %>" placeholder="e.g., Beauty, Gaming, Tech">
             </div>
             <div class="form-group" style="flex: 1; min-width: 150px; margin-bottom: 0;">
                  <label for="minFollowers">Minimum Followers:</label>
                  <input type="number" id="minFollowers" name="minFollowers" value="<%= searchMinFollowers %>" min="0" placeholder="e.g., 10000">
             </div>
             <div class="form-group" style="margin-bottom: 0;">
                 <button type="submit" class="btn btn-primary" style="width: auto;">Search</button>
                 <% if (!searchNiche.isEmpty() || !searchMinFollowers.isEmpty()) { %>
                     <a href="<%= contextPath %>/business/search" class="btn btn-secondary" style="width: auto; margin-left: 0.5rem;">Clear Filters</a>
                 <% } %>
            </div>
         </div>
    </form>
</div>

<%-- Display messages from redirects (handled by header now) --%>
<%-- <% if (request.getAttribute("successMessage") != null) { %> <p class="message success-message"><%= request.getAttribute("successMessage") %></p> <% } %> --%>
<%-- <% if (request.getAttribute("errorMessage") != null) { %> <p class="message error-message"><%= request.getAttribute("errorMessage") %></p> <% } %> --%>


<p>Found <%= totalCreators %> creators matching your criteria.</p>

<% if (creators == null || creators.isEmpty()) { %>
    <p class="message info-message">
        <% if (!searchNiche.isEmpty() || !searchMinFollowers.isEmpty()) { %>
             No creators found matching your current filters. Try broadening your search.
        <% } else { %>
             No creators found on the platform yet.
        <% } %>
    </p>
<% } else { %>
    <div class="card-grid creator-list">
        <% for (Creator creator : creators) { %>
            <div class="card creator-card">
                <div class="card-header">
                    <h3><%= creator.getDisplayName() %></h3>
                    <% if (creator.getNiche() != null && !creator.getNiche().isEmpty()) { %>
                         <small style="color: var(--text-color-muted); text-transform: capitalize;">Niche: <%= creator.getNiche() %></small>
                    <% } %>
                </div>
                 <div class="card-content">
                     <p>
                          <% String bio = creator.getBio();
                             if (bio != null && !bio.isEmpty()) {
                                out.print(bio.length() > 120 ? bio.substring(0, 120) + "..." : bio);
                             } else {
                                out.print("<i>No bio provided.</i>");
                             }
                          %>
                     </p>
                 </div>
                  <div class="card-meta">
                       <span>Followers: <%= numberFormatter.format(creator.getFollowerCount()) %></span>
                       <%-- Optional: Display pricing summary if available --%>
                       <% if (creator.getPricingInfo() != null && !creator.getPricingInfo().isEmpty()) { %>
                            <span>Rates: <%= creator.getPricingInfo().length() > 50 ? creator.getPricingInfo().substring(0, 50) + "..." : creator.getPricingInfo() %></span>
                       <% } %>
                  </div>
                 <div class="card-actions">
                    <%-- Optional: Link to public creator profile --%>
                    <%-- <a href="#" class="btn btn-secondary btn-sm">View Profile</a> --%>

                    <%-- Invite Button/Form --%>
                    <% if (myActiveCampaigns != null && !myActiveCampaigns.isEmpty()) { %>
                        <form action="<%= contextPath %>/business/invite" method="POST" class="invite-form" style="flex-grow: 1; display:flex; gap: 0.5rem; align-items: center;">
                            <input type="hidden" name="creatorId" value="<%= creator.getUserId() %>">
                             <%-- Keep track of current search params for redirect --%>
                             <input type="hidden" name="redirectBackUrl" value="<%= request.getRequestURI() %>?niche=<%= java.net.URLEncoder.encode(searchNiche, "UTF-8") %>&minFollowers=<%= java.net.URLEncoder.encode(searchMinFollowers, "UTF-8") %>&page=<%= currentPage %>">

                            <select name="campaignId" required style="padding: 0.4rem; border-radius: 4px; background: var(--input-bg); color: var(--input-text); border: 1px solid var(--input-border); flex-grow:1;">
                                <option value="" disabled selected>Invite to Campaign...</option>
                                <% for (Campaign c : myActiveCampaigns) { %>
                                    <option value="<%= c.getCampaignId() %>"><%= c.getTitle() %></option>
                                <% } %>
                            </select>
                             <%-- Optional: Add a small text area for invite message here if needed --%>
                            <button type="submit" class="btn btn-success btn-sm" style="width: auto;">Invite</button>
                        </form>
                     <% } else { %>
                         <button class="btn btn-secondary btn-sm" disabled title="Create an active campaign first to invite creators">Invite</button>
                     <% } %>
                </div>
            </div>
        <% } %>
    </div>

    <%-- Pagination Controls --%>
    <% if (totalPages > 1) { %>
        <nav aria-label="Creator search pagination" class="pagination-nav" style="margin-top: 2rem; text-align: center;">
            <ul style="display: inline-flex; gap: 0.5rem;">
                 <%-- Build base URL for pagination links --%>
                 <% String paginationBaseUrl = contextPath + "/business/search?niche=" + java.net.URLEncoder.encode(searchNiche, "UTF-8") + "&minFollowers=" + java.net.URLEncoder.encode(searchMinFollowers, "UTF-8"); %>

                <%-- Previous Button --%>
                <% if (currentPage > 1) { %>
                    <li><a href="<%= paginationBaseUrl %>&page=<%= currentPage - 1 %>" class="btn btn-secondary">« Prev</a></li>
                <% } else { %>
                     <li class="disabled"><span class="btn btn-secondary" style="opacity: 0.5; cursor: default;">« Prev</span></li>
                <% } %>

                <%-- Page Numbers (Simplified) --%>
                <% for (int i = 1; i <= totalPages; i++) {
                      boolean showPage = (i == 1 || i == totalPages || i == currentPage || i == currentPage - 1 || i == currentPage + 1);
                      if (showPage) { %>
                        <li><a href="<%= paginationBaseUrl %>&page=<%= i %>" class="btn <%= (i == currentPage) ? "btn-primary" : "btn-secondary" %>"><%= i %></a></li>
                 <%   } else if (i == currentPage - 2 || i == currentPage + 2) { %>
                          <li><span class="btn btn-secondary" style="cursor: default; border: none; background: none;">...</span></li>
                 <%   }
                   } %>

                <%-- Next Button --%>
                <% if (currentPage < totalPages) { %>
                    <li><a href="<%= paginationBaseUrl %>&page=<%= currentPage + 1 %>" class="btn btn-secondary">Next »</a></li>
                <% } else { %>
                     <li class="disabled"><span class="btn btn-secondary" style="opacity: 0.5; cursor: default;">Next »</span></li>
                <% } %>
            </ul>
        </nav>
    <% } %>

<% } %>


<%@ include file="../common/footer.jsp" %>