package controller.shared;

import dao.MessageDAO;
import dao.UserDAO; // To get user list for chat sidebar
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Message;
import model.User;
import model.Creator; // For display name
import model.Business; // For company name
import dao.CreatorDAO;
import dao.BusinessDAO;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Using a JSON library like Gson would simplify JSON handling significantly.
// Since it's disallowed, we'll build JSON strings manually.
// Add `import com.google.gson.Gson;` if you decide to use it later.


@WebServlet("/chat")
public class MessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MessageDAO messageDAO;
    private UserDAO userDAO;
     private CreatorDAO creatorDAO;
     private BusinessDAO businessDAO;
     // private Gson gson = new Gson(); // Uncomment if using Gson library

    public void init() {
        messageDAO = new MessageDAO();
        userDAO = new UserDAO();
         creatorDAO = new CreatorDAO();
         businessDAO = new BusinessDAO();
        System.out.println("MessageServlet Initialized");
    }

    // doGet handles loading the chat page, fetching users, and fetching messages via AJAX
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
         if (loggedInUser.isBanned()) {
             session.invalidate();
             response.sendRedirect(request.getContextPath() + "/login?error=banned");
             return;
         }
        // --- End Authorization Check ---

        String action = request.getParameter("action");

        try {
            if ("getUsers".equals(action)) {
                // AJAX Call: Get list of users for chat sidebar
                handleGetUsers(request, response, loggedInUser);
            } else if ("getMessages".equals(action)) {
                // AJAX Call: Get messages for a specific conversation
                handleGetMessages(request, response, loggedInUser);
            } else {
                // Initial Page Load: Show the main chat interface JSP
                System.out.println("Loading chat page for User ID: " + loggedInUser.getUserId());
                 // Fetch initial data if needed (e.g., latest conversations for sidebar)
                 // List<Message> latestConversations = messageDAO.getLatestMessagesPerConversation(loggedInUser.getUserId());
                 // request.setAttribute("latestConversations", latestConversations); // Pass to JSP
                 request.setAttribute("currentUserId", loggedInUser.getUserId()); // Make user ID available in JSP/JS
                RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/shared/chat.jsp");
                dispatcher.forward(request, response);
            }
        } catch (Exception e) {
             System.err.println("Error in MessageServlet doGet (Action: " + action + "): " + e.getMessage());
             e.printStackTrace();
             // Handle AJAX errors appropriately
             if (action != null && (action.equals("getUsers") || action.equals("getMessages"))) {
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 response.setContentType("application/json");
                 response.getWriter().write("{\"error\":\"Server error processing chat request.\"}");
             } else {
                 // Error loading main page
                 request.setAttribute("errorMessage", "Failed to load chat interface.");
                 RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/common/error.jsp");
                 dispatcher.forward(request, response);
             }
        }
    }

    // doPost handles sending new messages via AJAX
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 for AJAX
            response.getWriter().write("{\"error\":\"Not authenticated.\"}");
            return;
        }
        if (loggedInUser.isBanned()) {
             response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 for banned
             response.getWriter().write("{\"error\":\"Account banned.\"}");
            return;
        }
        // --- End Authorization Check ---

        String action = request.getParameter("action");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String jsonResponse;

        try {
            if ("sendMessage".equals(action)) {
                // AJAX Call: Send a new message
                jsonResponse = handleSendMessage(request, loggedInUser);
            } else {
                 System.out.println("Invalid POST action received in MessageServlet: " + action);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                jsonResponse = "{\"success\":false, \"message\":\"Invalid action.\"}";
            }
        } catch (Exception e) {
            System.err.println("Error in MessageServlet doPost (Action: " + action + "): " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            jsonResponse = "{\"success\":false, \"message\":\"Server error processing message.\"}";
        }

        out.print(jsonResponse);
        out.flush();
    }

    // --- Handler Methods for Actions ---

    private void handleGetUsers(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
         System.out.println("Handling AJAX request: getUsers for User ID: " + currentUser.getUserId());
         response.setContentType("application/json");
         PrintWriter out = response.getWriter();
         String jsonOutput;

         try {
              // Fetch all non-banned users EXCEPT the current user
              List<User> allUsers = userDAO.getAllUsers(); // Includes admins currently
              List<User> potentialChatPartners = allUsers.stream()
                  .filter(user -> !user.isBanned() && user.getUserId() != currentUser.getUserId())
                   // Optionally filter out admins if desired: .filter(user -> !user.isAdmin())
                  .collect(Collectors.toList());

              // Manually build JSON array (Replace with Gson for simplicity if allowed)
              StringBuilder jsonBuilder = new StringBuilder("[");
              for (int i = 0; i < potentialChatPartners.size(); i++) {
                   User user = potentialChatPartners.get(i);
                   // Fetch profile details for display name
                   String displayName = determineDisplayName(user);

                   jsonBuilder.append("{");
                   jsonBuilder.append("\"userId\":").append(user.getUserId()).append(",");
                   jsonBuilder.append("\"email\":\"").append(escapeJson(user.getEmail())).append("\","); // Include email
                   jsonBuilder.append("\"role\":\"").append(escapeJson(user.getRole())).append("\",");
                    jsonBuilder.append("\"displayName\":\"").append(escapeJson(displayName)).append("\""); // Use determined name
                   jsonBuilder.append("}");
                   if (i < potentialChatPartners.size() - 1) {
                       jsonBuilder.append(",");
                   }
              }
              jsonBuilder.append("]");
              jsonOutput = jsonBuilder.toString();
              System.out.println("Returning " + potentialChatPartners.size() + " users for chat list.");

         } catch (Exception e) {
              System.err.println("Error fetching users for chat: " + e.getMessage());
              e.printStackTrace();
              response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
              jsonOutput = "{\"error\":\"Failed to load user list.\"}";
         }
         out.print(jsonOutput);
         out.flush();
    }


     private void handleGetMessages(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
         System.out.println("Handling AJAX request: getMessages for User ID: " + currentUser.getUserId());
         response.setContentType("application/json");
         PrintWriter out = response.getWriter();
         String jsonOutput;
         String partnerIdParam = request.getParameter("partnerId");
         // Optional: Implement 'since' parameter for fetching only new messages
         // String sinceTimestampParam = request.getParameter("since");

         if (partnerIdParam == null || partnerIdParam.trim().isEmpty()) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              out.print("{\"error\":\"Missing partnerId parameter.\"}");
              out.flush();
              return;
         }

         try {
              int partnerId = Integer.parseInt(partnerIdParam.trim());
              int currentUserId = currentUser.getUserId();
              System.out.println("Fetching conversation between " + currentUserId + " and " + partnerId);

              // Define limit/offset for message fetching if needed (e.g., load last 50)
              int limit = 50;
              int offset = 0; // Fetch latest first

              // Fetch messages
              List<Message> messages = messageDAO.getConversation(currentUserId, partnerId, limit, offset);

               // Mark messages from this partner as read (do this *after* fetching)
              int markedRead = messageDAO.markMessagesAsRead(currentUserId, partnerId);
               if (markedRead > 0) System.out.println("Marked " + markedRead + " messages as read from User " + partnerId);


              // Manually build JSON array (Replace with Gson)
              StringBuilder jsonBuilder = new StringBuilder("[");
              // Iterate in reverse to send oldest first for easier JS appending? Or send latest first (as fetched). Let's send latest first.
              for (int i = 0; i < messages.size(); i++) {
                   Message msg = messages.get(i);
                   jsonBuilder.append("{");
                   jsonBuilder.append("\"messageId\":").append(msg.getMessageId()).append(",");
                   jsonBuilder.append("\"senderUserId\":").append(msg.getSenderUserId()).append(",");
                   jsonBuilder.append("\"receiverUserId\":").append(msg.getReceiverUserId()).append(",");
                   jsonBuilder.append("\"content\":\"").append(escapeJson(msg.getContent())).append("\",");
                   jsonBuilder.append("\"sentAt\":\"").append(msg.getSentAt().toInstant().toString()).append("\","); // ISO 8601 format
                   jsonBuilder.append("\"isRead\":").append(msg.isRead());
                   jsonBuilder.append("}");
                   if (i < messages.size() - 1) {
                       jsonBuilder.append(",");
                   }
              }
              jsonBuilder.append("]");
              jsonOutput = jsonBuilder.toString();
              System.out.println("Returning " + messages.size() + " messages.");


         } catch (NumberFormatException e) {
              System.err.println("Invalid partnerId parameter: " + partnerIdParam);
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              jsonOutput = "{\"error\":\"Invalid partner ID format.\"}";
         } catch (Exception e) {
              System.err.println("Error fetching messages: " + e.getMessage());
              e.printStackTrace();
              response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
              jsonOutput = "{\"error\":\"Failed to load messages.\"}";
         }
          out.print(jsonOutput);
          out.flush();
     }


     private String handleSendMessage(HttpServletRequest request, User currentUser) throws IOException {
         System.out.println("Handling AJAX request: sendMessage from User ID: " + currentUser.getUserId());
         String receiverIdParam = request.getParameter("receiverId");
         String content = request.getParameter("content");
         String jsonResponse;

         if (receiverIdParam == null || content == null || content.trim().isEmpty()) {
             // response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Status set in calling method
             return "{\"success\":false, \"message\":\"Missing receiverId or content.\"}";
         }

         try {
             int receiverId = Integer.parseInt(receiverIdParam.trim());
             int senderId = currentUser.getUserId();

              if (senderId == receiverId) {
                  return "{\"success\":false, \"message\":\"Cannot send message to yourself.\"}";
              }

             // Optional: Verify receiver exists and is not banned?
             User receiver = userDAO.findUserById(receiverId);
             if (receiver == null || receiver.isBanned()) {
                  return "{\"success\":false, \"message\":\"Receiver not found or cannot receive messages.\"}";
             }

             Message newMessage = new Message();
             newMessage.setSenderUserId(senderId);
             newMessage.setReceiverUserId(receiverId);
             newMessage.setContent(content.trim()); // Trim content before saving

             boolean sent = messageDAO.sendMessage(newMessage);

             if (sent) {
                 System.out.println("Message successfully saved to DB from " + senderId + " to " + receiverId);
                 jsonResponse = "{\"success\":true, \"message\":\"Message sent.\"}";
                 // Optional: Return the sent message object?
                 // jsonResponse = "{\"success\":true, \"message\":\"Message sent.\", \"sentMessage\":" + messageToJson(newMessage) + "}";

             } else {
                  System.err.println("MessageDAO.sendMessage returned false.");
                 jsonResponse = "{\"success\":false, \"message\":\"Failed to save message.\"}";
             }

         } catch (NumberFormatException e) {
              System.err.println("Invalid receiverId parameter: " + receiverIdParam);
             // response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             jsonResponse = "{\"success\":false, \"message\":\"Invalid receiver ID format.\"}";
         } catch (Exception e) {
              System.err.println("Error sending message: " + e.getMessage());
              e.printStackTrace();
             // response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             jsonResponse = "{\"success\":false, \"message\":\"Server error sending message.\"}";
         }
          return jsonResponse;
     }

     // Helper to determine display name based on role
     private String determineDisplayName(User user) {
         if (user == null) return "Unknown User";
         if (user.isCreator()) {
             Creator c = creatorDAO.getCreatorByUserId(user.getUserId());
             return (c != null && c.getDisplayName() != null && !c.getDisplayName().isEmpty()) ? c.getDisplayName() : user.getEmail();
         } else if (user.isBusiness()) {
             Business b = businessDAO.getBusinessByUserId(user.getUserId());
             return (b != null && b.getCompanyName() != null && !b.getCompanyName().isEmpty()) ? b.getCompanyName() : user.getEmail();
         } else if (user.isAdmin()) {
             return "Admin (" + user.getEmail() + ")"; // Admins likely don't chat, but handle anyway
         } else {
             return user.getEmail(); // Fallback
         }
     }


     // Basic JSON string escaping (Replace with library if possible)
     private String escapeJson(String str) {
         if (str == null) return "";
         // Basic escaping, not exhaustive for all unicode chars etc.
         return str.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t")
                   .replace("/", "\\/"); // Escape forward slash for safety in script tags
     }

     // Optional: Helper to convert Message to JSON manually (if not using Gson)
     /*
     private String messageToJson(Message msg) {
         if (msg == null) return "null";
         return String.format(
             "{\"messageId\":%d, \"senderUserId\":%d, \"receiverUserId\":%d, \"content\":\"%s\", \"sentAt\":\"%s\", \"isRead\":%b}",
             msg.getMessageId(),
             msg.getSenderUserId(),
             msg.getReceiverUserId(),
             escapeJson(msg.getContent()),
             msg.getSentAt() != null ? msg.getSentAt().toInstant().toString() : "null",
             msg.isRead()
         );
     }
     */

}