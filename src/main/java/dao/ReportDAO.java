package dao;

import model.Report;
import model.User; // To link report details to users
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    /**
     * Creates a new report against a user.
     * @param report Report object containing reporterId, reportedId, reason, and details.
     * @return true if the report was created successfully, false otherwise.
     */
    public boolean createReport(Report report) {
        String sql = "INSERT INTO reports (reporter_user_id, reported_user_id, reason, details, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        if (report == null || report.getReporterUserId() <= 0 || report.getReportedUserId() <= 0 || report.getReason() == null || report.getReason().trim().isEmpty()) {
             System.err.println("Create report failed: Invalid report object or missing required fields.");
             return false;
        }
         // Prevent self-reporting?
         if (report.getReporterUserId() == report.getReportedUserId()) {
              System.err.println("Create report failed: User cannot report themselves (User ID: " + report.getReporterUserId() + ").");
              return false;
         }

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, report.getReporterUserId());
            pstmt.setInt(2, report.getReportedUserId());
            pstmt.setString(3, report.getReason());
            pstmt.setString(4, report.getDetails()); // Can be null or empty
            pstmt.setString(5, Report.Status.PENDING.getStatusName()); // Initial status

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Report created successfully: ReporterID=" + report.getReporterUserId() + ", ReportedID=" + report.getReportedUserId());
            } else {
                 System.err.println("Report creation failed: No rows affected.");
            }
        } catch (SQLException e) {
             // Check for FK violations
             if (e.getSQLState().startsWith("23")) {
                 System.err.println("Create report failed: Reporter or Reported User ID does not exist.");
             } else {
                 System.err.println("SQL Error creating report: " + e.getMessage());
             }
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

    /**
     * Retrieves a specific report by its ID.
     * Optionally joins with users table to get reporter/reported emails.
     * @param reportId The ID of the report.
     * @param includeUserDetails If true, fetches associated user emails.
     * @return Report object if found, null otherwise.
     */
    public Report getReportById(int reportId, boolean includeUserDetails) {
        String baseSql = "SELECT r.*";
        String joinSql = "";
        String fromSql = " FROM reports r";

         if (includeUserDetails) {
             baseSql += ", reporter.email as reporter_email, reported.email as reported_email";
             joinSql = " LEFT JOIN users reporter ON r.reporter_user_id = reporter.user_id" +
                       " LEFT JOIN users reported ON r.reported_user_id = reported.user_id";
         }

        String sql = baseSql + fromSql + joinSql + " WHERE r.report_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Report report = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reportId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                report = mapResultSetToReport(rs);
                if (includeUserDetails && report != null) {
                    User reporter = new User();
                    reporter.setUserId(report.getReporterUserId());
                    reporter.setEmail(rs.getString("reporter_email"));
                    report.setReporterUser(reporter);

                    User reported = new User();
                    reported.setUserId(report.getReportedUserId());
                    reported.setEmail(rs.getString("reported_email"));
                    report.setReportedUser(reported);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting report by ID " + reportId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return report;
    }

    /**
     * Retrieves all reports, ordered by reported date (newest first).
     * Includes reporter and reported user emails for admin view.
     * @return List of Report objects with associated User emails.
     */
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT r.*, reporter.email as reporter_email, reported.email as reported_email " +
                     "FROM reports r " +
                     "LEFT JOIN users reporter ON r.reporter_user_id = reporter.user_id " +
                     "LEFT JOIN users reported ON r.reported_user_id = reported.user_id " +
                     "ORDER BY r.reported_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                 Report report = mapResultSetToReport(rs);

                 User reporter = new User();
                 reporter.setUserId(report.getReporterUserId());
                 reporter.setEmail(rs.getString("reporter_email"));
                 report.setReporterUser(reporter);

                 User reported = new User();
                 reported.setUserId(report.getReportedUserId());
                 reported.setEmail(rs.getString("reported_email"));
                 report.setReportedUser(reported);

                 reports.add(report);
            }
            System.out.println("Retrieved " + reports.size() + " reports for admin view.");
        } catch (SQLException e) {
            System.err.println("SQL Error getting all reports: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return reports;
    }

    /**
     * Updates the status of a report and sets the reviewed timestamp.
     * Typically done by an admin.
     * @param reportId The ID of the report to update.
     * @param newStatus The new status string (use Report.Status enum constants).
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateReportStatus(int reportId, String newStatus) {
        // Validate status
         if (!isValidStatus(newStatus)) {
             System.err.println("Update report status failed: Invalid status value '" + newStatus + "' for report ID " + reportId);
             return false;
         }

        String sql = "UPDATE reports SET status = ?, reviewed_at = CURRENT_TIMESTAMP WHERE report_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, reportId);

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Report status updated successfully for ID: " + reportId + " to " + newStatus);
            } else {
                 System.err.println("Failed to update report status for ID: " + reportId + ". Report not found.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error updating report status for ID " + reportId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }


    /**
     * Deletes a report. Use with caution. Maybe only allow for 'dismissed' reports?
     * @param reportId The ID of the report to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteReport(int reportId) {
        String sql = "DELETE FROM reports WHERE report_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reportId);

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
             if (success) {
                 System.out.println("Report deleted successfully: ID=" + reportId);
             } else {
                  System.err.println("Failed to delete report ID: " + reportId + ". Report not found.");
             }
        } catch (SQLException e) {
            System.err.println("SQL Error deleting report ID " + reportId + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }


    /**
     * Helper method to map a ResultSet row to a Report object.
     * Does not map associated User objects by default.
     * @param rs The ResultSet containing report data.
     * @return A populated Report object.
     * @throws SQLException If a database access error occurs.
     */
    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getInt("report_id"));
        report.setReporterUserId(rs.getInt("reporter_user_id"));
        report.setReportedUserId(rs.getInt("reported_user_id"));
        report.setReason(rs.getString("reason"));
        report.setDetails(rs.getString("details"));
        report.setStatus(rs.getString("status"));
        report.setReportedAt(rs.getTimestamp("reported_at"));
        report.setReviewedAt(rs.getTimestamp("reviewed_at")); // Can be null
        return report;
    }

     /**
      * Helper to validate status strings against the enum.
      * @param status The status string to check.
      * @return true if the status is valid, false otherwise.
      */
     private boolean isValidStatus(String status) {
         if (status == null) return false;
         for (Report.Status s : Report.Status.values()) {
             if (s.getStatusName().equalsIgnoreCase(status)) {
                 return true;
             }
         }
         return false;
     }
}