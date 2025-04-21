<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Campaign" %>
<%@ page import="model.User" %> <%-- For header --%>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.sql.Date" %>

<%
    // Determine if we are creating or editing
    String formAction = (String) request.getAttribute("formAction"); // "create" or "edit"
    Campaign campaign = (Campaign) request.getAttribute("campaign");

    boolean isEditMode = "edit".equals(formAction);
    String pageTitle = isEditMode ? "Edit Campaign" : "Create New Campaign";
    request.setAttribute("pageTitle", pageTitle);

    // Set default values for form fields
    String title = "";
    String description = "";
    String requirements = "";
    String status = "active"; // Default status for new campaigns
    BigDecimal budget = null;
    Date startDate = null;
    Date endDate = null;
    String productImagePath = null;
    int campaignId = 0; // Will be > 0 in edit mode

    if (campaign != null) {
        title = campaign.getTitle() != null ? campaign.getTitle() : "";
        description = campaign.getDescription() != null ? campaign.getDescription() : "";
        requirements = campaign.getRequirements() != null ? campaign.getRequirements() : "";
        status = campaign.getStatus() != null ? campaign.getStatus() : "active";
        budget = campaign.getBudget();
        startDate = campaign.getStartDate();
        endDate = campaign.getEndDate();
        productImagePath = campaign.getProductImagePath();
        campaignId = campaign.getCampaignId(); // Will be 0 for new campaign
    }

    // Get error message from request attribute if validation failed
    String validationError = (String) request.getAttribute("validationError"); // Set by ManageCampaignsServlet
%>

<%@ include file="../common/header.jsp" %>

<div class="form-container campaign-form-container">
    <h2><%= pageTitle %></h2>

    <%-- Display validation error message if present --%>
    <% if (validationError != null && !validationError.isEmpty()) { %>
        <p class="message error-message"><%= validationError %></p>
    <% } %>

    <%-- Error/Success messages from redirects are handled by header --%>

    <form action="<%= contextPath %>/business/campaigns" method="POST" enctype="multipart/form-data">
        <%-- Set action based on mode --%>
        <input type="hidden" name="action" value="<%= isEditMode ? "edit" : "create" %>">
        <% if (isEditMode) { %>
            <input type="hidden" name="campaignId" value="<%= campaignId %>">
            <%-- Store existing image path to keep it if no new file uploaded --%>
            <input type="hidden" name="existingImagePath" value="<%= productImagePath != null ? productImagePath : "" %>">
        <% } %>

        <div class="form-group">
            <label for="title">Campaign Title:</label>
            <input type="text" id="title" name="title" value="<%= title %>" required maxlength="255">
        </div>

        <div class="form-group">
            <label for="description">Campaign Description:</label>
            <textarea id="description" name="description" rows="6" required placeholder="Describe the campaign goals, target audience, and what you're looking for in a creator." maxlength="5000"><%= description %></textarea>
        </div>

        <div class="form-group">
            <label for="requirements">Creator Requirements:</label>
            <textarea id="requirements" name="requirements" rows="4" placeholder="List specific requirements (e.g., platform type, content format, number of posts, specific hashtags, deadlines)." maxlength="2000"><%= requirements %></textarea>
        </div>

        <div class="form-group">
            <label for="status">Campaign Status:</label>
            <select id="status" name="status" required>
                <option value="active" <%= "active".equalsIgnoreCase(status) ? "selected" : "" %>>Active (Visible to Creators)</option>
                <option value="inactive" <%= "inactive".equalsIgnoreCase(status) ? "selected" : "" %>>Inactive (Draft)</option>
                <option value="completed" <%= "completed".equalsIgnoreCase(status) ? "selected" : "" %>>Completed</option>
                <%-- 'deleted' status is handled by the delete button, not usually set here --%>
            </select>
        </div>

         <div class="form-group" style="display: flex; gap: 1rem; flex-wrap:wrap;">
             <div style="flex: 1; min-width: 200px;">
                 <label for="budget">Budget (Optional):</label>
                 <input type="number" id="budget" name="budget" value="<%= (budget != null) ? budget.toPlainString() : "" %>" step="0.01" min="0" placeholder="e.g., 500.00">
             </div>
              <div style="flex: 1; min-width: 150px;">
                   <label for="startDate">Start Date (Optional):</label>
                   <input type="date" id="startDate" name="startDate" value="<%= (startDate != null) ? startDate.toString() : "" %>">
               </div>
                <div style="flex: 1; min-width: 150px;">
                     <label for="endDate">End Date (Optional):</label>
                     <input type="date" id="endDate" name="endDate" value="<%= (endDate != null) ? endDate.toString() : "" %>">
                 </div>
         </div>

         <div class="form-group">
              <label for="productImageFile">Upload Product Image (Optional - JPG, PNG, GIF, WebP - Max 10MB):</label>
              <input type="file" id="productImageFile" name="productImageFile" accept=".jpg,.jpeg,.png,.gif,.webp">
              <% if (productImagePath != null && !productImagePath.isEmpty()) { %>
                  <p style="margin-top: 0.5rem; font-size: 0.9em;">
                      Current image: <a href="<%= contextPath %>/<%= productImagePath %>" target="_blank" class="current-file-link"><%= productImagePath.substring(productImagePath.lastIndexOf('/') + 1) %></a>
                       <img src="<%= contextPath %>/<%= productImagePath %>" alt="Current Product Image" style="max-width: 100px; max-height: 100px; display: block; margin-top: 5px; border: 1px solid var(--border-color);">
                      <br><small>(Uploading a new image will replace the current one)</small>
                  </p>
              <% } %>
         </div>


        <div class="button-group">
            <button type="submit" class="btn btn-primary"><%= isEditMode ? "Save Changes" : "Create Campaign" %></button>
            <a href="<%= contextPath %>/business/campaigns" class="btn btn-secondary">Cancel</a>
        </div>

    </form>
</div>

<%@ include file="../common/footer.jsp" %>