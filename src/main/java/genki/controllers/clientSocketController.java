package genki.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bson.codecs.pojo.Convention;

import genki.models.Conversation;
import genki.models.User;
import genki.models.MessageData;
import genki.network.ClientHandler;
import genki.network.ClientsThreads;
import genki.network.MessageListener;
import genki.network.t2;
import genki.utils.GsonUtility;
import genki.utils.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;


public class clientSocketController implements t2{

	
	private ClientsThreads ClientThread;
	private String username;
	private List<User> connectedUsers = new ArrayList<>();
	private Consumer<MessageData> onNewMessageCallback;
	private Consumer<genki.models.Notification> onNewNotificationCallback;  // Callback for new notifications
	private HomeController homeController;  // Reference to HomeController for updating chat header

	
	
	public clientSocketController(String username) {
		ClientThread = new ClientsThreads("192.168.8.104", 5001, this);
		this.username = username;
		ClientThread.connect();

		ClientThread.sendMessage(this.username);
	}
	
	public String getUser() {
		return this.username;
	}
	
	public void setOnNewMessageCallback(Consumer<MessageData> callback) {
		this.onNewMessageCallback = callback;
	}
	
	public void setOnNewNotificationCallback(Consumer<genki.models.Notification> callback) {
		this.onNewNotificationCallback = callback;
	}
	
	public void setHomeController(HomeController homeController) {
		this.homeController = homeController;
	}
	
//	public void initialiseClient() {
//		ClientThread = new ClientsThreads("127.0.0.1", 5001, this);
//		ClientThread.connect();
//		
//	}
	
	public void sendMessages(String message) {
		ClientThread.sendMessage(message);
		
		Platform.runLater(() -> {
			//dddd
		});
	}
	
	/**
	 * Send a group join acceptance notification via socket
	 * This notifies the requester that their group join request was accepted
	 * @param message JSON message containing GROUP_JOIN_ACCEPTED notification
	 */
	public void sendGroupJoinAcceptanceNotification(String message) {
		ClientThread.sendMessage(message);
		System.out.println("✓ Sent GROUP_JOIN_ACCEPTED notification via socket");
	}
	
	/**
	 * Send a friend request acceptance notification via socket
	 * This notifies the requester that their friend request was accepted
	 * @param message JSON message containing FRIEND_REQUEST_ACCEPTED notification
	 */
	public void sendFriendRequestAcceptanceNotification(String message) {
		ClientThread.sendMessage(message);
		System.out.println("✓ Sent FRIEND_REQUEST_ACCEPTED notification via socket");
	}

	@Override
	public void onMessageReceived(String message) {
		Platform.runLater(() -> {
			System.out.println("\n📨 Client received from socket: " + message.substring(0, Math.min(100, message.length())));
			
			// Skip system messages
			if (message.startsWith("---") || message.startsWith("Server: ---")) {
				System.out.println("   → System message (skipped)");
				return;
			}

			// Check if this is a users list message
			if (message.startsWith("Server: USERS_LIST:")) {
				System.out.println("   → Users list message");
				String jsonPart = message.substring("Server: USERS_LIST:".length());
				parseAndUpdateUsersList(jsonPart);
				return;
			}
			
			try {
				// Strip "Server: " prefix if present
				String jsonMessage = message;
				if (message.startsWith("Server: ")) {
					jsonMessage = message.substring("Server: ".length());
				}
				
				// Parse as JSON to check message type
				com.google.gson.JsonObject jsonObj = com.google.gson.JsonParser.parseString(jsonMessage).getAsJsonObject();
				
				// CHECK -1: Is this a FRIEND_REQUEST_ACCEPTED notification?
				if (jsonObj.has("type") && jsonObj.get("type").getAsString().equals("FRIEND_REQUEST_ACCEPTED")) {
					System.out.println("   → 👥 FRIEND_REQUEST_ACCEPTED MESSAGE DETECTED");
					
					try {
						String recipientId = jsonObj.has("recipientId") ? jsonObj.get("recipientId").getAsString() : null;
						String requesterUsername = jsonObj.get("requesterUsername").getAsString();
						String acceptorUsername = jsonObj.get("acceptorUsername").getAsString();
						String acceptedBy = jsonObj.get("acceptedBy").getAsString();
						
						// Only process if this message is for the current user
						if (recipientId != null && recipientId.equals(UserSession.getUserId())) {
							System.out.println("✓ Friend Request Accepted:");
							System.out.println("  - Friend: " + acceptorUsername);
							System.out.println("  - Accepted by: " + acceptedBy);
							
							// Notify HomeController to add the friend conversation to UI immediately
							if (homeController != null) {
								//homeController.addFriendConversationFromAcceptance(acceptorUsername);
								System.out.println("✅ Friend conversation added to UI\n");
							} else {
								System.out.println("⚠️ HomeController reference is null!\n");
							}
						} else {
							System.out.println("⚠️ FRIEND_REQUEST_ACCEPTED message is for different user (recipient: " + recipientId + ", current: " + UserSession.getUserId() + ")");
						}
						return; // Don't try to parse as other message types
					} catch (Exception e) {
						System.err.println("❌ Error parsing FRIEND_REQUEST_ACCEPTED message: " + e.getMessage());
						e.printStackTrace();
					}
				}
				
				// CHECK 0: Is this a GROUP_JOIN_ACCEPTED notification?
				if (jsonObj.has("type") && jsonObj.get("type").getAsString().equals("GROUP_JOIN_ACCEPTED")) {
					System.out.println("   → 🎉 GROUP_JOIN_ACCEPTED MESSAGE DETECTED");
					
					try {
						String recipientId = jsonObj.has("recipientId") ? jsonObj.get("recipientId").getAsString() : null;
						String requesterUsername = jsonObj.get("requesterUsername").getAsString();
						String groupName = jsonObj.get("groupName").getAsString();
						String groupId = jsonObj.get("groupId").getAsString();
						String acceptedBy = jsonObj.get("acceptedBy").getAsString();
						
						// Only process if this message is for the current user (the requester/new member)
						if (recipientId != null && recipientId.equals(UserSession.getUserId())) {
							System.out.println("✓ Group Join Request Accepted:");
							System.out.println("  - Group: " + groupName + " (ID: " + groupId + ")");
							System.out.println("  - Accepted by: " + acceptedBy);
							
							// The requester (new member) needs the group conversation UI created
							// The admin doesn't need a new UI since they already have the group
							if (homeController != null) {
								homeController.addGroupConversationFromAcceptance(groupId, groupName);
								System.out.println("✅ Group conversation added to requester's UI\n");
							} else {
								System.out.println("⚠️ HomeController reference is null!\n");
							}
						} else {
							System.out.println("⚠️ GROUP_JOIN_ACCEPTED message is for different user (recipient: " + recipientId + ", current: " + UserSession.getUserId() + ")");
						}
						return; // Don't try to parse as other message types
					} catch (Exception e) {
						System.err.println("❌ Error parsing GROUP_JOIN_ACCEPTED message: " + e.getMessage());
						e.printStackTrace();
					}
				}
				
				// CHECK 1: Is this a NOTIFICATION?
				if (jsonObj.has("messageType") && jsonObj.get("messageType").getAsString().equals("notification")) {
					System.out.println("   → 🔔 NOTIFICATION MESSAGE DETECTED");
					
					try {
						// Extract the notification from the wrapper
						com.google.gson.JsonObject notificationObj = jsonObj.getAsJsonObject("notification");
						genki.models.Notification notification = GsonUtility.getGson().fromJson(notificationObj, genki.models.Notification.class);
						
						System.out.println("✓ Parsed Notification:");
						System.out.println("  - From: " + notification.getSenderName());
						System.out.println("  - Type: " + notification.getType());
						System.out.println("  - Content: " + notification.getContent());
						
						if (onNewNotificationCallback != null) {
							System.out.println("✓ Invoking notification callback...");
							onNewNotificationCallback.accept(notification);
							System.out.println("✅ Notification added to list\n");
						} else {
							System.out.println("⚠️ Notification callback is null!\n");
						}
						return; // Don't try to parse as regular message
					} catch (Exception e) {
						System.err.println("❌ Error parsing notification: " + e.getMessage());
						e.printStackTrace();
					}
				}
				
				// CHECK 2: Is this a REGULAR MESSAGE?
				if (jsonObj.has("messageText")) {
					System.out.println("   → 💬 REGULAR MESSAGE DETECTED");
					
					try {
						MessageData msgData = GsonUtility.getGson().fromJson(jsonMessage, MessageData.class);
						
						System.out.println("✓ Parsed Message:");
						System.out.println("  - From: " + msgData.senderName);
						System.out.println("  - Text: " + msgData.messageText.substring(0, Math.min(50, msgData.messageText.length())));
						
						if (onNewMessageCallback != null) {
							System.out.println("✓ Invoking message callback...");
							onNewMessageCallback.accept(msgData);
							System.out.println("✅ Message added to chat\n");
						} else {
							System.out.println("⚠️ Message callback is null!\n");
						}
					} catch (Exception e) {
						System.err.println("❌ Error parsing message: " + e.getMessage());
						e.printStackTrace();
					}
					return;
				}
				
				// Unknown message type
				System.out.println("   → ⚠️ Unknown message type (not notification or message)\n");
				
			} catch (Exception e) {
				System.err.println("❌ Error processing message: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	private void parseAndUpdateUsersList(String jsonUsers) {
		try {
			User[] users = GsonUtility.getGson().fromJson(jsonUsers, User[].class);
			connectedUsers = Arrays.asList(users);
			
			// Update the session with the connected users
			UserSession.setConnectedUsers(new ArrayList<>(connectedUsers));
			
			System.out.println("✓ Updated connected users list with " + connectedUsers.size() + " users");
			System.out.println("  Connected users: " + UserSession.ConnectedUsers);
			
			// Update online status for ALL conversation items on JavaFX thread
			Platform.runLater(() -> {
				// Update user conversation items
				ArrayList<HBox> conversationItems = UserSession.getConversationItems();
				if (conversationItems == null || conversationItems.isEmpty()) {
					System.out.println("⚠ No conversation items loaded yet (will be refreshed after load)");
				} else {
					System.out.println("✓ Updating " + conversationItems.size() + " conversation items");
					for(HBox conversationItem : conversationItems) {
						Object userData = conversationItem.getUserData();
						if (userData instanceof java.util.Map) {
							// New structure: Map with "user" and "conversationId"
							java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) userData;
							User friend = (User) dataMap.get("user");
							
							if (friend != null) {
								boolean isOnline = connectedUsers.stream()
									.anyMatch(u -> u.getUsername() != null && u.getUsername().equals(friend.getUsername()));
								updateConversationItemOnlineStatus(conversationItem, isOnline);
								
								// Also update chat header if this is the currently open conversation
								if (homeController != null && friend.getId() != null) {
									String friendId = friend.getId().toString();
									homeController.updateChatHeaderStatusForUser(friendId, friend.getUsername(), isOnline);
								}
								
								System.out.println("  - " + friend.getUsername() + ": " + (isOnline ? "ONLINE" : "OFFLINE"));
							}
						} else if (userData instanceof User) {
							// Fallback for old structure (direct User object)
							User friend = (User) userData;
							boolean isOnline = connectedUsers.stream()
								.anyMatch(u -> u.getUsername() != null && u.getUsername().equals(friend.getUsername()));
							updateConversationItemOnlineStatus(conversationItem, isOnline);
							
							// Also update chat header if this is the currently open conversation
							if (homeController != null && friend.getId() != null) {
								String friendId = friend.getId().toString();
								homeController.updateChatHeaderStatusForUser(friendId, friend.getUsername(), isOnline);
							}
							
							System.out.println("  - " + friend.getUsername() + ": " + (isOnline ? "ONLINE" : "OFFLINE"));
						}
					}
				}
				
				// Also update group conversation items (they should show "Group" status, not online)
				ArrayList<HBox> groupItems = UserSession.getGroupConversationItems();
				if (groupItems != null && !groupItems.isEmpty()) {
					System.out.println("✓ " + groupItems.size() + " group items present");
				}
				
				System.out.println("✓ Finished updating conversation items");
			});
		} catch (Exception e) {
			System.err.println("✗ Error parsing users list: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Update the online status indicator for a conversation item
	 */
	private void updateConversationItemOnlineStatus(HBox conversationItem, boolean isOnline) {
		// Find the status circle (second child in the profile container StackPane)
		javafx.scene.layout.StackPane profileContainer = (javafx.scene.layout.StackPane) conversationItem.getChildren().get(0);
		if (profileContainer.getChildren().size() > 1) {
			javafx.scene.shape.Circle statusCircle = (javafx.scene.shape.Circle) profileContainer.getChildren().get(1);
			statusCircle.setFill(isOnline ? javafx.scene.paint.Color.web("#4ade80") : javafx.scene.paint.Color.web("#9ca3af"));
		}
	}
	
	/**
	 * Public method to refresh online status for all loaded conversation items
	 * Called by HomeController after loading conversations
	 * Uses the same comparison logic as parseAndUpdateUsersList()
	 */
	public void refreshConversationOnlineStatus() {
		Platform.runLater(() -> {
			ArrayList<HBox> conversationItems = UserSession.getConversationItems();
			ArrayList<User> currentConnectedUsers = UserSession.getConnectedUsers();
			System.out.println("🔄 Refreshing online status for " + (conversationItems != null ? conversationItems.size() : 0) + " conversation items");
			System.out.println("  Using " + (currentConnectedUsers != null ? currentConnectedUsers.size() : 0) + " connected users from session");
			
			if (conversationItems != null && !conversationItems.isEmpty()) {
				if (currentConnectedUsers == null || currentConnectedUsers.isEmpty()) {
					System.out.println("⚠ No connected users in session, marking all offline");
				}
				
				for(HBox conversationItem : conversationItems) {
					Object userData = conversationItem.getUserData();
					if (userData instanceof User) {
						User friend = (User) userData;
						// Use same comparison logic as parseAndUpdateUsersList()
						boolean isOnline = currentConnectedUsers != null && currentConnectedUsers.stream()
							.anyMatch(u -> u.getUsername() != null && u.getUsername().equals(friend.getUsername()));
						updateConversationItemOnlineStatus(conversationItem, isOnline);
						
						// Also update chat header if this is the currently open conversation
						if (homeController != null && friend.getId() != null) {
							String friendId = friend.getId().toString();
							homeController.updateChatHeaderStatusForUser(friendId, friend.getUsername(), isOnline);
						}
						
						System.out.println("  🔵 " + friend.getUsername() + " -> " + (isOnline ? "🟢 ONLINE" : "⚫ OFFLINE"));
					}
				}
				System.out.println("✓ Conversation refresh complete");
			} else {
				System.out.println("⚠ No conversation items to refresh");
			}
		});
	}
	
	public List<User> getConnectedUsers() {
		return new ArrayList<>(connectedUsers);
	}

	@Override
	public void onConnectionClosed(String reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClientConnected(ClientHandler handler) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onClientConnected'");
	}
	
	
}
