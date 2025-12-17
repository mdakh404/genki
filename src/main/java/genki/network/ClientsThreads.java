package genki.network;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ClientsThreads {
    private final String host;
    private final int port;
    private final MessageListener listener; 
    
    private Socket socket;
    private PrintWriter sender;
    private Thread receiverThread; 
    private volatile boolean receiving = false; // Simple flag to control the loop
    private final CountDownLatch counterLatch = new CountDownLatch(1);
    private final CountDownLatch senderReadyLatch = new CountDownLatch(1);

    public ClientsThreads(String host, int port, MessageListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }
    
    
    // --- Public Methods for the Controller to Call ---

    public void connect() {
        // Run connection logic in a separate thread too, as it can also block
        new Thread(() -> {
            try {
                this.socket = new Socket(host, port);
                counterLatch.countDown();
                sender = new PrintWriter(socket.getOutputStream(), true);
                senderReadyLatch.countDown(); // Signal that sender is ready
                // Do not send a stray hello; client will send its username explicitly.
                
                startReceiverThread(); // Start the simplified listener thread
                
                listener.onMessageReceived("--- Connected to Server: " + host + ":" + port + " ---");

            } catch (IOException e) {
                listener.onConnectionClosed("Failed to connect: " + e.getMessage());
            }
        }).start();
    }

    public void sendMessage(String message) {
    	try {
			senderReadyLatch.await(); // Wait until sender is fully ready
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        if (sender != null) {
            sender.println(message);
        }
    }
    
    public String getUserInfo() {
    	try {
			counterLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (socket == null) return "Not connected";
        return socket.getLocalAddress().getHostName() 
             + " | " + socket.getLocalAddress().getHostAddress()
             + " : " + socket.getLocalPort();
    }


    public void disconnect() {
        this.receiving = false; // Set flag to stop the loop
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    // --- Simplified Receiver Thread (No JavaFX Task) ---

    private void startReceiverThread() {
        this.receiving = true;
        // Create a new Thread and provide its job using a simple lambda (Runnable)
        receiverThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                
                // The continuous loop is controlled by the simple 'receiving' flag
                while (receiving && (line = reader.readLine()) != null) {
                    final String receivedMessage = line;
                    
                    // Crucial: Use Platform.runLater to safely update the UI
                    Platform.runLater(() -> listener.onMessageReceived("Server: " + receivedMessage));
                }
            } catch (IOException e) {
                if (receiving) { // Only report error if not cancelled by disconnect()
                    Platform.runLater(() -> listener.onConnectionClosed("Connection lost: " + e.getMessage()));
                }
            }
            // After loop exits (due to disconnect or error), ensure resources are closed
            disconnect();
        });
        
        // Start the background thread
        receiverThread.setDaemon(true); // Allow application to exit
        receiverThread.start(); 
    }
}