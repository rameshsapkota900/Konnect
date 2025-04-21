document.addEventListener('DOMContentLoaded', () => {
    console.log("Konnect script.js initializing...");

    // --- Theme Toggler ---
    const themeToggleButton = document.getElementById('themeToggleBtn');
    const preferDark = window.matchMedia('(prefers-color-scheme: dark)');

    // Function to apply theme based on preference or saved setting
    function applyTheme(theme) {
        if (theme === 'light') {
            document.body.classList.remove('dark-theme');
            document.body.classList.add('light-theme');
             if(themeToggleButton) themeToggleButton.textContent = '🌙'; // Show moon icon for toggle to dark
        } else {
            document.body.classList.add('dark-theme'); // Default to dark
            document.body.classList.remove('light-theme');
             if(themeToggleButton) themeToggleButton.textContent = '☀️'; // Show sun icon for toggle to light
        }
        console.log("Applied theme:", theme);
    }

    // Function to toggle theme and save preference
    function toggleTheme() {
        let newTheme = document.body.classList.contains('dark-theme') ? 'light' : 'dark';
        applyTheme(newTheme);
        try {
            localStorage.setItem('theme', newTheme);
            console.log("Saved theme preference:", newTheme);
        } catch (e) {
            console.error("Could not save theme preference to localStorage:", e);
        }
    }

    // Initial theme setup
    let savedTheme = null;
     try {
        savedTheme = localStorage.getItem('theme');
     } catch (e) {
         console.error("Could not read theme preference from localStorage:", e);
     }

    if (savedTheme) {
        applyTheme(savedTheme);
    } else {
        // If no saved theme, check OS preference
        applyTheme(preferDark.matches ? 'dark' : 'light');
    }

    // Add listener for toggle button
    if (themeToggleButton) {
        themeToggleButton.addEventListener('click', toggleTheme);
    } else {
         console.warn("Theme toggle button (#themeToggleBtn) not found.");
    }

    // Optional: Listen for OS theme changes
    preferDark.addEventListener('change', (e) => {
         // Only change if no theme is explicitly saved by the user
         let currentSavedTheme = null;
         try { currentSavedTheme = localStorage.getItem('theme'); } catch (err) {}

         if (!currentSavedTheme) {
             applyTheme(e.matches ? 'dark' : 'light');
             console.log("OS theme preference changed, updated theme.");
         }
    });


    // --- Conditional display for Registration Form ---
    const roleRadios = document.querySelectorAll('input[name="role"]');
    const creatorFields = document.getElementById('creatorFields');
    const businessFields = document.getElementById('businessFields');

    function toggleRegistrationFields() {
         // Check if the elements exist before trying to access style
         if (!creatorFields || !businessFields) {
            // console.log("Creator or Business fields not found on this page.");
            return;
         }

        const selectedRole = document.querySelector('input[name="role"]:checked');
        if (selectedRole) {
            console.log("Selected role:", selectedRole.value);
            if (selectedRole.value === 'creator') {
                creatorFields.style.display = 'block';
                businessFields.style.display = 'none';
                 // Make fields required/not required dynamically (optional but good UX)
                 setInputRequired(creatorFields, true);
                 setInputRequired(businessFields, false);
            } else if (selectedRole.value === 'business') {
                creatorFields.style.display = 'none';
                businessFields.style.display = 'block';
                 setInputRequired(creatorFields, false);
                 setInputRequired(businessFields, true);
            } else {
                 // Should not happen with radio buttons, but good to handle
                 creatorFields.style.display = 'none';
                 businessFields.style.display = 'none';
                 setInputRequired(creatorFields, false);
                 setInputRequired(businessFields, false);
            }
        } else {
            // Hide both if nothing selected initially
            creatorFields.style.display = 'none';
            businessFields.style.display = 'none';
             setInputRequired(creatorFields, false);
             setInputRequired(businessFields, false);
             console.log("No role selected initially.");
        }
    }
    // Helper to set required attribute on inputs within a container
     function setInputRequired(container, isRequired) {
         if (!container) return;
         const inputs = container.querySelectorAll('input, select, textarea');
         inputs.forEach(input => {
              // Only toggle required for specific inputs if needed, e.g., not hidden ones
              if (isRequired) {
                  input.setAttribute('required', '');
              } else {
                   input.removeAttribute('required');
              }
         });
     }

    if (roleRadios.length > 0) {
         console.log("Found role selection radios, adding listeners.");
        roleRadios.forEach(radio => {
            radio.addEventListener('change', toggleRegistrationFields);
        });
        // Initial check in case a role is pre-selected (e.g., due to form validation error back-end)
        toggleRegistrationFields();
    } else {
        // console.log("Role selection radios not found on this page.");
    }


     // --- Basic Chat Functionality ---
     const chatContainer = document.querySelector('.chat-container');
     if (chatContainer) {
        console.log("Chat container found, initializing chat JS...");
        const chatMessagesContainer = document.getElementById('chatMessages');
        const messageInput = document.getElementById('messageInput');
        const sendMessageBtn = document.getElementById('sendMessageBtn');
        const chatUserList = document.querySelector('.chat-user-list'); // Assuming a UL
        const chatHeaderUsername = document.querySelector('.chat-header .username'); // Span/Div to show username
        let currentChatPartnerId = null; // Store the user ID of the current chat partner
        let pollIntervalId = null; // To store the interval ID for polling
        const currentUserId = document.body.dataset.userId; // Get current user ID from <body data-user-id="...">

        // Base URL for the application
        // Getting context path reliably in JS can be tricky without server-side help.
        // Assume it's root or set via data attribute.
        const contextPath = document.body.dataset.contextPath || '';
        console.log("Context Path:", contextPath);
        console.log("Current User ID:", currentUserId);

        if (!currentUserId) {
            console.error("CRITICAL: Current User ID not found in body data attribute. Chat may not function correctly.");
        }

        // Function to load users into the sidebar
        function loadChatUsers() {
            if (!chatUserList) {
                console.error("Chat user list element not found.");
                return;
             }

            console.log("Fetching chat users...");
            fetch(`${contextPath}/chat?action=getUsers`)
                .then(response => {
                    if (!response.ok) { throw new Error(`HTTP error! status: ${response.status}`); }
                    return response.json();
                 })
                .then(users => {
                    console.log("Received users:", users);
                    chatUserList.innerHTML = ''; // Clear existing list
                    if (users.length === 0) {
                         chatUserList.innerHTML = '<li>No other users found.</li>';
                         return;
                    }
                    users.forEach(user => {
                        const li = document.createElement('li');
                        li.textContent = user.displayName || 'Unknown User'; // Use display name from backend
                        li.dataset.userId = user.userId;
                        li.addEventListener('click', () => selectChatUser(user));
                        chatUserList.appendChild(li);
                    });
                     console.log("Chat user list populated.");
                })
                .catch(error => {
                    console.error('Error loading chat users:', error);
                    if (chatUserList) chatUserList.innerHTML = '<li>Error loading users</li>';
                });
        }

        // Function to select a user and load messages
        function selectChatUser(user) {
            if (!user || !user.userId) {
                console.error("Invalid user object passed to selectChatUser");
                return;
             }
            console.log("Selecting chat user:", user);
            currentChatPartnerId = user.userId;
            if (chatHeaderUsername) {
                chatHeaderUsername.textContent = user.displayName || 'Unknown User';
                 // Make header visible if it was hidden initially
                 const chatHeader = document.querySelector('.chat-header');
                 if (chatHeader) chatHeader.style.display = '';
            } else {
                console.warn("Chat header username element not found.");
            }
             // Hide initial placeholder message if present
             const chatPlaceholder = document.getElementById('chatPlaceholder');
             if (chatPlaceholder) chatPlaceholder.style.display = 'none';
             // Show message input area
             const chatInputArea = document.querySelector('.chat-input');
             if (chatInputArea) chatInputArea.style.display = 'flex';


            // Highlight selected user in the list
            document.querySelectorAll('.chat-user-list li').forEach(li => {
                li.classList.remove('active');
                if (li.dataset.userId == currentChatPartnerId) {
                    li.classList.add('active');
                }
            });

            stopPollingMessages(); // Stop polling for previous user
            loadMessages(currentChatPartnerId); // Load messages for new user
            startPollingMessages(currentChatPartnerId); // Start polling for new user
        }

        // Function to load messages for the selected user
        function loadMessages(partnerId) {
            if (!chatMessagesContainer || !partnerId) {
                 console.warn("Cannot load messages: container or partnerId missing.");
                return;
            }

            console.log(`Fetching messages for partner ID: ${partnerId}`);
            fetch(`${contextPath}/chat?action=getMessages&partnerId=${partnerId}`)
                .then(response => {
                     if (!response.ok) { throw new Error(`HTTP error! status: ${response.status}`); }
                     return response.json();
                 })
                .then(messages => {
                     console.log(`Received ${messages.length} messages.`);
                     chatMessagesContainer.innerHTML = ''; // Clear existing messages before adding new batch
                     messages.forEach(addMessageToChat);
                     scrollToBottom();
                     console.log("Messages loaded and displayed.");
                })
                .catch(error => {
                    console.error('Error loading messages:', error);
                    if(chatMessagesContainer) chatMessagesContainer.innerHTML = '<div class="message error-message">Could not load messages.</div>';
                });
        }

        // Function to add a single message object to the chat display
        function addMessageToChat(message) {
            if (!chatMessagesContainer || !message) return;
            const messageDiv = document.createElement('div');
            messageDiv.classList.add('message-bubble');

            if (message.senderUserId == currentUserId) {
                messageDiv.classList.add('sent');
            } else {
                messageDiv.classList.add('received');
            }

            // Sanitize content before inserting? Basic textContent is safer.
            // For HTML content, use a sanitizer library.
            messageDiv.textContent = message.content;

            // Append to the end (container is normal flex order now)
            chatMessagesContainer.appendChild(messageDiv);
        }

        // Function to send a message
        function sendMessage() {
            if (!messageInput || !currentChatPartnerId || !currentUserId) {
                 console.warn("Cannot send message: Input, partner ID, or current user ID missing.");
                 return;
            }
            const messageContent = messageInput.value.trim();
            if (messageContent === '') return; // Don't send empty messages

            console.log(`Sending message to ${currentChatPartnerId}: ${messageContent}`);

             // Disable input/button while sending
             messageInput.disabled = true;
             sendMessageBtn.disabled = true;


            fetch(`${contextPath}/chat`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `action=sendMessage&receiverId=${currentChatPartnerId}&content=${encodeURIComponent(messageContent)}`
            })
            .then(response => {
                 if (!response.ok) {
                      // Try to parse error message from backend if available
                      return response.json().then(errData => {
                          throw new Error(errData.message || `HTTP error! status: ${response.status}`);
                      }).catch(() => { // If parsing JSON fails, throw generic error
                           throw new Error(`HTTP error! status: ${response.status}`);
                      });
                 }
                 return response.json();
             })
            .then(result => {
                console.log("Send message response:", result);
                if (result.success) {
                    // Add the sent message immediately to the UI (optimistic update)
                    const sentMessage = {
                         senderUserId: parseInt(currentUserId), // Ensure number type
                         receiverUserId: currentChatPartnerId,
                         content: messageContent,
                         sentAt: new Date().toISOString() // Approximate client timestamp
                    };
                    addMessageToChat(sentMessage);
                    messageInput.value = ''; // Clear input
                    scrollToBottom();
                    console.log("Message sent and added to UI.");
                } else {
                    console.error('Server indicated message send failed:', result.message);
                    // Show error to user? Maybe an alert or inline message.
                    alert('Failed to send message: ' + (result.message || 'Unknown server error'));
                }
            })
            .catch(error => {
                console.error('Error sending message:', error);
                alert('Error sending message: ' + error.message + '. Please check connection and try again.');
            })
             .finally(() => {
                  // Re-enable input/button
                  messageInput.disabled = false;
                  sendMessageBtn.disabled = false;
                  messageInput.focus(); // Focus back on input
             });
        }

        // Add event listener for send button
        if (sendMessageBtn && messageInput) {
            sendMessageBtn.addEventListener('click', sendMessage);
            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) { // Send on Enter, allow Shift+Enter for newline
                    e.preventDefault();
                    sendMessage();
                }
            });
        } else {
             console.warn("Message input or send button not found.");
        }

        // Function to periodically poll for new messages
        function startPollingMessages(partnerId) {
             if (!partnerId || !currentUserId) {
                 console.warn("Cannot start polling: partnerId or currentUserId missing.");
                 return;
             }
            stopPollingMessages(); // Clear any existing interval

            console.log(`Starting polling for new messages with partner ID: ${partnerId}`);
            pollIntervalId = setInterval(() => {
                // Optimization: Fetch only messages since the last known message? Requires tracking last message timestamp/ID.
                // For simplicity, refetch the conversation (or last N messages). DAO marks as read.
                console.log(`Polling for messages between ${currentUserId} and ${partnerId}...`);
                fetch(`${contextPath}/chat?action=getMessages&partnerId=${partnerId}`)
                     .then(response => {
                         if (!response.ok) { throw new Error(`Poll HTTP error! status: ${response.status}`); }
                         return response.json();
                     })
                     .then(messages => {
                          if (messages && messages.length > 0) {
                               // Naive update: redraw all messages. More efficient would be to check for new ones.
                               console.log(`Poll received ${messages.length} messages. Updating display.`);
                               chatMessagesContainer.innerHTML = ''; // Clear and redraw
                               messages.forEach(addMessageToChat);
                               // Don't scroll if user has scrolled up? Check scroll position.
                               if (isScrolledToBottom()) {
                                    scrollToBottom();
                               }
                          } else {
                               // console.log("Poll: No new messages.");
                          }
                     })
                     .catch(error => {
                          console.warn('Polling error:', error);
                          // Optional: Stop polling after several errors?
                          // stopPollingMessages();
                     });
            }, 7000); // Poll every 7 seconds (adjust interval)
        }

        // Function to stop polling
        function stopPollingMessages() {
            if (pollIntervalId) {
                clearInterval(pollIntervalId);
                pollIntervalId = null;
                console.log("Stopped message polling.");
            }
        }

        // Function to scroll chat to bottom
        function scrollToBottom() {
            if (chatMessagesContainer) {
                chatMessagesContainer.scrollTop = chatMessagesContainer.scrollHeight;
            }
        }

        // Helper to check if chat is scrolled to the bottom
        function isScrolledToBottom() {
             if (!chatMessagesContainer) return true; // Assume yes if container doesn't exist
             const threshold = 10; // Pixels threshold
             return chatMessagesContainer.scrollHeight - chatMessagesContainer.scrollTop - chatMessagesContainer.clientHeight < threshold;
         }

        // Initial load for chat page
        loadChatUsers();
        // Hide chat header/input until a user is selected
         const chatHeader = document.querySelector('.chat-header');
         const chatInputArea = document.querySelector('.chat-input');
         if (chatHeader) chatHeader.style.display = 'none';
         if (chatInputArea) chatInputArea.style.display = 'none';

    } else {
         console.log("Chat container not found on this page.");
    }


    // --- Confirmation for Delete Buttons ---
    const deleteButtons = document.querySelectorAll('.btn-delete-confirm');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            const message = this.dataset.confirmMessage || 'Are you sure you want to delete this item?';
            if (!confirm(message)) {
                event.preventDefault(); // Stop the form submission or link navigation
                 console.log("Delete action cancelled by user.");
            } else {
                 console.log("Delete action confirmed by user.");
            }
        });
    });


    // --- Add other JS functionalities as needed ---
    // E.g., client-side form validation hints (though backend validation is key),
    // simple animations, character counters for textareas.

    console.log("Konnect script.js initialized successfully.");
});