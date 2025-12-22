package genki.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.geometry.Bounds;
import javafx.stage.Popup;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
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
import genki.models.MessageData;
import org.bson.types.ObjectId;
import genki.utils.ConversationDAO;

public class HomeController {

    private static final Logger logger = Logger.getLogger(HomeController.class.getName());

    @FXML
    private Button btnSettings;

    @FXML
    private Button btnAll;
    @FXML
    private Button btnUnread;
    @FXML
    private Button btnGroups;

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
    private ScrollPane messagesScrollPane;
    @FXML
    private TextField messageInput;

    @FXML
    private Button btnSend;
    @FXML
    private ImageView rightProfileImage;
    @FXML
    private Label rightContactName;
    @FXML
    private Label rightContactTitle;
    @FXML
    private Label rightContactBio;

    private Boolean rightSideVisibilite = false;
    // Track the currently open conversation
    private ObjectId currentConversationId = null;
    private String currentRecipientId = null;  // Track recipient for message sending
    private String currentRecipientName = null;  // Track recipient name for fallback matching

    @FXML
    private Button btnAdd;
    @FXML
    private VBox rightSideContainer;
    @FXML
    private ImageView profilTrigger;
    @FXML
    private VBox UserNameStatus;
    @FXML
    private ImageView messageProfil;
    @FXML
    private Label CurrentUsername;
    @FXML
    private Button btnNotifications;

    private Popup addMenuPopup;

    // CSS Style Constants - centralized styles to avoid hardcoding
    private static final String MENU_BUTTON_STYLE_DEFAULT = 
        "-fx-background-color: transparent; " +
        "-fx-text-fill: black; " +
        "-fx-cursor: hand; " +
        "-fx-padding: 5; " +
        "-fx-alignment: CENTER-LEFT; " +
        "-fx-font-size: 14px;";
    
    private static final String MENU_BUTTON_STYLE_HOVER = 
        MENU_BUTTON_STYLE_DEFAULT + "-fx-background-color: rgba(255, 255, 255, 0.1);";
    
    private static final String FILTER_BUTTON_ACTIVE_STYLE = 
        "-fx-background-color: #4a5fff; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 16;";
    
    private static final String FILTER_BUTTON_INACTIVE_STYLE = 
        "-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-background-radius: 20; -fx-padding: 8 16;";

    /**
     * 
     * IMPROVEMENT 1: Resource Management
     * Using DBConnection singleton pattern to avoid connection leaks
     */
    private static DBConnection dbConnectionInstance = null;

    private static synchronized DBConnection getDBConnection() {
        if (dbConnectionInstance == null) {
            dbConnectionInstance = new DBConnection("genki_testing");
        }
        return dbConnectionInstance;
    }

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
            // Load at 160x160 for better clarity when displaying at 40x40
            Image image = new Image(UserSession.getImageUrl(), 160, 160, false, true);
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
            String senderImageUrl = UserSession.getImageUrl();

            // Add to UI immediately
            messagesContainer.getChildren().add(
                    MessageItemBuilder.createSentMessage(senderImageUrl, senderName, messageText));
            messageInput.clear();
            // Auto-scroll to bottom
            scrollToBottom();
            
            // Create structured message data with all required info
            MessageData msgData = new MessageData(
                currentConversationId.toString(),
                senderId,
                senderName,
                messageText,
                senderImageUrl,
                System.currentTimeMillis(),
                currentRecipientId,  // Include recipient ID
                currentRecipientName  // Include recipient name for fallback
            );
            
            // Send message as JSON via socket
            String jsonMessage = genki.utils.GsonUtility.getGson().toJson(msgData);
            UserSession.getClientSocket().sendMessages(jsonMessage);

            // IMPROVEMENT 2: Thread Safety and UI Updates
            // Save to DB in background thread and update UI safely if needed
            new Thread(() -> {
                MessageDAO messageDAO = new MessageDAO();
                messageDAO.sendMessage(currentConversationId, senderId, senderName, senderImageUrl, messageText);
                
                // If you need to update UI after save (e.g., show success icon), use Platform.runLater()
                // Platform.runLater(() -> {
                //     // Update UI elements here
                // });
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

        // IMPROVEMENT 4: String Constants - Using style constants instead of hardcoded strings
        btnAll.setOnMouseClicked(e -> {
            btnAll.setStyle(FILTER_BUTTON_ACTIVE_STYLE);
            btnUnread.setStyle(FILTER_BUTTON_INACTIVE_STYLE);
            btnGroups.setStyle(FILTER_BUTTON_INACTIVE_STYLE);
        });
        
        btnUnread.setOnMouseClicked(e -> {
            btnUnread.setStyle(FILTER_BUTTON_ACTIVE_STYLE);
            btnAll.setStyle(FILTER_BUTTON_INACTIVE_STYLE);
            btnGroups.setStyle(FILTER_BUTTON_INACTIVE_STYLE);
        });

        btnGroups.setOnMouseClicked(e -> {
            btnGroups.setStyle(FILTER_BUTTON_ACTIVE_STYLE);
            btnUnread.setStyle(FILTER_BUTTON_INACTIVE_STYLE);
            btnAll.setStyle(FILTER_BUTTON_INACTIVE_STYLE);
        });

        // Register callback for incoming messages
        UserSession.getClientSocket().setOnNewMessageCallback(msgData -> {
            System.out.println("HomeController callback triggered!");
            System.out.println("Received message conversationId: " + msgData.conversationId);
            System.out.println("Current conversationId: " + currentConversationId);
            // Only add message if it's for the current conversation
            if (msgData.conversationId != null && 
                currentConversationId != null && 
                msgData.conversationId.equals(currentConversationId.toString())) {
                
                System.out.println("Match found! Adding message from " + msgData.senderName);
                messagesContainer.getChildren().add(
                    MessageItemBuilder.createReceivedMessage(
                        msgData.senderProfileImage,
                        msgData.senderName,
                        msgData.messageText
                    )
                );
                // Auto-scroll to bottom
                scrollToBottom();
            } else {
                System.out.println("No match: conversationId=" + msgData.conversationId + ", currentConversationId=" + currentConversationId);
            }
        });

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
            // IMPROVEMENT 1: Resource Management - Use singleton DBConnection instead of creating new instance
            DBConnection dbConnection = getDBConnection();
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
                // Store the recipient ID for message sending
                this.currentRecipientId = friendIdStr;
                
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
                            // Store the recipient name as well for fallback matching
                            this.currentRecipientName = friendName;
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
                                    // Load at 180x180 for better clarity when displaying at 43x43
                                    Image friendImg = new Image(photoUrl, 180, 180, false, true);
                                    profilTrigger.setImage(friendImg);
                                    profilTrigger.setFitWidth(43);
                                    profilTrigger.setFitHeight(43);
                                    profilTrigger.setPreserveRatio(false);
                                    javafx.scene.shape.Circle friendClip = new javafx.scene.shape.Circle(21.5, 21.5, 21.5);
                                    profilTrigger.setClip(friendClip);
                                    profilTrigger.getStyleClass().add("avatar");
                                    if (rightProfileImage != null) {
                                        // Load at 400x400 for the larger right panel image (100x100 display)
                                        Image rightImg = new Image(photoUrl, 400, 400, false, true);
                                        rightProfileImage.setImage(rightImg);
                                        rightProfileImage.setFitWidth(100);
                                        rightProfileImage.setFitHeight(100);
                                        rightProfileImage.setPreserveRatio(false);
                                        javafx.scene.shape.Circle rightClip = new javafx.scene.shape.Circle(50, 50, 50);
                                        rightProfileImage.setClip(rightClip);
                                        rightProfileImage.getStyleClass().add("avatar");
                                    }
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                    profilTrigger
                                            .setImage(new Image("genki/img/user-default.png", 180, 180, false, true));
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
                String senderImageUrl = doc.getString("senderImageUrl");
                if (senderImageUrl == null) {
                    senderImageUrl = doc.getString("photo_url"); // Fallback for legacy data
                }
                System.out.println("Image message url : " + senderImageUrl);
                if (senderId != null && senderId.equals(currentUserId)) {
                    messagesContainer.getChildren().add(
                            MessageItemBuilder.createSentMessage(senderImageUrl, senderName, content));
                } else {
                    messagesContainer.getChildren().add(
                            MessageItemBuilder.createReceivedMessage(senderImageUrl, senderName, content));
                }
            }
            // Auto-scroll to bottom after loading messages
            scrollToBottom();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading messages for conversation", e);
        }
    }

    /**
     * Scroll the messages container to the bottom to show the latest messages
     */
    private void scrollToBottom() {
        if (messagesScrollPane != null) {
            // Use runLater to ensure the scroll happens after layout updates
            Platform.runLater(() -> {
                messagesScrollPane.setVvalue(1.0);  // 1.0 = bottom of scroll pane
            });
        }
    }

    @FXML
    private void handleSettingsBtnClick() {
        try {
            logger.log(Level.INFO, "Loading Settings.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/Settings.fxml"));
            Parent root = loader.load();

            Stage settingsStage = new Stage();
            try {
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/setting.png"), 50, 50, true, true);
                settingsStage.getIcons().add(logo);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load application logo", e);
            }
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
        // Si le popup est déjà affiché, le fermer
        if (addMenuPopup != null && addMenuPopup.isShowing()) {
            addMenuPopup.hide();
            addMenuPopup = null;
            return;
        }

        // Créer le conteneur du menu
        VBox menuContainer = new VBox(5);
        menuContainer.setPadding(new Insets(10));
        menuContainer.setMaxWidth(100);
        menuContainer.setBackground(new Background(new BackgroundFill(
                Color.rgb(51, 213, 214),
                new CornerRadii(8),
                Insets.EMPTY)));

        // Ajouter une ombre
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        dropShadow.setRadius(10);
        dropShadow.setOffsetY(3);
        menuContainer.setEffect(dropShadow);

        // Créer le bouton "Add User"
        Button addUserBtn = new Button("Add User");
        addUserBtn.setPrefWidth(150);
        // IMPROVEMENT 4: String Constants - Using style constants instead of hardcoded strings
        addUserBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT);
        addUserBtn.setOnMouseEntered(e -> addUserBtn.setStyle(MENU_BUTTON_STYLE_HOVER));
        addUserBtn.setOnMouseExited(e -> addUserBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT));
        addUserBtn.setOnAction(e -> {
            addMenuPopup.hide();
            addMenuPopup = null;
            openAddUserDialog();
        });

        // Créer le bouton "Add Group"
        Button addGroupBtn = new Button("Add Group");
        addGroupBtn.setPrefWidth(150);
        // IMPROVEMENT 4: String Constants - Using style constants instead of hardcoded strings
        addGroupBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT);
        addGroupBtn.setOnMouseEntered(e -> addGroupBtn.setStyle(MENU_BUTTON_STYLE_HOVER));
        addGroupBtn.setOnMouseExited(e -> addGroupBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT));
        addGroupBtn.setOnAction(e -> {
            addMenuPopup.hide();
            addMenuPopup = null;
            openAddGroupDialog();
        });

        // ajouter ca
        // Créer le bouton "Join Group"
        Button joinGroupBtn = new Button("Join Group");
        joinGroupBtn.setPrefWidth(150);
        // IMPROVEMENT 4: String Constants - Using style constants instead of hardcoded strings
        joinGroupBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT);
        joinGroupBtn.setOnMouseEntered(e -> joinGroupBtn.setStyle(MENU_BUTTON_STYLE_HOVER));
        joinGroupBtn.setOnMouseExited(e -> joinGroupBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT));
        joinGroupBtn.setOnAction(e -> {
            addMenuPopup.hide();
            addMenuPopup = null;
            openJoinGroupDialog();
        });

        // Ajouter les boutons au conteneur
        menuContainer.getChildren().addAll(addUserBtn, addGroupBtn, joinGroupBtn);

        // Créer le popup
        addMenuPopup = new Popup();
        addMenuPopup.setAutoHide(true);
        addMenuPopup.getContent().add(menuContainer);

        // Calculer la position (sous le bouton btnAdd)
        Bounds bounds = btnAdd.localToScreen(btnAdd.getBoundsInLocal());
        addMenuPopup.show(btnAdd, bounds.getMinX(), bounds.getMaxY());
    }

    private void openAddUserDialog() {
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
            dialogStage.initOwner(btnAdd.getScene().getWindow());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root, 360, 260));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading AddUser dialog", e);
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to load AddUser dialog.");
            errorAlert.showAndWait();
        }
    }

    private void openAddGroupDialog() {
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
            dialogStage.initOwner(btnAdd.getScene().getWindow());
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

    // ajouter ca :
    private void openJoinGroupDialog() {
        try {
            logger.log(Level.INFO, "Loading AddGroup.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/JoinGroup.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            try {
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/icone_add_group.jpg"), 128, 128, true,
                        true);
                dialogStage.getIcons().add(logo);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load application logo", e);
            }
            dialogStage.setTitle("Join New Group");
            dialogStage.initOwner(btnAdd.getScene().getWindow());
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

    // ajouter ca :
    @FXML
    public void openNotifications() {
        try {
            logger.log(Level.INFO, "Loading Notifications.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/Notifications.fxml"));
            Parent root = loader.load();

            Stage notificationStage = new Stage();
            try {
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/notifications.jpg"), 50, 50, true,
                        true);
                notificationStage.getIcons().add(logo);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load notification icon", e);
            }

            notificationStage.setTitle("Notifications");
            notificationStage.setResizable(false);
            notificationStage.initModality(Modality.APPLICATION_MODAL);

            if (btnNotifications != null && btnNotifications.getScene() != null) {
                notificationStage.initOwner(btnNotifications.getScene().getWindow());
            }

            notificationStage.setScene(new Scene(root));
            notificationStage.centerOnScreen();
            notificationStage.showAndWait();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading Notifications dialog", e);
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to load Notifications dialog.");
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
     * 
     * IMPROVEMENT 3: Data Access Objects (DAO)
     * This method orchestrates the loading of conversations by delegating
     * data transformation to DAO classes where appropriate.
     */
    private void loadConversations() {
        try {
            UserDAO userDAO = new UserDAO();
            String currentUsername = UserSession.getUsername();

            // Get all friends for the current user
            List<Document> friends = userDAO.getFriendsForUser(currentUsername);

            // Convert Document friends to List<User>
            ArrayList<genki.models.User> userFriends = new ArrayList<>();
            if (friends != null) {
                for (Document friendDoc : friends) {
                    genki.models.User user = new genki.models.User();
                    user.setId(friendDoc.getObjectId("_id").toHexString());
                    user.setUsername(friendDoc.getString("username"));
                    user.setPhotoUrl(friendDoc.getString("photo_url"));
                    user.setBio(friendDoc.getString("bio"));
                    user.setRole(friendDoc.getString("role"));
                    userFriends.add(user);
                }
            }

            // Build conversations list from friends
            ArrayList<genki.models.Conversation> conversations = new ArrayList<>();
            if (friends != null) {
                String currentUserId = UserSession.getUserId();
                for (Document friendDoc : friends) {
                    genki.models.Conversation conversation = new genki.models.Conversation();
                    conversation.setType("direct");

                    // Set participant IDs
                    String friendId = friendDoc.getObjectId("_id").toHexString();
                    List<String> participantIds = new ArrayList<>();
                    participantIds.add(currentUserId);
                    participantIds.add(friendId);
                    conversation.setParticipantIds(participantIds);

                    // Set last message info
                    conversation.setLastMessageContent(friendDoc.getString("lastMessageContent"));
                    conversation.setLastMessageSenderId(friendDoc.getString("lastMessageSenderId"));

                    // Parse last message time
                    Object lastMsgTimeObj = friendDoc.get("lastMessageTime");
                    if (lastMsgTimeObj instanceof java.time.LocalDateTime) {
                        conversation.setLastMessageTime((java.time.LocalDateTime) lastMsgTimeObj);
                    } else if (lastMsgTimeObj instanceof java.util.Date) {
                        java.util.Date date = (java.util.Date) lastMsgTimeObj;
                        conversation.setLastMessageTime(java.time.LocalDateTime.ofInstant(date.toInstant(),
                                java.time.ZoneId.systemDefault()));
                    }

                    conversations.add(conversation);
                }
            }

            // Initialize UserSession static lists
            UserSession.loadConversations(userFriends, conversations);
            System.out.println("Conversations : "+ UserSession.getConversations());
            System.out.println("Friends : " + UserSession.getFriends());
            if (UserSession.getFriends() == null || UserSession.getFriends().isEmpty()) {
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
                    // IMPROVEMENT 1: Resource Management - Use singleton DBConnection
                    DBConnection dbConnection = getDBConnection();
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
                                msgTime = java.time.LocalDateTime.ofInstant(date.toInstant(),
                                        java.time.ZoneId.systemDefault());
                            } else if (lastMsgTimeObj instanceof String) {
                                try {
                                    msgTime = java.time.LocalDateTime.parse((String) lastMsgTimeObj);
                                } catch (Exception ignore) {
                                }
                            }
                            if (msgTime != null) {
                                java.time.LocalDate today = java.time.LocalDate.now();
                                if (msgTime.toLocalDate().equals(today)) {
                                    time = String.format("%02d:%02d", msgTime.getHour(), msgTime.getMinute());
                                } else {
                                    time = String.format("%02d/%02d/%02d", msgTime.getDayOfMonth(),
                                            msgTime.getMonthValue(), msgTime.getYear() % 100);
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
                boolean isOnline = false;
                
                 

                HBox conversationItem = ConversationItemBuilder.createConversationItem(
                        photoUrl != null ? photoUrl : "genki/img/user-default.png",
                        friendName,
                        lastMessage != null ? lastMessage : "",
                        time != null ? time : "",
                        unreadCount,
                        isOnline);

                // Store the friend User object in the HBox for later reference
                genki.models.User friendUser = new genki.models.User();
                friendUser.setId(friendId);
                friendUser.setUsername(friendName);
                friendUser.setPhotoUrl(photoUrl);
                conversationItem.setUserData(friendUser);

                // Add click handler to set current conversation
                conversationItem.setOnMouseClicked(e -> setCurrentConversation(conversationId));

                // Store in UserSession for easy access from other files
                UserSession.addConversationItem(conversationItem);
                conversationListContainer.getChildren().add(conversationItem);
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading conversations", e);
        }
    }
}
