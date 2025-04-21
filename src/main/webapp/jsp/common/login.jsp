<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Login"); %> <%-- Set page title --%>
<%@ include file="header.jsp" %> <%-- Include Header --%>

<div class="form-container auth-form-container"> <%-- Use generic form-container --%>
    <h2>Login to Konnect</h2>

    <%-- Display Error/Success Messages passed from Servlet or via query params --%>
    <%
        // Check request attributes first
        String reqErrorMessage = (String) request.getAttribute("errorMessage");
        String reqSuccessMessage = (String) request.getAttribute("successMessage");

        // Check query parameters (e.g., after registration redirect)
        String paramRegSuccess = request.getParameter("registration");
        String paramLogoutSuccess = request.getParameter("logout");
        String paramError = request.getParameter("error");
        String paramEmail = request.getParameter("email"); // For pre-filling email after registration

        String displayError = reqErrorMessage;
        String displaySuccess = reqSuccessMessage;

        if (displaySuccess == null && "success".equals(paramRegSuccess)) {
            displaySuccess = "Registration successful! Please login.";
        }
         if (displaySuccess == null && "success".equals(paramLogoutSuccess)) {
             displaySuccess = "You have been logged out successfully.";
         }

        if (displayError == null) {
            if ("auth".equals(paramError)) displayError = "Please login to access that page.";
            else if ("banned".equals(paramError)) displayError = "Your account is banned. Please contact support.";
            else if ("invalid".equals(paramError)) displayError = "Invalid email or password."; // Generic login fail
        }

         // Pre-fill email if passed from registration or failed login attempt
         String prefillEmail = (String) request.getAttribute("email"); // From failed attempt
         if (prefillEmail == null && paramEmail != null) {
             prefillEmail = paramEmail; // From registration redirect
         }
         if (prefillEmail == null) prefillEmail = ""; // Ensure not null

    %>

    <% if (displaySuccess != null) { %>
        <p class="message success-message"><%= displaySuccess %></p>
    <% } %>
    <% if (displayError != null) { %>
        <p class="message error-message"><%= displayError %></p>
    <% } %>


    <form action="<%= contextPath %>/login" method="POST" class="auth-form">
        <div class="form-group">
            <label for="email">Email Address:</label>
            <input type="email" id="email" name="email" required value="<%= prefillEmail %>" autofocus> <%-- Added autofocus --%>
        </div>
        <div class="form-group">
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>
        </div>
        <div class="form-group">
            <button type="submit" class="btn btn-primary">Login</button>
        </div>
    </form>
    <p style="text-align: center; margin-top: 1.5rem;">
        Don't have an account? <a href="<%= contextPath %>/register">Register here</a>
    </p>
</div>

<%@ include file="footer.jsp" %> <%-- Include Footer --%>