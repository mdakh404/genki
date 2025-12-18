package genki.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JoinGroupController {
	
    @FXML
    private TextField CodeJoinGroup;
    
    @FXML
    private Button btnJoinGroup;
    
    @FXML
    private Button btnCancel;


	   @FXML
	    private void handleJoinGroup() {
	        String codeGroup = CodeJoinGroup.getText().trim();
	        
	        if (codeGroup.isEmpty()) {
	            Alert alert = new Alert(Alert.AlertType.WARNING);
	            alert.setTitle("Validation Error");
	            alert.setHeaderText(null);
	            alert.setContentText("Please enter a group code.");
	            alert.showAndWait();
	            return;
	        }
	        
	        // TODO: Ajouter la logique pour sauvegarder le groupe dans votre base de données
	        System.out.println("Joining group: " + codeGroup);
	        
	        Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        alert.setTitle("Success");
	        alert.setHeaderText(null);
	        alert.setContentText("Group de code : '" + codeGroup + "' joined successfully!");
	        alert.showAndWait();
	        
	        closeWindow();
	    }
	
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) btnJoinGroup.getScene().getWindow();
        stage.close();
    }
}
