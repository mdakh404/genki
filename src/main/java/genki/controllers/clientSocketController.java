package genki.controllers;

import java.util.ArrayList;
import java.util.List;

import genki.network.ClientHandler;
import genki.network.ClientsThreads;
import genki.network.MessageListener;
import genki.network.t2;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class clientSocketController implements t2{

	
	private ClientsThreads ClientThread;
	private String username;
	
	
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
		});
		
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
