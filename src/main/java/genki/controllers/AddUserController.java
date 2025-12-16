package genki.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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

//    @FXML
//    private TextField txtEmail;

//    @FXML
//    private TextField txtFullName;

//    @FXML
//    private ComboBox<String> cmbRole;

//    @FXML
//    private ComboBox<String> cmbDepartment; 

//    @FXML
//    private TextField txtPhone;

//    @FXML
//    private CheckBox chkActive;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
//        // Remplir les rôles
//        if (cmbRole != null) {
//            cmbRole.getItems().addAll("Admin", "User", "Guest");
//        }
        
        // Remplir les départements
//        if (cmbDepartment != null) {
//            cmbDepartment.getItems().addAll("IT", "HR", "Sales", "Marketing", "Finance");
//        }
    	
        // Forcer le focus sur le TextField
        txtUsername.setEditable(true);
        txtUsername.setDisable(false);
        
        // Demander le focus après un court délai
        Platform.runLater(() -> {
            txtUsername.requestFocus();
        });
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