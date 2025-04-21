<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Creator" %>
<%@ page import="model.User" %> <%-- Need this for the header include --%>
<% request.setAttribute("pageTitle", "Creator Dashboard"); %>
<%@ include file="../common/header.jsp" %>

<%
    // Get data passed from servlet
    Creator creatorProfile = (Creator) request.getAttribute("creatorProfile");
    Long activeApplicationCount = (Long) request.getAttribute("activeApplicationCount");
    Long pendingInviteCount = (Long) request.getAttribute("pendingInviteCount");
    Boolean isProfileComplete = (Boolean) request.getAttribute("isProfileComplete");

    // Handle potential nulls for counts
    long appCount = (activeApplicationCount != null) ? activeApplicationCount : 0;
    long inviteCount = (pendingInviteCount != null) ? pendingInviteCount : 0;
    boolean profileComplete = (isProfileComplete != null) ? isProfileComplete : false; // Default to false if null

%>

<h2>Creator Dashboard</h2>

<% if (creatorProfile != null) { %>
    <p>Welcome back, <strong><%= creatorProfile.getDisplayName() != null ? creatorProfile.getDisplayName() : "Creator" %>!</strong></p>

    <%-- Profile Completeness Check --%>
    <% if (!profileComplete) { %>
        <div class="message info-message">
            Your profile seems incomplete. A complete profile attracts more businesses!
            <a href="<%= contextPath %>/creator/profile" style="font-weight:bold;">Update Profile Now</a>
        </div>
    <% } %>

     <div class="dashboard-summary">
         <div class="summary-card">
             <h3>Profile Status</h3>
             <p>
                 <span class="status-indicator <%= profileComplete ? "status-complete" : "status-incomplete" %>">
                     <%= profileComplete ? "Complete" : "Incomplete" %>
                 </span>
             </p>
             <a href="<%= contextPath %>/creator/profile" class="card-link">View/Edit Profile</a>
         </div>
         <div class="summary-card">
             <h3>Active Applications</h3>
             <p class="count"><%= appCount %></p>
             <a href="<%= contextPath %>/creator/applications" class="card-link">Manage Applications</a>
         </div>
         <div class="summary-card">
             <h3>Pending Invites</h3>
              <p class="count"><%= inviteCount %></p>
              <a href="<%= contextPath %>/creator/invites" class="card-link">View Invites</a>
         </div>
         <div class="summary-card">
             <h3>Find Opportunities</h3>
              <p>Discover new brand campaigns.</p> <%-- Placeholder text --%>
              <a href="<%= contextPath %>/creator/campaigns" class="card-link btn btn-primary" style="display:inline-block; margin-top:1rem;">Browse Campaigns</a>
         </div>
     </div>

     <%-- Add more sections as needed, e.g., recent messages, performance stats --%>
      <div class="dashboard-section" style="margin-top: 2rem;">
          <h3>Quick Actions</h3>
           <div class="button-group">
              <a href="<%= contextPath %>/creator/profile" class="btn btn-secondary">Update Profile</a>
              <a href="<%= contextPath %>/creator/campaigns" class="btn btn-secondary">Find Campaigns</a>
              <a href="<%= contextPath %>/chat" class="btn btn-secondary">Go to Chat</a>
          </div>
      </div>


<% } else { %>
     <%-- This case should ideally be handled by redirect in servlet, but added as fallback --%>
     <p class="message error-message">Could not load your creator dashboard. Your profile might be missing. Please try <a href="<%= contextPath %>/creator/profile">updating your profile</a> or contact support.</p>
<% } %>


<%@ include file="../common/footer.jsp" %>