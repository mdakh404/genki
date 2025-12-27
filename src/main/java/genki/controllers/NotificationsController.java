package genki.controllers;

import genki.models.Notification;
import genki.models.User;
import genki.utils.AlertConstruct;
import genki.utils.DBConnection;
import genki.utils.UserSession;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.types.ObjectId;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import genki.models.NotificationRequest;
import genki.models.NotificationRequest.NotificationType;

import java.util.logging.Logger;

public class NotificationsController {

    private static DBConnection notificationsDBConnection = new DBConnection("genki_testing");
    private static Logger logger = Logger.getLogger(NotificationsController.class.getName());

    private static MongoCollection<Document> usersCollection = notificationsDBConnection.getCollection("users");
    private static MongoCollection<Document> notificationsCollection = notificationsDBConnection.getCollection("notifications");
    private static MongoCollection<Document> groupsCollection = notificationsDBConnection.getCollection("groups");


    @FXML
    private ListView<NotificationRequest> notificationsList;
    
    @FXML
    private Button btnClose;
    
    @FXML
    private Label lblNoNotifications;
    
    // Liste observable des notifications
    private ObservableList<NotificationRequest> notifications = FXCollections.observableArrayList();
    
    // Reference to HomeController to update badge
    private HomeController homeController;
    
    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
    
    /**
     * Update notification status in database instead of deleting
     * @param notificationId The ID of the notification to update
     * @param newStatus The new status ("accepted", "rejected", etc.)
     * @return true if update was successful
     */
    private boolean updateNotificationStatus(ObjectId notificationId, String newStatus) {
        try {
            UpdateResult result = notificationsCollection.updateOne(
                    Filters.eq("_id", notificationId),
                    Updates.set("status", newStatus)
            );
            return result.getModifiedCount() > 0;
        } catch (MongoException e) {
            logger.warning("Failed to update notification status: " + e.getMessage());
            return false;
        }
    }
    
    @FXML
    public void initialize() {
        setupListView();
        loadNotifications();
        updateEmptyState();
    }
    
    /**
     * Configure la ListView avec un rendu personnalisé
     */
    private void setupListView() {
        notificationsList.setCellFactory(param -> new NotificationCell());
        notificationsList.setItems(notifications);
    }
    
    /**
     * Charge les notifications (simulées pour l'instant)
     * TODO: Remplacer par un appel à votre base de données
     */

    private String getSenderImageUrl(String senderId) {

        try {


            Document senderUserDoc = usersCollection.find(
                     Filters.eq("_id", new ObjectId(senderId))
            ).first();

            if (senderUserDoc != null) {
                return senderUserDoc.getString("photo_url");
            }


        } catch (MongoException e) {
            logger.warning("Failed to get sender image url " + e.getMessage());
        }

        return "";
    }

    private void loadNotifications() {

         for (Notification notification : UserSession.getNotifications()) {
             
             // Skip notifications with null type (shouldn't happen but safe check)
             if (notification == null || notification.getType() == null) {
                 logger.warning("⚠️ Skipping notification with null type or notification object");
                 continue;
             }

             notifications.add(
                     new NotificationRequest(
                          notification.getNotificationId(),
                          notification.getRequestType(),
                          notification.getSenderName(),
                          notification.getContent(),
                          notification.getType().equals("friend_request") ?
                                  NotificationType.FRIEND_REQUEST : NotificationType.GROUP_JOIN_REQUEST,
                          getSenderImageUrl(notification.getSenderId())
                     )
             );

         }


    }
    
    /**
     * Met à jour l'affichage si aucune notification n'existe
     */
    private void updateEmptyState() {
        boolean isEmpty = notifications.isEmpty();
        notificationsList.setVisible(!isEmpty);
        notificationsList.setManaged(!isEmpty);
        lblNoNotifications.setVisible(isEmpty);
        lblNoNotifications.setManaged(isEmpty);
    }
    
    /**
     * Accepte une demande
     */
    private void handleAccept(NotificationRequest request) {

        if (request.getType() == NotificationType.FRIEND_REQUEST) {
            logger.info(UserSession.getUsername() + " accepted " + request.getUsername() + "'s friend request");

            // Update notification status to "accepted" instead of deleting
            boolean statusUpdated = updateNotificationStatus(request.getNotificationId(), "accepted");

            if (statusUpdated) {
                logger.info("Updated notification " + request.getNotificationId().toHexString() + " status to 'accepted'");

                Document senderUserDoc = usersCollection.find(
                        Filters.eq("username", request.getUsername())
                ).first();

                UpdateResult recipientUpdate = usersCollection.updateOne(
                        Filters.eq("username", UserSession.getUsername()),
                        Updates.addToSet("friends", senderUserDoc.getObjectId("_id"))
                );

                UpdateResult senderUpdate = usersCollection.updateOne(
                        Filters.eq("username", request.getUsername()),
                        Updates.addToSet("friends", new ObjectId(UserSession.getUserId()))
                );

                if (senderUpdate.getModifiedCount() > 0 && recipientUpdate.getModifiedCount() > 0) {
                    logger.info("Updated friends array field for " + UserSession.getUsername()
                            + " and " + request.getUsername()
                    );
                    
                    // Show success message
                    AlertConstruct.alertConstructor(
                            "Friend Request Accepted",
                            "Success",
                            "You are now friends with " + request.getUsername(),
                            Alert.AlertType.INFORMATION
                    );
                    
                    // Remove from UI and UserSession
                    notifications.remove(request);
                    removeNotificationFromUserSession(request.getNotificationId());
                    updateEmptyState();
                    
                    // Update badge in HomeController
                    if (homeController != null) {
                        homeController.updateNotificationBadge();
                    }
                } else {
                    logger.warning("Failed to update friends list");
                    AlertConstruct.alertConstructor(
                            "Error",
                            "Partial failure",
                            "Friend request accepted but failed to update friends list. Please refresh.",
                            Alert.AlertType.WARNING
                    );
                }
            } else {
                logger.warning("An error occurred while accepting " + request.getUsername() + "'s friend request");
                AlertConstruct.alertConstructor(
                        "Unexpected Error",
                        "",
                        "Unexpected error occurred while accepting your friend, please try again in a few minutes",
                        Alert.AlertType.ERROR
                );
            }

        } else if (request.getType() == NotificationType.GROUP_JOIN_REQUEST) {
            logger.info(UserSession.getUsername() + " accepted " + request.getUsername() + "'s Group join request.");

            String groupId = request.getNotificationSubType().split("_")[1];
            Document groupDoc = groupsCollection.find(
                    Filters.eq("_id", new ObjectId(groupId))
            ).first();

            Document senderUserDoc = usersCollection.find(
                    Filters.eq("username", request.getUsername())
            ).first();

            // Update notification status to "accepted" instead of deleting
            boolean statusUpdated = updateNotificationStatus(request.getNotificationId(), "accepted");

            if (statusUpdated) {
                logger.info("Updated notification " + request.getNotificationId().toHexString() + " status to 'accepted'");

                UpdateResult groupUpdateResult = groupsCollection.updateOne(
                        Filters.eq("_id", groupDoc.getObjectId("_id")),
                        Updates.addToSet("users", senderUserDoc.getObjectId("_id").toHexString())
                );

                UpdateResult userUpdateResult = usersCollection.updateOne(
                        Filters.eq("username", request.getUsername()),
                        Updates.addToSet("groups", groupDoc.getObjectId("_id").toHexString())
                );

                if (groupUpdateResult.getModifiedCount() > 0 && userUpdateResult.getModifiedCount() > 0) {
                    logger.info("Updated groups array field for " + request.getUsername()
                            + " and users array field for " + groupDoc.getString("group_name")
                    );
                    
                    // Show success message
                    AlertConstruct.alertConstructor(
                            "Group Join Request Accepted",
                            "Success",
                            request.getUsername() + " has been added to " + groupDoc.getString("group_name"),
                            Alert.AlertType.INFORMATION
                    );
                    
                    // Remove from UI and UserSession
                    notifications.remove(request);
                    removeNotificationFromUserSession(request.getNotificationId());
                    updateEmptyState();
                    
                    // Send socket message to notify the requester that their group join request was accepted
                    sendGroupJoinAcceptanceNotification(
                        request.getUsername(), 
                        senderUserDoc.getObjectId("_id").toHexString(),  // Pass the requester's userId
                        groupDoc.getString("group_name"), 
                        groupDoc.getObjectId("_id").toString()
                    );
                    
                    // Update badge in HomeController
                    if (homeController != null) {
                        homeController.updateNotificationBadge();
                    }
                } else {
                    logger.warning("Failed to update group membership");
                    AlertConstruct.alertConstructor(
                            "Error",
                            "Partial failure",
                            "Group join request accepted but failed to update group. Please refresh.",
                            Alert.AlertType.WARNING
                    );
                }
            } else {
                logger.warning("An error occurred while accepting " + request.getUsername() + "'s group join request");
                AlertConstruct.alertConstructor(
                        "Unexpected Error",
                        "",
                        "Unexpected error occurred while accepting the group join request. Please try again.",
                        Alert.AlertType.ERROR
                );
            }
        }
        
        // Remove from UI list
        notifications.remove(request);
        updateEmptyState();
    }
    
    /**
     * Refuse une demande
     */
    private void handleReject(NotificationRequest request) {
        logger.info(UserSession.getUsername() + " rejected " + request.getUsername() + "'s request");
        
        try {
            // Update notification status to "rejected" instead of deleting
            boolean statusUpdated = updateNotificationStatus(request.getNotificationId(), "rejected");
            
            if (statusUpdated) {
                logger.info("Updated notification " + request.getNotificationId().toHexString() + " status to 'rejected'");
                
                // Show success message
                AlertConstruct.alertConstructor(
                        "Request Rejected",
                        "Success",
                        "You have rejected " + request.getUsername() + "'s request",
                        Alert.AlertType.INFORMATION
                );
                
                // Remove from UI and UserSession
                notifications.remove(request);
                removeNotificationFromUserSession(request.getNotificationId());
                updateEmptyState();
                
                // Update badge in HomeController
                if (homeController != null) {
                    homeController.updateNotificationBadge();
                }
            } else {
                logger.warning("Failed to update notification status to rejected");
                AlertConstruct.alertConstructor(
                        "Error",
                        "Failed to reject",
                        "Could not reject the request. Please try again.",
                        Alert.AlertType.ERROR
                );
            }
        } catch (Exception e) {
            logger.warning("Error while rejecting notification: " + e.getMessage());
            AlertConstruct.alertConstructor(
                    "Error",
                    "Failed to reject",
                    "An error occurred while rejecting the request: " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }
    
    /**
     * Ferme la fenêtre des notifications
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
    
    /**
     * Classe interne pour personnaliser le rendu de chaque notification
     */
    private class NotificationCell extends ListCell<NotificationRequest> {
        
        @Override
        protected void updateItem(NotificationRequest request, boolean empty) {
            super.updateItem(request, empty);
            
            if (empty || request == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            
            // Conteneur principal
            HBox container = new HBox(12);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(12));
            container.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 8; " +
                "-fx-border-width: 1;"
            );
            
            // Image de profil
            ImageView avatar = new ImageView();
            avatar.setFitWidth(48);
            avatar.setFitHeight(48);
            avatar.setPreserveRatio(true);
            avatar.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
            
            try {
                Image image = new Image(request.getImageUrl());
                avatar.setImage(image);
            } catch (Exception e) {
                System.out.println("Failed to load image: " + e.getMessage() + request.getImageUrl());
            }
            
            // VBox pour le nom et le message
            VBox textContainer = new VBox(4);
            textContainer.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(textContainer, Priority.ALWAYS);
            
            Label nameLabel = new Label(request.getUsername());
            nameLabel.setStyle(
                "-fx-font-size: 15px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #111827;"
            );
            
            Label messageLabel = new Label(request.getMessage());
            messageLabel.setStyle(
                "-fx-font-size: 13px; " +
                "-fx-text-fill: #6b7280;"
            );
            
            textContainer.getChildren().addAll(nameLabel, messageLabel);
            
            // Espaceur pour pousser les boutons à droite
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Bouton Accepter
            Button acceptBtn = new Button("✓");
            acceptBtn.setStyle(
                "-fx-background-color: #10b981; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 16;"
            );
            acceptBtn.setOnMouseEntered(e -> 
                acceptBtn.setStyle(acceptBtn.getStyle() + "-fx-background-color: #059669;")
            );
            acceptBtn.setOnMouseExited(e -> 
                acceptBtn.setStyle(acceptBtn.getStyle().replace("-fx-background-color: #059669;", ""))
            );
            acceptBtn.setOnAction(e -> handleAccept(request));
            
            // Bouton Refuser
            Button rejectBtn = new Button("✗");
            rejectBtn.setStyle(
                "-fx-background-color: #ef4444; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 16;"
            );
            rejectBtn.setOnMouseEntered(e -> 
                rejectBtn.setStyle(rejectBtn.getStyle() + "-fx-background-color: #dc2626;")
            );
            rejectBtn.setOnMouseExited(e -> 
                rejectBtn.setStyle(rejectBtn.getStyle().replace("-fx-background-color: #dc2626;", ""))
            );
            rejectBtn.setOnAction(e -> handleReject(request));
            
            // HBox pour les boutons
            HBox buttonBox = new HBox(8);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(acceptBtn, rejectBtn);
            
            // Assembler tous les éléments
            container.getChildren().addAll(avatar, textContainer, spacer, buttonBox);
            
            setGraphic(container);
        }
    }
    
    /**
     * Remove a notification from UserSession by its ID
     * This ensures notifications don't reappear on restart
     * @param notificationId The ID of the notification to remove
     */
    private void removeNotificationFromUserSession(ObjectId notificationId) {
        try {
            UserSession.getNotifications().removeIf(notification -> 
                notification.getNotificationId().equals(notificationId)
            );
            logger.info("✓ Notification " + notificationId.toHexString() + " removed from UserSession");
        } catch (Exception e) {
            logger.warning("Failed to remove notification from UserSession: " + e.getMessage());
        }
    }
    
    /**
     * Send a socket message to notify the requester that their group join request was accepted
     * This allows them to immediately add the group conversation to their UI
     * @param requesterUsername The username of the person who requested to join
     * @param requesterUserId The userId of the requester (for message routing)
     * @param groupName The name of the group
     * @param groupId The ID of the group
     */
    private void sendGroupJoinAcceptanceNotification(String requesterUsername, String requesterUserId, String groupName, String groupId) {
        try {
            // Create a notification message to send via socket with recipientId for proper routing
            Document notificationMessage = new Document()
                    .append("type", "GROUP_JOIN_ACCEPTED")
                    .append("recipientId", requesterUserId)  // Add recipientId so server can route to the right client
                    .append("requesterUsername", requesterUsername)
                    .append("groupName", groupName)
                    .append("groupId", groupId)
                    .append("acceptedBy", UserSession.getUsername())
                    .append("timestamp", System.currentTimeMillis());
            
            String jsonMessage = genki.utils.GsonUtility.getGson().toJson(notificationMessage);
            
            if (UserSession.getClientSocket() != null) {
                UserSession.getClientSocket().sendGroupJoinAcceptanceNotification(jsonMessage);
                logger.info("✓ Sent GROUP_JOIN_ACCEPTED notification for group: " + groupName + " to user: " + requesterUsername + " (ID: " + requesterUserId + ")");
            } else {
                logger.warning("Client socket is not initialized, cannot send group join acceptance notification");
            }
        } catch (Exception e) {
            logger.warning("Failed to send group join acceptance notification: " + e.getMessage());
        }
    }
}