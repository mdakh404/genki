package genki.controllers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import genki.network.ClientHandler;
import genki.network.MessageListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ServerSocketController implements MessageListener{

	private ServerSocket serverSocket;
	private Thread serverAcceptThread;
	private List<ClientHandler> ConnectedUsers = new ArrayList<>();
	private final int port = 5001 ;
	
	
	public void initialize() {
		//
	}
	
	
	public void startStopServer() {
		try {
			if(serverSocket == null || serverSocket.isClosed()) {
				startServer();
			}
			else {
				stopServer();
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void startServer() throws IOException {
		try {
		this.serverSocket = new ServerSocket(port);
		
		serverAcceptThread = new Thread(this::runServerAcceptLoop);
		serverAcceptThread.setDaemon(true);
		serverAcceptThread.start();
		}catch(Exception e) {
			
		}
	}
	
	public void runServerAcceptLoop() {
		try {
			while(!serverSocket.isClosed()) {
				Socket client = serverSocket.accept();
				System.out.println("New client has been connected... " );
				ClientHandler handler = new ClientHandler(client, this);
				ConnectedUsers.add(handler);
				System.out.println(ConnectedUsers);
				handler.start();
			}
			
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public void stopServer() {
		
		for (ClientHandler client:ConnectedUsers) {
			client.closeConnection();
		}
		ConnectedUsers.clear();
		if(serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
				System.out.println("Server has been ShutDown ..");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	@FXML
    private void handleSendMessageButton() {
//        String message; //= messageInput.getText();
//        if (message == null || message.trim().isEmpty()) return;
//
//        // Display the message locally
//        
//        
//        // Broadcast the message to all active clients
//        for (ClientHandler handler : ConnectedUsers) {
//            handler.sendMessage(message);
//        }
//        
////        messageInput.clear();
    }

	@Override
    public void onMessageReceived(String message) {
        // MUST use Platform.runLater because this method is called by the ClientHandler's background thread
        System.out.println("Server dit : "+message);
        for(ClientHandler ServerThread : ConnectedUsers) {
        	ServerThread.sendMessage(message);
        }
    }

	@Override
    public void onConnectionClosed(String reason) {
        // MUST use Platform.runLater because this method is called by the ClientHandler's background thread
        Platform.runLater(() -> {
            
            
            // Clean up the list of active handlers (remove the one that closed)
            ConnectedUsers.removeIf(handler -> !handler.isAlive());
        });
    }

    // Called when the FX application shuts down
    public void shutdown() {
        stopServer();
    }
    
    public void openClientWindows() {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("clientPage.fxml"));
        Parent root;
		try {
			
			root = loader.load();
			clientSocketController ctrl = loader.getController();
		    Stage stage = new Stage();
		    stage.setScene(new Scene(root));
		    stage.show();
		    ctrl.initialiseClient();
		    stage.setOnCloseRequest(event ->{
		    	Platform.runLater(() -> {
//		    		server.appendText("\n" + "Client has been Disconnected.....");
		    	});
		    });
		    
			  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
       
    }
    
    
	
	
}
