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
			ConnectedUsers.add(handler);
			UserSession.getConnectedUsers().add(handler.getUser());
			System.out.println("Registered user: " + handler.getUser().getUsername());
			System.out.println("Connected2222 : " + UserSession.getConnectedUsers());
			//printConnectedUsers();
			broadcastConnectedUsers();
		});
	}

	@Override
	public void onMessageReceived(String message, User user) {
	    System.out.println("Server received: " + message);
	    
	    // Nettoyage du message (suppression du préfixe [clientInfo])
	    String cleanMessage = message;
	    int bracketIndex = message.indexOf("] ");
	    if (bracketIndex != -1) {
	        cleanMessage = message.substring(bracketIndex + 2);
	    }
	    
	    // ---------------------------------------------------------
	    // 1. DÉTECTION DU MESSAGE GLOBAL (BROADCAST)
	    // ---------------------------------------------------------
	 // Dans ServerSocketController.java, remplacez la boucle de broadcast :
	    if (cleanMessage.startsWith("BROADCAST_MSG:")) {
	        String content = cleanMessage.substring("BROADCAST_MSG:".length());
	        
	        // On crée un objet MessageData formatté en JSON
	        MessageData broadcastData = new MessageData();
	        broadcastData.senderName = "ADMIN";
	        broadcastData.messageText = content;
	        broadcastData.recipientId = "ALL"; // Pour indiquer que c'est global
	        
	        String jsonMessage = GsonUtility.getGson().toJson(broadcastData);
	        
	        for (ClientHandler handler : ConnectedUsers) {
	            // On envoie le JSON avec un préfixe reconnaissable
	            handler.sendMessage("SERVER_BROADCAST:" + jsonMessage);
	        }
	        return;
	    }

	    // ---------------------------------------------------------
	    // 2. FILTRE DE SÉCURITÉ (USERS_LIST)
	    // ---------------------------------------------------------
	    if (cleanMessage.startsWith("USERS_LIST:")) {
	        System.out.println("Ignoring USERS_LIST broadcast (internal system)");
	        return;
	    }
	    
	    // ---------------------------------------------------------
	    // 3. ROUTAGE DES MESSAGES PRIVÉS (JSON MessageData)
	    // ---------------------------------------------------------
	    try {
	        // Tentative de parsing en MessageData (objet Gson)
	        MessageData msgData = GsonUtility.getGson().fromJson(cleanMessage, MessageData.class);
	        
	        if (msgData.recipientId != null || msgData.recipientName != null) {
	            System.out.println("Routing private message to: " + msgData.recipientName);
	            
	            for (ClientHandler handler : ConnectedUsers) {
	                User handlerUser = handler.getUser();
	                if (handlerUser != null) {
	                    String userId = handlerUser.getId() != null ? handlerUser.getId().toString() : null;
	                    String username = handlerUser.getUsername();
	                    
	                    // Match par ID, Username ou recipientName
	                    if ((userId != null && userId.equals(msgData.recipientId)) ||
	                        (username != null && username.equals(msgData.recipientId)) ||
	                        (username != null && username.equals(msgData.recipientName))) {
	                        
	                        handler.sendMessage(cleanMessage); 
	                        System.out.println("Message routed successfully.");
	                        break; 
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        // Si ce n'est ni un broadcast, ni un message privé valide
	        System.err.println("Unrecognized message format or parsing error: " + e.getMessage());
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
