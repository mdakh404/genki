package genki.models;

import java.time.LocalDateTime;
import java.util.List;
import org.bson.types.ObjectId;

public class Conversation {
    private ObjectId id; // MongoDB document ID
    private String type; // "direct" or "group"
    private List<String> participantIds; // List of user IDs
    private String lastMessageContent;
    private String lastMessageSenderId;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String groupName; // For group conversations
    private String photo_url; // For group conversations
    private Boolean isOnline; // Track if user is online (for direct conversations)

    public Conversation() {
    }

    public Conversation(String type, List<String> participantIds) {
        this.type = type;
        this.participantIds = participantIds;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPhotoUrl() {
        return photo_url;
    }

    public void setPhotoUrl(String photo_url) {
        this.photo_url = photo_url;
    }

    public Boolean isOnline() {
        return isOnline;
    }

    public void setOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

	@Override
	public String toString() {
		return "Conversation [type=" + type + ", participantIds=" + participantIds + ", lastMessageContent="
				+ lastMessageContent + ", lastMessageSenderId=" + lastMessageSenderId + ", lastMessageTime="
				+ lastMessageTime + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
    
    
    
}
