package genki.controllers;

import genki.models.SettingsModel;
import genki.utils.UpdateResult;
import genki.utils.UpdateStatus;
import genki.utils.UserSession;
import genki.utils.DBConnection;
import genki.utils.AlertConstruct;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
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

    private static final Logger logger = Logger.getLogger(GroupSettingsController.class.getName());
    private static final DBConnection GSettingsControllerDBConnection = DBConnection.getInstance("genki_testing");
    private static final SettingsModel settingsModel = new SettingsModel();
    private boolean uploadedPhoto;

    @FXML
    private TextField newUsername;
    @FXML
    private PasswordField currentPassword;
    @FXML
    private PasswordField newPassword;
    @FXML
    private TextArea bioField;

    @FXML
    private Button btnSaveSettings;
    @FXML
    private Button btnUploadPhoto;
    @FXML
    private Button btnDeleteAccount;

    @FXML
    private ImageView imageProfileView;

    @FXML
    public void handleUploadPhoto() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        FileChooser.ExtensionFilter filterImages = new FileChooser.ExtensionFilter("Image Files", "*.JPG", "*.JPEG", "*.PNG" ,"*.jpg", "*.jpeg", "*.png");
        fileChooser.getExtensionFilters().add(filterImages);

        Stage stage = (Stage) btnUploadPhoto.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {

            Image newImage = new Image(selectedFile.toURI().toString());
            imageProfileView.setImage(newImage);

            UpdateResult photoUploadResult = settingsModel.updatePhoto(UserSession.getUsername(), selectedFile);

            if (photoUploadResult.getStatus() == UpdateStatus.PHOTO_UPDATED) {
                uploadedPhoto = true;

                String uploadedPhotoURL = settingsModel.getUploadedPhotoURL();

                Image uploadedPhoto = new Image(uploadedPhotoURL, true);
                imageProfileView.setImage(uploadedPhoto);
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

        }


    }

    @FXML
    public void handleSaveSettings() {

        logger.log(Level.INFO, "Saving settings");

        String newUsernameText = newUsername.getText().trim();
        String currentPasswordText = currentPassword.getText().trim();
        String newPasswordText = newPassword.getText().trim();
        String bioText = bioField.getText().trim();

        if (newUsernameText.isEmpty()) {
            logger.log(Level.WARNING, "newUsername field is empty.");
            newUsername.setStyle("-fx-border-color: #FF6347");
        }

        else {
            UpdateResult updateUsernameResult = settingsModel.updateUsername(UserSession.getUsername(), newUsernameText);

            switch (updateUsernameResult.getStatus()) {
                case UpdateStatus.INVALID_USERNAME:
                    logger.log(Level.WARNING, "Invalid username supplied " + newUsernameText);
                    AlertConstruct.alertConstructor(
                            "Settings error",
                            "Invalid username",
                            "Your username must be 5–15 characters long and can only include letters, numbers, and underscores.",
                            AlertType.ERROR
                    );
                    break;

                case UpdateStatus.USERNAME_UPDATED:
                    UserSession.setUsername(newUsernameText);
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
        }

        if (currentPasswordText.isEmpty()) {
            logger.log(Level.WARNING, "currentPassword field is empty.");
            currentPassword.setStyle("-fx-border-color: #FF6347");
        }

        if (newPasswordText.isEmpty()) {
            logger.log(Level.WARNING, "newPassword field is empty.");
            newPassword.setStyle("-fx-border-color: #FF6347");
        }

        else {
            if (currentPasswordText.isEmpty()) {
                AlertConstruct.alertConstructor(
                        "Settings Error",
                        "Saving Settings Failed",
                        "Please enter your current password.",
                        AlertType.ERROR
                );
            }
            else {
                UpdateResult updatePasswordResult = settingsModel.updatePassword(
                        UserSession.getUsername(),
                        currentPasswordText,
                        newPasswordText);

                switch (updatePasswordResult.getStatus()) {

                    case UpdateStatus.INVALID_CURRENT_PASSWORD:
                        AlertConstruct.alertConstructor(
                                "Settings Error",
                                "Incorrect current password",
                                "The current password entered is incorrect.",
                                AlertType.ERROR
                        );
                        break;

                    case UpdateStatus.INVALID_NEW_PASSWORD:
                        AlertConstruct.alertConstructor(
                                "Settings Error",
                                "Invalid new password",
                                "Your new password needs at least 8 characters, with at least one uppercase letter and one symbol.",
                                AlertType.ERROR
                        );
                        break;

                    case UpdateStatus.PASSWORD_UPDATED:
                        AlertConstruct.alertConstructor(
                                "Saving Settings",
                                "Password Updated",
                                "Your password has been updated successfully.",
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
            }
        }

        if (bioText.isEmpty()) {
            logger.log(Level.WARNING, "bioField field is empty.");
            bioField.setStyle("-fx-border-color: #FF6347");
        }

        else {

            UpdateResult updateBioResult = settingsModel.updateBio(UserSession.getUsername(), bioText);

            if (updateBioResult.getStatus() == UpdateStatus.BIO_UPDATED) {
                AlertConstruct.alertConstructor(
                        "Saving Settings",
                        "Bio Updated",
                        "Your bio has been updated successfully.",
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
        }

        if (newUsernameText.isEmpty() &&
                (newPasswordText.isEmpty() || currentPasswordText.isEmpty()) &&
                bioText.isEmpty() && !uploadedPhoto
        ) {
            AlertConstruct.alertConstructor(
                    "Settings Error",
                    "Saving Settings Failed",
                    "Please fill the necessary fields to update your settings.",
                    AlertType.ERROR
            );
        }



    }


    @FXML
    public void handleDeleteAccount() {

        Alert deleteAccountAlert = new Alert(AlertType.CONFIRMATION);
        deleteAccountAlert.setTitle("Confirmation");
        deleteAccountAlert.setHeaderText("Delete account confirmation");
        deleteAccountAlert.setContentText("Are you sure you want to delete your account ?");

        Optional<ButtonType> deleteAccAlertResult = deleteAccountAlert.showAndWait();

        if (deleteAccAlertResult.isPresent() && deleteAccAlertResult.get() == ButtonType.OK) {

            UpdateResult deleteAccResult = settingsModel.deleteAccount(UserSession.getUsername());

            switch (deleteAccResult.getStatus()) {

                case UpdateStatus.ACCOUNT_DELETED:
                    UserSession.logout();
                    try {
                        Stage thisStage = (Stage) btnDeleteAccount.getScene().getWindow();
                        thisStage.close();
                        ScenesController.switchToScene("/genki/views/Login.fxml", "Genki - Sign in");
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Failed to switch to Genki - Sign in");
                    }
                    break;

                case UpdateStatus.ACCOUNT_DELETION_ERROR:
                    AlertConstruct.alertConstructor(
                            "Settings Error",
                            "Account deletion error",
                            "Failed to delete your account, please try again in a few minutes.",
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
        }
    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {

        MongoCollection<Document> usersCollection = GroupSettingsController.GSettingsControllerDBConnection.getCollection("users");
        Document userDoc = usersCollection.find(Filters.eq("username", UserSession.getUsername())).first();

        if (userDoc != null) {
            String photoUrl = userDoc.getString("photo_url");

            if (photoUrl != null && !photoUrl.isBlank()) {
                Image initPhotoURL = new Image(photoUrl, true);
                imageProfileView.setImage(initPhotoURL);
            }
        }

    }


}
