# 📝 DETAILED CHANGES - NOTIFICATION SYSTEM FIX

## 1️⃣ ServerSocketController.java

### Added Import:
```java
import genki.models.Notification;
```

### Added Method (lines 264-302):
```java
/**
 * Send a notification to a specific connected user via socket
 * @param recipientUserId The ID of the user to send notification to
 * @param notification The Notification object to send
 * @return true if notification was sent successfully
 */
public static boolean sendNotificationToUser(String recipientUserId, genki.models.Notification notification) {
    try {
        System.out.println("🔔 Attempting to send notification to user: " + recipientUserId);
        
        // Serialize notification to JSON
        String jsonNotification = GsonUtility.getGson().toJson(notification);
        System.out.println("📤 Notification JSON: " + jsonNotification);
        
        // Find the recipient in connected users
        for (ClientHandler handler : ConnectedUsers) {
            User handlerUser = handler.getUser();
            if (handlerUser != null) {
                String userId = handlerUser.getId() != null ? handlerUser.getId().toString() : null;
                
                if (userId != null && userId.equals(recipientUserId)) {
                    System.out.println("✓ Found recipient: " + handlerUser.getUsername() + ", sending notification...");
                    handler.sendMessage(jsonNotification);
                    System.out.println("✓ Notification sent successfully to " + handlerUser.getUsername());
                    return true;
                }
            }
        }
        
        System.out.println("⚠️ Recipient " + recipientUserId + " is not currently connected. Notification saved to DB only.");
        return false;
    } catch (Exception e) {
        System.err.println("✗ Error sending notification: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}
```

---

## 2️⃣ AddUserController.java

### Added Import:
```java
import genki.models.Notification;
```

### Modified handleAddUser Method (lines 125-149):
**Before:**
```java
ObjectId sendFriendRequestNotificationId = notificationDAO.sendFriendRequestNotification(
        recipientUserDoc.getObjectId("_id"),
        UserSession.getUserId(),
        UserSession.getUsername()
);

if (sendFriendRequestNotificationId != null) {
    logger.info("A friend request has been sent");
    AlertConstruct.alertConstructor(
           "Add User Success",
           "",
           "A friend request has been sent to " + username,
           Alert.AlertType.INFORMATION
    );
    closeWindow();
}
```

**After:**
```java
ObjectId sendFriendRequestNotificationId = notificationDAO.sendFriendRequestNotification(
        recipientUserDoc.getObjectId("_id"),
        UserSession.getUserId(),
        UserSession.getUsername()
);

if (sendFriendRequestNotificationId != null) {
    logger.info("A friend request has been sent");
    
    // 🔔 NEW: Get the notification object and send it via socket to recipient
    try {
        // Create notification object to send via socket
        genki.models.Notification notification = new genki.models.Notification(
            sendFriendRequestNotificationId,
            recipientUserDoc.getObjectId("_id"),
            "friend_request",
            "friend_request",
            UserSession.getUserId(),
            UserSession.getUsername(),
            UserSession.getUsername() + " wants to add you as a friend"
        );
        notification.setStatus("pending");
        
        // Send notification to recipient via socket (if they're online)
        ServerSocketController.sendNotificationToUser(
            recipientUserDoc.getObjectId("_id").toString(),
            notification
        );
        logger.info("✓ Notification sent to socket (if recipient is online)");
    } catch (Exception socketError) {
        logger.warning("Could not send notification via socket: " + socketError.getMessage());
        // Don't fail the entire operation if socket send fails - notification is already in DB
    }
    
    AlertConstruct.alertConstructor(
           "Add User Success",
           "",
           "A friend request has been sent to " + username,
           Alert.AlertType.INFORMATION
    );
    closeWindow();
}
```

---

## 3️⃣ JoinGroupController.java

### Added Import:
```java
import genki.models.Notification;
```

### Modified handleJoinGroupRequest Method (lines 305-341):
**Before:**
```java
ObjectId joinGroupNotificationId = notificationDAO.sendGroupJoinReq(
         groupAdminDoc.getObjectId("_id"),
         groupDoc.getObjectId("_id"),
         UserSession.getUserId(),
         UserSession.getUsername(),
         nameGroup
);

AlertConstruct.alertConstructor(
        "Join Request",
        "",
        "A join request has been submitted to " + nameGroup + "'s admin.",
        Alert.AlertType.INFORMATION
);

logger.info("GroupJoinRequest notification_id: " + joinGroupNotificationId);
```

**After:**
```java
ObjectId joinGroupNotificationId = notificationDAO.sendGroupJoinReq(
         groupAdminDoc.getObjectId("_id"),
         groupDoc.getObjectId("_id"),
         UserSession.getUserId(),
         UserSession.getUsername(),
         nameGroup
);

// 🔔 NEW: Send notification via socket to group admin (if online)
if (joinGroupNotificationId != null) {
    try {
        genki.models.Notification notification = new genki.models.Notification(
            joinGroupNotificationId,
            groupAdminDoc.getObjectId("_id"),
            "group_join_request",
            "group_" + groupDoc.getObjectId("_id"),
            UserSession.getUserId(),
            UserSession.getUsername(),
            UserSession.getUsername() + " wants to join " + nameGroup
        );
        notification.setStatus("pending");
        
        ServerSocketController.sendNotificationToUser(
            groupAdminDoc.getObjectId("_id").toString(),
            notification
        );
        logger.info("✓ Group join notification sent to socket (if admin is online)");
    } catch (Exception socketError) {
        logger.warning("Could not send notification via socket: " + socketError.getMessage());
    }
}

AlertConstruct.alertConstructor(
        "Join Request",
        "",
        "A join request has been submitted to " + nameGroup + "'s admin.",
        Alert.AlertType.INFORMATION
);

logger.info("GroupJoinRequest notification_id: " + joinGroupNotificationId);
```

---

## 4️⃣ clientSocketController.java (Previously Fixed)

### Modified onMessageReceived Method (lines 99-150):
Added notification detection BEFORE MessageData parsing:

```java
// Try to determine if this is a notification or regular message
// by parsing as JSON and checking for key fields
try {
    com.google.gson.JsonObject jsonObj = com.google.gson.JsonParser.parseString(jsonMessage).getAsJsonObject();
    
    // Check if this is a notification (has 'type' and 'content' but NO 'messageText')
    if (jsonObj.has("type") && jsonObj.has("content") && !jsonObj.has("messageText")) {
        System.out.println("🔔 Detected NOTIFICATION message");
        genki.models.Notification notification = GsonUtility.getGson().fromJson(jsonMessage, genki.models.Notification.class);
        System.out.println("✓ Successfully parsed Notification:");
        System.out.println("  - type: " + notification.getType());
        System.out.println("  - senderName: " + notification.getSenderName());
        System.out.println("  - content: " + notification.getContent());
        System.out.println("  - requestType: " + notification.getRequestType());
        
        if (onNewNotificationCallback != null) {
            System.out.println("✓ Callback found, dispatching notification to HomeController");
            onNewNotificationCallback.accept(notification);
        } else {
            System.out.println("⚠️  WARNING: onNewNotificationCallback is null!");
        }
        return; // Don't try to parse as MessageData
    }
} catch (Exception notifParseError) {
    System.out.println("ℹ️ Not a notification format, trying as regular message");
}
```

---

## Summary of Changes

| File | Change Type | Lines | Description |
|------|------------|-------|-------------|
| ServerSocketController.java | Addition | 264-302 | New method to broadcast notifications to online users |
| ServerSocketController.java | Addition | - | Import Notification class |
| AddUserController.java | Modification | 125-149 | Call sendNotificationToUser after creating notification |
| AddUserController.java | Addition | - | Import Notification class |
| JoinGroupController.java | Modification | 305-341 | Call sendNotificationToUser after creating notification |
| JoinGroupController.java | Addition | - | Import Notification class |
| clientSocketController.java | Modification | 99-150 | Detect and parse notification messages (previous fix) |

---

## Compilation Result
✅ **BUILD SUCCESS** - No errors or failures
