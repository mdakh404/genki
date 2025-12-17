package genki.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class AddUserController implements Initializable {
    
    @FXML
    private TextField txtUsername;
    
    @FXML
    private Button btnAddUser;
    
    @FXML
    private Button btnCancel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    }
    
    @FXML
    private void handleAddUser() {
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Enter a user name");
            alert.showAndWait();
            return;
        }
        
        System.out.println("Adding user: " + username);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("User " + username + " added successfully ");
        alert.showAndWait();
        
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) btnAddUser.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}