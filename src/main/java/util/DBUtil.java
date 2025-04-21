package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // Import PreparedStatement
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Keep Statement for general use if needed

public class DBUtil {
    // --- IMPORTANT: Replace with your actual DB credentials ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/konnect_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; 
    // --- ---

    static {
        try {
            // Explicitly load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("FATAL ERROR: MySQL JDBC Driver not found. Make sure the JAR is in WEB-INF/lib.");
            // Throwing a runtime exception is appropriate here as the app can't function without the driver.
            throw new RuntimeException("Failed to load database driver.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
         // System.out.println("Attempting to get database connection..."); // Debugging
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        // System.out.println("Database connection established successfully."); // Debugging
        return connection;
    }

    // Gracefully close ResultSet
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error closing ResultSet: " + e.getMessage());
                // Log this error properly in a real application
            }
        }
    }

    // Gracefully close Statement (covers PreparedStatement as well)
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing Statement: " + e.getMessage());
                // Log this error properly
            }
        }
    }

    // Gracefully close Connection
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    // System.out.println("Database connection closed."); // Debugging
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
                // Log this error properly
            }
        }
    }

    // Overloaded close method for convenience
    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }

     // Overloaded close method for operations without ResultSet (INSERT, UPDATE, DELETE)
    public static void closeResources(Statement stmt, Connection conn) {
        closeStatement(stmt);
        closeConnection(conn);
    }
}