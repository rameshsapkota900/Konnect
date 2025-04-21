<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Register"); %>
<%@ include file="header.jsp" %>

<%
    // Retrieve values passed back from Servlet on validation error
    String email = request.getAttribute("email") != null ? (String)request.getAttribute("email") : "";
    String displayName = request.getAttribute("displayName") != null ? (String)request.getAttribute("displayName") : "";
    String companyName = request.getAttribute("companyName") != null ? (String)request.getAttribute("companyName") : "";
    String selectedRole = request.getAttribute("selectedRole") != null ? (String)request.getAttribute("selectedRole") : "creator"; // Default selection
%>

<div class="form-container registration-form-container">
    <h2>Create Your Konnect Account</h2>

    <%-- Error message display included in header.jsp --%>

    <form action="<%= contextPath %>/register" method="POST" class="auth-form">

        <div class="form-group">
            <label for="email">Email Address:</label>
            <input type="email" id="email" name="email" required value="<%= email %>" maxlength="255">
        </div>

        <div class="form-group">
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required minlength="8">
            <small style="display: block; margin-top: 4px; color: var(--text-color-muted);">Minimum 8 characters.</small>
        </div>

        <div class="form-group">
            <label for="confirmPassword">Confirm Password:</label>
            <input type="password" id="confirmPassword" name="confirmPassword" required minlength="8">
        </div>

        <fieldset class="form-group">
            <legend>Choose your role:</legend>
            <div class="role-selection">
                <label>
                    <input type="radio" name="role" value="creator" <%= "creator".equals(selectedRole) ? "checked" : "" %> >
                    <span>I am a Content Creator</span>
                </label>
                <label>
                    <input type="radio" name="role" value="business" <%= "business".equals(selectedRole) ? "checked" : "" %> >
                    <span>I represent a Business</span>
                </label>
            </div>
        </fieldset>

        <%-- Conditional Fields (Initially hidden by JS) --%>
        <div id="creatorFields" class="form-group">
            <label for="displayName">Display Name (Public):</label>
            <input type="text" id="displayName" name="displayName" value="<%= displayName %>" maxlength="100">
        </div>

        <div id="businessFields" class="form-group">
            <label for="companyName">Company Name:</label>
            <input type="text" id="companyName" name="companyName" value="<%= companyName %>" maxlength="255">
        </div>
        <%-- End Conditional Fields --%>


        <div class="form-group">
            <button type="submit" class="btn btn-primary">Register</button>
        </div>
    </form>

     <p style="text-align: center; margin-top: 1.5rem;">
         Already have an account? <a href="<%= contextPath %>/login">Login here</a>
     </p>
</div>

<%@ include file="footer.jsp" %>