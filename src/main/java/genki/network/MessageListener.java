package genki.network;

//File: MessageListener.java
public interface MessageListener {
 /**
  * Called by a ClientHandler when a message is received from a client.
  * @param message The text received.
  */
 void onMessageReceived(String message);

 /**
  * Called by a ClientHandler when a connection is closed unexpectedly.
  * @param reason A description of the connection error.
  */
 void onConnectionClosed(String reason);
}
