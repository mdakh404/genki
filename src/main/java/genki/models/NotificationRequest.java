package genki.models;

/**
 * Modèle représentant une demande de notification (ami/groupe)
 */
public class NotificationRequest {
    private String username;
    private String message;
    private NotificationType type;
    private String imageUrl;
    
    public enum NotificationType {
        FRIEND_REQUEST,
        GROUP_INVITATION
    }
    
    public NotificationRequest(String username, String message, NotificationType type, String imageUrl) {
        this.username = username;
        this.message = message;
        this.type = type;
        this.imageUrl = imageUrl;
    }
    
    // Getters
    public String getUsername() {
        return username;
    }
    
    public String getMessage() {
        return message;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    // Setters
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}