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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.control.Alert;
import java.util.logging.Logger;

import genki.utils.UserSession;
import genki.utils.ConversationItemBuilder;

import java.util.logging.Level;
import java.io.IOException;

public class HomeController {
    private static final Logger logger = Logger.getLogger(HomeController.class.getName());
    
    @FXML private Button btnSettings;
    @FXML private Button btnAdd;
    @FXML private VBox rightSideContainer;
    @FXML private ImageView profilTrigger;
    @FXML private ImageView UserProfil;
    @FXML private VBox AmisNameStatus;
    @FXML private VBox conversationListContainer;
   // @FXML private VBox UserNameStatus;
    @FXML private ImageView messageProfil;
    @FXML private Label CurrentUsername;
  
    private Boolean rightSideVisibilite = false;
      
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
        	Image image = new Image(UserSession.getImageUrl());
        	UserProfil.setImage(image);
        }catch(Exception e) {
        	System.out.println(e.getMessage());
        }
        CurrentUsername.setText(UserSession.getUsername());
        
        // Load conversations
        loadConversations();
    }

    @FXML
    private void handleSettingsBtnClick() {
        try {
            logger.log(Level.INFO, "Loading Settings.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/Settings.fxml"));
            Parent root = loader.load();
            
            Stage settingsStage = new Stage();
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
        try {
            logger.log(Level.INFO, "Loading AddUserOrGroup.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/AddUserOrGroup.fxml"));
            Parent root = loader.load();
  
            AddUserOrGroupController controller = loader.getController();
            controller.setHomeController(this);
            
            Stage dialogStage = new Stage();
            try {
                Image logo = new Image(getClass().getResourceAsStream("/genki/img/add_user_group.jpg"), 100, 100, true, true);
                dialogStage.getIcons().add(logo);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load application logo", e);
            }
            dialogStage.setTitle("Add User Or Group");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (btnAdd != null && btnAdd.getScene() != null) {
                dialogStage.initOwner(btnAdd.getScene().getWindow());
            }
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading AddUserOrGroup dialog", e);
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to load AddUserOrGroup dialog.");
            errorAlert.showAndWait();
        }
    }
    
    public void AddUser() {
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
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root, 400, 300));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading AddUser dialog", e);
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to load AddUser dialog.");
            errorAlert.showAndWait();
        }
    }
    
    public void AddGroup() {
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
    
    // Example: Add a conversation dynamically
    public void addConversationExample() {
        HBox conversationItem = ConversationItemBuilder.createConversationItem(
            "url/to/image.png",           // profileImageUrl
            "Sarah Wilson",               // contactName
            "Sent a photo",              // lastMessage
            "12:45 PM",                  // time
            2,                           // unreadCount
            true                         // isOnline
        );

        conversationListContainer.getChildren().add(conversationItem);
    }
    
    /**
     * Load conversations for the current user from the database
     */
    private void loadConversations() {
        try {
            // Example: Add sample conversations (replace with database queries later)
            addConversation(
                "genki/img/user-default.png",
                "Sarah Wilson",
                "Sent a photo",
                "12:45 PM",
                2,
                true
            );
            
            addConversation(
                "genki/img/user-default.png",
                "John Developer",
                "Great project!",
                "10:30 AM",
                0,
                true
            );
            
            addConversation(
                "@../../../resources/img/user-default.png",
                "Emma Designer",
                "Let's discuss the design",
                "Yesterday",
                3,
                false
            );
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading conversations", e);
        }
    }
    
    /**
     * Helper method to add a conversation to the list
     */
    private void addConversation(String profileImageUrl, String contactName, String lastMessage, 
                                  String time, int unreadCount, boolean isOnline) {
        try {
            HBox conversationItem = ConversationItemBuilder.createConversationItem(
                profileImageUrl,
                contactName,
                lastMessage,
                time,
                unreadCount,
                isOnline
            );
            
            conversationListContainer.getChildren().add(conversationItem);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error adding conversation: " + contactName, e);
        }
    }
}
