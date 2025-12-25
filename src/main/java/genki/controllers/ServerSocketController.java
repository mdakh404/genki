package genki.controllers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import genki.models.User;
import genki.models.MessageData;
import genki.network.ClientHandler;
import genki.network.MessageListener;
import genki.utils.GsonUtility;
import genki.utils.UserSession;
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
			System.out.println("Ignoring USERS_LIST broadcast");
			return;
		}
		
		try {
			// Try to parse as MessageData (regular message)
			MessageData msgData = GsonUtility.getGson().fromJson(cleanMessage, MessageData.class);
			System.out.println("Looking for recipient: " + msgData.recipientId + " (or name: " + msgData.recipientName + ")");
			
			// Find the recipient and send the message to them
			if (msgData.recipientId != null) {
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
			}
		} catch (Exception e) {
			// If it's not a valid MessageData, it might be another type of message
			System.err.println("Error parsing message as MessageData: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void onConnectionClosed(String reason, User user) {
		// MUST use Platform.runLater because this method is called by the
		// ClientHandler's background thread
		Platform.runLater(() -> {
			// Clean up the list of active handlers (remove the one that closed)
			// Compare by username since User.equals() is not properly implemented
			UserSession.getConnectedUsers().removeIf(client -> 
				client.getUsername() != null && client.getUsername().equals(user.getUsername()));
			ConnectedUsers.removeIf(handler -> !handler.isAlive());
			System.out.println("Client had disconnected : " + user.getUsername());
			System.out.println(UserSession.getConnectedUsers());
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
