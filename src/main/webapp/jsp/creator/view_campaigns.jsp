<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Campaign" %>
<%@ page import="model.User" %> <%-- For header --%>
<% request.setAttribute("pageTitle", "Find Campaigns"); %>
<%@ include file="../common/header.jsp" %>

<%
    List<Campaign> campaigns = (List<Campaign>) request.getAttribute("campaigns");
    int currentPage = (request.getAttribute("currentPage") != null) ? (Integer) request.getAttribute("currentPage") : 1;
    int totalPages = (request.getAttribute("totalPages") != null) ? (Integer) request.getAttribute("totalPages") : 1;
    int totalCampaigns = (request.getAttribute("totalCampaigns") != null) ? (Integer) request.getAttribute("totalCampaigns") : 0;
    String searchTerm = (request.getAttribute("searchTerm") != null) ? (String) request.getAttribute("searchTerm") : "";

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US); // For formatting budget
%>

<h2>Available Campaigns (<%= totalCampaigns %> Found)</h2>

<%-- Search Form --%>
<div class="search-filter-form" style="margin-bottom: 2rem; background: var(--card-bg); padding: 1.5rem; border-radius: 8px;">
    <form action="<%= contextPath %>/creator/campaigns" method="GET">
         <div class="form-group">
              <label for="search">Search by Keyword (Title/Description):</label>
              <input type="search" id="search" name="search" value="<%= searchTerm %>" placeholder="Enter keywords..." style="width:auto; min-width: 300px; display: inline-block; margin-right: 1rem;">
              <button type="submit" class="btn btn-primary" style="width: auto;">Search</button>
              <% if (!searchTerm.isEmpty()) { %>
                  <a href="<%= contextPath %>/creator/campaigns" class="btn btn-secondary" style="width: auto; margin-left: 0.5rem;">Clear</a>
              <% } %>
         </div>
    </form>
</div>


<% if (campaigns == null || campaigns.isEmpty()) { %>
    <p class="message info-message">
        <% if (!searchTerm.isEmpty()) { %>
             No campaigns found matching your search term: "<%= searchTerm %>".
        <% } else { %>
             There are currently no active campaigns available. Check back soon!
        <% } %>
    </p>
<% } else { %>
    <div class="card-grid campaign-list">
        <% for (Campaign campaign : campaigns) { %>
            <div class="card campaign-card">
                 <%-- Display Campaign Image if available --%>
                 <% if (campaign.getProductImagePath() != null && !campaign.getProductImagePath().isEmpty()) { %>
                 <div class="campaign-image">
                    <img src="<%= contextPath %>/<%= campaign.getProductImagePath() %>" alt="<%= campaign.getTitle() %> Product Image" loading="lazy">
                 </div>
                 <% } %>

                <div class="card-header">
                    <h3><%= campaign.getTitle() %></h3>
                    <%-- Optionally show business name if available --%>
                    <% if (campaign.getBusiness() != null && campaign.getBusiness().getCompanyName() != null) { %>
                       <small style="color: var(--text-color-muted);">by <%= campaign.getBusiness().getCompanyName() %></small>
                    <% } %>
                </div>
                <div class="card-content">
                     <%-- Display truncated description --%>
                     <p>
                         <% String desc = campaign.getDescription();
                            if (desc != null) {
                                out.print(desc.length() > 150 ? desc.substring(0, 150) + "..." : desc);
                            }
                         %>
                     </p>
                </div>
                <div class="card-meta">
                      <%-- Display Budget --%>
                      <% if (campaign.getBudget() != null && campaign.getBudget().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
                          <span>Budget: <%= currencyFormatter.format(campaign.getBudget()) %></span>
                      <% } else { %>
                           <span>Budget: Not specified</span>
                      <% } %>
                      <%-- Display Dates --%>
                      <% if (campaign.getStartDate() != null || campaign.getEndDate() != null) { %>
                           <span>
                               <% if (campaign.getStartDate() != null) { %> Start: <%= campaign.getStartDate() %> <% } %>
                               <% if (campaign.getEndDate() != null) { %> | End: <%= campaign.getEndDate() %> <% } %>
                           </span>
                       <% } %>
                 </div>
                <div class="card-actions">
                     <a href="<%= contextPath %>/creator/campaigns?action=viewDetail&id=<%= campaign.getCampaignId() %>" class="btn btn-primary">View Details & Apply</a>
                </div>
            </div>
        <% } %>
    </div>

    <%-- Pagination Controls --%>
    <% if (totalPages > 1) { %>
        <nav aria-label="Campaign pagination" class="pagination-nav" style="margin-top: 2rem; text-align: center;">
            <ul style="display: inline-flex; gap: 0.5rem;">
                <%-- Previous Button --%>
                <% if (currentPage > 1) { %>
                    <li><a href="<%= contextPath %>/creator/campaigns?search=<%= java.net.URLEncoder.encode(searchTerm, "UTF-8") %>&page=<%= currentPage - 1 %>" class="btn btn-secondary">&laquo; Prev</a></li>
                <% } else { %>
                     <li class="disabled"><span class="btn btn-secondary" style="opacity: 0.5; cursor: default;">&laquo; Prev</span></li>
                <% } %>

                <%-- Page Numbers (Simplified: show current and nearby) --%>
                <% for (int i = 1; i <= totalPages; i++) {
                      // Basic pagination: show first, last, current, and adjacent pages
                      boolean showPage = (i == 1 || i == totalPages || i == currentPage || i == currentPage - 1 || i == currentPage + 1);
                      // Add ellipsis logic for larger number of pages if needed
                      if (showPage) {
                 %>
                        <li><a href="<%= contextPath %>/creator/campaigns?search=<%= java.net.URLEncoder.encode(searchTerm, "UTF-8") %>&page=<%= i %>"
                               class="btn <%= (i == currentPage) ? "btn-primary" : "btn-secondary" %>"><%= i %></a></li>
                 <%   } else if (i == currentPage - 2 || i == currentPage + 2) { %>
                          <li><span class="btn btn-secondary" style="cursor: default; border: none; background: none;">...</span></li>
                 <%   }
                   } %>

                <%-- Next Button --%>
                <% if (currentPage < totalPages) { %>
                    <li><a href="<%= contextPath %>/creator/campaigns?search=<%= java.net.URLEncoder.encode(searchTerm, "UTF-8") %>&page=<%= currentPage + 1 %>" class="btn btn-secondary">Next &raquo;</a></li>
                <% } else { %>
                     <li class="disabled"><span class="btn btn-secondary" style="opacity: 0.5; cursor: default;">Next &raquo;</span></li>
                <% } %>
            </ul>
        </nav>
    <% } %>

<% } %>


<%@ include file="../common/footer.jsp" %>