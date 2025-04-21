<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Application" %>
<%@ page import="model.Campaign" %>
<%@ page import="model.Creator" %>
<%@ page import="model.User" %> <%-- For header --%>

<%
    Campaign campaign = (Campaign) request.getAttribute("campaign");
    List<Application> applicants = (List<Application>) request.getAttribute("applicants");

    String pageTitle = "Applicants";
    if (campaign != null) {
        pageTitle = "Applicants for: " + campaign.getTitle();
    }
    request.setAttribute("pageTitle", pageTitle);
%>

<%@ include file="../common/header.jsp" %>

<% if (campaign == null) { %>
    <p class="message error-message">Could not load campaign details. Please go back and select a campaign.</p>
    <a href="<%= contextPath %>/business/campaigns" class="btn btn-secondary">Back to My Campaigns</a>
<% } else { %>
    <div style="margin-bottom: 1.5rem;">
        <h2>Applicants for: <%= campaign.getTitle() %></h2>
         <a href="<%= contextPath %>/business/campaigns" class="btn btn-secondary btn-sm">« Back to My Campaigns</a>
    </div>


    <%-- Error/Success messages handled by header --%>

    <% if (applicants == null || applicants.isEmpty()) { %>
        <p class="message info-message">No creators have applied to this campaign yet.</p>
    <% } else { %>
        <div class="table-container">
            <table class="data-table applicants-table">
                <thead>
                    <tr>
                        <th>Creator Name</th>
                        <th>Applied On</th>
                        <th>Pitch</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Application app : applicants) {
                         Creator creator = app.getCreator(); // Get attached basic creator info
                         User creatorUser = app.getCreatorUser(); // Get attached basic user info
                         String creatorName = (creator != null && creator.getDisplayName() != null) ? creator.getDisplayName() : "N/A";
                         String creatorEmail = (creatorUser != null && creatorUser.getEmail() != null) ? creatorUser.getEmail() : "N/A";
                         // Note: creatorUser.isBanned() should be false due to DAO filter, but can double-check
                    %>
                        <tr>
                            <td>
                                <%-- Optional: Link to creator public profile or admin view? --%>
                                <%= creatorName %>
                                <br><small style="color: var(--text-color-muted);"><%= creatorEmail %></small>
                            </td>
                             <td><%= app.getAppliedAt() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(app.getAppliedAt()) : "N/A" %></td>
                            <td>
                               <% String pitch = app.getPitchMessage();
                                  if (pitch != null) {
                                     out.print(pitch.length() > 100 ? pitch.substring(0, 100) + "..." : pitch);
                                  } else { out.print("-"); }
                               %>
                                <%-- Optional: Button to view full pitch in modal --%>
                            </td>
                             <td>
                                <% String statusClass = "";
                                   String status = app.getStatus();
                                   if ("pending".equalsIgnoreCase(status)) statusClass = "status-pending";
                                   else if ("accepted".equalsIgnoreCase(status)) statusClass = "status-accepted";
                                   else if ("rejected".equalsIgnoreCase(status)) statusClass = "status-rejected";
                                   else if ("withdrawn".equalsIgnoreCase(status)) statusClass = "status-withdrawn";
                                %>
                                <span class="status-badge <%= statusClass %>" style="text-transform: capitalize;"><%= status %></span>
                            </td>
                            <td>
                                <%-- Show Accept/Reject buttons only if pending --%>
                                <% if ("pending".equalsIgnoreCase(app.getStatus())) { %>
                                    <div class="action-group">
                                        <form action="<%= contextPath %>/business/applicants" method="POST" style="display: inline;">
                                             <input type="hidden" name="action" value="accept">
                                             <input type="hidden" name="applicationId" value="<%= app.getApplicationId() %>">
                                             <input type="hidden" name="campaignId" value="<%= campaign.getCampaignId() %>"> <%-- Need for redirect --%>
                                             <button type="submit" class="btn btn-success btn-sm">Accept</button>
                                        </form>
                                         <form action="<%= contextPath %>/business/applicants" method="POST" style="display: inline;">
                                             <input type="hidden" name="action" value="reject">
                                             <input type="hidden" name="applicationId" value="<%= app.getApplicationId() %>">
                                             <input type="hidden" name="campaignId" value="<%= campaign.getCampaignId() %>">
                                             <button type="submit" class="btn btn-danger btn-sm">Reject</button>
                                         </form>
                                    </div>
                                <% } else if ("accepted".equalsIgnoreCase(app.getStatus())) { %>
                                     <%-- Show Chat button if accepted --%>
                                      <a href="<%= contextPath %>/chat?partnerId=<%= app.getCreatorUserId() %>" class="btn btn-primary btn-sm">Chat with Creator</a>
                                <% } else { %>
                                     <span>-</span> <%-- No actions if rejected/withdrawn --%>
                                <% } %>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
        <%-- Include status badge styles --%>
        <style>
            .status-badge { padding: 0.3em 0.6em; border-radius: 4px; font-size: 0.85em; font-weight: bold; }
            .status-pending { background-color: orange; color: white; }
            .status-accepted { background-color: var(--success-color); color: white; }
            .status-rejected { background-color: var(--error-color); color: white; }
            .status-withdrawn { background-color: var(--text-color-muted); color: var(--bg-color); }
        </style>
    <% } %>
<% } %>

<%@ include file="../common/footer.jsp" %>