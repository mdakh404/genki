package genki.controllers;

import genki.models.GroupModel;
import genki.models.Group;
import genki.utils.DBConnection;
import genki.utils.UserSession;
import genki.utils.AlertConstruct;
import genki.utils.NotificationDAO;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.types.ObjectId;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class JoinGroupController {

    private static final NotificationDAO notificationDAO = new NotificationDAO();
    private static final GroupModel groupModel = new GroupModel();
    private static final Logger logger = Logger.getLogger(JoinGroupController.class.getName());
    private static final DBConnection JoinGroupDBConnection = new DBConnection("genki_testing");

    @FXML
    private TextField nameJoinGroup;
    
    @FXML
    private ListView<String> listSuggestions;
    
    @FXML
    private Button btnJoinGroup;
    
    @FXML
    private Button btnCancel;
    
    // Liste de tous les groupes disponibles
    private ObservableList<String> allGroups = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupAutoComplete();
        allGroups.addAll(groupModel.getGroupNames());
    }
    
    /**
     * Configure le système d'auto-complétion pour le TextField
     */
    private void setupAutoComplete() {
        FilteredList<String> filteredGroups = new FilteredList<>(allGroups, s -> true);
        listSuggestions.setItems(filteredGroups);
        
        // 🔥 Filtrage en temps réel
        nameJoinGroup.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                listSuggestions.setVisible(false);
                return;
            }
            
            filteredGroups.setPredicate(group ->
                group.toLowerCase().contains(newText.toLowerCase())
            );
            
            listSuggestions.setVisible(!filteredGroups.isEmpty());
        });
        
        // 🔥 Cliquer sur une suggestion
        listSuggestions.setOnMouseClicked(e -> {
            String selected = listSuggestions.getSelectionModel().getSelectedItem();
            if (selected != null) {
                nameJoinGroup.setText(selected);
                listSuggestions.setVisible(false);
            }
        });
        
        // 🔥 Masquer les suggestions si le champ perd le focus
        nameJoinGroup.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                // Délai pour permettre le clic sur la suggestion
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                        javafx.application.Platform.runLater(() -> 
                            listSuggestions.setVisible(false)
                        );
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
    
    @FXML
    private void handleJoinGroup() {

        String nameGroup = nameJoinGroup.getText().trim();

        for (Group group: UserSession.getGroups()) {

             if (group.getGroupName().equals(nameGroup)) {
                 AlertConstruct.alertConstructor(
                         "Failure",
                         "",
                         "You have already joined this group !",
                         Alert.AlertType.ERROR
                 );
                 return;
             }
        }
        
        if (nameGroup.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                     "Please enter a group code.");
            return;
        }
        

        logger.info("Joining group: " + nameGroup);

        try {

            MongoCollection<Document> groupsCollection = JoinGroupDBConnection.getCollection("groups");
            MongoCollection<Document> usersCollection = JoinGroupDBConnection.getCollection("users");

            Document groupDoc = groupsCollection.find(
                    Filters.eq("group_name", nameGroup)
            ).first();


            if (groupDoc.getBoolean("is_public")) {

                groupsCollection.updateOne(
                        Filters.eq("_id", groupDoc.getObjectId("_id")),
                        Updates.addToSet("users", UserSession.getUserId())
                );

                usersCollection.updateOne(
                        Filters.eq("_id", new ObjectId(UserSession.getUserId())),
                        Updates.addToSet("groups", groupDoc.getObjectId("_id").toHexString())
                );

                logger.info(UserSession.getUsername() + " has joined " + nameGroup);

                Group nvGroup = new Group(
                       groupDoc.getObjectId("_id").toHexString(),
                       groupDoc.getString("group_name"),
                       groupDoc.getString("description"),
                       groupDoc.getBoolean("is_public"),
                       groupDoc.getString("profile_picture"),
                       groupDoc.getString("group_admin")
                );

                UserSession.addGroup(nvGroup);

                AlertConstruct.alertConstructor(
                           "Success",
                        "",
                        "You have joined " + nameGroup,
                        Alert.AlertType.INFORMATION
                );

            } else {

                   logger.info("Sending joining request to " + nameGroup + "'s admin");
                   Document groupAdminDoc = JoinGroupDBConnection.getCollection("users").find(
                           Filters.eq("username", groupDoc.getString("group_admin"))
                   ).first();


                   ObjectId joinGroupNotificationId = notificationDAO.sendGroupJoinReq(
                            groupAdminDoc.getObjectId("_id"),
                            groupDoc.getObjectId("_id"),
                            UserSession.getUserId(),
                            UserSession.getUsername(),
                            nameGroup
                   );

                   AlertConstruct.alertConstructor(
                           "Join Request",
                           "",
                           "A join request has been submitted to " + nameGroup + "'s admin.",
                           Alert.AlertType.INFORMATION
                   );

                   logger.info("GroupJoinRequest notification_id: " + joinGroupNotificationId);

            }


        } catch (MongoException | NullPointerException ex) {
            AlertConstruct.alertConstructor(
                    "Unexpected Error",
                    "",
                    "Un unexpected error has occurred while processing your request, please try again in a few minutes.",
                    Alert.AlertType.ERROR
            );
            logger.warning(ex.getMessage());
        }
        
        /* showAlert(Alert.AlertType.INFORMATION, "Success",
                 "Group '" + codeGroup + "' joined successfully!");*/
        
        closeWindow();
    }
    
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    
    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Ferme la fenêtre actuelle
     */
    private void closeWindow() {
        Stage stage = (Stage) btnJoinGroup.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
    
    /**
     * Méthode pour mettre à jour la liste des groupes disponibles
     * (à appeler depuis l'extérieur si nécessaire)
     */
    public void setAvailableGroups(ObservableList<String> groups) {
        this.allGroups.setAll(groups);
    }


}