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
import java.util.function.Consumer;

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
    private Button btnAll;
    @FXML
    private Button btnUnread;
    @FXML
    private Button btnGroups;
    
    @FXML
    private Label chatContactStatus;

    @FXML
    private Label chatContactName;
    @FXML
    private javafx.scene.shape.Circle chatContactStatusCircle;
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
    private VBox loadingSpinnerContainer;
    @FXML
    private javafx.scene.control.ProgressIndicator loadingSpinner;
    @FXML
    private VBox groupsListContainer;
    @FXML
    private HBox messageInputArea;
    @FXML
    private VBox messagesContainer;
    @FXML
    private ScrollPane messagesScrollPane;
    @FXML
    private VBox messagesLoadingSpinnerContainer;
    @FXML
    private javafx.scene.control.ProgressIndicator messagesLoadingSpinner;
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
    
    // Track conversation loading progress
    private int totalConversations = 0;
    private int loadedConversations = 0;
    private Object loadingLock = new Object();

    private ObjectId currentConversationId = null;
    @FXML private Button btnAdd;
    @FXML private VBox rightSideContainer;
    @FXML private ImageView profilTrigger;
    @FXML private VBox UserNameStatus;
    @FXML private ImageView messageProfil;
    @FXML private Label CurrentUsername;
    @FXML private Button btnNotifications;
    



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
        
        // Update button styles to show which view is active
        Platform.runLater(() -> {
            if (switchToUsers) {
                // Users button active (cyan)
                btnAll.setStyle("-fx-background-color: #2bfbfb; -fx-text-fill: #232e2e; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 16;");
                btnGroups.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-background-radius: 20; -fx-padding: 8 16;");
            } else {
                // Groups button active (cyan)
                btnAll.setStyle("-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-background-radius: 20; -fx-padding: 8 16;");
                btnGroups.setStyle("-fx-background-color: #2bfbfb; -fx-text-fill: #232e2e; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 16;");
            }
        });
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
     * Holds the single DBConnection instance for this controller
     */
    private DBConnection dbConnection;

    @FXML
    public void initialize() {
        // Initialize the DB connection ONCE for this controller
        dbConnection = new DBConnection("genki_testing");

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

            messagesContainer.getChildren().add(
                    MessageItemBuilder.createSentMessage(senderImageUrl, senderName, messageText));
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
        
        // Load conversations in background thread for fast UI response
        new Thread(() -> {
            try {
                loadConversations();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error loading conversations in background", e);
            }
        }).start();

        // Load groups in background thread
        new Thread(() -> {
            try {
                loadGroups();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error loading groups in background", e);
            }
        }).start();

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
        // Configuration des filtres - Switch between users and groups with proper button styling
        // Just toggles visibility - data is already loaded during initialize()
        if (btnAll != null) {
            btnAll.setOnMouseClicked(e -> switchUsers(true));
        }
        if (btnGroups != null) {
            btnGroups.setOnMouseClicked(e -> switchUsers(false));
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
    public void setCurrentConversation(ObjectId conversationId, Boolean isOnligne) {
        System.out.println("The Set method...");
        this.currentConversationId = conversationId;
        
        // Show loading spinner immediately
        Platform.runLater(() -> {
            if (messagesLoadingSpinnerContainer != null) {
                messagesLoadingSpinnerContainer.setVisible(true);
                messagesLoadingSpinnerContainer.setManaged(true);
            }
            messagesContainer.getChildren().clear();
        });
        
        // Thread 1: Update chat header with friend's info in background
        new Thread(() -> {
            try {
                String currentUserId = UserSession.getUserId();
                // IMPROVEMENT 1: Resource Management - Use singleton DBConnection instead of creating new instance
                // Use the controller's dbConnection instance
                DBConnection dbConnection = this.dbConnection;
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

                                // Update UI on JavaFX thread - Thread Safe!
                                Platform.runLater(() -> {
                                    // Update text labels
                                    if (chatContactName != null) {
                                        chatContactName.setText(friendName != null ? friendName : "");
                                    }
                                    if (rightContactName != null) {
                                        rightContactName.setText(friendName != null ? friendName : "");
                                    }
                                    if (rightContactBio != null) {
                                        rightContactBio.setText(bio != null ? bio : "");
                                    }
                                    if (rightContactTitle != null) {
                                        rightContactTitle.setText(role != null ? role : "");
                                    }
                                    
                                    // Update status and color
                                    if (chatContactStatus != null) {
                                        chatContactStatus.setText(isOnligne ? "Online" : "Offline");
                                        String statusTextColor = isOnligne ? "-fx-text-fill: #4ade80" : "-fx-text-fill: #9ca3af";
                                        chatContactStatus.setStyle(statusTextColor + "; -fx-font-size: 12px;");
                                    }
                                    
                                    if (chatContactStatusCircle != null) {
                                        javafx.scene.paint.Color statusColor = isOnligne ? 
                                            javafx.scene.paint.Color.web("#4ade80") : javafx.scene.paint.Color.web("#9ca3af");
                                        chatContactStatusCircle.setFill(statusColor);
                                    }
                                    
                                    // Update profile images
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
                                        } catch (Exception e) {
                                            System.out.println("Error loading profile image: " + e.getMessage());
                                            profilTrigger.setImage(new Image("genki/img/user-default.png", 180, 180, false, true));
                                        }
                                    }
                                    
                                    // Update right panel profile image
                                    if (rightProfileImage != null && photoUrl != null) {
                                        try {
                                            // Load at 400x400 for the larger right panel image (100x100 display)
                                            Image rightImg = new Image(photoUrl, 400, 400, false, true);
                                            rightProfileImage.setImage(rightImg);
                                            rightProfileImage.setFitWidth(100);
                                            rightProfileImage.setFitHeight(100);
                                            rightProfileImage.setPreserveRatio(false);
                                            javafx.scene.shape.Circle rightClip = new javafx.scene.shape.Circle(50, 50, 50);
                                            rightProfileImage.setClip(rightClip);
                                            rightProfileImage.getStyleClass().add("avatar");
                                        } catch (Exception e) {
                                            System.out.println("Error loading right profile image: " + e.getMessage());
                                            rightProfileImage.setImage(new Image("genki/img/user-default.png", 400, 400, false, true));
                                        }
                                    }
                                });
                                
                                System.out.println("Conversation set for: " + friendName + ", Online: " + isOnligne);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error updating chat header", e);
            }
        }).start();
        
        // Thread 2: Load messages in parallel - runs at the same time as header update
        loadMessagesInBackground(conversationId);
    }
    
    /**
     * Load messages in a separate background thread (parallel to header update)
     */
    private void loadMessagesInBackground(ObjectId conversationId) {
        new Thread(() -> {
            try {
                MessageDAO messageDAO = new MessageDAO();
                String currentUserId = genki.utils.UserSession.getUserId();
                
                // Fetch only the latest 50 messages for better performance
                List<org.bson.Document> messagesList = new ArrayList<>();
                messageDAO.getDatabase().getCollection("Message")
                        .find(new org.bson.Document("conversationId", conversationId))
                        .sort(new org.bson.Document("timestamp", -1))  // Latest first
                        .limit(50)  // Only get last 50 messages
                        .forEach((java.util.function.Consumer<org.bson.Document>) messagesList::add);
                
                // Reverse to get chronological order (oldest to newest)
                java.util.Collections.reverse(messagesList);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    messagesContainer.getChildren().clear();
                    
                    for (org.bson.Document doc : messagesList) {
                        String senderId = doc.getString("senderId");
                        String senderName = doc.getString("senderName");
                        String content = doc.getString("content");
                        String senderImageUrl = doc.getString("senderImageUrl");
                        if (senderImageUrl == null) {
                            senderImageUrl = doc.getString("photo_url"); // Fallback for legacy data
                        }
                        
                        if (senderId != null && senderId.equals(currentUserId)) {
                            messagesContainer.getChildren().add(
                                    MessageItemBuilder.createSentMessage(senderImageUrl, senderName, content));
                        } else {
                            messagesContainer.getChildren().add(
                                    MessageItemBuilder.createReceivedMessage(senderImageUrl, senderName, content));
                        }
                    }
                    
                    // Hide loading spinner
                    if (messagesLoadingSpinnerContainer != null) {
                        messagesLoadingSpinnerContainer.setVisible(false);
                        messagesLoadingSpinnerContainer.setManaged(false);
                    }
                    
                    // Auto-scroll to bottom after loading messages
                    scrollToBottom();
                });
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error loading messages for conversation", e);
                // Hide spinner on error too
                Platform.runLater(() -> {
                    if (messagesLoadingSpinnerContainer != null) {
                        messagesLoadingSpinnerContainer.setVisible(false);
                        messagesLoadingSpinnerContainer.setManaged(false);
                    }
                });
            }
        }).start();
    }

    // Sending Messages

    /**
     * Fetch all messages for a conversation and display them in the UI
     * 
     * @param conversationId The ObjectId of the conversation
     */

    
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
            
            // Get the AddGroupController and set the HomeController reference
            AddGroupController controller = loader.getController();
            controller.setHomeController(this);

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
     * 
     * IMPROVEMENT 5: Background Threading & Parallel Processing
     * - Each friend's conversation loads on a separate thread
     * - All conversions load in parallel for faster performance
     * - UI updates wrapped with Platform.runLater() for thread safety
     */
    private void loadConversations() {
        try {
            UserDAO userDAO = new UserDAO();
            String currentUsername = UserSession.getUsername();

            // Get all friends for the current user
            List<Document> friends = userDAO.getFriendsForUser(currentUsername);

            // Show loading spinner
            Platform.runLater(() -> {
                if (loadingSpinnerContainer != null) {
                    loadingSpinnerContainer.setVisible(true);
                    loadingSpinnerContainer.setManaged(true);
                }
            });

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
                // Hide spinner if no conversations
                Platform.runLater(() -> {
                    if (loadingSpinnerContainer != null) {
                        loadingSpinnerContainer.setVisible(false);
                        loadingSpinnerContainer.setManaged(false);
                    }
                });
                return;
            }

            // Set total conversations count for progress tracking
            synchronized (loadingLock) {
                totalConversations = friends.size();
                loadedConversations = 0;
            }

            // Load each friend's conversation on a separate thread for parallel processing
            if (friends != null) {
                String currentUserId = UserSession.getUserId();
                for (Document friendDoc : friends) {
                    new Thread(() -> {
                        try {
                            String friendName = friendDoc.getString("username");
                            String photoUrl = friendDoc.getString("photo_url");
                            String friendId;
                            Object objId = friendDoc.get("_id");
                            if (objId instanceof org.bson.types.ObjectId) {
                                friendId = ((org.bson.types.ObjectId) objId).toHexString();
                            } else {
                                friendId = String.valueOf(objId);
                            }
                            
                            ConversationDAO conversationDAO = new ConversationDAO();
                            ObjectId conversationId = conversationDAO.createDirectConversation(currentUserId, friendId);

                            // Fetch last message from Conversation collection
                            String lastMessage = "";
                            String time = "";
                            try {
                                DBConnection dbConnection = this.dbConnection;
                                org.bson.Document conversationDoc = dbConnection
                                        .getDatabase()
                                        .getCollection("Conversation")
                                        .find(new org.bson.Document("_id", conversationId))
                                        .first();
                                if (conversationDoc != null) {
                                    lastMessage = conversationDoc.getString("lastMessageContent");
                                    Object lastMsgTimeObj = conversationDoc.get("lastMessageTime");
                                    if (lastMsgTimeObj != null) {
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
                            conversationItem.setOnMouseClicked(e -> setCurrentConversation(conversationId, isOnline));

                            // Store in UserSession for easy access from other files
                            UserSession.addConversationItem(conversationItem);
                            
                            // Update UI on JavaFX thread - Thread Safe!
                            Platform.runLater(() -> {
                                conversationListContainer.getChildren().add(conversationItem);
                                
                                // Update loading progress
                                synchronized (loadingLock) {
                                    loadedConversations++;
                                    System.out.println("Loaded " + loadedConversations + "/" + totalConversations + " conversations");
                                    
                                    // Hide spinner when all conversations are loaded
                                    if (loadedConversations >= totalConversations && loadingSpinnerContainer != null) {
                                        loadingSpinnerContainer.setVisible(false);
                                        loadingSpinnerContainer.setManaged(false);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error loading conversation for friend", e);
                            // Update counter even on error
                            synchronized (loadingLock) {
                                loadedConversations++;
                                if (loadedConversations >= totalConversations && loadingSpinnerContainer != null) {
                                    Platform.runLater(() -> {
                                        loadingSpinnerContainer.setVisible(false);
                                        loadingSpinnerContainer.setManaged(false);
                                    });
                                }
                            }
                        }
                    }).start();
                }
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading conversations", e);
            // Hide spinner on error
            Platform.runLater(() -> {
                if (loadingSpinnerContainer != null) {
                    loadingSpinnerContainer.setVisible(false);
                    loadingSpinnerContainer.setManaged(false);
                }
            });
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
                    DBConnection dbConnection = this.dbConnection;
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
	            
	            conversationItem.setOnMouseClicked(e -> setCurrentConversation(conversationId, isOnline));
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
            DBConnection dbConnection = this.dbConnection;
	        
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

	            conversationItem.setOnMouseClicked(e -> setCurrentConversation(conversationId, isOnline));
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

        if (userGroups != null && !userGroups.isEmpty()) {
            for (Group group : userGroups) {
                HBox nvGroupContainer = ConversationItemBuilder.createGroupConversationItem(
                        group.getGroupProfilePicture(),
                        group.getGroupName(),
                        "",
                        "",
                        2
                );
                groupsListContainer.getChildren().add(nvGroupContainer);
            }
        } else {
            Label noGroupsLabel = new Label("No groups found");
            noGroupsLabel.setStyle(
                    "-fx-text-fill: #6b9e9e; " +
                            "-fx-font-size: 14px; " +
                            "-fx-padding: 20;"
            );
            groupsListContainer.getChildren().add(noGroupsLabel);
        }
    }

    public void handleAddGroupFromDialog(Group newGroup) {
        Platform.runLater(() -> {
            if (groupsListContainer.getChildren().isEmpty()) {
                groupsListContainer.getChildren().clear();
            } else {
                // Remove "No groups found" message if it exists
                groupsListContainer.getChildren().removeIf(node -> 
                    node instanceof Label && ((Label)node).getText().equals("No groups found")
                );
            }

            HBox newGroupContainer = ConversationItemBuilder.createGroupConversationItem(
                    newGroup.getGroupProfilePicture(),
                    newGroup.getGroupName(),
                    "",
                    "",
                    2
            );
            groupsListContainer.getChildren().add(0, newGroupContainer);

            if (!usersPane.isVisible()) {
                switchUsers(false); // Switch to groups view if not already there
            }
        });
    }
}

