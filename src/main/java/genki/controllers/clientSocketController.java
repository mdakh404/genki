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
					
					System.out.println("Attempting to parse as MessageData");
					System.out.println("Message to parse: " + jsonMessage.substring(0, Math.min(100, jsonMessage.length())));
					MessageData msgData = GsonUtility.getGson().fromJson(jsonMessage, MessageData.class);
					System.out.println("Parsed MessageData: conversationId=" + msgData.conversationId + ", senderName=" + msgData.senderName);
					if (onNewMessageCallback != null) {
						System.out.println("Callback found, calling it");
						onNewMessageCallback.accept(msgData);
					} else {
						System.out.println("WARNING: onNewMessageCallback is null!");
					}
				} catch (Exception e) {
					System.err.println("Error parsing incoming message: " + e.getMessage());
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
			
			// Update online status for all conversation items
			for(HBox conversationItem : UserSession.getConversationItems()) {
				// Get the user data stored in the HBox
				Object userData = conversationItem.getUserData();
				if (userData instanceof User) {
					User friend = (User) userData;
					
					// Check if friend is in connected users list
					boolean isOnline = connectedUsers.stream()
						.anyMatch(u -> u.getUsername() != null && u.getUsername().equals(friend.getUsername()));
					
					// Update the online status indicator (status circle)
					updateConversationItemOnlineStatus(conversationItem, isOnline);
				}
			}
			
			System.out.println("Updated connected users: " + UserSession.ConnectedUsers);
			// You can now use connectedUsers list for UI updates
		} catch (Exception e) {
			System.err.println("Error parsing users list: " + e.getMessage());
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
