<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Invite" %>
<%@ page import="model.Campaign" %>
<%@ page import="model.Business" %>
<%@ page import="model.User" %> <%-- For header --%>
<% request.setAttribute("pageTitle", "Campaign Invites"); %>
<%@ include file="../common/header.jsp" %>

<%
    List<Invite> invites = (List<Invite>) request.getAttribute("invites");
%>

<h2>Campaign Invites</h2>

<%-- Error/Success messages handled by header --%>

<% if (invites == null || invites.isEmpty()) { %>
    <p class="message info-message">You have no pending campaign invites.</p>
<% } else { %>
    <div class="table-container">
        <table class="data-table invites-table">
            <thead>
                <tr>
                    <th>Campaign Title</th>
                    <th>Invited By</th>
                    <th>Received On</th>
                    <th>Message</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% for (Invite invite : invites) {
                     Campaign campaign = invite.getCampaign();
                     Business business = invite.getBusiness();
                     String campaignTitle = (campaign != null && campaign.getTitle() != null) ? campaign.getTitle() : "Campaign Not Found";
                     String businessName = (business != null && business.getCompanyName() != null) ? business.getCompanyName() : "Business Not Found";
                %>
                    <tr>
                        <td>
                           <% if (campaign != null) { %>
                               <a href="<%= contextPath %>/creator/campaigns?action=viewDetail&id=<%= invite.getCampaignId() %>"><%= campaignTitle %></a>
                           <% } else { %>
                                <%= campaignTitle %>
                           <% } %>
                        </td>
                         <td>
                            <%-- Optional: Link to business public profile? --%>
                            <%= businessName %>
                         </td>
                        <td><%= invite.getSentAt() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(invite.getSentAt()) : "N/A" %></td>
                        <td>
                           <% String msg = invite.getInviteMessage();
                              if (msg != null && !msg.isEmpty()) {
                                 out.print(msg.length() > 80 ? msg.substring(0, 80) + "..." : msg);
                              } else { out.print("-"); }
                           %>
                           <%-- Optional: Button to view full message --%>
                        </td>
                        <td>
                            <%-- Add styling based on status --%>
                            <% String statusClass = "";
                               String status = invite.getStatus();
                               if ("pending".equalsIgnoreCase(status)) statusClass = "status-pending";
                               else if ("accepted".equalsIgnoreCase(status)) statusClass = "status-accepted";
                               else if ("rejected".equalsIgnoreCase(status)) statusClass = "status-rejected";
                            %>
                            <span class="status-badge <%= statusClass %>" style="text-transform: capitalize; font-weight:bold;"><%= status %></span>
                        </td>
                        <td>
                            <%-- Show Accept/Reject buttons only if pending --%>
                            <% if ("pending".equalsIgnoreCase(invite.getStatus())) { %>
                                <div class="action-group">
                                    <form action="<%= contextPath %>/creator/invites" method="POST" style="display: inline;">
                                        <input type="hidden" name="action" value="accept">
                                        <input type="hidden" name="inviteId" value="<%= invite.getInviteId() %>">
                                        <button type="submit" class="btn btn-success btn-sm">Accept</button>
                                    </form>
                                    <form action="<%= contextPath %>/creator/invites" method="POST" style="display: inline;">
                                        <input type="hidden" name="action" value="reject">
                                        <input type="hidden" name="inviteId" value="<%= invite.getInviteId() %>">
                                        <button type="submit" class="btn btn-danger btn-sm">Reject</button>
                                    </form>
                                </div>
                            <% } else { %>
                                <span>-</span> <%-- No actions if not pending --%>
                            <% } %>
                        </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
    <%-- Include status badge styles if not already globally defined --%>
    <style>
        .status-badge { padding: 0.3em 0.6em; border-radius: 4px; font-size: 0.85em; }
        .status-pending { background-color: orange; color: white; }
        .status-accepted { background-color: var(--success-color); color: white; }
        .status-rejected { background-color: var(--error-color); color: white; }
    </style>
<% } %>

<%@ include file="../common/footer.jsp" %>