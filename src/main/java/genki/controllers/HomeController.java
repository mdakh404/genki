package genki.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.control.Alert;
import java.util.logging.Logger;

import genki.utils.UserSession;
import genki.utils.ConversationItemBuilder;
import genki.utils.DBConnection;
import genki.utils.UserDAO;
import genki.utils.MessageItemBuilder;
import org.bson.Document;

import java.util.logging.Level;
import java.io.IOException;
import java.util.List;

import genki.utils.MessageDAO;
import genki.models.Message;
import org.bson.types.ObjectId;
import genki.utils.ConversationDAO;

public class HomeController {

    private static final Logger logger = Logger.getLogger(HomeController.class.getName());

    @FXML
    private Button btnSettings;
    @FXML
    private Button btnAdd;
    @FXML
    private VBox rightSideContainer;
    @FXML
    private ImageView profilTrigger;
    @FXML
    private Label chatContactName;
    @FXML
    private ImageView UserProfil;
    @FXML
    private VBox AmisNameStatus;
    @FXML
    private VBox conversationListContainer;
    @FXML
    private VBox messagesContainer;
    @FXML
    private TextField messageInput;
    @FXML
    private ImageView messageProfil;
    @FXML
    private Label CurrentUsername;
    @FXML
    private Button btnSend;
    @FXML private ImageView rightProfileImage;
    @FXML private Label rightContactName;
    @FXML private Label rightContactTitle;
    @FXML private Label rightContactBio;

    private Boolean rightSideVisibilite = false;
    // Track the currently open conversation
    private ObjectId currentConversationId = null;

    @FXML
    public void initialize() {
        if (profilTrigger != null) {
            profilTrigger.setOnMouseClicked(e -> toggleRightPanel());
        }
        if (AmisNameStatus != null) {
            AmisNameStatus.setOnMouseClicked(e -> toggleRightPanel());
        }
        if (rightSideContainer != null) {
            rightSideContainer.setVisible(rightSideVisibilite);
            rightSideContainer.setManaged(rightSideVisibilite);
        }
        try {
            Image image = new Image(UserSession.getImageUrl(), 40, 40, false, true);
            UserProfil.setImage(image);
            UserProfil.setFitWidth(40);
            UserProfil.setFitHeight(40);
            UserProfil.setPreserveRatio(false);
            javafx.scene.shape.Circle userClip = new javafx.scene.shape.Circle(20, 20, 20);
            UserProfil.setClip(userClip);
            UserProfil.getStyleClass().add("avatar");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        CurrentUsername.setText(UserSession.getUsername());

        btnSend.setOnMouseClicked(e -> {
            String messageText = messageInput.getText();
            if (messageText == null || messageText.trim().isEmpty() || currentConversationId == null) {
                return;
            }
            String senderId = UserSession.getUserId();
            String senderName = UserSession.getUsername();

            // Add to UI immediately
            messagesContainer.getChildren().add(
                    MessageItemBuilder.createSentMessage("genki/img/user-default.png", senderName, messageText));
            messageInput.clear();

            // Save to DB in background
            new Thread(() -> {
                MessageDAO messageDAO = new MessageDAO();
                messageDAO.sendMessage(currentConversationId, senderId, senderName, messageText);
            }).start();
        });
        // Load conversations
        loadConversations();

        // Show some example messages dynamically
        if (messagesContainer != null) {
            messagesContainer.getChildren().clear();
            messagesContainer.getChildren().add(
                    MessageItemBuilder.createReceivedMessage(
                            "genki/img/user-default.png",
                            "Aimane Aboufadle",
                            "Long message text goes here, demonstrating a message received from another user."));
            messagesContainer.getChildren().add(
                    MessageItemBuilder.createSentMessage(
                            "genki/img/user-default.png",
                            "You",
                            "hhhhhhhhhhhhhh salam"));
        }
    }

    /**
     * Set the current conversation and show its messages
    */
    public void setCurrentConversation(ObjectId conversationId) {
        System.out.println("The Set method...");
        this.currentConversationId = conversationId;
        // Update chat header with friend's info using conversation participants
        try {
            String currentUserId = UserSession.getUserId();
            // Use a new DBConnection for fetching the conversation document
            DBConnection dbConnection = new DBConnection("genki_testing");
            org.bson.Document conversationDoc = dbConnection
                .getDatabase()
                .getCollection("Conversation")
                .find(new org.bson.Document("_id", conversationId))
                .first();
            if (conversationDoc != null && conversationDoc.containsKey("participantIds")) {
                java.util.List<?> participants = conversationDoc.getList("participantIds", Object.class);
                String friendIdStr = null;
                for (Object pid : participants) {
                    String pidStr = pid.toString();
                    if (!pidStr.equals(currentUserId)) {
                        friendIdStr = pidStr;
                        break;
                    }
                }
                if (friendIdStr != null) {
                    org.bson.types.ObjectId friendId = null;
                    try {
                        friendId = new org.bson.types.ObjectId(friendIdStr);
                    } catch (Exception e) {
                        System.out.println("Invalid ObjectId for friend: " + friendIdStr);
                    }
                    if (friendId != null) {
                        UserDAO userDAO = new UserDAO();
                        Document friendDoc = userDAO.getUserById(friendId);
                        if (friendDoc != null) {
                            String friendName = friendDoc.getString("username");
                            String photoUrl = friendDoc.getString("photo_url");
                            String bio = friendDoc.getString("bio");
                            String role = friendDoc.getString("role");

                            if (chatContactName != null)
                                chatContactName.setText(friendName != null ? friendName : "");
                            rightContactName.setText(friendName != null ? friendName : "");
                            rightContactBio.setText(bio != null ? bio : "");
                            rightContactTitle.setText(role != null ? role : "");
                            if (profilTrigger != null && photoUrl != null) {
                                try {
                                    Image friendImg = new Image(photoUrl, 40, 40, false, true);
                                    profilTrigger.setImage(friendImg);
                                    profilTrigger.setFitWidth(40);
                                    profilTrigger.setFitHeight(40);
                                    profilTrigger.setPreserveRatio(false);
                                    javafx.scene.shape.Circle friendClip = new javafx.scene.shape.Circle(20, 20, 20);
                                    profilTrigger.setClip(friendClip);
                                    profilTrigger.getStyleClass().add("avatar");
                                    if (rightProfileImage != null) {
                                        rightProfileImage.setImage(friendImg);
                                        rightProfileImage.setFitWidth(40);
                                        rightProfileImage.setFitHeight(40);
                                        rightProfileImage.setPreserveRatio(false);
                                        javafx.scene.shape.Circle rightClip = new javafx.scene.shape.Circle(20, 20, 20);
                                        rightProfileImage.setClip(rightClip);
                                        rightProfileImage.getStyleClass().add("avatar");
                                    }
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    profilTrigger.setImage(new Image("genki/img/user-default.png", 40, 40, false, true));
                                }
                            }
                        }
                    }
                }
            }
            System.out.println(chatContactName.getText() + profilTrigger.getImage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error updating chat header", e);
        }
        showConversationMessages(conversationId);
    }

    // Sending Messages

    /**
     * Fetch all messages for a conversation and display them in the UI
     * 
     * @param conversationId The ObjectId of the conversation
     */
    public void showConversationMessages(ObjectId conversationId) {
        if (messagesContainer == null || conversationId == null)
            return;
        messagesContainer.getChildren().clear();
        try {
            MessageDAO messageDAO = new MessageDAO();
            // Fetch all messages for the conversation, sorted by timestamp ascending
            com.mongodb.client.FindIterable<org.bson.Document> docs = messageDAO.getDatabase().getCollection("Message")
                    .find(new org.bson.Document("conversationId", conversationId))
                    .sort(new org.bson.Document("timestamp", 1));

            String currentUserId = genki.utils.UserSession.getUserId();
            for (org.bson.Document doc : docs) {
                String senderId = doc.getString("senderId");
                String senderName = doc.getString("senderName");
                String content = doc.getString("content");
                String photoUrl = "genki/img/user-default.png"; // Optionally fetch real photo
                if (senderId != null && senderId.equals(currentUserId)) {
                    messagesContainer.getChildren().add(
                            MessageItemBuilder.createSentMessage(photoUrl, senderName, content));
                } else {
                    messagesContainer.getChildren().add(
                            MessageItemBuilder.createReceivedMessage(photoUrl, senderName, content));
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading messages for conversation", e);
        }
    }

    @FXML
    private void handleSettingsBtnClick() {
        try {
            logger.log(Level.INFO, "Loading Settings.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/Settings.fxml"));
            Parent root = loader.load();

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            settingsStage.setResizable(false);
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            if (btnSettings != null && btnSettings.getScene() != null) {
                settingsStage.initOwner(btnSettings.getScene().getWindow());
            }
            settingsStage.setScene(new Scene(root));
            settingsStage.centerOnScreen();
            settingsStage.showAndWait();
        } catch (IOException loadingException) {
            logger.log(Level.WARNING, loadingException.getMessage());
            Alert failedLoadingAlert = new Alert(Alert.AlertType.ERROR, "Failed to load settings.fxml file.");
            failedLoadingAlert.showAndWait();
        }
    }

    @FXML
    public void AddUserOrGroup() {
        try {
            logger.log(Level.INFO, "Loading AddUserOrGroup.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/AddUserOrGroup.fxml"));
            Parent root = loader.load();

            AddUserOrGroupController controller = loader.getController();
            controller.setHomeController(this);

            Stage dialogStage = new Stage();
            try {
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/add_user_group.jpg"), 100, 100, true,
                        true);
                dialogStage.getIcons().add(logo);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load application logo", e);
            }
            dialogStage.setTitle("Add User Or Group");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (btnAdd != null && btnAdd.getScene() != null) {
                dialogStage.initOwner(btnAdd.getScene().getWindow());
            }
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading AddUserOrGroup dialog", e);
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to load AddUserOrGroup dialog.");
            errorAlert.showAndWait();
        }
    }

    public void AddUser() {
        try {
            logger.log(Level.INFO, "Loading AddUser.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/AddUser.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            try {
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/icone_add_user.jpg"), 128, 128, true,
                        true);
                dialogStage.getIcons().add(logo);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load application logo", e);
            }
            dialogStage.setTitle("Add New User");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root, 400, 300));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading AddUser dialog", e);
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to load AddUser dialog.");
            errorAlert.showAndWait();
        }
    }

    public void AddGroup() {
        try {
            logger.log(Level.INFO, "Loading AddGroup.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/AddGroup.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            try {
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/icone_add_group.jpg"), 128, 128, true,
                        true);
                dialogStage.getIcons().add(logo);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load application logo", e);
            }
            dialogStage.setTitle("Add New Group");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading AddGroup dialog", e);
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to load AddGroup dialog.");
            errorAlert.showAndWait();
        }
    }

    public void toggleRightPanel() {
        this.rightSideVisibilite = !rightSideVisibilite;

        if (rightSideContainer == null)
            return;

        if (rightSideVisibilite) {
            rightSideContainer.setManaged(true);
            rightSideContainer.setVisible(true);
            rightSideContainer.setPrefWidth(320.0);
            rightSideContainer.setMinWidth(320.0);
            rightSideContainer.setMaxWidth(320.0);
        } else {
            rightSideContainer.setPrefWidth(0.0);
            rightSideContainer.setMinWidth(0.0);
            rightSideContainer.setMaxWidth(0.0);
            rightSideContainer.setManaged(false);
            rightSideContainer.setVisible(false);
        }
    }

    // Example: Add a conversation dynamically
    public void addConversationExample() {
        HBox conversationItem = ConversationItemBuilder.createConversationItem(
                "url/to/image.png", // profileImageUrl
                "Sarah Wilson", // contactName
                "Sent a photo", // lastMessage
                "12:45 PM", // time
                2, // unreadCount
                true // isOnline
        );

        conversationListContainer.getChildren().add(conversationItem);
    }

    /**
     * Load conversations for the current user from the database
     */
    private void loadConversations() {
        try {
            UserDAO userDAO = new UserDAO();
            String currentUsername = UserSession.getUsername();

            // Get all friends for the current user
            List<Document> friends = userDAO.getFriendsForUser(currentUsername);

            if (friends == null || friends.isEmpty()) {
                logger.log(Level.INFO, "No friends found for user: " + currentUsername);
                return;
            }

            // For each friend, create a conversation item
            for (Document friendDoc : friends) {
                String friendName = friendDoc.getString("username");
                String photoUrl = friendDoc.getString("photo_url");
                String friendId;
                // Try to get the friend's user ID as a string (MongoDB _id is usually ObjectId)
                Object objId = friendDoc.get("_id");
                if (objId instanceof org.bson.types.ObjectId) {
                    friendId = ((org.bson.types.ObjectId) objId).toHexString();
                } else {
                    friendId = String.valueOf(objId);
                }
                String currentUserId = UserSession.getUserId();
                ConversationDAO conversationDAO = new ConversationDAO();
                ObjectId conversationId = conversationDAO.createDirectConversation(currentUserId, friendId);

                // Fetch last message from Conversation collection
                String lastMessage = "";
                String time = "";
                try {
                    DBConnection dbConnection = new DBConnection("genki_testing");
                    org.bson.Document conversationDoc = dbConnection
                        .getDatabase()
                        .getCollection("Conversation")
                        .find(new org.bson.Document("_id", conversationId))
                        .first();
                    if (conversationDoc != null) {
                        lastMessage = conversationDoc.getString("lastMessageContent");
                        Object lastMsgTimeObj = conversationDoc.get("lastMessageTime");
                        if (lastMsgTimeObj != null) {
                            // Try to format time as HH:mm if today, else show date
                            java.time.LocalDateTime msgTime = null;
                            if (lastMsgTimeObj instanceof java.time.LocalDateTime) {
                                msgTime = (java.time.LocalDateTime) lastMsgTimeObj;
                            } else if (lastMsgTimeObj instanceof java.util.Date) {
                                java.util.Date date = (java.util.Date) lastMsgTimeObj;
                                msgTime = java.time.LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
                            } else if (lastMsgTimeObj instanceof String) {
                                try {
                                    msgTime = java.time.LocalDateTime.parse((String) lastMsgTimeObj);
                                } catch (Exception ignore) {}
                            }
                            if (msgTime != null) {
                                java.time.LocalDate today = java.time.LocalDate.now();
                                if (msgTime.toLocalDate().equals(today)) {
                                    time = String.format("%02d:%02d", msgTime.getHour(), msgTime.getMinute());
                                } else {
                                    time = String.format("%02d/%02d/%02d", msgTime.getDayOfMonth(), msgTime.getMonthValue(), msgTime.getYear() % 100);
                                }
                            } else {
                                time = lastMsgTimeObj.toString();
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Error fetching last message: " + ex.getMessage());
                }

                int unreadCount = 0;
                boolean isOnline = true; // TODO: Get from presence system

                HBox conversationItem = ConversationItemBuilder.createConversationItem(
                        photoUrl != null ? photoUrl : "genki/img/user-default.png",
                        friendName,
                        lastMessage != null ? lastMessage : "",
                        time != null ? time : "",
                        unreadCount,
                        isOnline);

                // Add click handler to set current conversation
                conversationItem.setOnMouseClicked(e -> setCurrentConversation(conversationId));

                conversationListContainer.getChildren().add(conversationItem);
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading conversations", e);
        }
    }
}
