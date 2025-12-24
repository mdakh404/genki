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

    private static MongoCollection<Document> usersCollection = notificationsDBConnection.getCollection("username");
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


            DeleteResult userNotificationDeleteresult = notificationsCollection.deleteOne(
                        Filters.eq("_id", request.getNotificationId())
            );

            if (userNotificationDeleteresult.getDeletedCount() > 0) {

                   logger.info("Deleted notification " + request.getNotificationId().toHexString() + " from database");

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

                   if (senderUpdate.getModifiedCount() > 0 && recipientUpdate.getModifiedCount() > 0 ) {
                       logger.info("Updated friends array field for " + UserSession.getUsername()
                                       + " and " + request.getUsername()
                       );

                   }


            } else {

                  logger.warning("An error occurred while adding " + request.getUsername() + " as a friend");
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

            DeleteResult groupNotificationDeleteresult = notificationsCollection.deleteOne(
                    Filters.eq("_id", request.getNotificationId())
            );

            if (groupNotificationDeleteresult.getDeletedCount() > 0) {

                logger.info("Deleted notification " + request.getNotificationId().toHexString() + " from database");

                UpdateResult groupUpdateResult = groupsCollection.updateOne(
                        Filters.eq("_id", groupDoc.getObjectId("_id")),
                        Updates.addToSet("users", senderUserDoc.getObjectId("_id").toHexString())
                );

                UpdateResult userUpdateResult = usersCollection.updateOne(
                        Filters.eq("username", request.getUsername()),
                        Updates.addToSet("groups", groupDoc.getObjectId("_id").toHexString())
                );

                if (groupUpdateResult.getModifiedCount() > 0 && userUpdateResult.getModifiedCount() > 0 ) {
                    logger.info("Updated groups array field for " + request.getUsername()
                            + " and users array field for " + groupDoc.getString("group_name")
                    );

                }


            } else {

                logger.warning("An error occurred while adding " + request.getUsername() + " as a friend");
                AlertConstruct.alertConstructor(
                        "Unexpected Error",
                        "",
                        "Unexpected error occurred while accepting your friend, please try again in a few minutes",
                        Alert.AlertType.ERROR
                );

            }

        }
        
        // Supprimer la notification de la liste
        notifications.remove(request);
        updateEmptyState();
    }
    
    /**
     * Refuse une demande
     */
    private void handleReject(NotificationRequest request) {
        System.out.println("❌ Rejected: " + request.getUsername());
        
        // TODO: Ajouter la logique pour refuser dans votre base de données
        
        // Supprimer la notification de la liste
        notifications.remove(request);
        updateEmptyState();
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
}