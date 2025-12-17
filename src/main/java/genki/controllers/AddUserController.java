package genki.controllers;

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
    
    @FXML
    private TextField txtUsername;
    
    @FXML
    private Button btnAddUser;
    
    @FXML
    private Button btnCancel;
    
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
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("User Added");
        alert.setContentText("User '" + username + "' has been added successfully!");
        alert.showAndWait();
        
        closeWindow();
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