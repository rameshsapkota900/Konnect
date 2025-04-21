package model;

import java.sql.Timestamp;
import java.util.Objects;

public class Message {
    private int messageId;
    private int senderUserId;
    private int receiverUserId;
    private String content;
    private Timestamp sentAt;
    private boolean isRead;

    // Optional: Links to sender/receiver User objects (can be heavy if loading many messages)
    private User sender;
    private User receiver;

    // Constructors
    public Message() {}

    public Message(int messageId, int senderUserId, int receiverUserId, String content, Timestamp sentAt, boolean isRead) {
        this.messageId = messageId;
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.content = content;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    // Getters
    public int getMessageId() { return messageId; }
    public int getSenderUserId() { return senderUserId; }
    public int getReceiverUserId() { return receiverUserId; }
    public String getContent() { return content; }
    public Timestamp getSentAt() { return sentAt; }
    public boolean isRead() { return isRead; }
    public User getSender() { return sender; }
    public User getReceiver() { return receiver; }

    // Setters
    public void setMessageId(int messageId) { this.messageId = messageId; }
    public void setSenderUserId(int senderUserId) { this.senderUserId = senderUserId; }
    public void setReceiverUserId(int receiverUserId) { this.receiverUserId = receiverUserId; }
    public void setContent(String content) { this.content = content; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }
    public void setRead(boolean read) { isRead = read; }
    public void setSender(User sender) { this.sender = sender; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    // toString, equals, hashCode
    @Override
    public String toString() {
        return "Message{" +
               "messageId=" + messageId +
               ", senderUserId=" + senderUserId +
               ", receiverUserId=" + receiverUserId +
               ", isRead=" + isRead +
               ", sentAt=" + sentAt +
               ", content='" + (content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content) + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return messageId == message.messageId; // Primary key
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }
}