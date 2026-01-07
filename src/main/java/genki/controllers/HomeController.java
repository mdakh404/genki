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
    private String currentRecipientId = null;
    private String currentRecipientName = null;

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
    private Label notificationBadge;

    public void loadNotifications() {

        try {

            MongoCollection<Document> notificationsCollection = dbConnection.getCollection("notifications");

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

        updateNotificationBadge();
    }

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

        javafx.scene.Parent btnParent = btnNotifications.getParent();
        if (btnParent instanceof javafx.scene.layout.HBox) {
            javafx.scene.layout.HBox parentHBox = (javafx.scene.layout.HBox) btnParent;
            int btnIndex = parentHBox.getChildren().indexOf(btnNotifications);
            if (btnIndex >= 0) {
                parentHBox.getChildren().add(btnIndex + 1, notificationBadge);

                notificationBadge.setTranslateX(-20);
                notificationBadge.setTranslateY(-8);

                logger.info(" Notification badge overlay positioned on icon");
            }
        }
    }

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

    public void setupNotificationListener() {
        if (UserSession.getClientSocket() == null) {
            logger.warning("Client socket is not initialized yet");
            return;
        }

        UserSession.getClientSocket().setOnNewNotificationCallback(notification -> {
            logger.info(
                    " New notification received: " + notification.getSenderName() + " - " + notification.getType());

            Platform.runLater(() -> {
                try {
                    UserSession.addNotification(notification);

                    updateNotificationBadge();

                    logger.info("Notification badge updated - New count: " + UserSession.getNotifications().size());

                } catch (Exception e) {
                    logger.warning("Error processing incoming notification: " + e.getMessage());
                }
            });
        });

        logger.info("Real-time notification listener registered successfully");
    }

    private void cleanupOldNotifications() {
        Thread t1 = new Thread(() -> {
            try {
                MongoCollection<Document> notificationsCollection = dbConnection.getCollection("notifications");

                java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
                java.time.ZonedDateTime zdt = thirtyDaysAgo.atZone(java.time.ZoneId.systemDefault());
                java.util.Date cutoffDate = java.util.Date.from(zdt.toInstant());

                long deletedCount = notificationsCollection.deleteMany(
                        Filters.and(
                                Filters.lt("createdAt", cutoffDate),
                                Filters.ne("status", "pending")))
                        .getDeletedCount();

                if (deletedCount > 0) {
                    logger.info(" Cleaned up " + deletedCount + " old notifications");
                }
            } catch (MongoException e) {
                logger.warning("Error during notification cleanup: " + e.getMessage());
            }
        });
        t1.setDaemon(true);
        t1.start();
    }

    private void startNotificationCleanupScheduler() {
        Thread t2 = new Thread(() -> {
            while (true) {
                try {
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

    public void handleLogout() {
        UserSession.logout();
        try {
            ScenesController.switchToScene("/genki/views/Login.fxml", "Genki - Sign in");
        } catch (IOException ex) {
            logger.info("Error loading Login.fxml " + ex.getMessage());
        }
    }

    private void switchUsers(boolean switchToUsers) {
        usersPane.setVisible(switchToUsers);
        usersPane.setManaged(switchToUsers);

        groupsPane.setVisible(!switchToUsers);
        groupsPane.setManaged(!switchToUsers);

        Platform.runLater(() -> {
            if (switchToUsers) {
                btnAll.setStyle(
                        "-fx-background-color: #2bfbfb; -fx-text-fill: #232e2e; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 16;");
                btnGroups.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-background-radius: 20; -fx-padding: 8 16;");
            } else {
                btnAll.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #a0a0a0; -fx-background-radius: 20; -fx-padding: 8 16;");
                btnGroups.setStyle(
                        "-fx-background-color: #2bfbfb; -fx-text-fill: #232e2e; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 16;");
            }
        });
    }

    private Popup addMenuPopup;

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

    private DBConnection dbConnection;

    @FXML
    public void initialize() {
        dbConnection = DBConnection.getInstance("genki_testing");

        Platform.runLater(() -> setupNotificationBadge());

        switchUsers(true);

        if (UserSession.getGroups().isEmpty() && UserSession.getConversations().isEmpty()) {
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

            scrollToBottom();

            MessageData msgData = new MessageData(
                    currentConversationId.toString(),
                    senderId,
                    senderName,
                    messageText,
                    senderImageUrl,
                    System.currentTimeMillis(),
                    currentRecipientId,
                    currentRecipientName
            );

            String jsonMessage = genki.utils.GsonUtility.getGson().toJson(msgData);
            UserSession.getClientSocket().sendMessages(jsonMessage);

            updateConversationListWithMessage(msgData);

            Thread t3 = new Thread(() -> {
                MessageDAO messageDAO = new MessageDAO();
                messageDAO.sendMessage(currentConversationId, senderId, senderName, senderImageUrl, messageText);
            });
            t3.setDaemon(true);
            t3.start();
        });

        Thread t4 = new Thread(() -> {
            try {
                loadConversations();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error loading conversations in background", e);
            }
        });
        t4.setDaemon(true);
        t4.start();

        Thread t5 = new Thread(() -> {
            try {
                loadGroupConversations();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error loading group conversations in background", e);
            }
        });
        t5.setDaemon(true);
        t5.start();

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
            if (btnAll != null) {
                btnAll.setOnMouseClicked(e -> {
                    switchUsers(true);
                    showUserConversations();
                });
            }
            if (btnGroups != null) {
                btnGroups.setOnMouseClicked(e -> {
                    switchUsers(false);
                    showGroupConversations(); 
                });
            }

            if (btnNotifications != null) {
                btnNotifications.setOnMouseClicked(e -> openNotifications());
            }

            UserSession.getClientSocket().setOnNewMessageCallback(msgData -> {
                

                if (msgData.conversationId != null) {
                    updateConversationListWithMessage(msgData);
                }

                 if (msgData.conversationId != null && currentConversationId != null) {
                  
                    String msgConvId = msgData.conversationId;
                    String currentConvId = currentConversationId.toString();
                 

                    if (msgConvId.equals(currentConvId)) {
                       
                        Platform.runLater(() -> {
                            messagesContainer.getChildren().add(
                                    MessageItemBuilder.createReceivedMessage(
                                            msgData.senderProfileImage,
                                            msgData.senderName,
                                            msgData.messageText));
                            scrollToBottom();
                        });
                    } else {
                        
                    }
                } else {
                    
                }
            });

            UserSession.getClientSocket().setHomeController(this);

            Thread t7 = new Thread(() -> {
                try {
                    setupNotificationListener();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error setting up notification listener in background", e);
                }
            });
            t7.setDaemon(true);
            t7.start();

            startNotificationCleanupScheduler();
        }
    }

    private void handleGroupSettingsClick(String groupAdmin) {

       if (groupAdmin.equals(UserSession.getUsername())) {
           Platform.runLater(() -> {
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
           });
       } else {
              Platform.runLater(() -> {
                  logger.warning("Unauthorized access to group settings.");
                  AlertConstruct.alertConstructor(
                          "Access Control Error",
                          "",
                          "You're not authorized to edit group settings.",
                          Alert.AlertType.ERROR
                  );
              });
       }
    }


    private void updateConversationListWithMessage(MessageData msgData) {
        Platform.runLater(() -> {
            try {
                java.util.List<javafx.scene.Node> conversationItems = conversationListContainer.getChildren();

                for (javafx.scene.Node item : conversationItems) {
                    if (item instanceof HBox) {
                        HBox convItem = (HBox) item;
                        Object userData = convItem.getUserData();

                        if (userData instanceof java.util.Map) {
                            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) userData;
                            Object convId = dataMap.get("conversationId");

                            if (convId != null && convId.toString().equals(msgData.conversationId)) {
                                updateConversationItemUI(convItem, msgData);
                                
                                return;
                            }
                        }
                    }
                }

                java.util.List<javafx.scene.Node> groupItems = groupsListContainer.getChildren();

                for (javafx.scene.Node item : groupItems) {
                    if (item instanceof HBox) {
                        HBox groupItem = (HBox) item;
                        Object userData = groupItem.getUserData();

                        if (userData instanceof java.util.Map) {
                            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) userData;
                            Object convId = dataMap.get("conversationId");

                            if (convId != null && convId.toString().equals(msgData.conversationId)) {
                                updateConversationItemUI(groupItem, msgData);
                               
                                return;
                            }
                        }
                    }
                }

                
            } catch (Exception e) {
                
                e.printStackTrace();
            }
        });
    }

    private void updateConversationItemUI(HBox conversationItem, MessageData msgData) {
        try {
            Label messageLabel = null;
            Label timeLabel = null;

            for (javafx.scene.Node node : conversationItem.getChildren()) {
                if (node instanceof VBox) {
                    VBox vbox = (VBox) node;
                    java.util.List<javafx.scene.Node> vboxChildren = vbox.getChildren();

                    if (vboxChildren.size() >= 2) {
                        for (int i = 0; i < vboxChildren.size(); i++) {
                            javafx.scene.Node vnode = vboxChildren.get(i);
                            if (vnode instanceof Label) {
                                Label label = (Label) vnode;

                                if (messageLabel == null && i == 1) {
                                    messageLabel = label;
                                } else if (timeLabel == null && i == 2) {
                                    timeLabel = label;
                                }
                            }
                        }
                    }
                }
            }

            if (messageLabel != null) {
                String messagePreview = msgData.messageText;
                if (messagePreview.length() > 40) {
                    messagePreview = messagePreview.substring(0, 40) + "...";
                }
                messageLabel.setText(messagePreview);
               
            }

            if (timeLabel != null) {
                String formattedTime = formatMessageTime(msgData.timestamp);
                timeLabel.setText(formattedTime);
              
            }
        } catch (Exception e) {
            System.err.println("Error updating conversation item UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setCurrentConversation(ObjectId conversationId, Boolean isOnligne) {
      
        this.currentConversationId = conversationId;

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
         
            if (messagesContainer != null) {
                messagesContainer.setPadding(new Insets(10));
                messagesContainer.setSpacing(10);
            }
            messagesContainer.getChildren().clear();
        });

      
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
                      
                    	String groupId = conversationDoc.getString("groupId");
                        String groupName = conversationDoc.getString("groupName");
                        System.out.println("Group Name: " + groupName);
                        System.out.println("Group Conversation Loaded ✓");

                        this.currentRecipientId = null;
                        this.currentRecipientName = null;

                        Platform.runLater(() -> {
                            if (rightSideContainer != null) {
                                rightSideVisibilite = false;
                                rightSideContainer.setPrefWidth(0.0);
                                rightSideContainer.setMinWidth(0.0);
                                rightSideContainer.setMaxWidth(0.0);
                                rightSideContainer.setManaged(false);
                                rightSideContainer.setVisible(false);
                            }

                            MongoCollection<Document> groupsCollection = HomeControllerDBConnection.getCollection("groups");

                            String groupPhotoUrl = groupsCollection.find(
                                    Filters.eq("_id", new ObjectId(groupId))
                            ).first().getString("profile_picture");


                           
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

                            if (chatContactStatus != null) {
                                chatContactStatus.setText("Group");
                                chatContactStatus.setStyle("-fx-text-fill: #6b9e9e; -fx-font-size: 12px;");
                            }

                            if (chatContactStatusCircle != null) {
                                chatContactStatusCircle.setFill(javafx.scene.paint.Color.web("#6b9e9e"));
                            }

                            if (rightContactName != null) {
                                rightContactName.setText(groupName != null ? groupName : "Group Chat");
                            }
                            if (rightContactTitle != null) {
                                rightContactTitle.setText("Group");
                            }
                            if (rightContactBio != null) {
                                rightContactBio.setText("");
                            }

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

                            
                        });

                    } else {
                       
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
                                        groupSettingsBtn.setVisible(false);
                                        groupSettingsBtn.setManaged(false);
                                        
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

        
        loadMessagesInBackground(conversationId);
    }

    private void loadMessagesInBackground(ObjectId conversationId) {
        Thread t9 = new Thread(() -> {
            try {
                MessageDAO messageDAO = new MessageDAO();
                String currentUserId = genki.utils.UserSession.getUserId();

                
                List<org.bson.Document> messagesList = new ArrayList<>();
                messageDAO.getDatabase().getCollection("Message")
                        .find(new org.bson.Document("conversationId", conversationId))
                        .sort(new org.bson.Document("timestamp", -1)) 
                        .limit(50) 
                        .forEach((java.util.function.Consumer<org.bson.Document>) messagesList::add);

                
                java.util.Collections.reverse(messagesList);

              
                Platform.runLater(() -> {
                    messagesContainer.getChildren().clear();

                    for (org.bson.Document doc : messagesList) {
                        String senderId = doc.getString("senderId");
                        String senderName = doc.getString("senderName");
                        String content = doc.getString("content");
                        String senderImageUrl = doc.getString("senderImageUrl");
                        if (senderImageUrl == null) {
                            senderImageUrl = doc.getString("photo_url");
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

                   
                    if (messagesLoadingSpinnerContainer != null) {
                        messagesLoadingSpinnerContainer.setVisible(false);
                        messagesLoadingSpinnerContainer.setManaged(false);
                    }

                    
                    scrollToBottom();
                });
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error loading messages for conversation", e);
                
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

    

    private void scrollToBottom() {
        if (messagesScrollPane != null) {
            Platform.runLater(() -> {
                messagesScrollPane.setVvalue(1.0);
            });
        }
    }

    private String formatTimestamp(Object timestamp) {
        try {
            long timestampMs = 0;

            if (timestamp instanceof Number) {
                timestampMs = ((Number) timestamp).longValue();
            } else if (timestamp instanceof java.util.Date) {
                timestampMs = ((java.util.Date) timestamp).getTime();
            } else if (timestamp != null) {
                timestampMs = Long.parseLong(timestamp.toString());
            } else {
                return "";
            }

            if (timestampMs < 10000000000L) {
                timestampMs *= 1000;
            }

            java.time.LocalDateTime msgTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(timestampMs),
                    java.time.ZoneId.systemDefault());

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
        if (addMenuPopup != null && addMenuPopup.isShowing()) {
            addMenuPopup.hide();
            addMenuPopup = null;
            return;
        }

        VBox menuContainer = new VBox(5);
        menuContainer.setPadding(new Insets(10));
        menuContainer.setMaxWidth(100);
        menuContainer.setBackground(new Background(new BackgroundFill(
                Color.rgb(71, 82, 87),
                new CornerRadii(8),
                Insets.EMPTY)));

        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        dropShadow.setRadius(10);
        dropShadow.setOffsetY(3);
        menuContainer.setEffect(dropShadow);

        Button addUserBtn = new Button("Add Friend");
        addUserBtn.setPrefWidth(150);
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

        Button addGroupBtn = new Button("Add Group");
        addGroupBtn.setPrefWidth(150);
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

        Button joinGroupBtn = new Button("Join Group");
        joinGroupBtn.setPrefWidth(150);
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

        menuContainer.getChildren().addAll(addUserBtn, addGroupBtn, joinGroupBtn);

        addMenuPopup = new Popup();
        addMenuPopup.setAutoHide(true);
        addMenuPopup.getContent().add(menuContainer);

        Bounds bounds = btnAdd.localToScreen(btnAdd.getBoundsInLocal());
        addMenuPopup.show(btnAdd, bounds.getMinX(), bounds.getMaxY());
    }

    private void openAddUserDialog() {
        try {
            logger.log(Level.INFO, "Loading AddUser.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/AddUser.fxml"));
            Parent root = loader.load();
            AddUserController controller = loader.getController();
            controller.setHomeController(this);
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

    private void openJoinGroupDialog() {
        try {
            logger.log(Level.INFO, "Loading JoinGroup.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/JoinGroup.fxml"));
            Parent root = loader.load();

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

    @FXML
    public void openNotifications() {
        try {
            logger.log(Level.INFO, "Loading Notifications.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/Notifications.fxml"));
            Parent root = loader.load();

            
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
                "url/to/image.png", 
                "Sarah Wilson", 
                "Sent a photo",
                "12:45 PM", 
                2, 
                true 
        );

        conversationListContainer.getChildren().add(conversationItem);
    }

    private void loadConversations() {
        try {
            UserDAO userDAO = new UserDAO();
            String currentUsername = UserSession.getUsername();

            
            List<Document> friends = userDAO.getFriendsForUser(currentUsername);

            
            Platform.runLater(() -> {
                if (loadingSpinnerContainer != null) {
                    loadingSpinnerContainer.setVisible(true);
                    loadingSpinnerContainer.setManaged(true);
                }
            });

           
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

            
            ArrayList<genki.models.Conversation> conversations = new ArrayList<>();
            if (friends != null) {
                String currentUserId = UserSession.getUserId();
                for (Document friendDoc : friends) {
                    genki.models.Conversation conversation = new genki.models.Conversation();
                    conversation.setType("direct");

                    
                    String friendId = friendDoc.getObjectId("_id").toHexString();
                    List<String> participantIds = new ArrayList<>();
                    participantIds.add(currentUserId);
                    participantIds.add(friendId);
                    conversation.setParticipantIds(participantIds);

                    
                    conversation.setLastMessageContent(friendDoc.getString("lastMessageContent"));
                    conversation.setLastMessageSenderId(friendDoc.getString("lastMessageSenderId"));

                    
                    Object lastMsgTimeObj = friendDoc.get("lastMessageTime");
                    if (lastMsgTimeObj instanceof java.time.LocalDateTime) {
                        conversation.setLastMessageTime((java.time.LocalDateTime) lastMsgTimeObj);
                    } else if (lastMsgTimeObj instanceof java.util.Date) {
                        java.util.Date date = (java.util.Date) lastMsgTimeObj;
                        conversation.setLastMessageTime(java.time.LocalDateTime.ofInstant(date.toInstant(),
                                java.time.ZoneId.systemDefault()));
                    }

                    
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

           
            UserSession.loadConversations(userFriends, conversations);
            

            if (UserSession.getFriends() == null || UserSession.getFriends().isEmpty()) {
                logger.log(Level.INFO, "No friends found for user: " + currentUsername);
              
                Platform.runLater(() -> {
                    if (loadingSpinnerContainer != null) {
                        loadingSpinnerContainer.setVisible(false);
                        loadingSpinnerContainer.setManaged(false);
                    }
                });
                return;
            }

            
            synchronized (loadingLock) {
                totalConversations = friends.size();
                loadedConversations = 0;
            }

            
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

                            
                            ArrayList<User> connectedUsers = UserSession.getConnectedUsers();
                            if (connectedUsers != null && friendName != null) {
                                isOnlineStatus = connectedUsers.stream()
                                        .anyMatch(u -> u.getUsername() != null && u.getUsername().equals(friendName));
                            }

                            
                            final boolean isOnline = isOnlineStatus;
                            HBox conversationItem = ConversationItemBuilder.createConversationItem(
                                    photoUrl != null ? photoUrl : "genki/img/user-default.png",
                                    friendName,
                                    lastMessage != null && !lastMessage.isEmpty() ? lastMessage : "No messages yet",
                                    time != null && !time.isEmpty() ? time : "Just now",
                                    unreadCount,
                                    isOnline);

                            
                            genki.models.User friendUser = new genki.models.User();
                            friendUser.setId(friendId);
                            friendUser.setUsername(friendName);
                            friendUser.setPhotoUrl(photoUrl);

                           
                            java.util.Map<String, Object> userData = new java.util.HashMap<>();
                            userData.put("user", friendUser);
                            userData.put("conversationId", conversationId.toString());
                            conversationItem.setUserData(userData);

                           
                            conversationItem.setOnMouseClicked(e -> {
                                
                                ArrayList<User> currentConnectedUsers = UserSession.getConnectedUsers();
                                boolean isCurrentlyOnline = currentConnectedUsers != null && friendName != null &&
                                        currentConnectedUsers.stream()
                                                .anyMatch(u -> u.getUsername() != null
                                                        && u.getUsername().equals(friendName));

                                
                                setCurrentConversation(conversationId, isCurrentlyOnline);
                            });

                            
                            UserSession.addConversationItem(conversationItem);

                            
                            Platform.runLater(() -> {
                                conversationListContainer.getChildren().add(conversationItem);

                                
                                synchronized (loadingLock) {
                                    loadedConversations++;
                                    

                                    
                                    if (loadedConversations >= totalConversations && loadingSpinnerContainer != null) {
                                        loadingSpinnerContainer.setVisible(false);
                                        loadingSpinnerContainer.setManaged(false);

                                        
                                        System.out.println("✓ All " + UserSession.getConversationItems().size()
                                                + " conversation items loaded, refreshing online status...");
                                        UserSession.getClientSocket().refreshConversationOnlineStatus();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error loading conversation for friend", e);
                            
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
            
            Platform.runLater(() -> {
                if (loadingSpinnerContainer != null) {
                    loadingSpinnerContainer.setVisible(false);
                    loadingSpinnerContainer.setManaged(false);
                }
            });
        }
    }

    private void loadGroupConversations() {
        try {
            String currentUserId = UserSession.getUserId();

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

                
                String groupDescription = conversationDoc.getString("description");
                if (groupDescription == null) {
                    groupDescription = "";
                }

                String groupId = conversationDoc.getString("groupId");
                if (groupId == null) {
                    groupId = conversationId.toString(); 
                }

                Boolean isPublic = conversationDoc.getBoolean("isPublic", false);
                String groupAdmin = conversationDoc.getString("admin");
                if (groupAdmin == null) {
                    groupAdmin = "";
                }

                
                Group group = new Group(
                        groupId,
                        groupName,
                        groupDescription,
                        isPublic,
                        groupPhotoUrl,
                        groupAdmin);

                
                java.util.List<?> participantIds = conversationDoc.getList("participantIds", Object.class);
                if (participantIds != null) {
                    for (Object participantId : participantIds) {
                        group.addUser(participantId.toString());
                    }
                }

                UserSession.addGroup(group);
                logger.log(Level.INFO, " Group added to UserSession: " + groupName + " (ConversationID: "
                        + conversationId + ", GroupID: " + groupId + ")");

                
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

                
                conversationItem.setUserData(userData);
               
                conversationItem.setOnMouseClicked(e -> setCurrentConversation(conversationId, isOnline));

                
                UserSession.addGroupConversationItem(conversationItem);
                System.out.println("Cached group: " + groupName);
            }

            

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading group conversations", e);
            e.printStackTrace();
        }
    }

    public void handleAddUserFromDialog(String username) {

        UserDAO userDAO = new UserDAO();

        Document userDoc = userDAO.getUserByUsername(username);

        if (userDoc == null) {
            showAlert("User not found", "This user does not exist.");
            return;
        }

        String friendId = userDoc.getObjectId("_id").toHexString();
        String currentUserId = UserSession.getUserId();

        ConversationDAO conversationDAO = new ConversationDAO();
        ObjectId conversationId = conversationDAO.findDirectConversation(currentUserId, friendId);

        if (conversationId == null) {
            conversationId = conversationDAO.createDirectConversation(currentUserId, friendId);
        }
    }

    private void showAlert(String string, String string2) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(string);
            alert.setContentText(string2);
            alert.showAndWait();
        });
    }

    private void showUserConversations() {
        try {
            updateFilterButtonStyles(true);
            conversationListContainer.getChildren().clear();

            
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

            
            for (HBox item : conversationItems) {
                conversationListContainer.getChildren().add(item);
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error displaying user conversations", e);
        }
    }

    private void showGroupConversations() {
        try {
         
            updateFilterButtonStyles(false);

            
            groupsListContainer.getChildren().clear();

            
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

            
            for (HBox item : groupItems) {
                groupsListContainer.getChildren().add(item);
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error displaying group conversations", e);
        }
    }

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
        
    }

    public void addNewGroupToUI(HBox groupItem) {
        Platform.runLater(() -> {
           
            groupsListContainer.getChildren()
                    .removeIf(node -> node instanceof Label && ((Label) node).getText().equals("No groups found"));

            
            groupsListContainer.getChildren().add(0, groupItem);
            
        });
    }

    public void handleAddGroupFromDialog(Group newGroup) {
        Platform.runLater(() -> {
            String currentUserId = UserSession.getUserId();

           
            ArrayList<String> participantIds = new ArrayList<>(
                    newGroup.getListUsers() != null ? newGroup.getListUsers() : new ArrayList<>());
            if (!participantIds.contains(currentUserId)) {
                participantIds.add(currentUserId);
            }

            
            ConversationDAO conversationDAO = new ConversationDAO();
            ObjectId conversationId = conversationDAO.createGroupConversation(
                    participantIds,
                    newGroup.getGroupName(),
                    newGroup.getGroupProfilePicture(),
                    newGroup.getGroupId() 
            );

            if (conversationId != null) {
                
                if (groupsListContainer.getChildren().isEmpty()) {
                    groupsListContainer.getChildren().clear();
                } else {
                    groupsListContainer.getChildren().removeIf(
                            node -> node instanceof Label && ((Label) node).getText().equals("No groups found"));
                }

                
                HBox newGroupContainer = ConversationItemBuilder.createConversationItem(
                        newGroup.getGroupProfilePicture(),
                        newGroup.getGroupName(),
                        "No messages yet",
                        "Just now",
                        0,
                        false);

                
                newGroupContainer.setOnMouseClicked(e -> setCurrentConversation(conversationId, false));

               
                UserSession.addGroupConversationItem(newGroupContainer);

                
                groupsListContainer.getChildren().add(0, newGroupContainer);

                if (!usersPane.isVisible()) {
                    switchUsers(false); 
                }

               
            }
        });
    }

    public void updateChatHeaderStatusForUser(String userId, String userName, boolean isOnline) {
        Platform.runLater(() -> {
            
            if ((currentRecipientId != null && currentRecipientId.equals(userId)) ||
                    (currentRecipientName != null && currentRecipientName.equals(userName))) {

               
                if (chatContactStatusCircle != null) {
                    chatContactStatusCircle.setFill(
                            isOnline ? Color.web("#4ade80") : Color.web("#9ca3af"));
                }

                
                if (chatContactStatus != null) {
                    chatContactStatus.setText(isOnline ? "Online" : "Offline");
                }

               
            }
        });
    }

    public void addGroupConversationFromAcceptance(String groupId, String groupName) {
        Platform.runLater(() -> {
            try {
                String currentUserId = UserSession.getUserId();

                var groupConversations = this.dbConnection
                        .getDatabase()
                        .getCollection("Conversation")
                        .find(new org.bson.Document("groupId", groupId)
                                .append("type", "group"));

                org.bson.Document conversationDoc = groupConversations.first();

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

                    if (conversationDoc.getString("groupId") == null) {
                        this.dbConnection.getDatabase()
                                .getCollection("Conversation")
                                .updateOne(
                                        new org.bson.Document("_id", conversationId),
                                        new org.bson.Document("$set", new org.bson.Document("groupId", groupId)));
                        logger.log(Level.INFO, " Updated conversation with groupId: " + groupId);
                    }

                    java.util.List<?> participantIds = conversationDoc.getList("participantIds", Object.class);
                    boolean isParticipant = participantIds != null && participantIds.stream()
                            .anyMatch(id -> id.toString().equals(currentUserId));

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

                    HBox newGroupContainer = ConversationItemBuilder.createConversationItem(
                            groupPhotoUrl,
                            groupName,
                            lastMessage,
                            time,
                            0,
                            false
                    );

                    java.util.Map<String, Object> userData = new java.util.HashMap<>();
                    userData.put("conversationId", conversationId.toString());
                    userData.put("groupName", groupName);
                    userData.put("groupId", groupId);
                    newGroupContainer.setUserData(userData);

                    newGroupContainer.setOnMouseClicked(e -> setCurrentConversation(conversationId, false));

                    if(!UserSession.getGroupConversationItems().contains(newGroupContainer)){
                        UserSession.addGroupConversationItem(newGroupContainer);
                    }

                    groupsListContainer.getChildren().add(0, newGroupContainer);

                    groupsListContainer.getChildren().removeIf(
                            node -> node instanceof Label && ((Label) node).getText().equals("No groups found"));

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

    public void addFriendConversationFromAcceptance(String friendUsername) {
        Platform.runLater(() -> {
            try {
                
                UserDAO userDAO = new UserDAO();
                Document friendDoc = userDAO.getUserByUsername(friendUsername);

                if (friendDoc == null) {
                    logger.log(Level.WARNING, " Could not find friend in database: " + friendUsername);
                    return;
                }

                String friendId = friendDoc.getObjectId("_id").toHexString();
                String currentUserId = UserSession.getUserId();

                
                ConversationDAO conversationDAO = new ConversationDAO();
                ObjectId conversationId = conversationDAO.findDirectConversation(currentUserId, friendId);

                
                if (conversationId == null) {
                    conversationId = conversationDAO.createDirectConversation(currentUserId, friendId);
                    logger.log(Level.INFO, " Created new direct conversation with friend: " + friendUsername);
                }

                if (conversationId == null) {
                    logger.log(Level.WARNING,
                            "Could not create or find conversation with friend: " + friendUsername);
                    return;
                }

               
                String friendPhotoUrl = friendDoc.getString("photo_url");
                if (friendPhotoUrl == null) {
                    friendPhotoUrl = "genki/img/user-default.png";
                }

                
                boolean isFriendOnline = false;
                ArrayList<genki.models.User> connectedUsers = UserSession.getConnectedUsers();
                if (connectedUsers != null) {
                    isFriendOnline = connectedUsers.stream()
                            .anyMatch(u -> u.getId() != null && u.getId().equals(friendId));

                }

                
                HBox newFriendContainer = ConversationItemBuilder.createConversationItem(
                        friendPhotoUrl,
                        friendUsername,
                        "No messages yet",
                        "",
                        0, 
                        isFriendOnline 
                );

                
                java.util.Map<String, Object> userData = new java.util.HashMap<>();
                userData.put("conversationId", conversationId.toString());
                userData.put("friendName", friendUsername);
                userData.put("friendId", friendId);
                newFriendContainer.setUserData(userData);

                
                final ObjectId finalConversationId = conversationId;
                newFriendContainer.setOnMouseClicked(e -> {
                    
                    boolean isCurrentlyOnline = checkIfUserIsOnline(friendId);
                    setCurrentConversation(finalConversationId, isCurrentlyOnline);
                });

                
                UserSession.addConversationItem(newFriendContainer);
                
               
                genki.models.User friendUser = new genki.models.User();
                friendUser.setId(friendId);
                friendUser.setUsername(friendUsername);
                friendUser.setPhotoUrl(friendPhotoUrl);
                ArrayList<genki.models.User> friends = UserSession.getFriends();
                if (friends != null && !friends.stream().anyMatch(u -> u.getId().equals(friendId))) {
                    friends.add(friendUser);
                }

                
                conversationListContainer.getChildren().add(0, newFriendContainer);

                conversationListContainer.getChildren().removeIf(
                        node -> node instanceof Label && ((Label) node).getText().equals("No conversations found"));

              
               

                if (!usersPane.isVisible()) {
                    switchUsers(true);
                }

                logger.log(Level.INFO, "Friend conversation added to UI from acceptance: " + friendUsername
                        + " (FriendID: " + friendId + ")");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error adding friend conversation from acceptance", e );
                e.printStackTrace();
            }
        });
    }

    private boolean checkIfUserIsOnline(String userId) {
        try {
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