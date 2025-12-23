package genki.controllers;

import genki.utils.UserSession;
import genki.utils.AddGroupResult;
import genki.utils.AddGroupStatus;
import genki.utils.AlertConstruct;
import genki.models.GroupModel;
import genki.models.Group;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert;
import javafx.stage.Stage;


public class AddGroupController {

    private HomeController homeController;

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

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
    
    @FXML
    private void handleAddGroup() {

        String groupName = txtGroupName.getText().trim();
        String groupDescription = txtDescription.getText().trim();
        String privacy = rbPublic.isSelected() ? "Public" : "Private";

        boolean groupPrivacyPublic = privacy.equals("Public");

        if (groupName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a group name.");
            alert.showAndWait();
            return;
        }

        AddGroupResult addGroupResult = GroupModel.addGroup(groupName, groupDescription, groupPrivacyPublic, UserSession.getUsername());

        switch (addGroupResult.getResult()) {

            case AddGroupStatus.GROUP_ADD_SUCCESS:
                 AlertConstruct.alertConstructor(
                         "Create Group",
                              "Group creation success",
                              "Your group has been successfully created !",
                         AlertType.INFORMATION
                 );
                 
                 // Get the newly created group from UserSession and notify HomeController
                 Group newGroup = UserSession.getGroups().get(UserSession.getGroups().size() - 1);
                 if (homeController != null) {
                     homeController.handleAddGroupFromDialog(newGroup);
                 }
                 
                 closeWindow();
                 break;

            case AddGroupStatus.GROUP_ADD_FAILURE:
                AlertConstruct.alertConstructor(
                        "Create Group",
                        "Group creation failure",
                        "An unexpected error occurred while creating your group, please try again.",
                        AlertType.INFORMATION
                );
                break;

            case AddGroupStatus.DB_ERROR:
                AlertConstruct.alertConstructor(
                        "Network Error",
                        "Database Connection Error",
                        "Failed to connect to database, please try again in a few minutes.",
                        AlertType.ERROR
                );
                break;

            default:
                AlertConstruct.alertConstructor(
                        "Unexpected Error",
                        "Something went wrong",
                        "An unexpected error occurred, please ty again in a few minutes.",
                        AlertType.ERROR
                );

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