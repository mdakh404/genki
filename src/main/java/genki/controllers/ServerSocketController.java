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
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onClientConnected(ClientHandler handler) {
		Platform.runLater(() -> {
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
			
			broadcastConnectedUsers();
		});
	}

	@Override
	public void onMessageReceived(String message, User user) {
		System.out.println("Server received: " + message);
		
		String cleanMessage = message;
		int bracketIndex = message.indexOf("] ");
		if (bracketIndex != -1) {
			cleanMessage = message.substring(bracketIndex + 2);
		}
		
		if (cleanMessage.startsWith("USERS_LIST:")) {
			return;
		}
		
		try {
			JsonObject jsonObj = JsonParser.parseString(cleanMessage).getAsJsonObject();
			
			if (jsonObj.has("messageType") && jsonObj.get("messageType").getAsString().equals("send_notification")) {
				System.out.println("\n🔔 RECEIVED NOTIFICATION REQUEST FROM CLIENT");
				
				try {
					String recipientId = jsonObj.get("recipientId").getAsString();
					JsonObject notificationObj = jsonObj.getAsJsonObject("notification");
					Notification notification = GsonUtility.getGson().fromJson(notificationObj, Notification.class);
					
					System.out.println("   From: " + notification.getSenderName());
					System.out.println("   Type: " + notification.getType());
					System.out.println("   To: " + recipientId);
					
					sendNotificationToUser(recipientId, notification);
					return;
				} catch (Exception e) {
					System.err.println("❌ Error processing notification request: " + e.getMessage());
					e.printStackTrace();
					return;
				}
			}
			
			MessageData msgData = GsonUtility.getGson().fromJson(cleanMessage, MessageData.class);
			
			if (msgData.recipientId != null) {
				System.out.println("DIRECT MESSAGE - Looking for recipient: " + msgData.recipientId + " (or name: " + msgData.recipientName + ")");
				System.out.println("Connected users count: " + ConnectedUsers.size());
				
				for (ClientHandler handler : ConnectedUsers) {
					User handlerUser = handler.getUser();
					if (handlerUser != null) {
						String userId = handlerUser.getId() != null ? handlerUser.getId().toString() : null;
						String username = handlerUser.getUsername();
						System.out.println("  Checking user: id=" + userId + ", username=" + username);
						
						if ((userId != null && userId.equals(msgData.recipientId)) ||
							(username != null && username.equals(msgData.recipientId)) ||
							(username != null && username.equals(msgData.recipientName))) {
							System.out.println("MATCH FOUND! Routing message from " + user.getUsername() + " to " + username);
							handler.sendMessage(cleanMessage);
							break;
						}
					}
				}
			} else if (msgData.conversationId != null) {
				System.out.println("GROUP MESSAGE - Looking for conversation: " + msgData.conversationId);
				
				try {
					ConversationDAO conversationDAO = new ConversationDAO();
					Conversation conversation = conversationDAO.getConversationById(new ObjectId(msgData.conversationId));
					
					if (conversation != null && conversation.getParticipantIds() != null) {
						List<String> participantIds = conversation.getParticipantIds();
						System.out.println("Found " + participantIds.size() + " participants in group");
						
						for (ClientHandler handler : ConnectedUsers) {
							User handlerUser = handler.getUser();
							if (handlerUser != null) {
								String userId = handlerUser.getId() != null ? handlerUser.getId().toString() : null;
								
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
			System.err.println("Error parsing message as MessageData: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
public void onConnectionClosed(String reason, User user) {
    Platform.runLater(() -> {
        UserSession.getConnectedUsers().removeIf(u -> 
            u.getUsername() != null && u.getUsername().equals(user.getUsername()));

        ConnectedUsers.removeIf(handler -> 
            handler.getUser() != null && handler.getUser().getUsername().equals(user.getUsername()));

        System.out.println("Client has disconnected: " + user.getUsername());
        
        broadcastConnectedUsers();
    });
}
    
    public void shutdown() {
		stopServer();
	}

	public List<String> getConnectedUserNames() {
		List<String> names = new ArrayList<>();
		for (ClientHandler h : ConnectedUsers) {
			names.add(h.getUser().toString());
		}
		return names;
	}

	public void printConnectedUsers() {
		System.out.println("Connected users: " + getConnectedUserNames());
	}

	@FXML
	private void handleShowConnectedUsers() {
		printConnectedUsers();
	}

	private void broadcastConnectedUsers() {
		List<User> connectedUsersList = UserSession.getConnectedUsers();
		String jsonUsers = GsonUtility.getGson().toJson(connectedUsersList);
		String message = "USERS_LIST:" + jsonUsers;
		
		for (ClientHandler handler : ConnectedUsers) {
			handler.sendMessage(message);
		}
		
		System.out.println("Broadcasted users list: " + message);
	}

	public static boolean sendNotificationToUser(String recipientUserId, genki.models.Notification notification) {
		try {
			
			
			com.google.gson.JsonObject notificationWrapper = new com.google.gson.JsonObject();
			notificationWrapper.addProperty("messageType", "notification");
			
			String notificationJson = GsonUtility.getGson().toJson(notification);
			com.google.gson.JsonObject notificationObj = com.google.gson.JsonParser.parseString(notificationJson).getAsJsonObject();
			notificationWrapper.add("notification", notificationObj);
			
			String wrappedMessage = notificationWrapper.toString();
			System.out.println("   Message: " + wrappedMessage.substring(0, Math.min(150, wrappedMessage.length())));
			
			System.out.println("   Searching " + ConnectedUsers.size() + " connected users...");
			for (ClientHandler handler : ConnectedUsers) {
				User handlerUser = handler.getUser();
				if (handlerUser != null) {
					String userId = handlerUser.getId() != null ? handlerUser.getId().toString() : null;
					
					if (userId != null && userId.equals(recipientUserId)) {
						System.out.println("✓ Found recipient: " + handlerUser.getUsername());
						System.out.println("✓ Sending via socket...");
						handler.sendMessage(wrappedMessage);
						System.out.println(" Notification sent successfully\n");
						return true;
					}
				}
			}
			
			System.out.println(" Recipient not connected (will be loaded from DB on login)\n");
			return false;
		} catch (Exception e) {
			System.err.println("Error sending notification: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}
