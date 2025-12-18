package genki.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.util.logging.Level;
import java.io.IOException;

public class HomeController {
    private static final Logger logger = Logger.getLogger(HomeController.class.getName());
    
    @FXML private Button btnSettings;
    @FXML private Button btnAdd;
    @FXML private VBox rightSideContainer;
    @FXML private ImageView profilTrigger;
    @FXML private VBox UserNameStatus;
    @FXML private ImageView messageProfil;
    @FXML private Label CurrentUsername;
    @FXML private Button btnNotifications;
  
    private Boolean rightSideVisibilite = false;
    private Popup addMenuPopup;
      
    @FXML
    public void initialize() {
        if (profilTrigger != null) {
            profilTrigger.setOnMouseClicked(e -> toggleRightPanel());
        }
        if (UserNameStatus != null) {
            UserNameStatus.setOnMouseClicked(e -> toggleRightPanel());
        }
        if (rightSideContainer != null) {
            rightSideContainer.setVisible(rightSideVisibilite);
            rightSideContainer.setManaged(rightSideVisibilite);
        }
        try {
            Image image = new Image(UserSession.getImageUrl());
            profilTrigger.setImage(image);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        CurrentUsername.setText(UserSession.getUsername());
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
            Stage dialogStage = new Stage();
            try {
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/icone_add_user.jpg"), 128, 128, true, true);
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
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/icone_add_group.jpg"), 128, 128, true, true);
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
        
        if (rightSideContainer == null) return;
        
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
}