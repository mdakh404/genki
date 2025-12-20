package genki.controllers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import genki.models.User;
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
		System.out.println("Server recievied : " + message);
		for (ClientHandler handler : ConnectedUsers) {
			if (handler.getUser().equals(user)) {
				System.out.println("message recieved from : " + user.getUsername());
			}
		}
	}

	@Override
	public void onConnectionClosed(String reason, User user) {
		// MUST use Platform.runLater because this method is called by the
		// ClientHandler's background thread
		Platform.runLater(() -> {
			// Clean up the list of active handlers (remove the one that closed)
			UserSession.getConnectedUsers().removeIf(client -> client.equals(user));
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
