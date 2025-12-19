package genki.models;

import org.bson.types.ObjectId;
import java.time.LocalDateTime;

public class Message {
    private ObjectId conversationId;
    private String senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;

    public Message() {
    }

    public Message(ObjectId conversationId, String senderId, String senderName, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }


    // Getters and Setters
    public ObjectId getConversationId() {
        return this.conversationId;
    }
    public void setConversationId(ObjectId conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
