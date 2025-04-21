<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User" %> <%-- For header --%>
<% request.setAttribute("pageTitle", "Admin Dashboard"); %>
<%@ include file="../common/header.jsp" %>

<%
    // Get data passed from servlet
    Long totalUserCount = (Long) request.getAttribute("totalUserCount");
    Long creatorCount = (Long) request.getAttribute("creatorCount");
    Long businessCount = (Long) request.getAttribute("businessCount");
    Long bannedUserCount = (Long) request.getAttribute("bannedUserCount");
    Long totalCampaignCount = (Long) request.getAttribute("totalCampaignCount");
    Long pendingReportCount = (Long) request.getAttribute("pendingReportCount");

    // Handle potential nulls
    long users = (totalUserCount != null) ? totalUserCount : 0;
    long creators = (creatorCount != null) ? creatorCount : 0;
    long businesses = (businessCount != null) ? businessCount : 0;
    long banned = (bannedUserCount != null) ? bannedUserCount : 0;
    long campaigns = (totalCampaignCount != null) ? totalCampaignCount : 0;
    long reports = (pendingReportCount != null) ? pendingReportCount : 0;
%>

<h2>Admin Dashboard</h2>
<p>Platform Overview and Management</p>

 <div class="dashboard-summary">
     <div class="summary-card">
         <h3>Total Users</h3>
         <p class="count"><%= users %></p>
         <span style="font-size: 0.9em; color: var(--text-color-muted);">Creators: <%= creators %> | Businesses: <%= businesses %></span><br>
         <span style="font-size: 0.9em; color: var(--error-color);">Banned: <%= banned %></span>
         <a href="<%= contextPath %>/admin/users" class="card-link">Manage Users</a>
     </div>
     <div class="summary-card">
         <h3>Total Campaigns</h3>
         <p class="count"><%= campaigns %></p>
         <%-- Add counts for active/inactive if needed from DAO --%>
         <a href="<%= contextPath %>/admin/campaigns" class="card-link">View All Campaigns</a>
     </div>
     <div class="summary-card">
         <h3>Pending Reports</h3>
          <p class="count"><%= reports %></p>
          <a href="<%= contextPath %>/admin/reports" class="card-link">Review Reports</a>
     </div>
      <div class="summary-card">
          <h3>System Management</h3>
          <p>Access key administrative functions.</p>
           <div class="button-group" style="margin-top: 1rem; flex-direction: column; align-items: stretch;">
               <a href="<%= contextPath %>/admin/users" class="btn btn-secondary">User Management</a>
               <a href="<%= contextPath %>/admin/campaigns" class="btn btn-secondary">Campaign Overview</a>
                <a href="<%= contextPath %>/admin/reports" class="btn btn-secondary">Report Management</a>
                <%-- Add links to other potential admin functions --%>
           </div>
      </div>
 </div>


<%@ include file="../common/footer.jsp" %>