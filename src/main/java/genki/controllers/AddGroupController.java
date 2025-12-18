package genki.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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
    private RadioButton rbPublic;

    @FXML
    private RadioButton rbPrivate;
    
    // Ajoutez cette méthode initialize
    @FXML
    public void initialize() {
        // Créer un ToggleGroup pour les RadioButtons
        ToggleGroup privacyGroup = new ToggleGroup();
        rbPublic.setToggleGroup(privacyGroup);
        rbPrivate.setToggleGroup(privacyGroup);
        
        // Sélectionner "Public" par défaut
        rbPublic.setSelected(true);
    }
    
    @FXML
    private void handleAddGroup() {
        String groupName = txtGroupName.getText().trim();

        String privacy = rbPublic.isSelected() ? "Public" : "Private";
        
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
    private void handleCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) btnAddGroup.getScene().getWindow();
        stage.close();
    }
}