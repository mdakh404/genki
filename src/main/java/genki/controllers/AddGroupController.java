package genki.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class AddGroupController {
    
    @FXML
    private TextField txtGroupName;
    
    @FXML
    private Button btnAddGroup;
    
    @FXML
    private Button btnCancel;

    @FXML
    private TextArea txtDescription;

    @FXML
    private ComboBox<String> cmbGroupType;

    @FXML
    private ComboBox<String> cmbCategory;

    @FXML
    private RadioButton rbPublic;

    @FXML
    private RadioButton rbPrivate;

    @FXML
    private CheckBox chkCanPost;

    @FXML
    private CheckBox chkCanInvite;

    @FXML
    private CheckBox chkCanEdit;
    
    @FXML
    private void handleAddGroup() {
        String groupName = txtGroupName.getText().trim();
        
        if (groupName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a group name.");
            alert.showAndWait();
            return;
        }
        
        // TODO: Ajouter la logique pour sauvegarder le groupe dans votre base de données
        System.out.println("Adding group: " + groupName);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Group '" + groupName + "' added successfully!");
        alert.showAndWait();
        
        closeWindow();
    }
    
    @FXML
    private void initialize() {
    	
    	
        if (cmbGroupType != null) {
            cmbGroupType.getItems().addAll("Project Team", "Department", "Social", "Study Group", "Committee");

        }
        
        if (cmbCategory != null) {
            cmbCategory.getItems().addAll("Work", "Education", "Entertainment", "Sports", "Technology");

        }
  
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) btnAddGroup.getScene().getWindow();
        stage.close();
    }
}