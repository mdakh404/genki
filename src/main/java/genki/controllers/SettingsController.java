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
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;


import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;
import java.io.File;

public class SettingsController implements Initializable{

       private static Logger logger = Logger.getLogger(SettingsController.class.getName());
       private static final DBConnection SettingsControllerDBConnection = new DBConnection("genki_testing");
       private static SettingsModel settingsModel = new SettingsModel();
       private boolean uploadedPhoto;
       private String uploadedPhotoURL;

       @FXML
       private TextField newUsername;
       @FXML
       private TextField currentPassword;
       @FXML
       private TextField newPassword;
       @FXML
       private TextField bioField;

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
                            uploadedPhotoURL = settingsModel.getUploadedPhotoURL();

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

           String newUsernameText = newUsername.getText();
           String currentPasswordText = currentPassword.getText();
           String newPasswordText = newPassword.getText();
           String bioText = bioField.getText();

           if (uploadedPhoto) {

           }




       }

       @Override
       public void initialize(URL location, ResourceBundle resources) {

             MongoCollection<Document> usersCollection = SettingsControllerDBConnection.getUsersCollection();
             Document userDoc = usersCollection.find(Filters.eq("username", UserSession.getUsername())).first();

             if (userDoc != null && userDoc.getString("photo_url") != null) {
                   Image initPhotoURL = new Image(userDoc.getString("photo_url"), true);
                   imageProfileView.setImage(initPhotoURL);
             }

       }


}
