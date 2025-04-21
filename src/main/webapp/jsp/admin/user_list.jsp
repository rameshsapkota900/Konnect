<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.User" %> <%-- For header and user list --%>
<% request.setAttribute("pageTitle", "User Management"); %>
<%@ include file="../common/header.jsp" %>

<%
    List<User> users = (List<User>) request.getAttribute("users");
%>

<h2>User Management</h2>

<%-- Error/Success messages handled by header --%>

<% if (users == null || users.isEmpty()) { %>
    <p class="message info-message">No users found on the platform.</p>
<% } else { %>
    <p>Total users found: <%= users.size() %></p>
    <div class="table-container">
        <table class="data-table users-table">
            <thead>
                <tr>
                    <th>User ID</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Registered On</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% for (User user : users) { %>
                    <tr>
                        <td><%= user.getUserId() %></td>
                        <td><%= user.getEmail() %></td>
                        <td style="text-transform: capitalize;"><%= user.getRole() %></td>
                        <td><%= user.getCreatedAt() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(user.getCreatedAt()) : "N/A" %></td>
                        <td>
                           <% if (user.isBanned()) { %>
                               <span class="status-badge status-banned" style="background-color: var(--error-color); color: white;">Banned</span>
                           <% } else { %>
                               <span class="status-badge status-active" style="background-color: var(--success-color); color: white;">Active</span>
                           <% } %>
                        </td>
                        <td>
                            <%-- Prevent admin from banning/unbanning themselves --%>
                            <% if (user.getUserId() == loggedInUser.getUserId()) { %>
                                <span>(Your Account)</span>
                            <% } else { %>
                               <div class="action-group">
                                   <% if (user.isBanned()) { %>
                                       <%-- Unban Action --%>
                                       <form action="<%= contextPath %>/admin/users" method="POST" style="display: inline;">
                                           <input type="hidden" name="action" value="unban">
                                           <input type="hidden" name="userId" value="<%= user.getUserId() %>">
                                           <button type="submit" class="btn btn-success btn-sm">Unban</button>
                                       </form>
                                   <% } else { %>
                                       <%-- Ban Action --%>
                                       <form action="<%= contextPath %>/admin/users" method="POST" style="display: inline;">
                                           <input type="hidden" name="action" value="ban">
                                           <input type="hidden" name="userId" value="<%= user.getUserId() %>">
                                           <button type="submit" class="btn btn-danger btn-sm btn-delete-confirm" data-confirm-message="Are you sure you want to ban this user? They will not be able to log in.">Ban</button>
                                       </form>
                                   <% } %>
                                    <%-- Optional: Add link to view profile details (if implemented) --%>
                                    <%-- <a href="#" class="btn btn-secondary btn-sm">View Details</a> --%>
                                    <%-- Optional: Add hard delete user (use with extreme caution!) --%>
                                    <%-- <form action="<%= contextPath %>/admin/users" method="POST" style="display: inline;">
                                           <input type="hidden" name="action" value="delete">
                                           <input type="hidden" name="userId" value="<%= user.getUserId() %>">
                                           <button type="submit" class="btn btn-danger btn-sm btn-delete-confirm" data-confirm-message="DANGER! Are you absolutely sure you want to permanently delete this user and all their data? This CANNOT be undone!">Delete Permanently</button>
                                       </form> --%>
                               </div>
                            <% } %>
                        </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
     <style>
            .status-badge { padding: 0.3em 0.6em; border-radius: 4px; font-size: 0.85em; font-weight: bold; }
     </style>
<% } %>

<%@ include file="../common/footer.jsp" %>