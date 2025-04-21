<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Application" %>
<%@ page import="model.Campaign" %> <%-- For campaign title --%>
<%@ page import="model.User" %> <%-- For header --%>
<% request.setAttribute("pageTitle", "My Applications"); %>
<%@ include file="../common/header.jsp" %>

<%
    List<Application> applications = (List<Application>) request.getAttribute("applications");
%>

<h2>My Campaign Applications</h2>

<%-- Error/Success messages handled by header --%>

<% if (applications == null || applications.isEmpty()) { %>
    <p class="message info-message">You haven't applied to any campaigns yet.</p>
    <p><a href="<%= contextPath %>/creator/campaigns" class="btn btn-primary">Find Campaigns to Apply To</a></p>
<% } else { %>
    <div class="table-container">
        <table class="data-table applications-table">
            <thead>
                <tr>
                    <th>Campaign Title</th>
                    <th>Applied On</th>
                    <th>Your Pitch</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% for (Application app : applications) {
                     Campaign campaign = app.getCampaign(); // Get attached campaign basic info
                     String campaignTitle = (campaign != null && campaign.getTitle() != null) ? campaign.getTitle() : "Campaign Not Found";
                %>
                    <tr>
                        <td>
                           <% if (campaign != null) { %>
                              <%-- Link to campaign detail, might be inactive now but still link --%>
                              <a href="<%= contextPath %>/creator/campaigns?action=viewDetail&id=<%= app.getCampaignId() %>"><%= campaignTitle %></a>
                           <% } else { %>
                               <%= campaignTitle %>
                           <% } %>
                        </td>
                        <td><%= app.getAppliedAt() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(app.getAppliedAt()) : "N/A" %></td>
                        <td>
                           <% String pitch = app.getPitchMessage();
                              if (pitch != null) {
                                 out.print(pitch.length() > 100 ? pitch.substring(0, 100) + "..." : pitch);
                              } else { out.print("-"); }
                           %>
                        </td>
                        <td>
                            <%-- Add styling based on status --%>
                            <% String statusClass = "";
                               String status = app.getStatus();
                               if ("pending".equalsIgnoreCase(status)) statusClass = "status-pending";
                               else if ("accepted".equalsIgnoreCase(status)) statusClass = "status-accepted";
                               else if ("rejected".equalsIgnoreCase(status)) statusClass = "status-rejected";
                               else if ("withdrawn".equalsIgnoreCase(status)) statusClass = "status-withdrawn";
                            %>
                            <span class="status-badge <%= statusClass %>" style="text-transform: capitalize; font-weight:bold;"><%= status %></span>
                        </td>
                        <td>
                           <div class="action-group">
                            <%-- Allow withdrawal only if pending --%>
                            <% if ("pending".equalsIgnoreCase(app.getStatus())) { %>
                                <form action="<%= contextPath %>/creator/applications" method="POST" style="display: inline;">
                                    <input type="hidden" name="action" value="withdraw">
                                    <input type="hidden" name="applicationId" value="<%= app.getApplicationId() %>">
                                    <button type="submit" class="btn btn-secondary btn-sm btn-delete-confirm" data-confirm-message="Are you sure you want to withdraw this application?">Withdraw</button>
                                </form>
                            <% } %>
                             <%-- Add Chat button if accepted --%>
                             <% if ("accepted".equalsIgnoreCase(app.getStatus()) && campaign != null) { %>
                                 <a href="<%= contextPath %>/chat?partnerId=<%= campaign.getBusinessUserId() %>" class="btn btn-primary btn-sm">Chat with Business</a>
                             <% } %>

                             <%-- Optional: View Pitch Detail button --%>
                             <%-- <button class="btn btn-secondary btn-sm" onclick="showPitch('<%= app.getApplicationId() %>')">View Pitch</button> --%>
                             <% if (!"pending".equalsIgnoreCase(app.getStatus()) && !"accepted".equalsIgnoreCase(app.getStatus())) { %>
                                 <span>-</span> <%-- Placeholder if no actions --%>
                             <% } %>
                           </div>
                        </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
    <%-- Add styling for status badges --%>
    <style>
        .status-badge { padding: 0.3em 0.6em; border-radius: 4px; font-size: 0.85em; }
        .status-pending { background-color: orange; color: white; }
        .status-accepted { background-color: var(--success-color); color: white; }
        .status-rejected { background-color: var(--error-color); color: white; }
        .status-withdrawn { background-color: var(--text-color-muted); color: var(--bg-color); }
    </style>

     <%-- Optional: Modal or area to display full pitch message (requires JS) --%>

<% } %>

<%@ include file="../common/footer.jsp" %>