package genki.models;

import org.bson.types.ObjectId;
import java.time.LocalDateTime;

public class Notification {
    private ObjectId recipientUserId;
    private String type; // "friend_request", "group_invite", "message", etc.
    private String senderId;
    private String senderName;
    private String content;
    private String requestType; // Additional request type classification
    private String status; // "pending", "accepted", "rejected", "read"
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public Notification() {
    }

    public Notification(ObjectId recipientUserId, String type, String senderId, String senderName, String content) {
        this.recipientUserId = recipientUserId;
        this.type = type;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
        this.readAt = null;
    }

    public ObjectId getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(ObjectId recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    @Override
    public String toString() {
        return "Notification [type=" + type + ", senderName=" + senderName 
               + ", content=" + content + ", status=" + status + "]";
    }
}
