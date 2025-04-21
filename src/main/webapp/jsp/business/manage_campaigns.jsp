<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="model.Campaign" %>
<%@ page import="model.User" %> <%-- For header --%>
<% request.setAttribute("pageTitle", "Manage Campaigns"); %>
<%@ include file="../common/header.jsp" %>

<%
    List<Campaign> campaigns = (List<Campaign>) request.getAttribute("campaigns");
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
%>

<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
     <h2>My Campaigns</h2>
     <a href="<%= contextPath %>/business/campaigns?action=create" class="btn btn-success">Create New Campaign</a>
</div>


<%-- Error/Success messages handled by header --%>

<% if (campaigns == null || campaigns.isEmpty()) { %>
    <p class="message info-message">You haven't created any campaigns yet.</p>
    <p><a href="<%= contextPath %>/business/campaigns?action=create" class="btn btn-primary">Create Your First Campaign</a></p>
<% } else { %>
    <div class="table-container">
        <table class="data-table campaigns-table">
            <thead>
                <tr>
                    <th>Title</th>
                    <th>Status</th>
                    <th>Dates</th>
                    <th>Budget</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% for (Campaign campaign : campaigns) { %>
                    <tr>
                        <td><%= campaign.getTitle() != null ? campaign.getTitle() : "N/A" %></td>
                        <td>
                             <%-- Style status --%>
                             <% String statusClass = "";
                                String status = campaign.getStatus();
                                if ("active".equalsIgnoreCase(status)) statusClass = "status-accepted"; // Green for active
                                else if ("inactive".equalsIgnoreCase(status)) statusClass = "status-pending"; // Orange for inactive
                                else if ("completed".equalsIgnoreCase(status)) statusClass = "status-withdrawn"; // Grey for completed
                                // Deleted campaigns are filtered out by the servlet/DAO
                             %>
                             <span class="status-badge <%= statusClass %>" style="text-transform: capitalize;"><%= status %></span>
                        </td>
                        <td>
                            <% if (campaign.getStartDate() != null || campaign.getEndDate() != null) { %>
                                <%= campaign.getStartDate() != null ? campaign.getStartDate() : "N/A" %> -
                                <%= campaign.getEndDate() != null ? campaign.getEndDate() : "Ongoing" %>
                            <% } else { %>
                                N/A
                            <% } %>
                        </td>
                        <td>
                             <% if (campaign.getBudget() != null && campaign.getBudget().compareTo(java.math.BigDecimal.ZERO) >= 0) { %>
                                 <%= currencyFormatter.format(campaign.getBudget()) %>
                             <% } else { %>
                                 N/A
                             <% } %>
                        </td>
                        <td>
                           <div class="action-group">
                               <%-- View Applicants Link --%>
                               <a href="<%= contextPath %>/business/applicants?campaignId=<%= campaign.getCampaignId() %>" class="btn btn-primary btn-sm">View Applicants</a>
                               <%-- Edit Link --%>
                                <a href="<%= contextPath %>/business/campaigns?action=edit&id=<%= campaign.getCampaignId() %>" class="btn btn-secondary btn-sm">Edit</a>
                                <%-- Delete Form/Button --%>
                                <form action="<%= contextPath %>/business/campaigns" method="POST" style="display: inline;">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="campaignId" value="<%= campaign.getCampaignId() %>">
                                    <button type="submit" class="btn btn-danger btn-sm btn-delete-confirm" data-confirm-message="Are you sure you want to delete this campaign? This cannot be undone.">Delete</button>
                                </form>
                           </div>
                        </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
    <%-- Re-include status styles if needed --%>
    <style>
        .status-badge { padding: 0.3em 0.6em; border-radius: 4px; font-size: 0.85em; font-weight: bold; }
        .status-accepted { background-color: var(--success-color); color: white; } /* Active */
        .status-pending { background-color: orange; color: white; } /* Inactive */
        .status-withdrawn { background-color: var(--text-color-muted); color: var(--bg-color); } /* Completed */
    </style>
<% } %>

<%@ include file="../common/footer.jsp" %>