<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.Creator" %>
<%@ page import="model.User" %> <%-- Needed for header --%>
<% request.setAttribute("pageTitle", "My Profile"); %>
<%@ include file="../common/header.jsp" %>

<%
    Creator creatorProfile = (Creator) request.getAttribute("creatorProfile");
    // Check if this is a newly created profile (for messaging)
    boolean isNewProfile = request.getAttribute("isNewProfile") != null && (Boolean)request.getAttribute("isNewProfile");

    // Defaults for form fields if profile is null or fields are null
    String displayName = (creatorProfile != null && creatorProfile.getDisplayName() != null) ? creatorProfile.getDisplayName() : "";
    String bio = (creatorProfile != null && creatorProfile.getBio() != null) ? creatorProfile.getBio() : "";
    String niche = (creatorProfile != null && creatorProfile.getNiche() != null) ? creatorProfile.getNiche() : "";
    int followerCount = (creatorProfile != null) ? creatorProfile.getFollowerCount() : 0;
    String pricingInfo = (creatorProfile != null && creatorProfile.getPricingInfo() != null) ? creatorProfile.getPricingInfo() : "";
    String mediaKitPath = (creatorProfile != null && creatorProfile.getMediaKitPath() != null) ? creatorProfile.getMediaKitPath() : null;
    String socialLinksJson = (creatorProfile != null && creatorProfile.getSocialMediaLinks() != null) ? creatorProfile.getSocialMediaLinks() : "{}";

    // Basic parsing of JSON for display (Replace with Gson if used)
    String instagram = ""; String youtube = ""; String tiktok = "";
    try {
        if (socialLinksJson.contains("\"instagram\":")) {
            instagram = socialLinksJson.split("\"instagram\":\"")[1].split("\"")[0];
        }
         if (socialLinksJson.contains("\"youtube\":")) {
            youtube = socialLinksJson.split("\"youtube\":\"")[1].split("\"")[0];
        }
         if (socialLinksJson.contains("\"tiktok\":")) {
            tiktok = socialLinksJson.split("\"tiktok\":\"")[1].split("\"")[0];
        }
    } catch (Exception e) { System.err.println("Simple JSON parse error in JSP: " + e.getMessage()); /* Ignore error, keep defaults */ }

%>

<div class="form-container profile-form-container">
    <h2>My Creator Profile</h2>

    <% if (isNewProfile) { %>
        <p class="message info-message">Welcome! Please complete your profile details below.</p>
    <% } %>

    <%-- Error/Success messages handled by header.jsp --%>

    <%-- Profile Edit Form --%>
    <form action="<%= contextPath %>/creator/profile" method="POST" enctype="multipart/form-data">
        <%-- We don't need an action parameter here as POST always means update --%>

        <div class="form-group">
            <label for="displayName">Display Name (Public):</label>
            <input type="text" id="displayName" name="displayName" value="<%= displayName %>" required maxlength="100">
        </div>

        <div class="form-group">
            <label for="bio">Bio / About Me:</label>
            <textarea id="bio" name="bio" rows="5" placeholder="Tell businesses about yourself and your content..." maxlength="1000"><%= bio %></textarea> <%-- Added placeholder & maxlength --%>
        </div>

        <div class="form-group">
            <label for="niche">Primary Niche:</label>
            <input type="text" id="niche" name="niche" value="<%= niche %>" placeholder="e.g., Beauty, Gaming, Tech, Fitness" maxlength="100">
        </div>

        <div class="form-group">
            <label for="followerCount">Total Follower Count (Approx.):</label>
            <input type="number" id="followerCount" name="followerCount" value="<%= followerCount %>" min="0" placeholder="Across all major platforms">
        </div>

         <div class="form-group">
            <label for="pricingInfo">Pricing Info / Rate Card Summary:</label>
            <textarea id="pricingInfo" name="pricingInfo" rows="3" placeholder="e.g., Starting at $100/post, $500/video. Packages available." maxlength="500"><%= pricingInfo %></textarea>
        </div>

        <fieldset class="form-group">
             <legend>Social Media Links:</legend>
             <div class="form-group">
                  <label for="social_instagram" style="font-weight:normal;font-size:0.9em;color:var(--text-color-muted)">Instagram URL:</label>
                  <input type="url" id="social_instagram" name="social_instagram" value="<%= instagram %>" placeholder="https://instagram.com/yourprofile" maxlength="255">
             </div>
              <div class="form-group">
                   <label for="social_youtube" style="font-weight:normal;font-size:0.9em;color:var(--text-color-muted)">YouTube Channel URL:</label>
                   <input type="url" id="social_youtube" name="social_youtube" value="<%= youtube %>" placeholder="https://youtube.com/c/yourchannel" maxlength="255">
              </div>
               <div class="form-group">
                    <label for="social_tiktok" style="font-weight:normal;font-size:0.9em;color:var(--text-color-muted)">TikTok Profile URL:</label>
                    <input type="url" id="social_tiktok" name="social_tiktok" value="<%= tiktok %>" placeholder="https://tiktok.com/@yourprofile" maxlength="255">
               </div>
        </fieldset>

        <div class="form-group">
             <label for="mediaKitFile">Upload Media Kit (PDF, JPG, PNG - Max 10MB):</label>
             <input type="file" id="mediaKitFile" name="mediaKitFile" accept=".pdf,.jpg,.jpeg,.png">
             <% if (mediaKitPath != null && !mediaKitPath.isEmpty()) { %>
                 <p style="margin-top: 0.5rem; font-size: 0.9em;">
                     Current file: <a href="<%= contextPath %>/<%= mediaKitPath %>" target="_blank" class="current-file-link"><%= mediaKitPath.substring(mediaKitPath.lastIndexOf('/') + 1) %></a>
                     <br><small>(Uploading a new file will replace the current one)</small>
                 </p>
             <% } %>
        </div>


        <div class="form-group">
            <button type="submit" class="btn btn-primary">Save Profile Changes</button>
        </div>

    </form>

</div>

<%@ include file="../common/footer.jsp" %>