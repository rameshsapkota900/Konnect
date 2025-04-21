<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Business" %>
<%@ page import="model.User" %> <%-- For header --%>
<% request.setAttribute("pageTitle", "Business Dashboard"); %>
<%@ include file="../common/header.jsp" %>

<%
    // Get data passed from servlet
    Business businessProfile = (Business) request.getAttribute("businessProfile");
    Long activeCampaignCount = (Long) request.getAttribute("activeCampaignCount");
    Long pendingApplicantCount = (Long) request.getAttribute("pendingApplicantCount");
    Boolean isProfileComplete = (Boolean) request.getAttribute("isProfileComplete");

    // Handle potential nulls
    long campaignCount = (activeCampaignCount != null) ? activeCampaignCount : 0;
    long applicantCount = (pendingApplicantCount != null) ? pendingApplicantCount : 0;
    boolean profileComplete = (isProfileComplete != null) ? isProfileComplete : false;

%>

<h2>Business Dashboard</h2>

<% if (businessProfile != null) { %>
    <p>Welcome back, <strong><%= businessProfile.getCompanyName() != null ? businessProfile.getCompanyName() : "Business User" %>!</strong></p>

    <%-- Profile Completeness Check --%>
    <% if (!profileComplete) { %>
        <div class="message info-message">
            Your business profile seems incomplete. Complete profiles build trust with creators!
            <a href="<%= contextPath %>/business/profile" style="font-weight:bold;">Update Profile Now</a>
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
             <a href="<%= contextPath %>/business/profile" class="card-link">View/Edit Profile</a>
         </div>
         <div class="summary-card">
             <h3>Active Campaigns</h3>
             <p class="count"><%= campaignCount %></p>
             <a href="<%= contextPath %>/business/campaigns" class="card-link">Manage Campaigns</a>
         </div>
         <div class="summary-card">
             <h3>Pending Applicants</h3>
              <p class="count"><%= applicantCount %></p>
              <%-- Link to manage campaigns page, where they can view applicants per campaign --%>
              <a href="<%= contextPath %>/business/campaigns" class="card-link">View Applicants by Campaign</a>
         </div>
         <div class="summary-card">
             <h3>Find Talent</h3>
              <p>Search for creators to invite to your campaigns.</p> <%-- Placeholder --%>
              <a href="<%= contextPath %>/business/search" class="card-link btn btn-primary" style="display:inline-block; margin-top:1rem;">Search Creators</a>
         </div>
     </div>

     <%-- Add more sections as needed --%>
      <div class="dashboard-section" style="margin-top: 2rem;">
          <h3>Quick Actions</h3>
           <div class="button-group">
               <a href="<%= contextPath %>/business/campaigns?action=create" class="btn btn-success">Create New Campaign</a>
               <a href="<%= contextPath %>/business/campaigns" class="btn btn-secondary">Manage Campaigns</a>
               <a href="<%= contextPath %>/business/search" class="btn btn-secondary">Find Creators</a>
              <a href="<%= contextPath %>/chat" class="btn btn-secondary">Go to Chat</a>
          </div>
      </div>


<% } else { %>
     <%-- Fallback if profile is missing --%>
     <p class="message error-message">Could not load your business dashboard. Your profile might be missing. Please try <a href="<%= contextPath %>/business/profile">updating your profile</a> or contact support.</p>
<% } %>


<%@ include file="../common/footer.jsp" %>