<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<% request.setAttribute("pageTitle", "Error"); %>
<%@ include file="header.jsp" %>

<div class="container error-page" style="text-align: center; padding: 3rem 1rem;">
    <h2>Oops! Something Went Wrong</h2>

    <% // --- Start of SINGLE Scriptlet Block ---

        // Get custom error message from servlet attribute first
        String displayErrorMessage = (String) request.getAttribute("errorMessage");

        // Standard servlet error attributes
        // RENAME the local variable to avoid conflict with JSP implicit 'exception' object
        Throwable errorException = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String servletName = (String) request.getAttribute("jakarta.servlet.error.servlet_name");
        String requestUriWithError = (String) request.getAttribute("jakarta.servlet.error.request_uri");


        // Determine the final error message if no custom one was provided
        if (displayErrorMessage == null || displayErrorMessage.trim().isEmpty()) {
            if (statusCode != null) {
                switch (statusCode) {
                    case 400: displayErrorMessage = "Bad Request. The server could not understand the request."; break;
                    case 401: displayErrorMessage = "Unauthorized. Please login."; break;
                    case 403: displayErrorMessage = "Forbidden. You do not have permission to access this page."; break;
                    case 404: displayErrorMessage = "Not Found. The page you requested could not be found."; break;
                    case 500: displayErrorMessage = "Internal Server Error. Please try again later."; break;
                    case 503: displayErrorMessage = "Service Unavailable. The server is temporarily unable to handle the request."; break;
                    default: displayErrorMessage = "An error occurred (Status Code: " + statusCode + ")."; break;
                }
            } else if (errorException != null) { // Use the renamed variable
                // Use exception message if available, otherwise a generic message
                 displayErrorMessage = "An application error occurred" + (errorException.getMessage() != null ? ": " + errorException.getMessage() : ".");
            } else {
                 displayErrorMessage = "An unknown error occurred."; // Default fallback
            }
        }

        // Prepare details for potential debug output
        String debugInfo = "";
        boolean showDebugInfo = false; // CHANGE TO true ONLY FOR LOCAL DEVELOPMENT

        if (showDebugInfo) {
            StringBuilder debugBuilder = new StringBuilder();
            debugBuilder.append("<h4>Debug Information:</h4><pre>");
             if (statusCode != null) debugBuilder.append("Status Code: ").append(statusCode).append("\n");
             if (servletName != null) debugBuilder.append("Servlet Name: ").append(servletName).append("\n");
             if (requestUriWithError != null) debugBuilder.append("Request URI: ").append(requestUriWithError).append("\n");

            if (errorException != null) { // Use the renamed variable
                 debugBuilder.append("\nException Type: ").append(errorException.getClass().getName()).append("\n");
                 debugBuilder.append("Exception Message: ").append(errorException.getMessage()).append("\n\n");
                 // Use a StringWriter to capture the stack trace
                 java.io.StringWriter sw = new java.io.StringWriter();
                 java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                 errorException.printStackTrace(pw); // Use the renamed variable
                 debugBuilder.append("Stack Trace:\n").append(sw.toString());
                 pw.close();
                 sw.close();
            } else {
                 debugBuilder.append("No exception object available from request attributes.");
                 // You could potentially check the implicit 'exception' object here if needed,
                 // but fetching from request attributes is the standard Servlet way.
            }
            debugBuilder.append("</pre>");
            debugInfo = debugBuilder.toString();
        }

    %> <%-- --- End of SINGLE Scriptlet Block --- %>

    <%-- Display the determined error message --%>
    <p class="message error-message" style="font-size: 1.1rem;">
        <%= displayErrorMessage %>
    </p>

    <p>We apologize for the inconvenience. You can try:</p>
    <ul>
        <%-- Use contextPath defined in header.jsp --%>
        <li style="margin-bottom: 0.5rem;"><a href="<%= contextPath %>/login">Returning to the Login Page</a></li>
        <%-- Use loggedInUser defined in header.jsp --%>
        <% if(loggedInUser != null && loggedInUser.getRole() != null) { %>
             <li style="margin-bottom: 0.5rem;"><a href="<%= contextPath %>/<%= loggedInUser.getRole().toLowerCase() %>/dashboard">Going to your Dashboard</a></li>
        <% } %>
        <li style="margin-bottom: 0.5rem;">Checking the URL for typos</li>
         <li style="margin-bottom: 0.5rem;">Trying again later</li>
    </ul>

    <%-- Display debug information if enabled --%>
    <% if (showDebugInfo) { %>
            <div style="margin-top: 2rem; text-align: left; background: var(--card-bg); padding: 1rem; border-radius: 5px; font-family: monospace; font-size: 0.8rem; max-height: 300px; overflow: auto; border: 1px solid var(--border-color);">
                <%-- Output the prepared debug string --%>
                <%= debugInfo %>
           </div>
     <% } %>

</div>

<%@ include file="footer.jsp" %>