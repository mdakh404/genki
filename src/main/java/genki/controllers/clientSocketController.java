package genki.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import genki.models.Conversation;
import genki.models.User;
import genki.models.MessageData;
import genki.network.ClientHandler;
import genki.network.ClientsThreads;
import genki.network.t2;
import genki.utils.GsonUtility;
import genki.utils.UserSession;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class clientSocketController implements t2 {

    private ClientsThreads ClientThread;
    private String username;
    private List<User> connectedUsers = new ArrayList<>();
    private Consumer<MessageData> onNewMessageCallback;

    public clientSocketController(String username) {
        ClientThread = new ClientsThreads("127.0.0.1", 5001, this);
        this.username = username;
        ClientThread.connect();
        ClientThread.sendMessage(this.username);
    }

    public String getUser() {
        return this.username;
    }

    public void setOnNewMessageCallback(Consumer<MessageData> callback) {
        this.onNewMessageCallback = callback;
    }

    public void sendMessages(String message) {
        ClientThread.sendMessage(message);
    }

    @Override
    public void onMessageReceived(String message) {
        Platform.runLater(() -> {
            System.out.println("Client received raw: " + message);

            // 1. GESTION DU BROADCAST ADMIN (Reçu en JSON via SERVER_BROADCAST)
            if (message.contains("SERVER_BROADCAST:")) {
                try {
                    String jsonPart = message.substring(message.indexOf("SERVER_BROADCAST:") + 17);
                    MessageData msgData = GsonUtility.getGson().fromJson(jsonPart, MessageData.class);

                    System.out.println("DEBUG: Broadcast Admin reçu -> " + msgData.messageText);

                    // Mise à jour de la session pour inclure la conversation ADMIN si elle n'existe pas
                    boolean exists = UserSession.getConversations().stream()
                            .anyMatch(c -> "ADMIN_SYSTEM".equals(c.getLastMessageSenderId()));

                    if (!exists) {
                        Conversation adminConv = new Conversation();
                        adminConv.setLastMessageSenderId("ADMIN_SYSTEM");
                        adminConv.setLastMessageContent(msgData.messageText);
                        UserSession.addConversation(adminConv);
                    }

                    // On envoie le message au callback pour qu'il s'affiche dans le chat
                    if (onNewMessageCallback != null) {
                        onNewMessageCallback.accept(msgData);
                    }
                    return; // Fin du traitement pour ce message
                } catch (Exception e) {
                    System.err.println("Erreur parsing Broadcast: " + e.getMessage());
                }
            }

            // 2. NETTOYAGE DU MESSAGE POUR LES AUTRES FLUX (USERS_LIST ou PRIVÉ)
            String payload = message.startsWith("Server: ") ? message.substring(8).trim() : message.trim();

            if (payload.startsWith("---")) return;

            // 3. MISE À JOUR DE LA LISTE DES UTILISATEURS CONNECTÉS
            if (payload.startsWith("USERS_LIST:")) {
                String jsonPart = payload.substring("USERS_LIST:".length());
                parseAndUpdateUsersList(jsonPart);
                return;
            }

            // 4. RÉCEPTION DES MESSAGES PRIVÉS STANDARDS
            try {
                if (payload.startsWith("{")) {
                    MessageData msgData = GsonUtility.getGson().fromJson(payload, MessageData.class);
                    if (msgData != null && onNewMessageCallback != null) {
                        onNewMessageCallback.accept(msgData);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur parsing JSON message: " + e.getMessage());
            }
        });
    }

    private void parseAndUpdateUsersList(String jsonUsers) {
        try {
            User[] users = GsonUtility.getGson().fromJson(jsonUsers, User[].class);
            connectedUsers = Arrays.asList(users);
            
            // Mise à jour de la session globale
            UserSession.setConnectedUsers(new ArrayList<>(connectedUsers));
            
            // Mise à jour visuelle des indicateurs de statut (cercles verts/gris)
            for (HBox conversationItem : UserSession.getConversationItems()) {
                Object userData = conversationItem.getUserData();
                if (userData instanceof User) {
                    User friend = (User) userData;
                    boolean isOnline = connectedUsers.stream()
                        .anyMatch(u -> u.getUsername() != null && u.getUsername().equals(friend.getUsername()));
                    updateConversationItemOnlineStatus(conversationItem, isOnline);
                }
            }
            System.out.println("Updated connected users count: " + connectedUsers.size());
        } catch (Exception e) {
            System.err.println("Error parsing users list: " + e.getMessage());
        }
    }

    private void updateConversationItemOnlineStatus(HBox conversationItem, boolean isOnline) {
        try {
            StackPane profileContainer = (StackPane) conversationItem.getChildren().get(0);
            if (profileContainer.getChildren().size() > 1) {
                Circle statusCircle = (Circle) profileContainer.getChildren().get(1);
                statusCircle.setFill(isOnline ? Color.web("#4ade80") : Color.web("#9ca3af"));
            }
        } catch (Exception e) {
            // Ignorer si la structure de l'interface n'est pas encore prête
        }
    }

    @Override
    public void onConnectionClosed(String reason) {
        System.out.println("Connexion fermée : " + reason);
    }

    @Override
    public void onClientConnected(ClientHandler handler) {
        // Logique serveur non requise ici
    }
}