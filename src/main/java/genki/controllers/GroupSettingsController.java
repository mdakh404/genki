package genki.controllers;

import genki.models.Group;
import genki.models.GroupSettingsModel;
import genki.utils.UpdateResult;
import genki.utils.UpdateStatus;
import genki.utils.UserSession;
import genki.utils.DBConnection;
import genki.utils.AlertConstruct;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;


import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;
import java.util.Optional;
import java.io.File;
import java.io.IOException;



public class GroupSettingsController implements Initializable{

    private static String groupId;

    private static final Logger logger = Logger.getLogger(GroupSettingsController.class.getName());
    private static final DBConnection GSettingsControllerDBConnection = DBConnection.getInstance("genki_testing");
    private static final GroupSettingsModel groupSettingsModel = new GroupSettingsModel();
    private boolean uploadedPhoto;

    @FXML
    private TextField newGroupName;
    @FXML
    private TextArea newDescription;
    @FXML
    private ToggleGroup visibilityGroup;
    @FXML
    private RadioButton publicRadio;

    @FXML
    private RadioButton privateRadio;

    @FXML
    private Button btnSaveGroup;
    @FXML
    private Button btnUploadGroupImage;
    @FXML
    private Button btnDeleteGroup;

    @FXML
    private ImageView groupImageView;

    public static void setGroupId(String groupId) {
        GroupSettingsController.groupId = groupId;
    }

    @FXML
    public void handleUploadGroupImage() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Group Image");
        FileChooser.ExtensionFilter filterImages = new FileChooser.ExtensionFilter("Image Files", "*.JPG", "*.JPEG", "*.PNG" ,"*.jpg", "*.jpeg", "*.png");
        fileChooser.getExtensionFilters().add(filterImages);

        Stage stage = (Stage) btnUploadGroupImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {

            Image newImage = new Image(selectedFile.toURI().toString());
            groupImageView.setImage(newImage);

            Thread updateImgThread = new Thread(() -> {
                UpdateResult photoUploadResult = groupSettingsModel.updatePhoto(groupId, selectedFile);

                if (photoUploadResult.getStatus() == UpdateStatus.PHOTO_UPDATED) {
                    uploadedPhoto = true;

                    String uploadedPhotoURL = groupSettingsModel.getUploadedPhotoURL();

                    Image uploadedPhoto = new Image(uploadedPhotoURL, true);
                    groupImageView.setImage(uploadedPhoto);
                }

                if (photoUploadResult.getStatus() == UpdateStatus.IMG_UPLOAD_ERROR) {
                    logger.log(Level.WARNING, "Error opening file");
                    AlertConstruct.alertConstructor(
                            "Upload Failed",
                            "File Upload Error",
                            "Error while uploading file.",
                            AlertType.ERROR
                    );
                }
            });

            updateImgThread.start();

        }


    }

    @FXML
    public void handleSaveGroupSettings() {

        logger.log(Level.INFO, "Saving Group settings");

        String newGroupNameText = newGroupName.getText().trim();
        String newDescriptionText = newDescription.getText().trim();
        Toggle selectedPrivacyToggle = visibilityGroup.getSelectedToggle();

        if (newGroupNameText.isEmpty()) {
            logger.log(Level.WARNING, "newGroupName field is empty.");
            newGroupName.setStyle("-fx-border-color: #FF6347");
        }


        else {

            Thread updateGroupNameThread = new Thread(() -> {
                UpdateResult updateGroupNameResult = groupSettingsModel.updateGroupName(groupId, newGroupNameText);

                switch (updateGroupNameResult.getStatus()) {

                    case UpdateStatus.GROUP_NAME_UPDATED:

                        AlertConstruct.alertConstructor(
                                "Saving Group Settings",
                                "",
                                "Group name has been updated successfully.",
                                AlertType.INFORMATION
                        );

                        for (Group group : UserSession.getGroups()) {
                            if (group.getGroupId().equals(groupId)) {
                                group.setGroupName(newGroupNameText);
                            }
                        }
                        break;

                    case UpdateStatus.DB_ERROR:
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
            });

            updateGroupNameThread.start();
        }

        if (newDescriptionText.isEmpty()) {
            logger.log(Level.WARNING, "newDescriptionText field is empty.");
            newDescription.setStyle("-fx-border-color: #FF6347");
        }


        else {

                Thread updateGroupDescriptionThread = new Thread(() -> {
                    UpdateResult updateDescriptionResult = groupSettingsModel.updateGroupDescription(
                            groupId,
                            newDescriptionText);

                    switch (updateDescriptionResult.getStatus()) {

                        case UpdateStatus. GROUP_DESCRIPTION_UPDATED:
                            AlertConstruct.alertConstructor(
                                    "Saving Group Settings",
                                    "",
                                    "Group description has been updated successfully.",
                                    AlertType.INFORMATION
                            );
                            break;

                        case UpdateStatus.DB_ERROR:
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
                });

            updateGroupDescriptionThread.start();

        }



        if (selectedPrivacyToggle != null) {

            RadioButton selectedPrivacy = (RadioButton) selectedPrivacyToggle;
            String privacy = selectedPrivacy.getText();

            Thread updatePrivacyThread = new Thread(() -> {
                UpdateResult updateVisibilityResult = groupSettingsModel.updateVisibility(groupId, privacy);

                if (updateVisibilityResult.getStatus() == UpdateStatus.GROUP_VISIBILITY_UPDATED) {
                    AlertConstruct.alertConstructor(
                            "Saving Group Settings",
                            "",
                            "Your group visibility has been updated successfully to be " + privacy,
                            AlertType.INFORMATION
                    );
                }

                else {
                    AlertConstruct.alertConstructor(
                            "Unexpected Error",
                            "Something went wrong",
                            "An unexpected error occurred updating your bio, please ty again in a few minutes.",
                            AlertType.ERROR
                    );
                }
            });

            updatePrivacyThread.start();
        }

        if (newGroupNameText.isEmpty() &&
                newDescriptionText.isEmpty() &&
                visibilityGroup.getSelectedToggle() == null && !uploadedPhoto
        ) {
            AlertConstruct.alertConstructor(
                    "Group Settings Error",
                    "Saving Settings Failed",
                    "Please fill the necessary fields to update your group settings.",
                    AlertType.ERROR
            );
        }



    }


    @FXML
    public void handleGroupDeletion() {

        Alert deleteGroupAlert = new Alert(AlertType.CONFIRMATION);
        deleteGroupAlert.setTitle("Confirmation");
        deleteGroupAlert.setHeaderText("Delete Group Confirmation");
        deleteGroupAlert.setContentText("Are you sure you want to delete your group ?");

        Optional<ButtonType> deleteGroupOption = deleteGroupAlert.showAndWait();

        if (deleteGroupOption.isPresent() && deleteGroupOption.get() == ButtonType.OK) {

            Thread deleteGroupThread = new Thread(() -> {
                UpdateResult deleteGroupResult = groupSettingsModel.deleteGroup(groupId);

                switch (deleteGroupResult.getStatus()) {

                    case UpdateStatus.GROUP_DELETED:
                        AlertConstruct.alertConstructor(
                                "Group Settings",
                                "",
                                "This group has been deleted.",
                                AlertType.INFORMATION
                        );
                        break;

                    case UpdateStatus. GROUP_DELETION_ERROR:
                        AlertConstruct.alertConstructor(
                                "Group Settings Error",
                                "Group deletion error",
                                "Failed to delete your group, please try again in a few minutes.",
                                AlertType.ERROR
                        );
                        break;

                    case UpdateStatus.DB_ERROR:
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
            });

            deleteGroupThread.start();
        }
    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {

        MongoCollection<Document> groupsCollection = GroupSettingsController.GSettingsControllerDBConnection.getCollection("groups");
        Document groupDoc = groupsCollection.find(Filters.eq("username", UserSession.getUsername())).first();

        if (groupDoc != null) {
            String profilePicture = groupDoc.getString("profile_picture");

            if (profilePicture.startsWith("https")) {
                Image initPhotoURL = new Image(profilePicture, true);
                groupImageView.setImage(initPhotoURL);
            }
        }

    }


}
