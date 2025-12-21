package genki.models;

import com.google.gson.annotations.Expose;

public class MessageData {
    @Expose
    public String conversationId;
    @Expose
    public String senderId;
    @Expose
    public String senderName;
    @Expose
    public String messageText;
    @Expose
    public String senderProfileImage;
    @Expose
    public long timestamp;
    @Expose
    public String recipientId;  // Who should receive this message
    @Expose
    public String recipientName;  // Recipient username (fallback for matching)

    public MessageData() {
    }

    public MessageData(String conversationId, String senderId, String senderName, 
                       String messageText, String senderProfileImage, long timestamp) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.senderProfileImage = senderProfileImage;
        this.timestamp = timestamp;
    }

    public MessageData(String conversationId, String senderId, String senderName, 
                       String messageText, String senderProfileImage, long timestamp, String recipientId) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.senderProfileImage = senderProfileImage;
        this.timestamp = timestamp;
        this.recipientId = recipientId;
    }

    public MessageData(String conversationId, String senderId, String senderName, 
                       String messageText, String senderProfileImage, long timestamp, String recipientId, String recipientName) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.senderProfileImage = senderProfileImage;
        this.timestamp = timestamp;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
    }
}
