package genki.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import genki.models.User;
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

public class clientSocketController implements t2{

	
	private ClientsThreads ClientThread;
	private String username;
	private List<User> connectedUsers = new ArrayList<>();
	
	
	public clientSocketController(String username) {
		ClientThread = new ClientsThreads("127.0.0.1", 5001, this);
		this.username = username;
		ClientThread.connect();

		ClientThread.sendMessage(this.username);
	}
	
	public String getUser() {
		return this.username;
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
			
			// Check if this is a users list message
			if (message.startsWith("Server: USERS_LIST:")) {
				// Extract JSON from message
				String jsonPart = message.substring("Server: USERS_LIST:".length());
				parseAndUpdateUsersList(jsonPart);
				
			}
		});
	}
	
	private void parseAndUpdateUsersList(String jsonUsers) {
		try {
			User[] users = GsonUtility.getGson().fromJson(jsonUsers, User[].class);
			connectedUsers = Arrays.asList(users);
			
			// Update the session with the connected users
			UserSession.setConnectedUsers(new ArrayList<>(connectedUsers));
			
			System.out.println("Updated connected users: " + UserSession.ConnectedUsers);
			// You can now use connectedUsers list for UI updates
		} catch (Exception e) {
			System.err.println("Error parsing users list: " + e.getMessage());
			e.printStackTrace();
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
