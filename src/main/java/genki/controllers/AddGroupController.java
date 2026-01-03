package genki.controllers;

import genki.models.GroupSettingsModel;
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
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

public class AddGroupController {

    private HomeController homeController;
    private static final Dotenv env = Dotenv.load();
    private static final Cloudinary cloudinary = new Cloudinary(env.get("CLOUDINARY_URL"));
    private String selectedPhotoPath = null;

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
    
    @FXML
    private ImageView imgGroupPhoto;
    
    @FXML
    private Button btnUploadPhoto;
    
    @FXML
    private Label lblPhotoStatus;
    
    @FXML
    public void initialize() {

        ToggleGroup privacyGroup = new ToggleGroup();
        rbPublic.setToggleGroup(privacyGroup);
        rbPrivate.setToggleGroup(privacyGroup);
        
        rbPublic.setSelected(true);
        
        try {

            Image defaultImage = new Image(getClass().getResourceAsStream("/genki/img/group-default.png"));
            if (imgGroupPhoto != null) {
                imgGroupPhoto.setImage(defaultImage);
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(50, 50, 50);
                imgGroupPhoto.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement image par défaut: " + e.getMessage());
        }
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
    
    @FXML
    private void handlePhotoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Group Photo");
        
        // Filtres pour les images
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) btnUploadPhoto.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {

                Map<?, ?> result = AddGroupController.cloudinary.uploader().upload(
                        selectedFile,
                        ObjectUtils.asMap(
                                "folder", "genki_production/group_images",
                                "resource_type", "image"
                        )
                );

                selectedPhotoPath = result.get("secure_url").toString();

                // Afficher l'image dans l'ImageView
                Image image = new Image(selectedFile.toURI().toString());
                imgGroupPhoto.setImage(image);
                
                // Mettre à jour le label de statut
                if (lblPhotoStatus != null) {
                    lblPhotoStatus.setText("✓ Photo uploaded");
                    lblPhotoStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                }
                
                System.out.println("Photo uploaded successfully: " + selectedPhotoPath);
                
            } catch (IOException e) {
                System.err.println("Error uploading photo: " + e.getMessage());
                e.printStackTrace();
                
                if (lblPhotoStatus != null) {
                    lblPhotoStatus.setText("✗ Upload failed");
                    lblPhotoStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
                }
                
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Upload Error");
                alert.setHeaderText("Failed to upload photo");
                alert.setContentText("An error occurred while uploading the photo. Please try again.");
                alert.showAndWait();
            }
        }
    }
    
    /**
     * Récupère l'extension d'un fichier
     */
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // Pas d'extension
        }
        return fileName.substring(lastIndexOf);
    }
    
    @FXML
    private void handleAddGroup() {
        String groupName = txtGroupName.getText().trim();
        String groupDescription = txtDescription.getText().trim();
        String privacy = rbPublic.isSelected() ? "Public" : "Private";
        boolean groupPrivacyPublic = privacy.equals("Public");

        // Validation du nom de groupe
        if (groupName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a group name.");
            alert.showAndWait();
            return;
        }

        // Si aucune photo n'a été uploadée, utiliser l'image par défaut
        String photoPath = selectedPhotoPath != null ? selectedPhotoPath : "genki/img/group-default.png";

        // Appeler la méthode modifiée avec le chemin de la photo
        AddGroupResult addGroupResult = GroupModel.addGroupWithPhoto(
            groupName, 
            groupDescription, 
            groupPrivacyPublic, 
            UserSession.getUsername(),
            photoPath
        );

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