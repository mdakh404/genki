package genki.network;

//File: ClientHandler.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.bson.Document;

import genki.models.User;
import genki.utils.FindUsers;
import genki.utils.UserDAO;

public class ClientHandler extends Thread {
 private final Socket socket;
 private final MessageListener listener; // The ServerController object
 
 private BufferedReader in;
 private PrintWriter out;
 private volatile boolean isRunning = true; 
 private User user;
 private UserDAO userMethods;

 public ClientHandler(Socket socket, MessageListener listener) {
     this.socket = socket;
     this.listener = listener;
     user = null;
     userMethods = new UserDAO();
 }
 public ClientHandler(Socket socket, User user  , MessageListener listener) {
     this.socket = socket;
     this.listener = listener;
     this.user = user;
 }

 

 /**
  * Sets up the input/output streams for communication.
  */
 public void setupStreams() throws IOException {
     if (in == null || out == null) {
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         // The 'true' argument enables auto-flushing
         out = new PrintWriter(socket.getOutputStream(), true);
     }
 }

 /**
  * Sends a message to the connected client. (Non-blocking)
  */
 public void sendMessage(String message) {
     if (out != null) {
         out.println(message);
     }
 }

 /**
  * Contains the continuous, blocking listener loop. Runs on a separate thread.
  */
 @Override
 public void run() {
     String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
     try {
         setupStreams(); 
         String line;
         String Username = in.readLine();
         
         // Create FindUsers object with the username to fetch all user info
         try {
             Document userDoc = userMethods.getUserByUsername(Username);
             this.user = userMethods.documentToUser(userDoc);
         } catch (Exception e) {
             System.out.println("User not found or error fetching user: " + Username);
             closeConnection();
             return;
         }
         
            // Notify the server that this handler has received the client's username
            try {
                listener.onClientConnected(this);
            } catch (Exception ex) {
                // listener implementations should handle exceptions; log locally
                System.out.println("Error notifying server of new client: " + ex.getMessage());
            }
         
         // This line blocks until a message is received or the socket closes.
         while (isRunning && (line = in.readLine()) != null) {
             // When a message arrives, send it back to the controller via the listener.
             listener.onMessageReceived("[" + clientInfo + "] " + line, this.user);
         }

     } catch (IOException e) {
         if (isRunning) { 
             listener.onConnectionClosed("[" + clientInfo + "] disconnected: " + e.getMessage());
         }
     } finally {
         closeConnection();
     }
 }
 
 
//     public String getNom() {
//		return nom;
//	}
//
//	 public void setNom(String name) {
//		this.nom = name;
//	 }
 /**
  * Closes all resources gracefully and stops the run loop.
  */
 public void closeConnection() {
     this.isRunning = false;
     try {
         if (socket != null && !socket.isClosed()) {
             socket.close();
         }
         if (in != null) in.close();
         if (out != null) out.close();
     } catch (IOException ignored) {}
 }

 
 


 public User getUser() {
	return user;
}
 public void setUser(User user) {
	this.user = user;
 }
 @Override
 public String toString() {
	return this.user.toString();
 }
 
 
}



