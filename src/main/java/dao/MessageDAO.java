package dao;

import model.Message;
import model.User; // To potentially join user info
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    /**
     * Saves a new message to the database.
     * @param message Message object containing senderId, receiverId, and content.
     * @return true if the message was saved successfully, false otherwise.
     */
    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO messages (sender_user_id, receiver_user_id, content, is_read) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        if (message == null || message.getSenderUserId() <= 0 || message.getReceiverUserId() <= 0 || message.getContent() == null || message.getContent().trim().isEmpty()) {
            System.err.println("Send message failed: Invalid message object or empty content.");
            return false;
        }
         // Prevent users from messaging themselves?
         if (message.getSenderUserId() == message.getReceiverUserId()) {
             System.err.println("Send message failed: Sender and receiver cannot be the same (User ID: " + message.getSenderUserId() + ").");
             return false;
         }

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, message.getSenderUserId());
            pstmt.setInt(2, message.getReceiverUserId());
            pstmt.setString(3, message.getContent());
            pstmt.setBoolean(4, false); // New messages are initially unread

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (success) {
                System.out.println("Message sent successfully from User " + message.getSenderUserId() + " to User " + message.getReceiverUserId());
            } else {
                 System.err.println("Message sending failed: No rows affected.");
            }
        } catch (SQLException e) {
             // Check for FK violations if user IDs don't exist
             if (e.getSQLState().startsWith("23")) {
                 System.err.println("Send message failed: Sender or Receiver User ID does not exist.");
             } else {
                 System.err.println("SQL Error sending message: " + e.getMessage());
             }
            e.printStackTrace();
            success = false;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return success;
    }

    /**
     * Retrieves the conversation history between two users.
     * Fetches messages where either user is the sender and the other is the receiver.
     * Orders messages chronologically.
     * @param userId1 ID of the first user.
     * @param userId2 ID of the second user.
     * @param limit Max number of messages to retrieve (for pagination or initial load).
     * @param offset Starting message index (for pagination).
     * @return List of Message objects representing the conversation.
     */
    public List<Message> getConversation(int userId1, int userId2, int limit, int offset) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages " +
                     "WHERE (sender_user_id = ? AND receiver_user_id = ?) OR (sender_user_id = ? AND receiver_user_id = ?) " +
                     "ORDER BY sent_at DESC " + // Fetch latest first for typical chat display (reversed in JS)
                     "LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId1);
            pstmt.setInt(2, userId2);
            pstmt.setInt(3, userId2); // Reversed pair
            pstmt.setInt(4, userId1);
            pstmt.setInt(5, limit);
            pstmt.setInt(6, offset);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
            System.out.println("Retrieved " + messages.size() + " messages for conversation between User " + userId1 + " and User " + userId2);
        } catch (SQLException e) {
            System.err.println("SQL Error getting conversation between User " + userId1 + " and User " + userId2 + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return messages; // Note: This list is typically reversed again in the UI to show oldest first
    }


    /**
     * Retrieves the most recent message for each conversation the user is part of.
     * Used for the chat user list sidebar.
     * This query is more complex, involving grouping and finding the max timestamp per conversation pair.
     * @param userId The ID of the current user.
     * @return A List of the latest Message object for each distinct conversation partner.
     */
    public List<Message> getLatestMessagesPerConversation(int userId) {
         List<Message> latestMessages = new ArrayList<>();
         // This SQL finds the latest message_id for each conversation pair involving the user,
         // then joins back to messages to get the full message details.
         // It handles conversations where the user is either sender or receiver.
         String sql =
             "SELECT m1.*, " +
             "       CASE WHEN m1.sender_user_id = ? THEN r_user.email ELSE s_user.email END as partner_email, " + // Get partner email
             "       CASE WHEN m1.sender_user_id = ? THEN r_cr.display_name ELSE s_cr.display_name END as partner_creator_name, " + // Get partner creator name
             "       CASE WHEN m1.sender_user_id = ? THEN r_biz.company_name ELSE s_biz.company_name END as partner_business_name, " + // Get partner business name
             "       CASE WHEN m1.sender_user_id = ? THEN m1.receiver_user_id ELSE m1.sender_user_id END as partner_user_id " + // Determine partner ID
             "FROM messages m1 " +
             "INNER JOIN (" +
             "    SELECT " +
             "        LEAST(sender_user_id, receiver_user_id) as user_a, " + // Normalize conversation pair
             "        GREATEST(sender_user_id, receiver_user_id) as user_b, " +
             "        MAX(message_id) as max_message_id " + // Get the ID of the latest message in the pair
             "    FROM messages " +
             "    WHERE sender_user_id = ? OR receiver_user_id = ? " + // Where the current user is involved
             "    GROUP BY user_a, user_b" + // Group by the normalized conversation pair
             ") m2 ON m1.message_id = m2.max_message_id " + // Join back to get the actual latest message row
             // Join to get partner details (handle potential nulls if partner is not creator/business)
             "LEFT JOIN users s_user ON m1.sender_user_id = s_user.user_id " +
             "LEFT JOIN users r_user ON m1.receiver_user_id = r_user.user_id " +
             "LEFT JOIN creators s_cr ON m1.sender_user_id = s_cr.user_id " +
             "LEFT JOIN creators r_cr ON m1.receiver_user_id = r_cr.user_id " +
             "LEFT JOIN businesses s_biz ON m1.sender_user_id = s_biz.user_id " +
             "LEFT JOIN businesses r_biz ON m1.receiver_user_id = r_biz.user_id " +
             "ORDER BY m1.sent_at DESC"; // Order conversations by the latest message timestamp

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId); // For partner_email CASE
            pstmt.setInt(2, userId); // For partner_creator_name CASE
            pstmt.setInt(3, userId); // For partner_business_name CASE
            pstmt.setInt(4, userId); // For partner_user_id CASE
            pstmt.setInt(5, userId); // For WHERE clause
            pstmt.setInt(6, userId); // For WHERE clause

            rs = pstmt.executeQuery();
            while (rs.next()) {
                Message message = mapResultSetToMessage(rs);

                // Create a minimal partner User object to attach display info
                User partner = new User();
                partner.setUserId(rs.getInt("partner_user_id"));
                partner.setEmail(rs.getString("partner_email"));
                 // Determine partner's role/name (this logic could be refined)
                 String creatorName = rs.getString("partner_creator_name");
                 String businessName = rs.getString("partner_business_name");
                 // You might need more sophisticated logic here to definitively determine role and display name
                 // For simplicity, store these potential names in the message object temporarily or extend User model
                 // Here, we'll just add them as temporary fields to the message for the DAO result
                 message.setSender(partner); // Using sender field to hold the 'partner' for simplicity here
                 message.setContent(message.getContent() + "||" + creatorName + "||" + businessName); // Hacky way to pass names

                latestMessages.add(message);
            }
            System.out.println("Retrieved " + latestMessages.size() + " latest messages per conversation for User " + userId);
        } catch (SQLException e) {
            System.err.println("SQL Error getting latest messages per conversation for User " + userId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return latestMessages;
    }


    /**
     * Marks messages as read for a specific receiver from a specific sender.
     * @param receiverUserId The user whose messages should be marked as read.
     * @param senderUserId The user who sent the messages.
     * @return The number of messages marked as read, or -1 on error.
     */
    public int markMessagesAsRead(int receiverUserId, int senderUserId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE receiver_user_id = ? AND sender_user_id = ? AND is_read = FALSE";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int rowsAffected = -1;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, receiverUserId);
            pstmt.setInt(2, senderUserId);

            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected >= 0) {
                System.out.println("Marked " + rowsAffected + " messages as read for Receiver " + receiverUserId + " from Sender " + senderUserId);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error marking messages as read for Receiver " + receiverUserId + " from Sender " + senderUserId + ": " + e.getMessage());
            e.printStackTrace();
            rowsAffected = -1;
        } finally {
            DBUtil.closeResources(pstmt, conn);
        }
        return rowsAffected;
    }

    /**
     * Counts the number of unread messages for a specific user.
     * Can be used for notification indicators.
     * @param userId The ID of the user.
     * @return The count of unread messages, or -1 on error.
     */
    public int countUnreadMessages(int userId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE receiver_user_id = ? AND is_read = FALSE";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = -1;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error counting unread messages for User " + userId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(rs, pstmt, conn);
        }
        return count;
    }


    /**
     * Helper method to map a ResultSet row to a Message object.
     * @param rs The ResultSet containing message data.
     * @return A populated Message object.
     * @throws SQLException If a database access error occurs.
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setMessageId(rs.getInt("message_id"));
        msg.setSenderUserId(rs.getInt("sender_user_id"));
        msg.setReceiverUserId(rs.getInt("receiver_user_id"));
        msg.setContent(rs.getString("content"));
        msg.setSentAt(rs.getTimestamp("sent_at"));
        msg.setRead(rs.getBoolean("is_read"));
        return msg;
    }
}