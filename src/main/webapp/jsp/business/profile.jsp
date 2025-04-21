<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Business" %>
<%@ page import="model.User" %> <%-- For header --%>
<% request.setAttribute("pageTitle", "Business Profile"); %>
<%@ include file="../common/header.jsp" %>

<%
    Business businessProfile = (Business) request.getAttribute("businessProfile");
    boolean isNewProfile = request.getAttribute("isNewProfile") != null && (Boolean)request.getAttribute("isNewProfile");

    // Defaults for form fields
    String companyName = (businessProfile != null && businessProfile.getCompanyName() != null) ? businessProfile.getCompanyName() : "";
    String website = (businessProfile != null && businessProfile.getWebsite() != null) ? businessProfile.getWebsite() : "";
    String industry = (businessProfile != null && businessProfile.getIndustry() != null) ? businessProfile.getIndustry() : "";
    String description = (businessProfile != null && businessProfile.getDescription() != null) ? businessProfile.getDescription() : "";

%>

<div class="form-container profile-form-container">
    <h2>My Business Profile</h2>

    <% if (isNewProfile) { %>
        <p class="message info-message">Welcome! Please complete your business profile details below.</p>
    <% } %>

    <%-- Error/Success messages handled by header.jsp --%>

    <%-- Profile Edit Form --%>
    <form action="<%= contextPath %>/business/profile" method="POST">
        <%-- No action parameter needed, POST implies update --%>

        <div class="form-group">
            <label for="companyName">Company Name:</label>
            <input type="text" id="companyName" name="companyName" value="<%= companyName %>" required maxlength="255">
        </div>

        <div class="form-group">
            <label for="website">Company Website:</label>
            <input type="url" id="website" name="website" value="<%= website %>" placeholder="https://yourcompany.com" maxlength="255">
        </div>

        <div class="form-group">
            <label for="industry">Industry:</label>
            <input type="text" id="industry" name="industry" value="<%= industry %>" placeholder="e.g., Fashion, Technology, Food & Beverage" maxlength="100">
        </div>

        <div class="form-group">
            <label for="description">Company Description:</label>
            <textarea id="description" name="description" rows="5" placeholder="Tell creators about your company and brand..." maxlength="2000"><%= description %></textarea> <%-- Added maxlength --%>
        </div>

        <div class="form-group">
            <button type="submit" class="btn btn-primary">Save Profile Changes</button>
        </div>

    </form>

</div>

<%@ include file="../common/footer.jsp" %>