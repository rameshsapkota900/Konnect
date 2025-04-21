<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Campaign" %>
<%@ page import="model.Business" %>
<%@ page import="model.Application" %> <%-- For checking existing application --%>
<%@ page import="model.User" %> <%-- For header --%>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<%
    Campaign campaign = (Campaign) request.getAttribute("campaign");
    Application existingApplication = (Application) request.getAttribute("existingApplication"); // Check if already applied
     String pitchMessage = request.getAttribute("pitchMessage") != null ? (String)request.getAttribute("pitchMessage") : ""; // Retain pitch on error

    String pageTitle = "Campaign Details";
    if (campaign != null) {
        pageTitle = campaign.getTitle();
    }
    request.setAttribute("pageTitle", pageTitle);

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
%>

<%@ include file="../common/header.jsp" %>

<% if (campaign == null) { %>
    <%-- Error message handled by header.jsp, show a simple message here too --%>
    <p class="message error-message">The requested campaign could not be found or is no longer available.</p>
    <p><a href="<%= contextPath %>/creator/campaigns" class="btn btn-secondary">Back to Campaigns</a></p>
<% } else { %>
    <div class="campaign-detail-container" style="background: var(--card-bg); padding: 2rem; border-radius: 8px;">

        <%-- Campaign Header --%>
        <div class="campaign-header" style="border-bottom: 1px solid var(--border-color); padding-bottom: 1.5rem; margin-bottom: 1.5rem;">
            <h1><%= campaign.getTitle() %></h1>
            <% Business business = campaign.getBusiness();
               if (business != null && business.getCompanyName() != null) { %>
                <p style="color: var(--text-color-muted); font-size: 1.1rem;">
                    Offered by: <strong><%= business.getCompanyName() %></strong>
                     <%-- Optional: Link to business public profile if implemented --%>
                     <%-- | <a href="<%= contextPath %>/public/business?id=<%= business.getUserId() %>">View Business Profile</a> --%>
                </p>
            <% } %>
        </div>

        <%-- Campaign Image --%>
         <% if (campaign.getProductImagePath() != null && !campaign.getProductImagePath().isEmpty()) { %>
            <div style="margin-bottom: 2rem; text-align: center;">
                 <img src="<%= contextPath %>/<%= campaign.getProductImagePath() %>" alt="<%= campaign.getTitle() %> Product" style="max-width: 400px; max-height: 400px; border-radius: 8px; border: 1px solid var(--border-color);">
            </div>
         <% } %>

        <%-- Campaign Details Section --%>
        <div class="campaign-details-section">
            <h3>Campaign Details</h3>
             <dl class="profile-details"> <%-- Reusing profile detail styles --%>
                  <dt>Description:</dt>
                  <dd><%= campaign.getDescription() != null ? campaign.getDescription().replace("\n", "<br>") : "N/A" %></dd>

                  <dt>Creator Requirements:</dt>
                  <dd><%= campaign.getRequirements() != null ? campaign.getRequirements().replace("\n", "<br>") : "N/A" %></dd>

                  <dt>Budget:</dt>
                  <dd>
                      <% if (campaign.getBudget() != null && campaign.getBudget().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
                          <%= currencyFormatter.format(campaign.getBudget()) %>
                      <% } else { %>
                           Not specified / Negotiable
                      <% } %>
                  </dd>

                  <dt>Dates:</dt>
                   <dd>
                       <% if (campaign.getStartDate() != null || campaign.getEndDate() != null) { %>
                            <% if (campaign.getStartDate() != null) { %> Start: <%= campaign.getStartDate() %> <% } %>
                            <% if (campaign.getEndDate() != null) { %> | End: <%= campaign.getEndDate() %> <% } %>
                        <% } else { %>
                             Dates not specified
                        <% } %>
                    </dd>

                 <dt>Status:</dt>
                 <dd style="text-transform: capitalize;"><%= campaign.getStatus() %></dd>

             </dl>
        </div>

        <%-- Application Section --%>
        <div class="campaign-application-section" style="margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid var(--border-color);">
            <h3>Apply to this Campaign</h3>

            <%-- Check if already applied --%>
            <% if (existingApplication != null) { %>
                <p class="message info-message">
                    You have already applied to this campaign. Status: <strong><%= existingApplication.getStatus() %></strong><br>
                    <a href="<%= contextPath %>/creator/applications">View My Applications</a>
                </p>
            <% } else { %>
                 <%-- Error message specifically for application failure (handled by header too, but can be specific here) --%>
                  <% String applyErrorMessage = (String) request.getAttribute("errorMessage"); %>
                  <% if (applyErrorMessage != null && !applyErrorMessage.isEmpty()) { %>
                      <p class="message error-message"><%= applyErrorMessage %></p>
                  <% } %>

                 <form action="<%= contextPath %>/creator/apply" method="POST" class="apply-form">
                     <input type="hidden" name="campaignId" value="<%= campaign.getCampaignId() %>">

                     <div class="form-group">
                         <label for="pitchMessage">Your Pitch Message:</label>
                         <textarea id="pitchMessage" name="pitchMessage" rows="6" required placeholder="Explain why you're a great fit for this campaign..." maxlength="2000"><%= pitchMessage %></textarea>
                         <small style="display: block; margin-top: 4px; color: var(--text-color-muted);">Max 2000 characters.</small>
                     </div>

                     <div class="form-group">
                         <button type="submit" class="btn btn-success">Submit Application</button>
                          <a href="<%= contextPath %>/creator/campaigns" class="btn btn-secondary" style="margin-left: 1rem;">Back to Campaigns</a>
                     </div>
                 </form>
            <% } %>

        </div>

    </div>
<% } %>


<%@ include file="../common/footer.jsp" %>