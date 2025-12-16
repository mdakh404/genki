package genki.network;

//File: ClientHandler.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
 private final Socket socket;
 private final MessageListener listener; // The ServerController object
 
 private BufferedReader in;
 private PrintWriter out;
 private volatile boolean isRunning = true; 

 public ClientHandler(Socket socket, MessageListener listener) {
     this.socket = socket;
     this.listener = listener;
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
         
         // This line blocks until a message is received or the socket closes.
         while (isRunning && (line = in.readLine()) != null) {
             // When a message arrives, send it back to the controller via the listener.
             listener.onMessageReceived("[" + clientInfo + "] " + line);
         }

     } catch (IOException e) {
         if (isRunning) { 
             listener.onConnectionClosed("[" + clientInfo + "] disconnected: " + e.getMessage());
         }
     } finally {
         closeConnection();
     }
 }
 
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
}