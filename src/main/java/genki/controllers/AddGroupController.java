package genki.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
    private ComboBox<String> cmbGroupType;

    @FXML
    private ComboBox<String> cmbCategory;

//    @FXML
//    private Spinner<Integer> spnMaxMembers;

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

//    @FXML
//    private CheckBox chkActive;
    
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
        
        // Afficher un message de succès
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Group '" + groupName + "' added successfully!");
        alert.showAndWait();
        
        // Fermer la fenêtre
        closeWindow();
    }
    
 // Méthode initialize pour initialiser les ComboBox et Spinner (optionnel)
    @FXML
    private void initialize() {
        // Remplir les types de groupe
        if (cmbGroupType != null) {
            cmbGroupType.getItems().addAll("Project Team", "Department", "Social", "Study Group", "Committee");
        }
        
        // Remplir les catégories
        if (cmbCategory != null) {
            cmbCategory.getItems().addAll("Work", "Education", "Entertainment", "Sports", "Technology");
        }
        
        // Configurer le Spinner pour les membres max
//        if (spnMaxMembers != null) {
//            SpinnerValueFactory<Integer> valueFactory = 
//                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0, 10);
//            spnMaxMembers.setValueFactory(valueFactory);
//        }
        
//        // Grouper les RadioButtons
//        if (rbPublic != null && rbPrivate != null) {
//            ToggleGroup privacyGroup = new ToggleGroup();
//            rbPublic.setToggleGroup(privacyGroup);
//            rbPrivate.setToggleGroup(privacyGroup);
//        }
    }
    
 // Ajoutez cette méthode à la fin de la classe
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