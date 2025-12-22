package genki.controllers;

import genki.models.Group;


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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.stage.Popup;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

import java.util.Objects;
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
import java.util.ArrayList;

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
    private Label chatContactName;
    @FXML
    private ImageView UserProfil;
    @FXML
    private VBox AmisNameStatus;
    @FXML
    private VBox mainChatArea;
    @FXML
    private HBox chatHeader;
    @FXML
    private ScrollPane usersPane;
    @FXML
    private ScrollPane groupsPane;
    @FXML
    private VBox conversationListContainer;
    @FXML
    private VBox groupsListContainer;
    @FXML
    private HBox messageInputArea;
    @FXML
    private VBox messagesContainer;
    @FXML
    private ScrollPane messagesScrollPane;
    @FXML
    private TextField messageInput;
    @FXML
    private Button btnSend;
    @FXML private ImageView rightProfileImage;
    @FXML private Label rightContactName;
    @FXML private Label rightContactTitle;
    @FXML private Label rightContactBio;

    private Boolean rightSideVisibilite = false;
    // Track the currently open conversation
    private String currentRecipientId = null;  // Track recipient for message sending
    private String currentRecipientName = null;  // Track recipient name for fallback matching

    private ObjectId currentConversationId = null;
    @FXML private Button btnAdd;
    @FXML private VBox rightSideContainer;
    @FXML private ImageView profilTrigger;
    @FXML private VBox UserNameStatus;
    @FXML private ImageView messageProfil;
    @FXML private Label CurrentUsername;
    @FXML private Button btnNotifications;
    

    @FXML private Button btnAll;
    @FXML private Button btnGroups;

    // handle logout of user
    public void handleLogout() {
        UserSession.logout();
        try {
            ScenesController.switchToScene("/genki/views/Login.fxml", "Genki - Sign in");
        } catch (IOException ex) {
            logger.info("Error loading Login.fxml " + ex.getMessage());
        }
    }

    // handle toggling between users and groups panes
    private void switchUsers(boolean switchToUsers) {
        usersPane.setVisible(switchToUsers);
        usersPane.setManaged(switchToUsers);

        groupsPane.setVisible(!switchToUsers);
        groupsPane.setManaged(!switchToUsers);
    }




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

        switchUsers(true);

        if (UserSession.getGroups().isEmpty() && UserSession.getConversations().isEmpty()) {

                     chatHeader.getChildren().clear();
                     messageInputArea.getChildren().clear();
                     messagesContainer.getChildren().clear();

                     ImageView startConversationImageView = new ImageView(new Image(HomeController.class.getResourceAsStream("/genki/img/start_conversation.png")));
                     startConversationImageView.setPreserveRatio(true);
                     startConversationImageView.setSmooth(true);
                     startConversationImageView.setFitWidth(700);
                     startConversationImageView.setFitHeight(700);

                     HBox buttonsContainer = new HBox();
                     buttonsContainer.setAlignment(Pos.CENTER);
                     buttonsContainer.setSpacing(10);

                     Button addFriendBtn = new Button();
                     ImageView addFriendIcon = new ImageView(new Image(HomeController.class.getResourceAsStream("/genki/img/add_friend.png")));
                     addFriendBtn.setGraphic(addFriendIcon);
                     addFriendBtn.setText("Add a Friend");
                     addFriendBtn.setStyle("""
                            -fx-background-color: #746996;
                            -fx-text-fill: white;
                            -fx-background-radius: 20;
                            -fx-padding: 8 16;
                            -fx-font-size: 14px;
                            -fx-cursor: hand;
                        """);

                        Button joinGroupBtn = new Button();
                        ImageView joinGroupIcon = new ImageView(new Image(HomeController.class.getResourceAsStream("/genki/img/join_group.png")));
                        joinGroupBtn.setGraphic(joinGroupIcon);
                        joinGroupBtn.setText("Join a Group");
                        joinGroupBtn.setStyle("""
                                        -fx-background-color: #76B885;
                                        -fx-text-fill: white;
                                        -fx-background-radius: 20;
                                        -fx-padding: 8 16;
                                        -fx-font-size: 14px;
                                        -fx-cursor: hand
                                    """);

                     buttonsContainer.getChildren().addAll(addFriendBtn, joinGroupBtn);

                     messagesContainer.getChildren().addAll(startConversationImageView, buttonsContainer);
                     messagesContainer.setAlignment(Pos.CENTER);

                     addFriendBtn.setOnAction(e-> {
                         openAddUserDialog();
                     });

                     joinGroupBtn.setOnAction(e-> {
                         openJoinGroupDialog();
                     });

        }


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

        if (UserSession.getImageUrl().isEmpty()) {
               UserProfil.setImage(new Image(Objects.requireNonNull(HomeController.class.getResourceAsStream("/genki/img/user-default.png"))));
               UserProfil.setFitWidth(40);
               UserProfil.setFitHeight(40);
               UserProfil.setPreserveRatio(false);
               javafx.scene.shape.Circle userClip = new javafx.scene.shape.Circle(20, 20, 20);
               UserProfil.setClip(userClip);
               UserProfil.getStyleClass().add("avatar");
        }
        else {
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
        }
        CurrentUsername.setText(UserSession.getUsername());

        btnSend.setOnMouseClicked(e -> {
            String messageText = messageInput.getText();
            if (messageText == null || messageText.trim().isEmpty() || currentConversationId == null) {
                return;
            }
            String senderId = UserSession.getUserId();
            String senderName = UserSession.getUsername();

            messagesContainer.getChildren().add(
                    MessageItemBuilder.createSentMessage("genki/img/user-default.png", senderName, messageText));
            messageInput.clear();

            // UserSession.getClientSocket().sendMessages(messageText);


            // Auto-scroll to bottom
            scrollToBottom();
            
            // Create structured message data with all required info
            MessageData msgData = new MessageData(
                currentConversationId.toString(),
                senderId,
                senderName,
                messageText,
                UserSession.getImageUrl(),
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
                messageDAO.sendMessage(currentConversationId, senderId, senderName, messageText);
                
                // If you need to update UI after save (e.g., show success icon), use Platform.runLater()
                // Platform.runLater(() -> {
                //     // Update UI elements here
                // });
            }).start();
        });
        loadConversations();

        if (messagesContainer != null) {
        // Show some example messages dynamically
        /*if (messagesContainer != null) {
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

        }*/
        // Configuration des filtres
        if (btnAll != null) {
            btnAll.setOnMouseClicked(e -> showUserConversations());
        }
        if (btnGroups != null) {

            btnGroups.setOnMouseClicked(e -> showGroupConversations());

        }
        

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
            Insets.EMPTY
        )));
        
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
        addUserBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: black; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 5; " +
            "-fx-alignment: CENTER-LEFT; " +
            "-fx-font-size: 14px;"
        );
        addUserBtn.setOnMouseEntered(e -> addUserBtn.setStyle(
            addUserBtn.getStyle() + "-fx-background-color: rgba(255, 255, 255, 0.1);"
        ));
        addUserBtn.setOnMouseExited(e -> addUserBtn.setStyle(
            addUserBtn.getStyle().replace("-fx-background-color: rgba(255, 255, 255, 0.1);", "")
        ));
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
        addGroupBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: black; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 5; " +
            "-fx-alignment: CENTER-LEFT; " +
            "-fx-font-size: 14px;"
        );
        addGroupBtn.setOnMouseEntered(e -> addGroupBtn.setStyle(
            addGroupBtn.getStyle() + "-fx-background-color: rgba(255, 255, 255, 0.1);"
        ));
        addGroupBtn.setOnMouseExited(e -> addGroupBtn.setStyle(
            addGroupBtn.getStyle().replace("-fx-background-color: rgba(255, 255, 255, 0.1);", "")
        ));
        addGroupBtn.setOnAction(e -> {
            addMenuPopup.hide();
            addMenuPopup = null;
            openAddGroupDialog();
        });
        
        //ajouter ca 
        // Créer le bouton "Join Group"
        Button joinGroupBtn = new Button("Join Group");
        joinGroupBtn.setPrefWidth(150);
        // IMPROVEMENT 4: String Constants - Using style constants instead of hardcoded strings
        joinGroupBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT);
        joinGroupBtn.setOnMouseEntered(e -> joinGroupBtn.setStyle(MENU_BUTTON_STYLE_HOVER));
        joinGroupBtn.setOnMouseExited(e -> joinGroupBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT));
        joinGroupBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: black; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 5; " +
            "-fx-alignment: CENTER-LEFT; " +
            "-fx-font-size: 14px;"
        );
        joinGroupBtn.setOnMouseEntered(e -> joinGroupBtn.setStyle(
        		joinGroupBtn.getStyle() + "-fx-background-color: rgba(255, 255, 255, 0.1);"
        ));
        joinGroupBtn.setOnMouseExited(e -> joinGroupBtn.setStyle(
        		joinGroupBtn.getStyle().replace("-fx-background-color: rgba(255, 255, 255, 0.1);", "")
        ));
        joinGroupBtn.setOnAction(e -> {
            addMenuPopup.hide();
            addMenuPopup = null;
            openJoinGroupDialog();
        });
        
        // Ajouter les boutons au conteneur
        menuContainer.getChildren().addAll(addUserBtn, addGroupBtn , joinGroupBtn);
        
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
            //hamza ajoute ca
            AddUserController controller = loader.getController();
            controller.setHomeController(this);
            //---------------
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
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/icone_add_group.jpg"), 128, 128, true, true);
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
    
    //ajouter ca :
    @FXML
    public void openNotifications() {
        try {
            logger.log(Level.INFO, "Loading Notifications.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/Notifications.fxml"));
            Parent root = loader.load();
            
            Stage notificationStage = new Stage();
            try {
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/notifications.jpg"), 50, 50, true, true);
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

    
    public void addGroupConversation() {
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
                // boolean isOnline = false;
                // UserDAO usrMethods = new UserDAO();
                // genki.models.User usr = usrMethods.documentToUser(friendDoc);
                // boolean isOnline;
                // if(UserSession.getConnectedUsers().contains(usr)){
                //     isOnline = true;
                // }else{
                //     isOnline = false;
                // }
                 
                boolean isOnline = true; // TODO: Get from presence system

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
    //hamza ajoute ca
    public void handleAddUserFromDialog(String username) {

        UserDAO userDAO = new UserDAO();

        // 1️⃣ Vérifier si l'utilisateur existe
        Document userDoc = userDAO.getUserByUsername(username);

        if (userDoc == null) {
            showAlert("User not found", "This user does not exist.");
//            return; 
        }

        String friendId = userDoc.getObjectId("_id").toHexString();
        String currentUserId = UserSession.getUserId();

        // 2️⃣ Vérifier s'il existe déjà une conversation
        ConversationDAO conversationDAO = new ConversationDAO();

        ObjectId conversationId =
            conversationDAO.findDirectConversation(currentUserId, friendId);

        // 3️⃣ Si pas de conversation → créer
        if (conversationId == null) {
            conversationId =
                conversationDAO.createDirectConversation(currentUserId, friendId);
        }

        // 4️⃣ Afficher / ouvrir la conversation
//        openConversation(conversationId);
    }
// hamza ajoute ca :
    
	private void showAlert(String string, String string2) {
		// TODO Auto-generated method stub
		Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(string);
        alert.setContentText(string2);
        alert.showAndWait();
	}
// aussi ca par hamza
	/**
	 * Affiche uniquement les conversations directes (users)
	 */
	private void showUserConversations() {
	    try {
	        // Mettre à jour les styles des boutons
	        updateFilterButtonStyles(true);
	        
	        // Effacer la liste actuelle
	        conversationListContainer.getChildren().clear();
	        
	        UserDAO userDAO = new UserDAO();
	        String currentUsername = UserSession.getUsername();
	        
	        // Récupérer tous les amis
	        List<Document> friends = userDAO.getFriendsForUser(currentUsername);
	        
	        if (friends == null || friends.isEmpty()) {
	        	Label noUsersLabel = new Label("No users found");
	            noUsersLabel.setStyle(
	                "-fx-text-fill: white; " +
	                "-fx-font-size: 14px; " +
	                "-fx-padding: 20;"
	            );
	            conversationListContainer.getChildren().add(noUsersLabel);
	            logger.log(Level.INFO, "No friends found for user: " + currentUsername);
	            return;
	        }
	        
	        String currentUserId = UserSession.getUserId();
	        ConversationDAO conversationDAO = new ConversationDAO();
	        
	        // Pour chaque ami, créer un item de conversation
	        for (Document friendDoc : friends) {
	            String friendName = friendDoc.getString("username");
	            String photoUrl = friendDoc.getString("photo_url");
	            
	            String friendId;
	            Object objId = friendDoc.get("_id");
	            if (objId instanceof org.bson.types.ObjectId) {
	                friendId = ((org.bson.types.ObjectId) objId).toHexString();
	            } else {
	                friendId = String.valueOf(objId);
	            }
	            
	            ObjectId conversationId = conversationDAO.createDirectConversation(currentUserId, friendId);
	            
	            // Récupérer le dernier message
	            String lastMessage = "";
	            String time = "";
	            try {
	                DBConnection dbConnection = new DBConnection("genki_testing");
	                org.bson.Document conversationDoc = dbConnection
	                    .getDatabase()
	                    .getCollection("Conversation")
	                    .find(new org.bson.Document("_id", conversationId)
	                        .append("type", "direct")) // Filtre pour type "direct"
	                    .first();
	                    
	                if (conversationDoc != null) {
	                    lastMessage = conversationDoc.getString("lastMessageContent");
	                    Object lastMsgTimeObj = conversationDoc.get("lastMessageTime");
	                    
	                    if (lastMsgTimeObj != null) {
	                        time = formatMessageTime(lastMsgTimeObj);
	                    }
	                }
	            } catch (Exception ex) {
	                logger.log(Level.WARNING, "Error fetching last message: " + ex.getMessage());
	            }
	            
	            int unreadCount = 0;
	            boolean isOnline = true;
	            
	            HBox conversationItem = ConversationItemBuilder.createConversationItem(
	                photoUrl != null ? photoUrl : "genki/img/user-default.png",
	                friendName,
	                lastMessage != null ? lastMessage : "",
	                time != null ? time : "",
	                unreadCount,
	                isOnline
	            );
	            
	            conversationItem.setOnMouseClicked(e -> setCurrentConversation(conversationId));
	            conversationListContainer.getChildren().add(conversationItem);
	        }
	        
	    } catch (Exception e) {
	        logger.log(Level.WARNING, "Error loading user conversations", e);
	    }
	}

	/**
	 * Affiche uniquement les conversations de groupe
	 */
	private void showGroupConversations() {
	    try {
	        // Mettre à jour les styles des boutons
	        updateFilterButtonStyles(false);
	        
	        // Effacer la liste actuelle
	        conversationListContainer.getChildren().clear();
	        
	        String currentUserId = UserSession.getUserId();
	        DBConnection dbConnection = new DBConnection("genki_testing");
	        
	        // Récupérer toutes les conversations de type "group" où l'utilisateur est participant
	        var groupConversations = dbConnection
	            .getDatabase()
	            .getCollection("Conversation")
	            .find(new org.bson.Document("type", "group")
	                .append("participantIds", currentUserId));
	        
	        for (org.bson.Document conversationDoc : groupConversations) {
	            ObjectId conversationId = conversationDoc.getObjectId("_id");
	            String groupName = conversationDoc.getString("groupName");
	            if (groupName == null || groupName.isEmpty()) {
	                groupName = "Group Chat";
	            }
	            
	            String lastMessage = conversationDoc.getString("lastMessageContent");
	            if (lastMessage == null) {
	                lastMessage = "No messages yet";
	            }
	            
	            String time = "";
	            Object lastMsgTimeObj = conversationDoc.get("lastMessageTime");
	            if (lastMsgTimeObj != null) {
	                time = formatMessageTime(lastMsgTimeObj);
	            }
	            
	            int unreadCount = 0;
	            
	            // Pour les groupes, pas de statut "online"
	            boolean isOnline = false;
	            
	            // Image par défaut pour les groupes
	            String groupPhotoUrl = conversationDoc.getString("photo_url");
	            if (groupPhotoUrl == null) {
	                groupPhotoUrl = "genki/img/group-default.png";
	            }
	            
	            HBox conversationItem = ConversationItemBuilder.createConversationItem(
	                groupPhotoUrl,
	                groupName,
	                lastMessage,
	                time,
	                unreadCount,
	                isOnline
	            );

	            conversationItem.setOnMouseClicked(e -> setCurrentConversation(conversationId));
	            conversationListContainer.getChildren().add(conversationItem);
	        }
	        
	    } catch (Exception e) {
	        logger.log(Level.WARNING, "Error loading group conversations", e);
	    }
	    
	// Ajouter ces lignes après la boucle :
	if (conversationListContainer.getChildren().isEmpty()) {

        if (UserSession.getGroups().isEmpty()) {
            Label noGroupsLabel = new Label("No groups found");
            noGroupsLabel.setStyle(
                    "-fx-text-fill: #6b9e9e; " +
                            "-fx-font-size: 14px; " +
                            "-fx-padding: 20;"
            );
            conversationListContainer.getChildren().add(noGroupsLabel);
        } else {

                for (Group group : UserSession.getGroups()) {
                    HBox nvGroupContainer = ConversationItemBuilder.createGroupConversationItem(
                            group.getGroupProfilePicture(),
                            group.getGroupName(),
                            "",
                            "",
                            2
                    );

                    conversationListContainer.getChildren().add(nvGroupContainer);
                }

        }
	}
	}

	// aussi ca par hamza
	/**
	 * Met à jour les styles des boutons de filtre
	 * @param showingUsers true si on affiche les users, false pour les groupes
	 */
	private void updateFilterButtonStyles(boolean showingUsers) {
	    if (btnAll != null && btnGroups != null) {
	        if (showingUsers) {
	            btnAll.setStyle(
	                "-fx-background-color: #2bfbfb; " +
	                "-fx-text-fill: #232e2e; " +
	                "-fx-font-weight: bold; " +
	                "-fx-background-radius: 20; " +
	                "-fx-padding: 8 16;"
	            );
	            btnGroups.setStyle(
	                "-fx-background-color: transparent; " +
	                "-fx-text-fill: #a0a0a0; " +
	                "-fx-background-radius: 20; " +
	                "-fx-padding: 8 16;"
	            );
	        } else {
	            btnAll.setStyle(
	                "-fx-background-color: transparent; " +
	                "-fx-text-fill: #a0a0a0; " +
	                "-fx-background-radius: 20; " +
	                "-fx-padding: 8 16;"
	            );
	            btnGroups.setStyle(
	                "-fx-background-color: #2bfbfb; " +
	                "-fx-text-fill: #232e2e; " +
	                "-fx-font-weight: bold; " +
	                "-fx-background-radius: 20; " +
	                "-fx-padding: 8 16;"
	            );
	        }
	    }
	}

	/**
	 * Formate l'heure du dernier message
	 * @param lastMsgTimeObj L'objet temps à formatter
	 * @return Une chaîne formatée (HH:mm ou DD/MM/YY)
	 */
	private String formatMessageTime(Object lastMsgTimeObj) {
	    try {
	        java.time.LocalDateTime msgTime = null;
	        
	        if (lastMsgTimeObj instanceof java.time.LocalDateTime) {
	            msgTime = (java.time.LocalDateTime) lastMsgTimeObj;
	        } else if (lastMsgTimeObj instanceof java.util.Date) {
	            java.util.Date date = (java.util.Date) lastMsgTimeObj;
	            msgTime = java.time.LocalDateTime.ofInstant(
	                date.toInstant(), 
	                java.time.ZoneId.systemDefault()
	            );
	        } else if (lastMsgTimeObj instanceof String) {
	            try {
	                msgTime = java.time.LocalDateTime.parse((String) lastMsgTimeObj);
	            } catch (Exception ignore) {}
	        }
	        
	        if (msgTime != null) {
	            java.time.LocalDate today = java.time.LocalDate.now();
	            if (msgTime.toLocalDate().equals(today)) {
	                return String.format("%02d:%02d", msgTime.getHour(), msgTime.getMinute());
	            } else {
	                return String.format("%02d/%02d/%02d", 
	                    msgTime.getDayOfMonth(), 
	                    msgTime.getMonthValue(), 
	                    msgTime.getYear() % 100
	                );
	            }
	        }
	    } catch (Exception e) {
	        logger.log(Level.WARNING, "Error formatting message time", e);
	    }
	    return "";
	}


    public void loadGroups() {

           ArrayList<Group> userGroups = UserSession.getGroups();


           if (userGroups != null) {

               for (Group group : userGroups) {
                   HBox nvGroupContainer = ConversationItemBuilder.createGroupConversationItem(
                           group.getGroupProfilePicture(),
                           group.getGroupName(),
                           "",
                           "",
                           2
                   );

                   conversationListContainer.getChildren().add(nvGroupContainer);
               }

           }

        if (conversationListContainer.getChildren().isEmpty()) {
            Label noGroupsLabel = new Label("No groups found");
            noGroupsLabel.setStyle(
                    "-fx-text-fill: #6b9e9e; " +
                            "-fx-font-size: 14px; " +
                            "-fx-padding: 20;"
            );
            conversationListContainer.getChildren().add(noGroupsLabel);
        }



    }
}

