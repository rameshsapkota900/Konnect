<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Report" %>
<%@ page import="model.User" %> <%-- For header & report details --%>
<% request.setAttribute("pageTitle", "Manage User Reports"); %>
<%@ include file="../common/header.jsp" %>

<%
    List<Report> reports = (List<Report>) request.getAttribute("reports");
%>

<h2>Manage User Reports</h2>

<%-- Error/Success messages handled by header --%>

<% if (reports == null || reports.isEmpty()) { %>
    <p class="message info-message">There are currently no user reports.</p>
<% } else { %>
    <p>Total reports found: <%= reports.size() %></p>
    <div class="table-container">
        <table class="data-table reports-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Reported On</th>
                    <th>Reporter</th>
                    <th>Reported User</th>
                    <th>Reason</th>
                    <th>Details</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% for (Report report : reports) {
                      User reporter = report.getReporterUser(); // Get attached basic info
                      User reported = report.getReportedUser();
                      String reporterEmail = (reporter != null && reporter.getEmail() != null) ? reporter.getEmail() : "(ID: " + report.getReporterUserId() + ")";
                      String reportedEmail = (reported != null && reported.getEmail() != null) ? reported.getEmail() : "(ID: " + report.getReportedUserId() + ")";
                %>
                    <tr>
                        <td><%= report.getReportId() %></td>
                        <td><%= report.getReportedAt() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(report.getReportedAt()) : "N/A" %></td>
                        <td><%= reporterEmail %></td>
                        <td>
                           <%= reportedEmail %>
                           <%-- Show if reported user is currently banned --%>
                            <% if (reported != null && reported.isBanned()) { %>
                                <br><span style="color: var(--error-color); font-size: 0.8em;">(Currently Banned)</span>
                            <% } %>
                        </td>
                        <td><%= report.getReason() != null ? report.getReason() : "-" %></td>
                        <td>
                            <% String details = report.getDetails();
                               if (details != null && !details.isEmpty()) {
                                   out.print(details.length() > 80 ? details.substring(0, 80) + "..." : details);
                                   // Optional: Button to view full details
                               } else { out.print("-"); }
                            %>
                        </td>
                        <td>
                            <% String statusClass = "";
                               String status = report.getStatus();
                               if ("pending".equalsIgnoreCase(status)) statusClass = "status-pending";
                               else if ("reviewed".equalsIgnoreCase(status)) statusClass = "status-reviewed";
                               else if ("action_taken".equalsIgnoreCase(status)) statusClass = "status-action-taken";
                               else if ("dismissed".equalsIgnoreCase(status)) statusClass = "status-dismissed";
                            %>
                            <span class="status-badge <%= statusClass %>" style="text-transform: capitalize;"><%= status %></span>
                        </td>
                        <td>
                           <%-- Actions only available if Pending or Reviewed? --%>
                            <% if ("pending".equalsIgnoreCase(status) || "reviewed".equalsIgnoreCase(status)) { %>
                               <div class="action-group">
                                   <%-- Ban User Button (if not already banned and not self) --%>
                                    <% if (reported != null && !reported.isBanned() && reported.getUserId() != loggedInUser.getUserId()) { %>
                                         <form action="<%= contextPath %>/admin/reports" method="POST" style="display:inline;">
                                             <input type="hidden" name="action" value="banUserBasedOnReport">
                                             <input type="hidden" name="reportId" value="<%= report.getReportId() %>">
                                             <button type="submit" class="btn btn-danger btn-sm btn-delete-confirm" data-confirm-message="Are you sure you want to BAN the reported user based on this report?">Ban User</button>
                                         </form>
                                     <% } %>

                                    <%-- Dismiss Button --%>
                                    <form action="<%= contextPath %>/admin/reports" method="POST" style="display:inline;">
                                        <input type="hidden" name="action" value="dismissReport">
                                        <input type="hidden" name="reportId" value="<%= report.getReportId() %>">
                                        <button type="submit" class="btn btn-secondary btn-sm">Dismiss</button>
                                    </form>

                                    <%-- Optional: Mark as Reviewed (if status is pending) --%>
                                     <% if ("pending".equalsIgnoreCase(status)) { %>
                                        <form action="<%= contextPath %>/admin/reports" method="POST" style="display:inline;">
                                             <input type="hidden" name="action" value="updateStatus">
                                             <input type="hidden" name="reportId" value="<%= report.getReportId() %>">
                                             <input type="hidden" name="newStatus" value="reviewed">
                                             <button type="submit" class="btn btn-secondary btn-sm">Mark Reviewed</button>
                                         </form>
                                     <% } %>
                               </div>
                            <% } else { %>
                                 <span>-</span> <%-- No actions if action taken or dismissed --%>
                            <% } %>
                        </td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
    <style>
        .status-badge { padding: 0.3em 0.6em; border-radius: 4px; font-size: 0.85em; font-weight: bold; }
        .status-pending { background-color: orange; color: white; }
        .status-reviewed { background-color: var(--primary-color); color: white; }
        .status-action-taken { background-color: var(--error-color); color: white; }
        .status-dismissed { background-color: var(--text-color-muted); color: var(--bg-color); }
    </style>
<% } %>

<%@ include file="../common/footer.jsp" %>