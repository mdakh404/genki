package genki.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import genki.models.Notification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    
    private DBConnection DBConnect = new DBConnection("genki_testing");
    private MongoDatabase database = DBConnect.getDatabase();
    private MongoCollection<Document> notifications = database.getCollection("notifications");
    
    /**
     * Insert a new notification into the database
     * @param notification The notification object to insert
     * @return The ObjectId of the inserted notification
     */
    public ObjectId insertNotification(Notification notification) {
        try {
            Document doc = new Document()
                    .append("recipientUserId", notification.getRecipientUserId())
                    .append("type", notification.getType())
                    .append("senderId", notification.getSenderId())
                    .append("senderName", notification.getSenderName())
                    .append("content", notification.getContent())
                    .append("requestType", notification.getRequestType())
                    .append("status", notification.getStatus())
                    .append("createdAt", notification.getCreatedAt())
                    .append("readAt", notification.getReadAt());
            
            var result = notifications.insertOne(doc);
            return result.getInsertedId().asObjectId().getValue();
        } catch (Exception e) {
            System.err.println("Error inserting notification: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create and send a friend request notification
     * @param recipientUserId The user receiving the request
     * @param senderId The user sending the request
     * @param senderName The name of the sender
     * @return The ObjectId of the created notification
     */
    public ObjectId sendFriendRequestNotification(ObjectId recipientUserId, String senderId, String senderName) {
        try {
            Notification notification = new Notification(
                new ObjectId(),
                recipientUserId,
                "friend_request",
                "friend_request",
                senderId,
                senderName,
                senderName + " wants to add you as a friend"
            );
            return insertNotification(notification);
        } catch (Exception e) {
            System.err.println("Error sending friend request notification: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create and send a group invite notification
     * @param recipientUserId The user receiving the invite
     * @param groupId The ID of the group
     * @param senderId The user sending the invite
     * @param senderName The name of the sender
     * @param groupName The name of the group
     * @return The ObjectId of the created notification
     */
    public ObjectId sendGroupJoinReq(ObjectId recipientUserId, ObjectId groupId, String senderId, String senderName, String groupName) {
        try {
            Notification notification = new Notification(
                new ObjectId(),
                recipientUserId,
                "group_join_request",
                "group_join_request",
                senderId,
                senderName,
                senderName + " wants to join " + groupName
            );
            notification.setRequestType("group_" + groupId);
            return insertNotification(notification);
        } catch (Exception e) {
            System.err.println("Error sending group invite notification: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get all pending notifications for a user
     * @param recipientUserId The user ID
     * @return List of pending notifications
     */
    public List<Notification> getPendingNotifications(ObjectId recipientUserId) {
        try {
            List<Notification> notificationList = new ArrayList<>();
            var cursor = notifications.find(
                Filters.and(
                    Filters.eq("recipientUserId", recipientUserId),
                    Filters.eq("status", "pending")
                )
            ).sort(new Document("createdAt", -1));
            
            for (Document doc : cursor) {
                notificationList.add(documentToNotification(doc));
            }
            return notificationList;
        } catch (Exception e) {
            System.err.println("Error getting pending notifications: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all notifications of a specific type for a user
     * @param recipientUserId The user ID
     * @param type The notification type
     * @return List of notifications of that type
     */
    public List<Notification> getNotificationsByType(ObjectId recipientUserId, String type) {
        try {
            List<Notification> notificationList = new ArrayList<>();
            var cursor = notifications.find(
                Filters.and(
                    Filters.eq("recipientUserId", recipientUserId),
                    Filters.eq("type", type)
                )
            ).sort(new Document("createdAt", -1));
            
            for (Document doc : cursor) {
                notificationList.add(documentToNotification(doc));
            }
            return notificationList;
        } catch (Exception e) {
            System.err.println("Error getting notifications by type: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Update notification status
     * @param notificationId The notification ID
     * @param newStatus The new status ("accepted", "rejected", "read")
     */
    public void updateNotificationStatus(ObjectId notificationId, String newStatus) {
        try {
            Document update = new Document("$set", new Document()
                    .append("status", newStatus));
            
            notifications.updateOne(
                    new Document("_id", notificationId),
                    update
            );
        } catch (Exception e) {
            System.err.println("Error updating notification status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Mark notification as read
     * @param notificationId The notification ID
     */
    public void markAsRead(ObjectId notificationId) {
        try {
            Document update = new Document("$set", new Document()
                    .append("status", "read")
                    .append("readAt", LocalDateTime.now()));
            
            notifications.updateOne(
                    new Document("_id", notificationId),
                    update
            );
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Delete a notification
     * @param notificationId The notification ID
     */
    public void deleteNotification(ObjectId notificationId) {
        try {
            notifications.deleteOne(new Document("_id", notificationId));
        } catch (Exception e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get unread notification count for a user
     * @param recipientUserId The user ID
     * @return Count of unread notifications
     */
    public long getUnreadCount(ObjectId recipientUserId) {
        try {
            return notifications.countDocuments(
                Filters.and(
                    Filters.eq("recipientUserId", recipientUserId),
                    Filters.ne("status", "read")
                )
            );
        } catch (Exception e) {
            System.err.println("Error counting unread notifications: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Convert MongoDB Document to Notification object
     */
    private Notification documentToNotification(Document doc) {
        Notification notification = new Notification();
        notification.setRecipientUserId(doc.getObjectId("recipientUserId"));
        notification.setType(doc.getString("type"));
        notification.setSenderId(doc.getString("senderId"));
        notification.setSenderName(doc.getString("senderName"));
        notification.setContent(doc.getString("content"));
        notification.setRequestType(doc.getString("requestType"));
        notification.setStatus(doc.getString("status"));
        notification.setCreatedAt(doc.getDate("createdAt") != null ? 
            doc.getDate("createdAt").toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        notification.setReadAt(doc.getDate("readAt") != null ?
            doc.getDate("readAt").toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        return notification;
    }

}
