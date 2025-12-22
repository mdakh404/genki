package genki.controllers;

import genki.utils.UserSession;
import genki.utils.AlertConstruct;
import genki.utils.DBConnection;
import genki.utils.NotificationDAO;
import genki.models.User;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.types.ObjectId;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AddUserController implements Initializable {
    
    private static final Logger logger = Logger.getLogger(AddUserController.class.getName());
    private static final DBConnection AddUserControllerDBConnection = new DBConnection("genki_testing");
    private static final NotificationDAO notificationDAO = new NotificationDAO();

    @FXML
    private TextField txtUsername;
    
    @FXML
    private Button btnAddUser;
    
    @FXML
    private Button btnCancel;
    
    // hamza ajoute ca
    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
    //---
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtUsername.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                txtUsername.setStyle("-fx-background-radius: 8; -fx-border-color: #4A5CFF; -fx-border-radius: 8; -fx-border-width: 2; -fx-font-size: 14; -fx-padding: 10;");
            } else {
                txtUsername.setStyle("-fx-background-radius: 8; -fx-border-color: #dce1e8; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-font-size: 14; -fx-padding: 10;");
            }
        });
        
        logger.log(Level.INFO, "AddUserController initialized");
    }
    
    @FXML
    private void handleAddUser() {
        String username = txtUsername.getText().trim();
        
        txtUsername.setStyle("-fx-background-radius: 8; -fx-border-color: #dce1e8; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-font-size: 14; -fx-padding: 10;");
        
        if (username.isEmpty()) {
            logger.log(Level.WARNING, "Username field is empty");
            txtUsername.setStyle("-fx-background-radius: 8; -fx-border-color: #FF6347; -fx-border-radius: 8; -fx-border-width: 2; -fx-font-size: 14; -fx-padding: 10;");
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Missing Required Field");
            alert.setContentText("Please enter a username.");
            alert.showAndWait();
            return;
        }
        
        logger.log(Level.INFO, "Adding user: " + username);
        //hamza ajoute ca 
        if (homeController != null) {
            homeController.handleAddUserFromDialog(username);
        }
        //---

        for (User user : UserSession.getFriends()) {

            if (user.getUsername().equals(username)) {
                 AlertConstruct.alertConstructor(
                         "Add User Error",
                         "",
                         "This user is already on your friends' list",
                         Alert.AlertType.ERROR
                 );

                 closeWindow();
                 return;
            }
        }

        try {

            MongoCollection<Document> usersCollection = AddUserControllerDBConnection.getCollection("users");
            Document recipientUserDoc = usersCollection.find(
                    Filters.eq("username", username)
            ).first();

            if (recipientUserDoc == null) {
                AlertConstruct.alertConstructor(
                            "Add User Error",
                            "",
                            "This user does not exist",
                            Alert.AlertType.ERROR
                 );

                closeWindow();

            }

            logger.info("Sending friend request to " +  recipientUserDoc.getString("username"));

            ObjectId sendFriendRequestNotificationId = notificationDAO.sendFriendRequestNotification(
                    recipientUserDoc.getObjectId("_id"),
                    UserSession.getUserId(),
                    UserSession.getUsername()
            );

            if (sendFriendRequestNotificationId != null) {
                logger.info("A friend request has been sent");
                AlertConstruct.alertConstructor(
                       "Add User Success",
                       "",
                       "A friend request has been sent to " + username,
                       Alert.AlertType.INFORMATION
                );
                closeWindow();
            }

            // TODO Check if a friend request has been sent before to the same user

        } catch (MongoException ex) {
             logger.warning(ex.getMessage());
             AlertConstruct.alertConstructor(
                  "Add User Error",
                  "",
                  "Error while sending a friend request to " + username + ", please try again in a few minutes",
                  Alert.AlertType.ERROR
             );
        } catch (NullPointerException ex) {
            logger.warning(ex.getMessage());
        }






    }
    
    @FXML
    private void handleCancel() {
        if (!txtUsername.getText().isEmpty()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Cancel");
            confirmAlert.setHeaderText("Discard changes?");
            confirmAlert.setContentText("Are you sure you want to cancel? All data will be lost.");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    closeWindow();
                }
            });
        } else {
            closeWindow();
        }


    }
    
    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}