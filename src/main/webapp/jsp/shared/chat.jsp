<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User" %> <%-- For header --%>
<%
    // Set page title
    request.setAttribute("pageTitle", "Chat");

     // Get logged in user ID (already set as data attribute in header.jsp)
     // int currentUserId = (loggedInUser != null) ? loggedInUser.getUserId() : -1;

     // Get partner ID from query parameter if provided (to auto-select user)
     String partnerIdParam = request.getParameter("partnerId");

%>
<%@ include file="../common/header.jsp" %>

<div class="chat-page-container">
    <h2>Direct Messages</h2>

    <div class="chat-container">
        <%-- Sidebar to list users/conversations --%>
        <aside class="chat-sidebar">
            <h3>Conversations</h3>
            <ul class="chat-user-list">
                <%-- User list will be loaded dynamically by script.js using AJAX --%>
                <li>Loading users...</li>
            </ul>
        </aside>

        <%-- Main chat area --%>
        <section class="chat-main">
            <%-- Header showing current chat partner --%>
            <header class="chat-header" style="display: none;"> <%-- Initially hidden --%>
                 Chatting with: <span class="username">Select a user</span>
            </header>

            <%-- Message display area --%>
            <div id="chatMessages" class="chat-messages">
                 <%-- Placeholder message until a chat is selected --%>
                 <div id="chatPlaceholder" style="text-align: center; margin: auto; color: var(--text-color-muted);">
                     Select a user from the list to start chatting.
                 </div>
                <%-- Messages will be loaded dynamically by script.js --%>
            </div>

            <%-- Message input area --%>
             <footer class="chat-input" style="display: none;"> <%-- Initially hidden --%>
                <input type="text" id="messageInput" placeholder="Type your message here..." autocomplete="off">
                <button id="sendMessageBtn" class="btn btn-primary">Send</button>
             </footer>
        </section>
    </div>
</div>

<%-- Add specific JS logic if needed, though most is in script.js --%>
<script>
    // Auto-select user if partnerId is provided in URL
    document.addEventListener('DOMContentLoaded', () => {
        const partnerIdFromUrl = "<%= partnerIdParam != null ? partnerIdParam : "" %>";
        if (partnerIdFromUrl && chatContainer) { // Ensure chat JS is initialized
             console.log("Partner ID found in URL, attempting to auto-select:", partnerIdFromUrl);
             // Need a way to trigger selection after users are loaded.
             // We can use a MutationObserver or a slight delay. Delay is simpler here.
             setTimeout(() => {
                 const userListItem = document.querySelector(`.chat-user-list li[data-user-id='${partnerIdFromUrl}']`);
                 if (userListItem) {
                     console.log("Found list item for partner, simulating click.");
                     userListItem.click(); // Simulate click to select user
                 } else {
                      console.warn("Could not find list item for partner ID:", partnerIdFromUrl);
                      // Optionally show a message
                      const chatPlaceholder = document.getElementById('chatPlaceholder');
                       if(chatPlaceholder) chatPlaceholder.textContent = "Could not find the specified user to chat with.";
                 }
             }, 1500); // Wait 1.5 seconds for user list to likely load via AJAX
        }
    });
</script>


<%@ include file="../common/footer.jsp" %>