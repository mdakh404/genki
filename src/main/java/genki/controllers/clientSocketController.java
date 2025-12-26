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
	private HomeController homeController;  // Reference to HomeController for updating chat header

	
	
	public clientSocketController(String username) {
		ClientThread = new ClientsThreads("127.0.0.1", 5001, this);
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

	@Override
	public void onMessageReceived(String message) {
		// TODO Auto-generated method stub
		Platform.runLater(() -> {
			System.out.println("Client recieved : " + message);
			
			// Skip system messages (connection status, etc.)
			if (message.startsWith("---") || message.startsWith("Server: ---")) {
				System.out.println("Skipping system message");
				return;
			}

			
			// Check if this is a users list message
			if (message.startsWith("Server: USERS_LIST:")) {
				// Extract JSON from message
				String jsonPart = message.substring("Server: USERS_LIST:".length());
				parseAndUpdateUsersList(jsonPart);
			}
			else {
				// This is a regular message - parse and notify callback
				try {
					// Strip "Server: " prefix if present
					String jsonMessage = message;
					if (message.startsWith("Server: ")) {
						jsonMessage = message.substring("Server: ".length());
					}
					
					System.out.println("╔════════════════════════════════════════════════════╗");
					System.out.println("║ RECEIVED MESSAGE FROM SERVER                       ║");
					System.out.println("╚════════════════════════════════════════════════════╝");
					System.out.println("Raw message (first 150 chars): " + jsonMessage.substring(0, Math.min(150, jsonMessage.length())));
					
					MessageData msgData = GsonUtility.getGson().fromJson(jsonMessage, MessageData.class);
					System.out.println("✓ Successfully parsed MessageData:");
					System.out.println("  - conversationId: " + msgData.conversationId);
					System.out.println("  - senderId: " + msgData.senderId);
					System.out.println("  - senderName: " + msgData.senderName);
					System.out.println("  - messageText: " + msgData.messageText);
					System.out.println("  - senderProfileImage: " + msgData.senderProfileImage);
					System.out.println("  - timestamp: " + msgData.timestamp);
					System.out.println("  - recipientId: " + msgData.recipientId);
					System.out.println("  - recipientName: " + msgData.recipientName);
					
					if (onNewMessageCallback != null) {
						System.out.println("✓ Callback found, dispatching message to HomeController");
						onNewMessageCallback.accept(msgData);
					} else {
						System.out.println("⚠️  WARNING: onNewMessageCallback is null!");
					}
				} catch (Exception e) {
					System.err.println("✗ Error parsing incoming message: " + e.getMessage());
					e.printStackTrace();
				}
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
