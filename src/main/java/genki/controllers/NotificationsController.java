package genki.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

public class NotificationsController {
    
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
    private void loadNotifications() {
        // Données de test - Remplacez par vos vraies données
        notifications.addAll(
            new NotificationRequest(
                "Sarah Wilson",
                "wants to add you as a friend",
                NotificationType.FRIEND_REQUEST,
                "/genki/img/user-default.png"
            ),
            new NotificationRequest(
                "Alex Johnson",
                "wants to add you as a friend",
                NotificationType.FRIEND_REQUEST,
                "/genki/img/user-default.png"
            ),
            new NotificationRequest(
                "Design Team",
                "invited you to join the group",
                NotificationType.GROUP_INVITATION,
                "/genki/img/user-default.png"
            ),
            new NotificationRequest(
                "Mike Chen",
                "wants to add you as a friend",
                NotificationType.FRIEND_REQUEST,
                "/genki/img/user-default.png"
            )
        );
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
        System.out.println("✅ Accepted: " + request.getUsername());
        
        // TODO: Ajouter la logique pour accepter dans votre base de données
        if (request.getType() == NotificationType.FRIEND_REQUEST) {
            // Logique d'ajout d'ami
            System.out.println("Adding friend: " + request.getUsername());
        } else {
            // Logique de rejoindre un groupe
            System.out.println("Joining group: " + request.getUsername());
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
                Image image = new Image(getClass().getResourceAsStream(request.getImageUrl()));
                avatar.setImage(image);
            } catch (Exception e) {
                System.out.println("Failed to load image: " + e.getMessage());
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