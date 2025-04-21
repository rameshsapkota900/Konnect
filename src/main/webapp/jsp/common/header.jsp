<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User" %>
<%
    // Get context path (base URL) of the application
    String contextPath = request.getContextPath();

    // Get user from session, if available
    User loggedInUser = (User) session.getAttribute("user");
    String userRole = null;
    int currentUserId = -1;
    if (loggedInUser != null) {
        userRole = loggedInUser.getRole();
        currentUserId = loggedInUser.getUserId(); // Get user ID for JS data attribute
    }

    // Determine active page for navigation highlighting (basic example)
    String requestUri = request.getRequestURI();
    String activePage = ""; // Variable to hold the active page identifier

    // Check for specific paths to determine the active page
    if (requestUri.endsWith("/dashboard")) activePage = "dashboard"; // Generic dashboard check first
    else if (requestUri.endsWith("/profile")) activePage = "profile"; // Generic profile check
    else if (requestUri.contains("/creator/campaigns")) activePage = "find_campaigns";
    else if (requestUri.contains("/creator/applications")) activePage = "my_applications";
    else if (requestUri.contains("/creator/invites")) activePage = "invites";
    else if (requestUri.contains("/business/campaigns")) activePage = "manage_campaigns";
    else if (requestUri.contains("/business/search")) activePage = "find_creators";
    else if (requestUri.contains("/chat")) activePage = "chat";
    else if (requestUri.contains("/admin/users")) activePage = "admin_users";
    else if (requestUri.contains("/admin/campaigns")) activePage = "admin_campaigns";
    else if (requestUri.contains("/admin/reports")) activePage = "admin_reports";
    else if (requestUri.endsWith("/login")) activePage = "login"; // Handle login/register/index explicitly
    else if (requestUri.endsWith("/register")) activePage = "register";
    else if (requestUri.endsWith("/")) activePage = "index"; // Root path for landing page

    // ** Removed the problematic lambda function **
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- Dynamically set page title if passed from servlet --%>
    <title>Konnect<%=(request.getAttribute("pageTitle") != null ? " - " + request.getAttribute("pageTitle") : "") %></title>
    <link rel="stylesheet" href="<%= contextPath %>/css/style.css">
    <%-- Add Favicon link here if you have one --%>
    <%-- <link rel="icon" href="<%= contextPath %>/favicon.ico" type="image/x-icon"> --%>
</head>
<%-- Add user ID and context path as data attributes for easy access in JavaScript --%>
<body class="dark-theme" data-user-id="<%= currentUserId %>" data-context-path="<%= contextPath %>">

<header class="main-header">
    <div class="container header-content">
        <%-- Logo links to root (handled by IndexServlet), which redirects to dashboard if logged in --%>
        <a href="<%= contextPath %>/" class="logo">Konnect</a>

        <nav class="main-nav">
            <ul>
                <% if (loggedInUser != null && userRole != null) { %>
                    <%-- Creator Navigation --%>
                    <% if ("creator".equalsIgnoreCase(userRole)) { %>
                        <li><a href="<%= contextPath %>/creator/dashboard" class="<%= "dashboard".equals(activePage) ? "active" : "" %>">Dashboard</a></li>
                        <li><a href="<%= contextPath %>/creator/profile" class="<%= "profile".equals(activePage) ? "active" : "" %>">Profile</a></li>
                        <li><a href="<%= contextPath %>/creator/campaigns" class="<%= "find_campaigns".equals(activePage) ? "active" : "" %>">Find Campaigns</a></li>
                        <li><a href="<%= contextPath %>/creator/applications" class="<%= "my_applications".equals(activePage) ? "active" : "" %>">My Applications</a></li>
                        <li><a href="<%= contextPath %>/creator/invites" class="<%= "invites".equals(activePage) ? "active" : "" %>">Invites</a></li>
                        <li><a href="<%= contextPath %>/chat" class="<%= "chat".equals(activePage) ? "active" : "" %>">Chat</a></li>
                    <% } %>

                    <%-- Business Navigation --%>
                    <% if ("business".equalsIgnoreCase(userRole)) { %>
                        <li><a href="<%= contextPath %>/business/dashboard" class="<%= "dashboard".equals(activePage) ? "active" : "" %>">Dashboard</a></li>
                        <li><a href="<%= contextPath %>/business/profile" class="<%= "profile".equals(activePage) ? "active" : "" %>">Profile</a></li>
                        <li><a href="<%= contextPath %>/business/campaigns" class="<%= "manage_campaigns".equals(activePage) ? "active" : "" %>">Manage Campaigns</a></li>
                        <li><a href="<%= contextPath %>/business/search" class="<%= "find_creators".equals(activePage) ? "active" : "" %>">Find Creators</a></li>
                        <li><a href="<%= contextPath %>/chat" class="<%= "chat".equals(activePage) ? "active" : "" %>">Chat</a></li>
                    <% } %>

                    <%-- Admin Navigation --%>
                     <% if ("admin".equalsIgnoreCase(userRole)) { %>
                        <li><a href="<%= contextPath %>/admin/dashboard" class="<%= "dashboard".equals(activePage) ? "active" : "" %>">Dashboard</a></li>
                        <li><a href="<%= contextPath %>/admin/users" class="<%= "admin_users".equals(activePage) ? "active" : "" %>">Users</a></li>
                        <li><a href="<%= contextPath %>/admin/campaigns" class="<%= "admin_campaigns".equals(activePage) ? "active" : "" %>">Campaigns</a></li>
                        <li><a href="<%= contextPath %>/admin/reports" class="<%= "admin_reports".equals(activePage) ? "active" : "" %>">Reports</a></li>
                     <% } %>

                    <%-- Logout Link --%>
                    <li><a href="<%= contextPath %>/logout">Logout (<%= loggedInUser.getEmail() %>)</a></li>
                <% } else { %>
                    <%-- Logged Out Navigation --%>
                    <%-- Corrected login/register active state check --%>
                    <li><a href="<%= contextPath %>/login" class="<%= "login".equals(activePage) ? "active" : "" %>">Login</a></li>
                    <li><a href="<%= contextPath %>/register" class="<%= "register".equals(activePage) ? "active" : "" %>">Register</a></li>
                <% } %>
            </ul>
        </nav>

         <div class="theme-switcher">
              <label for="themeToggleBtn">Toggle Theme</label> <%-- Label for accessibility --%>
              <button id="themeToggleBtn" title="Toggle Theme">☀️</button> <%-- Initial icon (will be updated by JS) --%>
         </div>
    </div>
</header>

<main class="main-content">
    <div class="container page-content">
         <%-- Display flash messages passed via request attributes (if any) --%>
         <% String successMessage = (String) request.getAttribute("successMessage"); %>
         <% String errorMessage = (String) request.getAttribute("errorMessage"); %>
         <% String infoMessage = (String) request.getAttribute("infoMessage"); %>

         <% if (successMessage != null && !successMessage.isEmpty()) { %>
             <div class="message success-message"><%= successMessage %></div>
         <% } %>
         <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
             <div class="message error-message"><%= errorMessage %></div>
         <% } %>
         <% if (infoMessage != null && !infoMessage.isEmpty()) { %>
              <div class="message info-message"><%= infoMessage %></div>
          <% } %>

        <%-- Page specific content will be included below this line in other JSPs --%>