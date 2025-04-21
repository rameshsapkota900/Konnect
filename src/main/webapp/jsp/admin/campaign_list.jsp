<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Campaign" %>
<%@ page import="model.Business" %> <%-- For business name --%>
<%@ page import="model.User" %> <%-- For header --%>
<% request.setAttribute("pageTitle", "All Campaigns"); %>
<%@ include file="../common/header.jsp" %>

<%
    List<Campaign> campaigns = (List<Campaign>) request.getAttribute("campaigns");
     NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
%>

<h2>All Platform Campaigns</h2>

<%-- Error/Success messages handled by header (if any POST actions were added) --%>

<% if (campaigns == null || campaigns.isEmpty()) { %>
    <p class="message info-message">No campaigns found on the platform.</p>
<% } else { %>
     <p>Total campaigns: <%= campaigns.size() %></p>
    <div class="table-container">
        <table class="data-table campaigns-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Title</th>
                    <th>Business</th>
                    <th>Status</th>
                    <th>Created On</th>
                     <th>Dates</th>
                     <th>Budget</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% for (Campaign campaign : campaigns) {
                      Business business = campaign.getBusiness(); // Get attached basic business info
                      String businessName = (business != null && business.getCompanyName() != null) ? business.getCompanyName() : "(Business ID: " + campaign.getBusinessUserId() + ")";
                 %>
                    <tr>
                        <td><%= campaign.getCampaignId() %></td>
                        <td><%= campaign.getTitle() != null ? campaign.getTitle() : "N/A" %></td>
                        <td>
                            <%= businessName %>
                             <%-- Optional: Link to admin view of business profile? --%>
                        </td>
                         <td>
                             <% String statusClass = "";
                                String status = campaign.getStatus();
                                if ("active".equalsIgnoreCase(status)) statusClass = "status-accepted";
                                else if ("inactive".equalsIgnoreCase(status)) statusClass = "status-pending";
                                else if ("completed".equalsIgnoreCase(status)) statusClass = "status-withdrawn";
                                else if ("deleted".equalsIgnoreCase(status)) statusClass = "status-rejected"; // Red for deleted
                             %>
                             <span class="status-badge <%= statusClass %>" style="text-transform: capitalize;"><%= status %></span>
                        </td>
                        <td><%= campaign.getCreatedAt() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(campaign.getCreatedAt()) : "N/A" %></td>
                         <td>
                            <% if (campaign.getStartDate() != null || campaign.getEndDate() != null) { %>
                                <%= campaign.getStartDate() != null ? campaign.getStartDate() : "N/A" %> -
                                <%= campaign.getEndDate() != null ? campaign.getEndDate() : "Ongoing" %>
                            <% } else { %> N/A <% } %>
                         </td>
                          <td>
                             <% if (campaign.getBudget() != null && campaign.getBudget().compareTo(java.math.BigDecimal.ZERO) >= 0) { %>
                                 <%= currencyFormatter.format(campaign.getBudget()) %>
                             <% } else { %> N/A <% } %>
                          </td>
                        <td>
                           <div class="action-group">
                               <%-- Optional Admin Actions --%>
                               <%-- <a href="#" class="btn btn-secondary btn-sm">View Details</a> --%>
                               <%-- Example: Force status change (requires POST handling in servlet) --%>
                               <%-- <form action='...' method='POST'><button>Set Active</button></form> --%>
                               <%-- Example: Hard delete (use with extreme caution!) --%>
                               <%-- <form action='...' method='POST'><button class='btn-delete-confirm'>Delete Permanently</button></form> --%>
                               <span>-</span> <%-- Placeholder for no actions --%>
                           </div>
                        </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
    <%-- Include status badge styles --%>
    <style>
        .status-badge { padding: 0.3em 0.6em; border-radius: 4px; font-size: 0.85em; font-weight: bold; }
        .status-accepted { background-color: var(--success-color); color: white; } /* Active */
        .status-pending { background-color: orange; color: white; } /* Inactive */
        .status-withdrawn { background-color: var(--text-color-muted); color: var(--bg-color); } /* Completed */
         .status-rejected { background-color: var(--error-color); color: white; } /* Deleted */
    </style>
<% } %>

<%@ include file="../common/footer.jsp" %>