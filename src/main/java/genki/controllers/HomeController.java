package genki.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.geometry.Bounds;
import javafx.stage.Popup;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

import genki.utils.UserSession;
import genki.utils.ConversationItemBuilder;
import genki.utils.MessageItemBuilder;

public class HomeController {

    private static final Logger logger = Logger.getLogger(HomeController.class.getName());

    // --- TOUS TES BOUTONS FXML (Indispensables pour que ça clique) ---
    @FXML private Button btnSettings;
    @FXML private Button btnAdd;
    @FXML private Button btnNotifications;
    @FXML private VBox conversationListContainer;
    @FXML private VBox messagesContainer;
    @FXML private Label chatContactName;

    private Popup addMenuPopup;

    // Styles pour tes menus (Gardés de ta version précédente)
    private static final String MENU_BUTTON_STYLE_DEFAULT = "-fx-background-color: transparent; -fx-text-fill: black; -fx-cursor: hand; -fx-padding: 5; -fx-alignment: CENTER-LEFT; -fx-font-size: 14px;";

    @FXML
    public void initialize() {
        // --- NOUVEAUTÉ : Écouteur Admin uniquement ---
        if (UserSession.getClientSocket() != null) {
            UserSession.getClientSocket().setOnNewMessageCallback(msgData -> {
                if ("ADMIN".equals(msgData.senderName)) {
                    Platform.runLater(() -> {
                        addAdminToConversationList(msgData.messageText);
                    });
                }
            });
        }
    }

    private void addAdminToConversationList(String messageContent) {
        HBox adminItem = ConversationItemBuilder.createConversationItem(
                "genki/img/user-default.png", "ADMIN SYSTEM", messageContent, "Maintenant", 1, true
        );
        adminItem.setOnMouseClicked(e -> {
            chatContactName.setText("ADMIN SYSTEM");
            messagesContainer.getChildren().clear();
            messagesContainer.getChildren().add(MessageItemBuilder.createReceivedMessage("genki/img/user-default.png", "ADMIN", messageContent));
        });
        conversationListContainer.getChildren().add(0, adminItem);
    }

    // --- FONCTIONNALITÉ : PARAMÈTRES (Rétablie à l'identique) ---
    @FXML
    private void handleSettingsBtnClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/Settings.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnSettings.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur chargement paramètres", e);
        }
    }

    // --- FONCTIONNALITÉ : BOUTON + (Rétablie à l'identique) ---
    @FXML
    public void AddUserOrGroup() {
        if (addMenuPopup != null && addMenuPopup.isShowing()) {
            addMenuPopup.hide();
            return;
        }

        VBox menu = new VBox(5);
        menu.setPadding(new Insets(10));
        menu.setBackground(new Background(new BackgroundFill(Color.rgb(51, 213, 214), new CornerRadii(8), Insets.EMPTY)));
        
        Button addUser = new Button("Add User");
        addUser.setStyle(MENU_BUTTON_STYLE_DEFAULT);
        addUser.setOnAction(e -> { addMenuPopup.hide(); /* appel de ta logique add user */ });

        menu.getChildren().add(addUser);
        addMenuPopup = new Popup();
        addMenuPopup.getContent().add(menu);
        
        Bounds bounds = btnAdd.localToScreen(btnAdd.getBoundsInLocal());
        addMenuPopup.show(btnAdd, bounds.getMinX(), bounds.getMaxY());
    }

    // --- FONCTIONNALITÉ : NOTIFICATIONS (Rétablie à l'identique) ---
    @FXML
    public void openNotifications() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/genki/views/Notifications.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Notifications");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur notifications", e);
        }
    }
}