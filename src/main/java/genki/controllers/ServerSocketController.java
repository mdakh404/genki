package genki.controllers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import genki.models.User;
import genki.models.MessageData;
import genki.models.Conversation;
import genki.models.Notification;
import genki.network.ClientHandler;
import genki.network.MessageListener;
import genki.utils.GsonUtility;
import genki.utils.UserSession;
import genki.utils.ConversationDAO;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.types.ObjectId;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ServerSocketController implements MessageListener {

	private ServerSocket serverSocket;
	private Thread serverAcceptThread;
	public static List<ClientHandler> ConnectedUsers = new ArrayList<>();
	private final int port = 5001;

	public void initialize() {
		//
	}

	public void startStopServer() {
		try {
			if (serverSocket == null || serverSocket.isClosed()) {
				startServer();
			} else {
				stopServer();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void startServer() throws IOException {
		try {
			this.serverSocket = new ServerSocket(port);

			serverAcceptThread = new Thread(this::runServerAcceptLoop);
			serverAcceptThread.setDaemon(true);
			serverAcceptThread.start();
		} catch (Exception e) {

		}
	}

	public void runServerAcceptLoop() {
		try {
			while (!serverSocket.isClosed()) {
				Socket client = serverSocket.accept();
				System.out.println("New client has been connected... ");
				ClientHandler handler = new ClientHandler(client, this);

				handler.start();
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void stopServer() {

		for (ClientHandler client : ConnectedUsers) {
			client.closeConnection();
		}
		ConnectedUsers.clear();
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
				System.out.println("Server has been ShutDown ..");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onClientConnected(ClientHandler handler) {
		// Called from ClientHandler background thread; update UI/state on FX thread
		Platform.runLater(() -> {
			// Check if this user is already connected (prevent duplicates)
			String username = handler.getUser().getUsername();
			boolean alreadyConnected = ConnectedUsers.stream()
				.anyMatch(h -> h.getUser().getUsername() != null && 
					h.getUser().getUsername().equals(username));
			
			if (alreadyConnected) {
				System.out.println("⚠️ User " + username + " already connected, not adding duplicate");
				return;
			}
			
			ConnectedUsers.add(handler);
			UserSession.getConnectedUsers().add(handler.getUser());
			System.out.println("✓ Registered user: " + handler.getUser().getUsername());
			System.out.println("✓ Total connected: " + UserSession.getConnectedUsers().size());
			
			// Broadcast updated list to all clients (including the newly connected one)
			broadcastConnectedUsers();
		});
	}

	@Override
	public void onMessageReceived(String message, User user) {
		System.out.println("Server received: " + message);
		
		// Strip the [clientInfo] prefix that ClientHandler adds
		String cleanMessage = message;
		int bracketIndex = message.indexOf("] ");
		if (bracketIndex != -1) {
			cleanMessage = message.substring(bracketIndex + 2);
		}
		
		// Skip USERS_LIST broadcasts - they're handled separately
		if (cleanMessage.startsWith("USERS_LIST:")) {
			//
			return;
		}
		
		try {
			// Parse as JSON to check message type
			JsonObject jsonObj = JsonParser.parseString(cleanMessage).getAsJsonObject();
			
			// CHECK 1: Is this a notification request from client?
			if (jsonObj.has("messageType") && jsonObj.get("messageType").getAsString().equals("send_notification")) {
				System.out.println("\n🔔 RECEIVED NOTIFICATION REQUEST FROM CLIENT");
				
				try {
					String recipientId = jsonObj.get("recipientId").getAsString();
					JsonObject notificationObj = jsonObj.getAsJsonObject("notification");
					Notification notification = GsonUtility.getGson().fromJson(notificationObj, Notification.class);
					
					System.out.println("   From: " + notification.getSenderName());
					System.out.println("   Type: " + notification.getType());
					System.out.println("   To: " + recipientId);
					
					// Now broadcast to recipient if online
					sendNotificationToUser(recipientId, notification);
					return;
				} catch (Exception e) {
					System.err.println("❌ Error processing notification request: " + e.getMessage());
					e.printStackTrace();
					return;
				}
			}
			
			// CHECK 2: Try to parse as regular MessageData
			MessageData msgData = GsonUtility.getGson().fromJson(cleanMessage, MessageData.class);
			
			// Check if this is a GROUP message (recipientId is null) or DIRECT message (recipientId is not null)
			if (msgData.recipientId != null) {
				// DIRECT MESSAGE - route to single recipient
				System.out.println("DIRECT MESSAGE - Looking for recipient: " + msgData.recipientId + " (or name: " + msgData.recipientName + ")");
				System.out.println("Connected users count: " + ConnectedUsers.size());
				
				for (ClientHandler handler : ConnectedUsers) {
					User handlerUser = handler.getUser();
					if (handlerUser != null) {
						String userId = handlerUser.getId() != null ? handlerUser.getId().toString() : null;
						String username = handlerUser.getUsername();
						System.out.println("  Checking user: id=" + userId + ", username=" + username);
						
						// Try to match by ID first, then fall back to username
						if ((userId != null && userId.equals(msgData.recipientId)) ||
							(username != null && username.equals(msgData.recipientId)) ||
							(username != null && username.equals(msgData.recipientName))) {
							System.out.println("MATCH FOUND! Routing message from " + user.getUsername() + " to " + username);
							handler.sendMessage(cleanMessage);  // Send the clean JSON without the prefix
							break;
						}
					}
				}
			} else if (msgData.conversationId != null) {
				// GROUP MESSAGE - broadcast to all participants in the group
				System.out.println("GROUP MESSAGE - Looking for conversation: " + msgData.conversationId);
				
				try {
					// Get the conversation to find all participants
					ConversationDAO conversationDAO = new ConversationDAO();
					Conversation conversation = conversationDAO.getConversationById(new ObjectId(msgData.conversationId));
					
					if (conversation != null && conversation.getParticipantIds() != null) {
						List<String> participantIds = conversation.getParticipantIds();
						System.out.println("Found " + participantIds.size() + " participants in group");
						
						// Send to all connected participants
						for (ClientHandler handler : ConnectedUsers) {
							User handlerUser = handler.getUser();
							if (handlerUser != null) {
								String userId = handlerUser.getId() != null ? handlerUser.getId().toString() : null;
								
								// Send to all participants except the sender
								if (userId != null && participantIds.contains(userId) && !userId.equals(msgData.senderId)) {
									System.out.println("  Broadcasting to group member: " + handlerUser.getUsername());
									handler.sendMessage(cleanMessage);
								}
							}
						}
					} else {
						System.err.println("Conversation not found or has no participants: " + msgData.conversationId);
					}
				} catch (Exception e) {
					System.err.println("Error handling group message: " + e.getMessage());
					e.printStackTrace();
				}
			} else {
				System.err.println("Message has neither recipientId (direct) nor conversationId (group)");
			}
		} catch (Exception e) {
			// If it's not a valid MessageData, it might be another type of message
			System.err.println("Error parsing message as MessageData: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
public void onConnectionClosed(String reason, User user) {
    // MUST use Platform.runLater because this is called from ClientHandler thread
    Platform.runLater(() -> {
        // 1. Remove from the Session list (Used for UI data)
        UserSession.getConnectedUsers().removeIf(u -> 
            u.getUsername() != null && u.getUsername().equals(user.getUsername()));

        // 2. FIX: Remove from the active handlers list
        // DON'T use handler.isAlive(). Use the username or the object itself.
        ConnectedUsers.removeIf(handler -> 
            handler.getUser() != null && handler.getUser().getUsername().equals(user.getUsername()));

        System.out.println("Client has disconnected: " + user.getUsername());
        
        // 3. Broadcast the corrected list to everyone else
        broadcastConnectedUsers();
    });
}
    
    



	// Called when the FX application shuts down
	public void shutdown() {
		stopServer();
	}

	// Return list of connected usernames
	public List<String> getConnectedUserNames() {
		List<String> names = new ArrayList<>();
		for (ClientHandler h : ConnectedUsers) {
			names.add(h.getUser().toString());
		}
		return names;
	}

	// Print connected users to console (useful for debugging)
	public void printConnectedUsers() {
		System.out.println("Connected users: " + getConnectedUserNames());
	}

	@FXML
	private void handleShowConnectedUsers() {
		printConnectedUsers();
	}

	// Broadcast the list of connected users to all clients via socket
	private void broadcastConnectedUsers() {
		List<User> connectedUsersList = UserSession.getConnectedUsers();
		String jsonUsers = GsonUtility.getGson().toJson(connectedUsersList);
		String message = "USERS_LIST:" + jsonUsers;
		
		// Send to all connected clients
		for (ClientHandler handler : ConnectedUsers) {
			handler.sendMessage(message);
		}
		
		System.out.println("Broadcasted users list: " + message);
	}

	/**
	 * Send a notification to a specific connected user via socket
	 * @param recipientUserId The ID of the user to send notification to
	 * @param notification The Notification object to send
	 * @return true if notification was sent successfully
	 */
	public static boolean sendNotificationToUser(String recipientUserId, genki.models.Notification notification) {
		try {
			System.out.println("\n🔔 SENDING NOTIFICATION TO USER");
			System.out.println("   Recipient: " + recipientUserId);
			System.out.println("   Type: " + notification.getType());
			System.out.println("   From: " + notification.getSenderName());
			
			// Add a messageType field to identify this as a notification
			// We'll wrap it in a JSON object with messageType
			com.google.gson.JsonObject notificationWrapper = new com.google.gson.JsonObject();
			notificationWrapper.addProperty("messageType", "notification");
			
			// Serialize the notification and add it to wrapper
			String notificationJson = GsonUtility.getGson().toJson(notification);
			com.google.gson.JsonObject notificationObj = com.google.gson.JsonParser.parseString(notificationJson).getAsJsonObject();
			notificationWrapper.add("notification", notificationObj);
			
			String wrappedMessage = notificationWrapper.toString();
			System.out.println("   Message: " + wrappedMessage.substring(0, Math.min(150, wrappedMessage.length())));
			
			// Find the recipient in connected users
			System.out.println("   Searching " + ConnectedUsers.size() + " connected users...");
			for (ClientHandler handler : ConnectedUsers) {
				User handlerUser = handler.getUser();
				if (handlerUser != null) {
					String userId = handlerUser.getId() != null ? handlerUser.getId().toString() : null;
					
					if (userId != null && userId.equals(recipientUserId)) {
						System.out.println("✓ Found recipient: " + handlerUser.getUsername());
						System.out.println("✓ Sending via socket...");
						handler.sendMessage(wrappedMessage);
						System.out.println("✅ Notification sent successfully\n");
						return true;
					}
				}
			}
			
			System.out.println("⚠️ Recipient not connected (will be loaded from DB on login)\n");
			return false;
		} catch (Exception e) {
			System.err.println("❌ Error sending notification: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// public void openClientWindows() {
	// FXMLLoader loader = new
	// FXMLLoader(getClass().getResource("clientPage.fxml"));
	// Parent root;
	// try {
	//
	// root = loader.load();
	// clientSocketController ctrl = loader.getController();
	// Stage stage = new Stage();
	// stage.setScene(new Scene(root));
	// stage.show();
	// ctrl.in
	// stage.setOnCloseRequest(event ->{
	// Platform.runLater(() -> {
	//// server.appendText("\n" + "Client has been Disconnected.....");
	// });
	// });
	//
	//
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	//
}
