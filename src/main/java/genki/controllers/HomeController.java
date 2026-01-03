package genki.controllers;

import genki.models.Notification;
import genki.models.Group;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.MongoException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.application.Platform;

import java.time.LocalDateTime;
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
import genki.models.User;

import org.bson.types.ObjectId;

import genki.utils.AlertConstruct;
import genki.utils.ConversationDAO;

public class HomeController {
    private static final Logger logger = Logger.getLogger(HomeController.class.getName());
    private static final DBConnection HomeControllerDBConnection = DBConnection.getInstance("genki_testing");
    @FXML
    private Button btnSettings;

    @FXML
    private Button btnAll;
    @FXML
    private Button btnUnread;
    @FXML
    private Button btnGroups;
    @FXML private Button groupSettingsBtn;
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
    private String currentRecipientId = null; // Track recipient for message sending
    private String currentRecipientName = null; // Track recipient name for fallback matching

    // Track conversation loading progress
    private int totalConversations = 0;
    private int loadedConversations = 0;
    private Object loadingLock = new Object();

    private ObjectId currentConversationId = null;
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
    private Label notificationBadge; // Badge to show unread notification count

    // load notifications for this UserSession
    public void loadNotifications() {

        try {

            MongoCollection<Document> notificationsCollection = dbConnection.getCollection("notifications");

            // Load only pending notifications (not accepted or rejected)
            long notificationsCount = notificationsCollection.countDocuments(
                    Filters.and(
                            Filters.eq("recipientUserId", new ObjectId(UserSession.getUserId())),
                            Filters.eq("status", "pending")));

            if (notificationsCount > 0) {

                notificationsCollection.find(
                        Filters.and(
                                Filters.eq("recipientUserId", new ObjectId(UserSession.getUserId())),
                                Filters.eq("status", "pending")))
                        .forEach(notificationDoc -> {

                            Notification nvNotification = new Notification(
                                    notificationDoc.getObjectId("_id"),
                                    notificationDoc.getObjectId("recipientUserId"),
                                    notificationDoc.getString("type"),
                                    notificationDoc.getString("requestType"),
                                    notificationDoc.getString("senderId"),
                                    notificationDoc.getString("senderName"),
                                    notificationDoc.getString("content"));

                            UserSession.addNotification(nvNotification);

                        });

            } else {
                UserSession.setNotificationsEmpty();
            }

        } catch (MongoException ex) {
            logger.warning("Failed to load notifications " + ex.getMessage());
        }

        // Update the notification badge with the count
        updateNotificationBadge();
    }

    /**
     * Create and setup the notification badge label
     * Red circle badge overlay positioned on top-right of the notification icon
     */
    private void setupNotificationBadge() {
        if (btnNotifications == null)
            return;

        notificationBadge = new Label();
        notificationBadge.setStyle(
                "-fx-background-color: #ef4444; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 50%; " +
                        "-fx-min-width: 18px; " +
                        "-fx-min-height: 18px; " +
                        "-fx-padding: 0; " +
                        "-fx-alignment: center; " +
                        "-fx-font-size: 10px; " +
                        "-fx-font-weight: bold;");
        notificationBadge.setVisible(false);
        notificationBadge.setManaged(false);

        // Add badge to the parent HBox after the button
        javafx.scene.Parent btnParent = btnNotifications.getParent();
        if (btnParent instanceof javafx.scene.layout.HBox) {
            javafx.scene.layout.HBox parentHBox = (javafx.scene.layout.HBox) btnParent;
            int btnIndex = parentHBox.getChildren().indexOf(btnNotifications);
            if (btnIndex >= 0) {
                // Add badge right after button
                parentHBox.getChildren().add(btnIndex + 1, notificationBadge);

                // Position badge on top of button using translate
                notificationBadge.setTranslateX(-20);
                notificationBadge.setTranslateY(-8);

                logger.info("✓ Notification badge overlay positioned on icon");
            }
        }
    }

    /**
     * Update the notification badge count
     */
    public void updateNotificationBadge() {
        Platform.runLater(() -> {
            int notificationCount = UserSession.getNotifications().size();

            if (notificationBadge == null) {
                setupNotificationBadge();
            }

            if (notificationCount > 0) {
                notificationBadge.setText(String.valueOf(notificationCount));
                notificationBadge.setVisible(true);
                notificationBadge.setManaged(true);
            } else {
                notificationBadge.setVisible(false);
                notificationBadge.setManaged(false);
            }
        });
    }

    /**
     * Setup real-time notification listener via WebSocket
     * This registers a callback to receive new notifications in real-time
     */
    public void setupNotificationListener() {
        if (UserSession.getClientSocket() == null) {
            logger.warning("Client socket is not initialized yet");
            return;
        }

        // Set callback for incoming notifications
        UserSession.getClientSocket().setOnNewNotificationCallback(notification -> {
            logger.info(
                    "📬 New notification received: " + notification.getSenderName() + " - " + notification.getType());

            Platform.runLater(() -> {
                try {
                    // Add notification to UserSession
                    UserSession.addNotification(notification);

                    // Update badge to show new count
                    updateNotificationBadge();

                    // Optional: Show a toast or system notification
                    logger.info("✅ Notification badge updated - New count: " + UserSession.getNotifications().size());

                } catch (Exception e) {
                    logger.warning("Error processing incoming notification: " + e.getMessage());
                }
            });
        });

        logger.info("✓ Real-time notification listener registered successfully");
    }

    /**
     * Cleanup old notifications from the database
     * Deletes notifications older than 30 days that have been accepted or rejected
     * This prevents database bloat and keeps notification history manageable
     */
    private void cleanupOldNotifications() {
        Thread t1 = new Thread(() -> {
            try {
                MongoCollection<Document> notificationsCollection = dbConnection.getCollection("notifications");

                // Calculate date 30 days ago
                java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
                java.time.ZonedDateTime zdt = thirtyDaysAgo.atZone(java.time.ZoneId.systemDefault());
                java.util.Date cutoffDate = java.util.Date.from(zdt.toInstant());

                // Delete notifications older than 30 days that are not pending
                long deletedCount = notificationsCollection.deleteMany(
                        Filters.and(
                                Filters.lt("createdAt", cutoffDate),
                                Filters.ne("status", "pending")))
                        .getDeletedCount();

                if (deletedCount > 0) {
                    logger.info("🧹 Cleaned up " + deletedCount + " old notifications");
                }
            } catch (MongoException e) {
                logger.warning("Error during notification cleanup: " + e.getMessage());
            }
        });
        t1.setDaemon(true);
        t1.start();
    }

    /**
     * Start periodic notification cleanup task
     * Runs every 12 hours to clean up old notifications
     */
    private void startNotificationCleanupScheduler() {
        Thread t2 = new Thread(() -> {
            while (true) {
                try {
                    // Run cleanup every 12 hours (43200000 ms)
                    Thread.sleep(43200000);
                    cleanupOldNotifications();
                } catch (InterruptedException e) {
                    logger.info("Notification cleanup scheduler interrupted");
                    break;
                } catch (Exception e) {
                    logger.warning("Error in notification cleanup scheduler: " + e.getMessage());
                }
            }
        }, "NotificationCleanupScheduler");
        t2.setDaemon(true);
        t2.start();
    }

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
                btnAll.setStyle(
                        "-fx-background-color: #2bfbfb; -fx-text-fill: #232e2e; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 16;");
                btnGroups.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-background-radius: 20; -fx-padding: 8 16;");
            } else {
                // Groups button active (cyan)
                btnAll.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-background-radius: 20; -fx-padding: 8 16;");
                btnGroups.setStyle(
                        "-fx-background-color: #2bfbfb; -fx-text-fill: #232e2e; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 16;");
            }
        });
    }

    private Popup addMenuPopup;

    // CSS Style Constants - centralized styles to avoid hardcoding
    private static final String MENU_BUTTON_STYLE_DEFAULT = "-fx-background-color: transparent; " +
            "-fx-text-fill: black; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 5; " +
            "-fx-alignment: CENTER-LEFT; " +
            "-fx-font-size: 14px;";

    private static final String MENU_BUTTON_STYLE_HOVER = MENU_BUTTON_STYLE_DEFAULT
            + "-fx-background-color: rgba(255, 255, 255, 0.1);";

    private static final String FILTER_BUTTON_ACTIVE_STYLE = "-fx-background-color: #4a5fff; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 16;";

    private static final String FILTER_BUTTON_INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-background-radius: 20; -fx-padding: 8 16;";

    /**
     * Holds the single DBConnection instance for this controller
     */
    private DBConnection dbConnection;

    @FXML
    public void initialize() {
        // Initialize the DB connection ONCE for this controller using Singleton pattern
        dbConnection = DBConnection.getInstance("genki_testing");

        // Setup notification badge after UI is loaded
        Platform.runLater(() -> setupNotificationBadge());

        switchUsers(true);

        if (UserSession.getGroups().isEmpty() && UserSession.getConversations().isEmpty()) {
            // chatHeader.getChildren().clear();
            // messageInputArea.getChildren().clear();
            // messagesContainer.getChildren().clear();

            ImageView startConversationImageView = new ImageView(
                    new Image(HomeController.class.getResourceAsStream("/genki/img/start_conversation.jpg")));
            startConversationImageView.setPreserveRatio(true);
            startConversationImageView.setSmooth(true);
            startConversationImageView.setFitWidth(300);
            startConversationImageView.setFitHeight(300);

            HBox buttonsContainer = new HBox();
            buttonsContainer.setAlignment(Pos.CENTER);
            buttonsContainer.setSpacing(10);

            Button addFriendBtn = new Button();
            ImageView addFriendIcon = new ImageView(
                    new Image(HomeController.class.getResourceAsStream("/genki/img/add_friend.png")));
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
            ImageView joinGroupIcon = new ImageView(
                    new Image(HomeController.class.getResourceAsStream("/genki/img/join_group.png")));
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

            addFriendBtn.setOnAction(e -> {
                openAddUserDialog();
            });

            joinGroupBtn.setOnAction(e -> {
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
                    MessageItemBuilder.createSentMessage(senderImageUrl, senderName, messageText,
                            formatTimestamp(System.currentTimeMillis())));
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
                    currentRecipientId, // Include recipient ID
                    currentRecipientName // Include recipient name for fallback
            );

            // Send message as JSON via socket
            String jsonMessage = genki.utils.GsonUtility.getGson().toJson(msgData);
            UserSession.getClientSocket().sendMessages(jsonMessage);

            // Update conversation list with the sent message (for both direct and group
            // conversations)
            updateConversationListWithMessage(msgData);

            // IMPROVEMENT 2: Thread Safety and UI Updates
            // Save to DB in background thread and update UI safely if needed
            Thread t3 = new Thread(() -> {
                MessageDAO messageDAO = new MessageDAO();
                messageDAO.sendMessage(currentConversationId, senderId, senderName, senderImageUrl, messageText);

                // If you need to update UI after save (e.g., show success icon), use
                // Platform.runLater()
                // Platform.runLater(() -> {
                // // Update UI elements here
                // });
            });
            t3.setDaemon(true);
            t3.start();
        });

        // Load conversations in background thread for fast UI response
        Thread t4 = new Thread(() -> {
            try {
                loadConversations();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error loading conversations in background", e);
            }
        });
        t4.setDaemon(true);
        t4.start();

        // Load group conversations in background thread with caching
        Thread t5 = new Thread(() -> {
            try {
                loadGroupConversations();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error loading group conversations in background", e);
            }
        });
        t5.setDaemon(true);
        t5.start();

        // Load notifications in background thread to avoid blocking UI
        Thread t6 = new Thread(() -> {
            try {
                loadNotifications();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error loading notifications in background", e);
            }
        });
        t6.setDaemon(true);
        t6.start();

        if (messagesContainer != null) {
            // Show some example messages dynamically
            /*
             * if (messagesContainer != null) {
             * messagesContainer.getChildren().clear();
             * messagesContainer.getChildren().add(
             * MessageItemBuilder.createReceivedMessage(
             * "genki/img/user-default.png",
             * "Aimane Aboufadle",
             * "Long message text goes here, demonstrating a message received from another user."
             * ));
             * messagesContainer.getChildren().add(
             * MessageItemBuilder.createSentMessage(
             * "genki/img/user-default.png",
             * "You",
             * "hhhhhhhhhhhhhh salam"));
             * }
             * 
             * }
             */
            // Configuration des filtres - Switch between users and groups with proper
            // button styling
            if (btnAll != null) {
                btnAll.setOnMouseClicked(e -> {
                    switchUsers(true);
                    showUserConversations();
                });
            }
            if (btnGroups != null) {
                btnGroups.setOnMouseClicked(e -> {
                    switchUsers(false);
                    showGroupConversations(); // Show cached group conversations
                });
            }

            // Add notification button click handler
            if (btnNotifications != null) {
                btnNotifications.setOnMouseClicked(e -> openNotifications());
            }

            // Register callback for incoming messages
            UserSession.getClientSocket().setOnNewMessageCallback(msgData -> {
                System.out.println("╔════════════════════════════════════════════════════╗");
                System.out.println("║ MESSAGE CALLBACK TRIGGERED                          ║");
                System.out.println("╚════════════════════════════════════════════════════╝");
                System.out.println("📨 Message details:");
                System.out.println("  - conversationId: " + msgData.conversationId);
                System.out.println("  - senderName: " + msgData.senderName);
                System.out.println("  - messageText: " + msgData.messageText);
                System.out.println("  - senderId: " + msgData.senderId);
                System.out.println("  - senderProfileImage: " + msgData.senderProfileImage);
                System.out.println("  - timestamp: " + msgData.timestamp);
                System.out.println("  - recipientId: " + msgData.recipientId);
                System.out.println("  - recipientName: " + msgData.recipientName);

                System.out.println("🎯 Current conversation context:");
                System.out.println("  - currentConversationId: " + currentConversationId);
                System.out.println("  - currentRecipientId: " + currentRecipientId);
                System.out.println("  - currentRecipientName: " + currentRecipientName);

                // Update conversation list with last message and time (always, regardless of
                // current view)
                if (msgData.conversationId != null) {
                    updateConversationListWithMessage(msgData);
                }

                // Only add message to chat area if it's for the current conversation
                if (msgData.conversationId != null && currentConversationId != null) {
                    System.out.println("✓ Conversation ID and msgData both present, comparing...");
                    String msgConvId = msgData.conversationId;
                    String currentConvId = currentConversationId.toString();
                    System.out.println("  Comparing: '" + msgConvId + "' == '" + currentConvId + "'");

                    if (msgConvId.equals(currentConvId)) {
                        System.out.println("✅ MATCH! Adding message to conversation");
                        Platform.runLater(() -> {
                            messagesContainer.getChildren().add(
                                    MessageItemBuilder.createReceivedMessage(
                                            msgData.senderProfileImage,
                                            msgData.senderName,
                                            msgData.messageText));
                            // Auto-scroll to bottom
                            scrollToBottom();
                        });
                    } else {
                        System.out.println("❌ NO MATCH: Message is for different conversation");
                    }
                } else {
                    System.out.println("⚠️  Cannot compare: msgData.conversationId=" + msgData.conversationId
                            + ", currentConversationId=" + currentConversationId);
                }
            });

            // Set reference to this HomeController in clientSocketController so it can
            // update chat header status
            UserSession.getClientSocket().setHomeController(this);

            // Setup real-time notification listener for incoming notifications in
            // background thread
            Thread t7 = new Thread(() -> {
                try {
                    setupNotificationListener();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error setting up notification listener in background", e);
                }
            });
            t7.setDaemon(true);
            t7.start();

            // Start periodic cleanup of old notifications
            startNotificationCleanupScheduler();

        }

    }


    private void handleGroupSettingsClick(String groupAdmin)  {

       if (groupAdmin.equals(UserSession.getUsername())) {
           try {

               logger.log(Level.INFO, "Loading GroupSettings.fxml");
               FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/GroupSettings.fxml"));
               Parent root = loader.load();

               Stage groupSettingsStage = new Stage();

               try {
                   Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/genki/img/setting.png")), 50, 50, true, true);
                   groupSettingsStage.getIcons().add(logo);
               } catch (Exception e) {
                   logger.log(Level.WARNING, "Failed to load application logo", e);
               }
               groupSettingsStage.setTitle("Group Settings");
               groupSettingsStage.setResizable(false);
               groupSettingsStage.initModality(Modality.APPLICATION_MODAL);
               if (groupSettingsBtn != null && groupSettingsBtn.getScene() != null) {
                   groupSettingsStage.initOwner(groupSettingsBtn.getScene().getWindow());
               }
               groupSettingsStage.setScene(new Scene(root));
               groupSettingsStage.centerOnScreen();
               groupSettingsStage.showAndWait();
           } catch (IOException loadingException) {
               logger.log(Level.WARNING, loadingException.getMessage());
               Alert failedLoadingAlert = new Alert(Alert.AlertType.ERROR, "Failed to load GroupSettings.fxml file.");
               failedLoadingAlert.showAndWait();
           }
       } else {
              logger.warning("Unauthorized access to group settings.");
              AlertConstruct.alertConstructor(
                      "Access Control Error",
                      "",
                      "You're not authorized to edit group settings.",
                      Alert.AlertType.ERROR
              );
       }
    }


    /**
     * Update the conversation list with the latest message and timestamp
     * This updates the UI without changing the main messages display area
     */
    private void updateConversationListWithMessage(MessageData msgData) {
        Platform.runLater(() -> {
            try {
                // Check direct conversations first
                java.util.List<javafx.scene.Node> conversationItems = conversationListContainer.getChildren();

                for (javafx.scene.Node item : conversationItems) {
                    if (item instanceof HBox) {
                        HBox convItem = (HBox) item;
                        Object userData = convItem.getUserData();

                        // Check if userData is a map containing conversation ID
                        if (userData instanceof java.util.Map) {
                            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) userData;
                            Object convId = dataMap.get("conversationId");

                            if (convId != null && convId.toString().equals(msgData.conversationId)) {
                                // Found the conversation item - update it with new message and time
                                updateConversationItemUI(convItem, msgData);
                                System.out.println("✅ Updated direct conversation: " + msgData.conversationId);
                                return;
                            }
                        }
                    }
                }

                // Check group conversations if not found in direct conversations
                java.util.List<javafx.scene.Node> groupItems = groupsListContainer.getChildren();

                for (javafx.scene.Node item : groupItems) {
                    if (item instanceof HBox) {
                        HBox groupItem = (HBox) item;
                        Object userData = groupItem.getUserData();

                        // Check if userData is a map containing conversation ID
                        if (userData instanceof java.util.Map) {
                            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) userData;
                            Object convId = dataMap.get("conversationId");

                            if (convId != null && convId.toString().equals(msgData.conversationId)) {
                                // Found the group item - update it with new message and time
                                updateConversationItemUI(groupItem, msgData);
                                System.out.println("✅ Updated group conversation: " + msgData.conversationId);
                                return;
                            }
                        }
                    }
                }

                System.out.println("⚠️  Conversation not found in list: " + msgData.conversationId);
            } catch (Exception e) {
                System.err.println("Error updating conversation list: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Update a specific conversation item UI with the latest message and timestamp
     */
    private void updateConversationItemUI(HBox conversationItem, MessageData msgData) {
        try {
            // The conversation item structure is typically:
            // HBox [ImageView (avatar)] [VBox [Label (name)], [Label (message)], [Label
            // (time)]]

            Label messageLabel = null;
            Label timeLabel = null;

            // Iterate through the children of the HBox to find the VBox with text labels
            for (javafx.scene.Node node : conversationItem.getChildren()) {
                if (node instanceof VBox) {
                    VBox vbox = (VBox) node;
                    java.util.List<javafx.scene.Node> vboxChildren = vbox.getChildren();

                    // Usually: label[0] = name, label[1] = message, label[2] = time
                    if (vboxChildren.size() >= 2) {
                        for (int i = 0; i < vboxChildren.size(); i++) {
                            javafx.scene.Node vnode = vboxChildren.get(i);
                            if (vnode instanceof Label) {
                                Label label = (Label) vnode;

                                // Try to identify which label is which
                                if (messageLabel == null && i == 1) {
                                    // Second label is usually the message
                                    messageLabel = label;
                                } else if (timeLabel == null && i == 2) {
                                    // Third label is usually the time
                                    timeLabel = label;
                                }
                            }
                        }
                    }
                }
            }

            // Update message label if found
            if (messageLabel != null) {
                String messagePreview = msgData.messageText;
                if (messagePreview.length() > 40) {
                    messagePreview = messagePreview.substring(0, 40) + "...";
                }
                messageLabel.setText(messagePreview);
                System.out.println("✅ Updated message preview: " + messagePreview);
            }

            // Update time label if found
            if (timeLabel != null) {
                String formattedTime = formatMessageTime(msgData.timestamp);
                timeLabel.setText(formattedTime);
                System.out.println("✅ Updated timestamp: " + formattedTime);
            }
        } catch (Exception e) {
            System.err.println("Error updating conversation item UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set the current conversation and show its messages
     */
    public void setCurrentConversation(ObjectId conversationId, Boolean isOnligne) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║ CONVERSATION OPENED - DEBUG INFO                  ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println("Conversation ID: " + conversationId);
        this.currentConversationId = conversationId;

        // Show loading spinner immediately
        Platform.runLater(() -> {
            if (messagesLoadingSpinnerContainer != null) {
                messagesLoadingSpinnerContainer.setVisible(true);
                messagesLoadingSpinnerContainer.setManaged(true);
            }
            if (chatHeader != null) {
                chatHeader.setVisible(true);
                chatHeader.setManaged(true);
            }
            if (messageInputArea != null) {
                messageInputArea.setVisible(true);
                messageInputArea.setManaged(true);
            }
            // Réinitialisez le padding/spacing
            if (messagesContainer != null) {
                messagesContainer.setPadding(new Insets(10));
                messagesContainer.setSpacing(10);
            }
            messagesContainer.getChildren().clear();
        });

        // Thread 1: Update chat header with friend's or group info in background
       Thread t8 = new Thread(() -> {
            try {
                String currentUserId = UserSession.getUserId();
                DBConnection dbConnection = this.dbConnection;
                org.bson.Document conversationDoc = dbConnection
                        .getDatabase()
                        .getCollection("Conversation")
                        .find(new org.bson.Document("_id", conversationId))
                        .first();

                if (conversationDoc != null) {
                    String conversationType = conversationDoc.getString("type");
                    System.out.println("Conversation Type: " + conversationType);

                    if ("group".equals(conversationType)) {
                        // ========== GROUP CONVERSATION ==========
                    	String groupId = conversationDoc.getString("groupId");
                        String groupName = conversationDoc.getString("groupName");
                        System.out.println("Group Name: " + groupName);
                        System.out.println("Group Conversation Loaded ✓");

                        this.currentRecipientId = null; // No single recipient for groups
                        this.currentRecipientName = null;

                        Platform.runLater(() -> {
                            MongoCollection<Document> groupsCollection = HomeControllerDBConnection.getCollection("groups");

                            String groupPhotoUrl = groupsCollection.find(
                                    Filters.eq("_id", new ObjectId(groupId))
                            ).first().getString("profile_picture");


                            // Update header
                            if (chatContactName != null) {
                                chatContactName.setText(groupName != null ? groupName : "Group Chat");

                                String groupAdmin = groupsCollection.find(
                                       Filters.eq("_id", new ObjectId(groupId))
                                ).first().getString("group_admin");



                                if (groupAdmin != null) {

                                      if (groupAdmin.equals(UserSession.getUsername())) {

                                                  groupSettingsBtn.setAlignment(Pos.CENTER_LEFT);
                                                  groupSettingsBtn.setVisible(true);
                                                  groupSettingsBtn.setManaged(true);
                                                  groupSettingsBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand");
                                      }

                                    if (groupSettingsBtn.isVisible() && groupSettingsBtn.isManaged()) {

                                        groupSettingsBtn.setOnMouseClicked(e->{

                                            GroupSettingsController.setGroupId(groupId);
                                            handleGroupSettingsClick(groupAdmin);
                                        });
                                    }
                                }

                            }

                            // For groups, don't show online/offline status
                            if (chatContactStatus != null) {
                                chatContactStatus.setText("Group");
                                chatContactStatus.setStyle("-fx-text-fill: #6b9e9e; -fx-font-size: 12px;");
                            }

                            if (chatContactStatusCircle != null) {
                                chatContactStatusCircle.setFill(javafx.scene.paint.Color.web("#6b9e9e"));
                            }

                            // Update right panel
                            if (rightContactName != null) {
                                rightContactName.setText(groupName != null ? groupName : "Group Chat");
                            }
                            if (rightContactTitle != null) {
                                rightContactTitle.setText("Group");
                            }
                            if (rightContactBio != null) {
                                rightContactBio.setText("");
                            }

                            // Update group images
                            String photoUrl = groupPhotoUrl != null ? groupPhotoUrl : "genki/img/group-default.png";

                            if (profilTrigger != null) {
                                try {
                                    Image groupImg = new Image(photoUrl, 180, 180, false, true);
                                    profilTrigger.setImage(groupImg);
                                    profilTrigger.setFitWidth(43);
                                    profilTrigger.setFitHeight(43);
                                    profilTrigger.setPreserveRatio(false);
                                    javafx.scene.shape.Circle groupClip = new javafx.scene.shape.Circle(21.5, 21.5,
                                            21.5);
                                    profilTrigger.setClip(groupClip);
                                    profilTrigger.getStyleClass().add("avatar");
                                } catch (Exception e) {
                                    System.out.println("Error loading group image: " + e.getMessage());
                                    profilTrigger
                                            .setImage(new Image("genki/img/group-default.png", 180, 180, false, true));
                                }
                            }

                            if (rightProfileImage != null) {
                                try {
                                    Image rightImg = new Image(photoUrl, 400, 400, false, true);
                                    rightProfileImage.setImage(rightImg);
                                    rightProfileImage.setFitWidth(100);
                                    rightProfileImage.setFitHeight(100);
                                    rightProfileImage.setPreserveRatio(false);
                                    javafx.scene.shape.Circle rightClip = new javafx.scene.shape.Circle(50, 50, 50);
                                    rightProfileImage.setClip(rightClip);
                                    rightProfileImage.getStyleClass().add("avatar");
                                } catch (Exception e) {
                                    System.out.println("Error loading right group image: " + e.getMessage());
                                    rightProfileImage
                                            .setImage(new Image("genki/img/group-default.png", 400, 400, false, true));
                                }
                            }

                            System.out.println("Group conversation set for: " + groupName);
                        });

                    } else {
                        // ========== DIRECT USER CONVERSATION ==========
                        java.util.List<?> participants = conversationDoc.getList("participantIds", Object.class);
                        String friendIdStr = null;
                        for (Object pid : participants) {
                            String pidStr = pid.toString();
                            if (!pidStr.equals(currentUserId)) {
                                friendIdStr = pidStr;
                                break;
                            }
                        }

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
                                    this.currentRecipientName = friendName;
                                    String photoUrl = friendDoc.getString("photo_url");
                                    String bio = friendDoc.getString("bio");
                                    String role = friendDoc.getString("role");

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
                                            String statusTextColor = isOnligne ? "-fx-text-fill: #4ade80"
                                                    : "-fx-text-fill: #9ca3af";
                                            chatContactStatus.setStyle(statusTextColor + "; -fx-font-size: 12px;");
                                        }

                                        if (chatContactStatusCircle != null) {
                                            javafx.scene.paint.Color statusColor = isOnligne
                                                    ? javafx.scene.paint.Color.web("#4ade80")
                                                    : javafx.scene.paint.Color.web("#9ca3af");
                                            chatContactStatusCircle.setFill(statusColor);
                                        }

                                        // Update profile images
                                        if (profilTrigger != null && photoUrl != null) {
                                            try {
                                                Image friendImg = new Image(photoUrl, 180, 180, false, true);
                                                profilTrigger.setImage(friendImg);
                                                profilTrigger.setFitWidth(43);
                                                profilTrigger.setFitHeight(43);
                                                profilTrigger.setPreserveRatio(false);
                                                javafx.scene.shape.Circle friendClip = new javafx.scene.shape.Circle(
                                                        21.5, 21.5, 21.5);
                                                profilTrigger.setClip(friendClip);
                                                profilTrigger.getStyleClass().add("avatar");
                                            } catch (Exception e) {
                                                System.out.println("Error loading profile image: " + e.getMessage());
                                                profilTrigger.setImage(
                                                        new Image("genki/img/user-default.png", 180, 180, false, true));
                                            }
                                        }

                                        // Update right panel profile image
                                        if (rightProfileImage != null && photoUrl != null) {
                                            try {
                                                Image rightImg = new Image(photoUrl, 400, 400, false, true);
                                                rightProfileImage.setImage(rightImg);
                                                rightProfileImage.setFitWidth(100);
                                                rightProfileImage.setFitHeight(100);
                                                rightProfileImage.setPreserveRatio(false);
                                                javafx.scene.shape.Circle rightClip = new javafx.scene.shape.Circle(50,
                                                        50, 50);
                                                rightProfileImage.setClip(rightClip);
                                                rightProfileImage.getStyleClass().add("avatar");
                                            } catch (Exception e) {
                                                System.out.println(
                                                        "Error loading right profile image: " + e.getMessage());
                                                rightProfileImage.setImage(
                                                        new Image("genki/img/user-default.png", 400, 400, false, true));
                                            }
                                        }
                                    });

                                    System.out.println(
                                            "User conversation set for: " + friendName + ", Online: " + isOnligne);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error updating chat header", e);
            }
        });
        t8.setDaemon(true);
        t8.start();

        // Thread 2: Load messages in parallel - runs at the same time as header update
        loadMessagesInBackground(conversationId);
    }

    /**
     * Load messages in a separate background thread (parallel to header update)
     */
    private void loadMessagesInBackground(ObjectId conversationId) {
        Thread t9 = new Thread(() -> {
            try {
                MessageDAO messageDAO = new MessageDAO();
                String currentUserId = genki.utils.UserSession.getUserId();

                // Fetch only the latest 50 messages for better performance
                List<org.bson.Document> messagesList = new ArrayList<>();
                messageDAO.getDatabase().getCollection("Message")
                        .find(new org.bson.Document("conversationId", conversationId))
                        .sort(new org.bson.Document("timestamp", -1)) // Latest first
                        .limit(50) // Only get last 50 messages
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

                        Object timestamp = doc.get("timestamp");
                        String formattedTime = formatTimestamp(timestamp);

                        if (senderId != null && senderId.equals(currentUserId)) {
                            messagesContainer.getChildren().add(
                                    MessageItemBuilder.createSentMessage(senderImageUrl, senderName, content,
                                            formattedTime));
                        } else {
                            messagesContainer.getChildren().add(
                                    MessageItemBuilder.createReceivedMessage(senderImageUrl, senderName, content,
                                            formattedTime));
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
        });
        t9.setDaemon(true);
        t9.start();
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
                messagesScrollPane.setVvalue(1.0); // 1.0 = bottom of scroll pane
            });
        }
    }

    /**
     * Format a timestamp to a readable time string
     * 
     * @param timestamp The timestamp in milliseconds or as a Date object
     * @return A formatted time string (HH:mm)
     */
    private String formatTimestamp(Object timestamp) {
        try {
            long timestampMs = 0;

            if (timestamp instanceof Number) {
                timestampMs = ((Number) timestamp).longValue();
            } else if (timestamp instanceof java.util.Date) {
                timestampMs = ((java.util.Date) timestamp).getTime();
            } else if (timestamp != null) {
                // Try to parse as string
                timestampMs = Long.parseLong(timestamp.toString());
            } else {
                return "";
            }

            // If timestamp is in seconds, convert to milliseconds
            if (timestampMs < 10000000000L) {
                timestampMs *= 1000;
            }

            java.time.LocalDateTime msgTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(timestampMs),
                    java.time.ZoneId.systemDefault());

            // Show time only format (HH:mm)
            java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            return msgTime.format(timeFormatter);
        } catch (Exception e) {
            return "";
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
                Color.rgb(71, 82, 87),
                new CornerRadii(8),
                Insets.EMPTY)));

        // Ajouter une ombre
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        dropShadow.setRadius(10);
        dropShadow.setOffsetY(3);
        menuContainer.setEffect(dropShadow);

        // Créer le bouton "Add User"
        Button addUserBtn = new Button("Add Friend");
        addUserBtn.setPrefWidth(150);
        // IMPROVEMENT 4: String Constants - Using style constants instead of hardcoded
        // strings
        addUserBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT);
        addUserBtn.setOnMouseEntered(e -> addUserBtn.setStyle(MENU_BUTTON_STYLE_HOVER));
        addUserBtn.setOnMouseExited(e -> addUserBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT));
        addUserBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5; " +
                        "-fx-alignment: CENTER-LEFT; " +
                        "-fx-font-size: 14px;");
        addUserBtn.setOnMouseEntered(e -> addUserBtn.setStyle(
                addUserBtn.getStyle() + "-fx-background-color: rgba(255, 255, 255, 0.1);"));
        addUserBtn.setOnMouseExited(e -> addUserBtn.setStyle(
                addUserBtn.getStyle().replace("-fx-background-color: rgba(255, 255, 255, 0.1);", "")));
        addUserBtn.setOnAction(e -> {
            addMenuPopup.hide();
            addMenuPopup = null;
            openAddUserDialog();
        });

        // Créer le bouton "Add Group"
        Button addGroupBtn = new Button("Add Group");
        addGroupBtn.setPrefWidth(150);
        // IMPROVEMENT 4: String Constants - Using style constants instead of hardcoded
        // strings
        addGroupBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT);
        addGroupBtn.setOnMouseEntered(e -> addGroupBtn.setStyle(MENU_BUTTON_STYLE_HOVER));
        addGroupBtn.setOnMouseExited(e -> addGroupBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT));
        addGroupBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: white; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5; " +
                        "-fx-alignment: CENTER-LEFT; " +
                        "-fx-font-size: 14px;");
        addGroupBtn.setOnMouseEntered(e -> addGroupBtn.setStyle(
                addGroupBtn.getStyle() + "-fx-background-color: rgba(255, 255, 255, 0.1);"));
        addGroupBtn.setOnMouseExited(e -> addGroupBtn.setStyle(
                addGroupBtn.getStyle().replace("-fx-background-color: rgba(255, 255, 255, 0.1);", "")));
        addGroupBtn.setOnAction(e -> {
            addMenuPopup.hide();
            addMenuPopup = null;
            openAddGroupDialog();
        });

        // ajouter ca
        // Créer le bouton "Join Group"
        Button joinGroupBtn = new Button("Join Group");
        joinGroupBtn.setPrefWidth(150);
        // IMPROVEMENT 4: String Constants - Using style constants instead of hardcoded
        // strings
        joinGroupBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT);
        joinGroupBtn.setOnMouseEntered(e -> joinGroupBtn.setStyle(MENU_BUTTON_STYLE_HOVER));
        joinGroupBtn.setOnMouseExited(e -> joinGroupBtn.setStyle(MENU_BUTTON_STYLE_DEFAULT));
        joinGroupBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5; " +
                        "-fx-alignment: CENTER-LEFT; " +
                        "-fx-font-size: 14px;");
        joinGroupBtn.setOnMouseEntered(e -> joinGroupBtn.setStyle(
                joinGroupBtn.getStyle() + "-fx-background-color: rgba(255, 255, 255, 0.1);"));
        joinGroupBtn.setOnMouseExited(e -> joinGroupBtn.setStyle(
                joinGroupBtn.getStyle().replace("-fx-background-color: rgba(255, 255, 255, 0.1);", "")));
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
            // hamza ajoute ca
            AddUserController controller = loader.getController();
            controller.setHomeController(this);
            // ---------------
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
            logger.log(Level.INFO, "Loading JoinGroup.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/JoinGroup.fxml"));
            Parent root = loader.load();

            // Get the JoinGroupController and set the HomeController reference
            JoinGroupController controller = loader.getController();
            controller.setHomeController(this);

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
            logger.log(Level.SEVERE, "Error loading JoinGroup dialog", e);
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to load JoinGroup dialog.");
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

            // Set HomeController reference in NotificationsController
            NotificationsController notificationsController = loader.getController();
            notificationsController.setHomeController(this);

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

                    // 🔥 Check if friend is currently online
                    boolean isOnline = false;
                    ArrayList<User> connectedUsers = UserSession.getConnectedUsers();
                    if (connectedUsers != null) {
                        for (User connectedUser : connectedUsers) {
                            String connectedUserId = connectedUser.getId();
                            if (connectedUserId != null && connectedUserId.equals(friendId)) {
                                isOnline = true;
                                break;
                            }
                        }
                    }
                    conversation.setOnline(isOnline);

                    conversations.add(conversation);
                }
            }

            // Initialize UserSession static lists
            UserSession.loadConversations(userFriends, conversations);
            System.out.println("Conversations : " + UserSession.getConversations());
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
                                                time = String.format("%02d:%02d", msgTime.getHour(),
                                                        msgTime.getMinute());
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
                            boolean isOnlineStatus = false;

                            // Check if friend is currently online by checking connected users list
                            ArrayList<User> connectedUsers = UserSession.getConnectedUsers();
                            if (connectedUsers != null && friendName != null) {
                                isOnlineStatus = connectedUsers.stream()
                                        .anyMatch(u -> u.getUsername() != null && u.getUsername().equals(friendName));
                            }

                            // Create final variable for use in lambda
                            final boolean isOnline = isOnlineStatus;
                            HBox conversationItem = ConversationItemBuilder.createConversationItem(
                                    photoUrl != null ? photoUrl : "genki/img/user-default.png",
                                    friendName,
                                    lastMessage != null && !lastMessage.isEmpty() ? lastMessage : "No messages yet",
                                    time != null && !time.isEmpty() ? time : "Just now",
                                    unreadCount,
                                    isOnline);

                            // Store the friend User object in the HBox for later reference
                            genki.models.User friendUser = new genki.models.User();
                            friendUser.setId(friendId);
                            friendUser.setUsername(friendName);
                            friendUser.setPhotoUrl(photoUrl);

                            // Create a map to store both user and conversation ID
                            java.util.Map<String, Object> userData = new java.util.HashMap<>();
                            userData.put("user", friendUser);
                            userData.put("conversationId", conversationId.toString());
                            conversationItem.setUserData(userData);

                            // Add click handler that dynamically checks current online status instead of
                            // using captured value
                            conversationItem.setOnMouseClicked(e -> {
                                // Dynamically check if user is currently online at click time
                                ArrayList<User> currentConnectedUsers = UserSession.getConnectedUsers();
                                boolean isCurrentlyOnline = currentConnectedUsers != null && friendName != null &&
                                        currentConnectedUsers.stream()
                                                .anyMatch(u -> u.getUsername() != null
                                                        && u.getUsername().equals(friendName));

                                // Set conversation with the current online status, not the captured one
                                setCurrentConversation(conversationId, isCurrentlyOnline);
                            });

                            // Store in UserSession for easy access from other files
                            UserSession.addConversationItem(conversationItem);

                            // Update UI on JavaFX thread - Thread Safe!
                            Platform.runLater(() -> {
                                conversationListContainer.getChildren().add(conversationItem);

                                // Update loading progress
                                synchronized (loadingLock) {
                                    loadedConversations++;
                                    System.out.println("Loaded " + loadedConversations + "/" + totalConversations
                                            + " conversations");

                                    // Hide spinner when all conversations are loaded
                                    if (loadedConversations >= totalConversations && loadingSpinnerContainer != null) {
                                        loadingSpinnerContainer.setVisible(false);
                                        loadingSpinnerContainer.setManaged(false);

                                        // Refresh online status NOW that all conversations are actually loaded
                                        System.out.println("✓ All " + UserSession.getConversationItems().size()
                                                + " conversation items loaded, refreshing online status...");
                                        UserSession.getClientSocket().refreshConversationOnlineStatus();
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

    /**
     * Load group conversations and cache HBox items for fast switching
     */
    private void loadGroupConversations() {
        try {
            String currentUserId = UserSession.getUserId();

            System.out.println("Loading group conversations for user: " + currentUserId);

            // Récupérer toutes les conversations de type "group" où l'utilisateur est
            // participant
            var groupConversations = this.dbConnection
                    .getDatabase()
                    .getCollection("Conversation")
                    .find(new org.bson.Document("type", "group")
                            .append("participantIds", new org.bson.Document("$in", List.of(currentUserId))));

            int groupCount = 0;
            for (org.bson.Document conversationDoc : groupConversations) {
                groupCount++;
                ObjectId conversationId = conversationDoc.getObjectId("_id");
                String groupName = conversationDoc.getString("groupName");
                if (groupName == null || groupName.isEmpty()) {
                    groupName = "Group Chat";
                }

                String lastMessage = conversationDoc.getString("lastMessageContent");
                if (lastMessage == null || lastMessage.isEmpty()) {
                    lastMessage = "No messages yet";
                }

                String time = "";
                Object lastMsgTimeObj = conversationDoc.get("lastMessageTime");
                if (lastMsgTimeObj != null) {
                    time = formatMessageTime(lastMsgTimeObj);
                }

                int unreadCount = 0;
                boolean isOnline = false;
                
                String groupPhotoUrl = conversationDoc.getString("profile_picture");
                if (groupPhotoUrl == null) {
                    groupPhotoUrl = "genki/img/group-default.png";
                }

                // Extract group information from conversation document
                String groupDescription = conversationDoc.getString("description");
                if (groupDescription == null) {
                    groupDescription = "";
                }

                String groupId = conversationDoc.getString("groupId");
                if (groupId == null) {
                    groupId = conversationId.toString(); // Fallback to conversation ID if groupId not set
                }

                Boolean isPublic = conversationDoc.getBoolean("isPublic", false);
                String groupAdmin = conversationDoc.getString("admin");
                if (groupAdmin == null) {
                    groupAdmin = "";
                }

                // Create Group object and add to UserSession
                Group group = new Group(
                        groupId, // Use the actual groupId instead of conversationId
                        groupName,
                        groupDescription,
                        isPublic,
                        groupPhotoUrl,
                        groupAdmin);

                // Extract participant IDs if available
                java.util.List<?> participantIds = conversationDoc.getList("participantIds", Object.class);
                if (participantIds != null) {
                    for (Object participantId : participantIds) {
                        group.addUser(participantId.toString());
                    }
                }

                UserSession.addGroup(group);
                logger.log(Level.INFO, "✓ Group added to UserSession: " + groupName + " (ConversationID: "
                        + conversationId + ", GroupID: " + groupId + ")");

                // Store conversation ID, group name, and groupId in userData map
                java.util.Map<String, Object> userData = new java.util.HashMap<>();
                userData.put("conversationId", conversationId.toString());
                userData.put("groupName", groupName);
                userData.put("groupId", groupId);

                HBox conversationItem = ConversationItemBuilder.createConversationItem(
                        groupPhotoUrl,
                        groupName,
                        lastMessage,
                        time,
                        unreadCount,
                        isOnline);

                // userData map already created above - no need to recreate it
                conversationItem.setUserData(userData);
                System.out.println("lllllllllllllllllllllll " + groupId);
                conversationItem.setOnMouseClicked(e -> setCurrentConversation(conversationId, isOnline));

                // Cache the group conversation item
                UserSession.addGroupConversationItem(conversationItem);
                System.out.println("Cached group: " + groupName);
            }

            System.out.println("Total groups loaded: " + groupCount);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading group conversations", e);
            e.printStackTrace();
        }
    }

    // hamza ajoute ca
    public void handleAddUserFromDialog(String username) {

        UserDAO userDAO = new UserDAO();

        // 1️⃣ Vérifier si l'utilisateur existe
        Document userDoc = userDAO.getUserByUsername(username);

        if (userDoc == null) {
            showAlert("User not found", "This user does not exist.");
            // return;
        }

        String friendId = userDoc.getObjectId("_id").toHexString();
        String currentUserId = UserSession.getUserId();

        // 2️⃣ Vérifier s'il existe déjà une conversation
        ConversationDAO conversationDAO = new ConversationDAO();

        ObjectId conversationId = conversationDAO.findDirectConversation(currentUserId, friendId);

        // 3️⃣ Si pas de conversation → créer
        if (conversationId == null) {
            conversationId = conversationDAO.createDirectConversation(currentUserId, friendId);
        }

        // 4️⃣ Afficher / ouvrir la conversation
        // openConversation(conversationId);
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
     * Affiche uniquement les conversations directes (users) - from cached
     * UserSession data
     */
    private void showUserConversations() {
        try {
            updateFilterButtonStyles(true);
            conversationListContainer.getChildren().clear();

            // Simply re-display the items that were already loaded in loadConversations()
            // UserSession stores the HBox conversation items directly
            List<HBox> conversationItems = UserSession.getConversationItems();

            if (conversationItems == null || conversationItems.isEmpty()) {
                Label noUsersLabel = new Label("No users found");
                noUsersLabel.setStyle(
                        "-fx-text-fill: white; " +
                                "-fx-font-size: 14px; " +
                                "-fx-padding: 20;");
                conversationListContainer.getChildren().add(noUsersLabel);
                logger.log(Level.INFO, "No conversations found");
                return;
            }

            // Add all cached conversation items back to the container
            for (HBox item : conversationItems) {
                conversationListContainer.getChildren().add(item);
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error displaying user conversations", e);
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
            groupsListContainer.getChildren().clear();

            // Simply re-display the cached group conversation items
            List<HBox> groupItems = UserSession.getGroupConversationItems();

            if (groupItems == null || groupItems.isEmpty()) {
                Label noGroupsLabel = new Label("No groups found");
                noGroupsLabel.setStyle(
                        "-fx-text-fill: #6b9e9e; " +
                                "-fx-font-size: 14px; " +
                                "-fx-padding: 20;");
                groupsListContainer.getChildren().add(noGroupsLabel);
                return;
            }

            // Add all cached group conversation items back to the container
            for (HBox item : groupItems) {
                groupsListContainer.getChildren().add(item);
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error displaying group conversations", e);
        }
    }

    // aussi ca par hamza
    /**
     * Met à jour les styles des boutons de filtre
     * 
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
                                "-fx-padding: 8 16;");
                btnGroups.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #a0a0a0; " +
                                "-fx-background-radius: 20; " +
                                "-fx-padding: 8 16;");
            } else {
                btnAll.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #a0a0a0; " +
                                "-fx-background-radius: 20; " +
                                "-fx-padding: 8 16;");
                btnGroups.setStyle(
                        "-fx-background-color: #2bfbfb; " +
                                "-fx-text-fill: #232e2e; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 20; " +
                                "-fx-padding: 8 16;");
            }
        }
    }

    /**
     * Formate l'heure du dernier message
     * 
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
                    return String.format("%02d:%02d", msgTime.getHour(), msgTime.getMinute());
                } else {
                    return String.format("%02d/%02d/%02d",
                            msgTime.getDayOfMonth(),
                            msgTime.getMonthValue(),
                            msgTime.getYear() % 100);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error formatting message time", e);
        }
        return "";
    }

    public void loadGroups() {
        // This method is no longer needed since we use group conversations instead
        // Kept for backward compatibility
    }

    /**
     * Called when user joins a group to immediately display it
     */
    public void addNewGroupToUI(HBox groupItem) {
        Platform.runLater(() -> {
            // Remove "No groups found" label if exists
            groupsListContainer.getChildren()
                    .removeIf(node -> node instanceof Label && ((Label) node).getText().equals("No groups found"));

            // Add new group to top of list
            groupsListContainer.getChildren().add(0, groupItem);
            System.out.println("✅ New group displayed in UI");
        });
    }

    public void handleAddGroupFromDialog(Group newGroup) {
        Platform.runLater(() -> {
            String currentUserId = UserSession.getUserId();

            // Get the group's user list and add current user if not already there
            ArrayList<String> participantIds = new ArrayList<>(
                    newGroup.getListUsers() != null ? newGroup.getListUsers() : new ArrayList<>());
            if (!participantIds.contains(currentUserId)) {
                participantIds.add(currentUserId);
            }

            // Create Conversation object for this group in the database
            ConversationDAO conversationDAO = new ConversationDAO();
            ObjectId conversationId = conversationDAO.createGroupConversation(
                    participantIds,
                    newGroup.getGroupName(),
                    newGroup.getGroupProfilePicture(),
                    newGroup.getGroupId() // Pass the group ID
            );

            if (conversationId != null) {
                // Clear "No groups found" message if exists
                if (groupsListContainer.getChildren().isEmpty()) {
                    groupsListContainer.getChildren().clear();
                } else {
                    groupsListContainer.getChildren().removeIf(
                            node -> node instanceof Label && ((Label) node).getText().equals("No groups found"));
                }

                // Create UI item using standard conversation builder
                HBox newGroupContainer = ConversationItemBuilder.createConversationItem(
                        newGroup.getGroupProfilePicture(),
                        newGroup.getGroupName(),
                        "No messages yet",
                        "Just now",
                        0,
                        false);

                // Add click handler to open group conversation
                newGroupContainer.setOnMouseClicked(e -> setCurrentConversation(conversationId, false));

                // 🔥 CRITICAL: Add to cache so it persists when switching views
                UserSession.addGroupConversationItem(newGroupContainer);

                // Display immediately in UI
                groupsListContainer.getChildren().add(0, newGroupContainer);

                if (!usersPane.isVisible()) {
                    switchUsers(false); // Switch to groups view if not already there
                }

                System.out.println("✅ New group added and cached: " + newGroup.getGroupName());
            }
        });
    }

    /**
     * Update the chat header status circle and text when a user's online status
     * changes
     * Only updates if the changed user is the currently open conversation
     * 
     * @param userId   The ID of the user whose status changed
     * @param userName The username of the user whose status changed
     * @param isOnline true if the user is online, false if offline
     */
    public void updateChatHeaderStatusForUser(String userId, String userName, boolean isOnline) {
        Platform.runLater(() -> {
            // Check if this is the currently open conversation
            if ((currentRecipientId != null && currentRecipientId.equals(userId)) ||
                    (currentRecipientName != null && currentRecipientName.equals(userName))) {

                // Update the status circle color
                if (chatContactStatusCircle != null) {
                    chatContactStatusCircle.setFill(
                            isOnline ? Color.web("#4ade80") : Color.web("#9ca3af"));
                }

                // Update the status label text
                if (chatContactStatus != null) {
                    chatContactStatus.setText(isOnline ? "Online" : "Offline");
                }

                System.out.println("✓ Updated chat header status for " + userName +
                        " to " + (isOnline ? "ONLINE" : "OFFLINE"));
            }
        });
    }

    /**
     * Add a group conversation to the UI immediately when a group join request is
     * accepted
     * This is called when receiving GROUP_JOIN_ACCEPTED socket message
     * 
     * @param groupId   The ID of the group
     * @param groupName The name of the group
     */
    public void addGroupConversationFromAcceptance(String groupId, String groupName) {
        Platform.runLater(() -> {
            try {
                String currentUserId = UserSession.getUserId();

                // First, try to find by groupId (preferred)
                var groupConversations = this.dbConnection
                        .getDatabase()
                        .getCollection("Conversation")
                        .find(new org.bson.Document("groupId", groupId)
                                .append("type", "group"));

                org.bson.Document conversationDoc = groupConversations.first();

                // If not found by groupId, try by groupName (fallback for older conversations)
                if (conversationDoc == null) {
                    logger.log(Level.INFO, "GroupId not found, searching by groupName: " + groupName);
                    groupConversations = dbConnection
                            .getDatabase()
                            .getCollection("Conversation")
                            .find(new org.bson.Document("groupName", groupName)
                                    .append("type", "group"));
                    conversationDoc = groupConversations.first();
                }

                if (conversationDoc != null) {
                    ObjectId conversationId = conversationDoc.getObjectId("_id");

                    // Update the conversation document to include groupId if it doesn't have it
                    if (conversationDoc.getString("groupId") == null) {
                        this.dbConnection.getDatabase()
                                .getCollection("Conversation")
                                .updateOne(
                                        new org.bson.Document("_id", conversationId),
                                        new org.bson.Document("$set", new org.bson.Document("groupId", groupId)));
                        logger.log(Level.INFO, "✓ Updated conversation with groupId: " + groupId);
                    }

                    // Check if current user is already a participant
                    java.util.List<?> participantIds = conversationDoc.getList("participantIds", Object.class);
                    boolean isParticipant = participantIds != null && participantIds.stream()
                            .anyMatch(id -> id.toString().equals(currentUserId));

                    // If not a participant, add them
                    if (!isParticipant) {
                        this.dbConnection.getDatabase()
                                .getCollection("Conversation")
                                .updateOne(
                                        new org.bson.Document("_id", conversationId),
                                        new org.bson.Document("$addToSet",
                                                new org.bson.Document("participantIds", currentUserId)));
                        logger.log(Level.INFO, "✓ Added user to group conversation participants");
                    }

                    String lastMessage = conversationDoc.getString("lastMessageContent");
                    if (lastMessage == null || lastMessage.isEmpty()) {
                        lastMessage = "No messages yet";
                    }

                    String time = "";
                    Object lastMsgTimeObj = conversationDoc.get("lastMessageTime");
                    if (lastMsgTimeObj != null) {
                        time = formatMessageTime(lastMsgTimeObj);
                    }

                    String groupPhotoUrl = conversationDoc.getString("photo_url");
                    if (groupPhotoUrl == null) {
                        groupPhotoUrl = "genki/img/group-default.png";
                    }

                    // Create UI item for the group conversation
                    HBox newGroupContainer = ConversationItemBuilder.createConversationItem(
                            groupPhotoUrl,
                            groupName,
                            lastMessage,
                            time,
                            0, // unread count
                            false // is online
                    );

                    // Store conversation ID and group ID in userData map
                    java.util.Map<String, Object> userData = new java.util.HashMap<>();
                    userData.put("conversationId", conversationId.toString());
                    userData.put("groupName", groupName);
                    userData.put("groupId", groupId);
                    newGroupContainer.setUserData(userData);

                    // Add click handler to open group conversation
                    newGroupContainer.setOnMouseClicked(e -> setCurrentConversation(conversationId, false));

                    // Cache the group conversation item
                    if(!UserSession.getGroupConversationItems().contains(newGroupContainer)){
                        UserSession.addGroupConversationItem(newGroupContainer);
                    }

                    // Add to the groups list container in UI
                    groupsListContainer.getChildren().add(0, newGroupContainer);

                    // Remove "No groups found" label if present
                    groupsListContainer.getChildren().removeIf(
                            node -> node instanceof Label && ((Label) node).getText().equals("No groups found"));

                    // Switch to groups view if not already there
                    if (!groupsPane.isVisible()) {
                        switchUsers(false);
                    }

                    logger.log(Level.INFO, "✅ Group conversation added to UI from acceptance: " + groupName
                            + " (GroupID: " + groupId + ")");
                } else {
                    logger.log(Level.WARNING,
                            "⚠️ Could not find conversation for group: " + groupName + " (GroupID: " + groupId + ")");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "❌ Error adding group conversation from acceptance", e);
                e.printStackTrace();
            }
        });
    }

    /**
     * Add a friend conversation to the UI immediately when a friend request is
     * accepted
     * This is called when receiving FRIEND_REQUEST_ACCEPTED socket message
     * 
     * @param friendUsername The username of the new friend
     */
    public void addFriendConversationFromAcceptance(String friendUsername) {
        Platform.runLater(() -> {
            try {
                // Fetch the friend's information from database
                UserDAO userDAO = new UserDAO();
                Document friendDoc = userDAO.getUserByUsername(friendUsername);

                if (friendDoc == null) {
                    logger.log(Level.WARNING, "⚠️ Could not find friend in database: " + friendUsername);
                    return;
                }

                String friendId = friendDoc.getObjectId("_id").toHexString();
                String currentUserId = UserSession.getUserId();

                // Find or create the direct conversation with this friend
                ConversationDAO conversationDAO = new ConversationDAO();
                ObjectId conversationId = conversationDAO.findDirectConversation(currentUserId, friendId);

                // If conversation doesn't exist, create it
                if (conversationId == null) {
                    conversationId = conversationDAO.createDirectConversation(currentUserId, friendId);
                    logger.log(Level.INFO, "✓ Created new direct conversation with friend: " + friendUsername);
                }

                if (conversationId == null) {
                    logger.log(Level.WARNING,
                            "⚠️ Could not create or find conversation with friend: " + friendUsername);
                    return;
                }

                // Get friend's profile picture
                String friendPhotoUrl = friendDoc.getString("photo_url");
                if (friendPhotoUrl == null) {
                    friendPhotoUrl = "genki/img/user-default.png";
                }

                // Check if friend is currently online
                boolean isFriendOnline = false;
                ArrayList<genki.models.User> connectedUsers = UserSession.getConnectedUsers();
                if (connectedUsers != null) {
                    isFriendOnline = connectedUsers.stream()
                            .anyMatch(u -> u.getId() != null && u.getId().equals(friendId));

                }

                // Create UI item for the friend conversation with correct online status
                HBox newFriendContainer = ConversationItemBuilder.createConversationItem(
                        friendPhotoUrl,
                        friendUsername,
                        "No messages yet",
                        "",
                        0, // unread count
                        isFriendOnline // Use actual online status
                );

                // Store conversation ID and friend info in userData map
                java.util.Map<String, Object> userData = new java.util.HashMap<>();
                userData.put("conversationId", conversationId.toString());
                userData.put("friendName", friendUsername);
                userData.put("friendId", friendId);
                newFriendContainer.setUserData(userData);

                // Add click handler to open friend conversation - check online status
                // dynamically
                final ObjectId finalConversationId = conversationId;
                newFriendContainer.setOnMouseClicked(e -> {
                    // Determine if friend is actually online by checking server's connected clients
                    boolean isCurrentlyOnline = checkIfUserIsOnline(friendId);
                    setCurrentConversation(finalConversationId, isCurrentlyOnline);
                });

                // Cache the conversation item
                UserSession.addConversationItem(newFriendContainer);
                System.out.println(UserSession.conversationItems);
                // Also add to friends list if not already there
                genki.models.User friendUser = new genki.models.User();
                friendUser.setId(friendId);
                friendUser.setUsername(friendUsername);
                friendUser.setPhotoUrl(friendPhotoUrl);
                ArrayList<genki.models.User> friends = UserSession.getFriends();
                if (friends != null && !friends.stream().anyMatch(u -> u.getId().equals(friendId))) {
                    friends.add(friendUser);
                }

                // Add to the users conversation list container in UI
                conversationListContainer.getChildren().add(0, newFriendContainer);

                // Remove "No conversations found" label if present
                conversationListContainer.getChildren().removeIf(
                        node -> node instanceof Label && ((Label) node).getText().equals("No conversations found"));

                // Switch to users view if not already there
                if (!usersPane.isVisible()) {
                    switchUsers(true);
                }

                logger.log(Level.INFO, "✅ Friend conversation added to UI from acceptance: " + friendUsername
                        + " (FriendID: " + friendId + ")");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "❌ Error adding friend conversation from acceptance", e);
                e.printStackTrace();
            }
        });
    }

    /**
     * Check if a user is currently online by querying the server
     * 
     * @param userId The user ID to check
     * @return true if the user is connected to the server, false otherwise
     */
    private boolean checkIfUserIsOnline(String userId) {
        try {
            // Query the users collection to check the last_activity timestamp
            // A user is considered online if their last activity is recent (within last 2
            // minutes)
            for (User usr : UserSession.getConnectedUsers()) {
                if (usr.getId().equals(userId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error checking user online status: " + e.getMessage());
        }
        return false;
    }
}