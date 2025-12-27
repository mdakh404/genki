package genki.controllers;

import genki.models.GroupModel;
import genki.models.Group;
import genki.models.Notification;
import genki.utils.DBConnection;
import genki.utils.UserSession;
import genki.utils.AlertConstruct;
import genki.utils.NotificationDAO;
import genki.utils.ConversationItemBuilder;
import genki.utils.ConversationDAO;
import genki.utils.GsonUtility;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.logging.Logger;
import java.util.List;

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
    private HomeController homeController;
    
    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
    
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
            
            // Check if group was found
            if (groupDoc == null) {
                AlertConstruct.alertConstructor(
                        "Group Not Found",
                        "",
                        "No group found with the name: " + nameGroup,
                        Alert.AlertType.ERROR
                );
                logger.warning("Group not found: " + nameGroup);
                return;
            }


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
                
                // 🔥 CRITICAL: Populate group members from the original group document
                if (groupDoc.getList("users", String.class) != null) {
                    for (String userId : groupDoc.getList("users", String.class)) {
                        nvGroup.addUser(userId);
                    }
                }

                UserSession.addGroup(nvGroup);
                
                // 🔥 Fetch the UPDATED group document to get the latest participant list
                Document updatedGroupDoc = groupsCollection.find(
                        Filters.eq("_id", groupDoc.getObjectId("_id"))
                ).first();
                
                ConversationDAO conversationDAO = new ConversationDAO();
                ObjectId conversationId;
                
                // 🔥 Check if conversation already exists for this group
                ObjectId existingConversationId = conversationDAO.findGroupConversation(groupDoc.getString("group_name"));
                
                if (existingConversationId != null) {
                    // Conversation exists - add the new user to participants if not already there
                    conversationId = existingConversationId;
                    
                    // Get the conversation and update its participants
                    MongoCollection<Document> conversationCollection = JoinGroupDBConnection.getCollection("Conversation");
                    Document conversationQuery = new Document("_id", existingConversationId);
                    Document conversationDoc = conversationCollection.find(conversationQuery).first();
                    
                    if (conversationDoc != null) {
                        List<String> currentParticipants = conversationDoc.getList("participantIds", String.class);
                        if (currentParticipants == null) {
                            currentParticipants = new java.util.ArrayList<>();
                        }
                        
                        // Add current user if not already in the list
                        if (!currentParticipants.contains(UserSession.getUserId())) {
                            currentParticipants.add(UserSession.getUserId());
                            
                            // Update the conversation in database
                            conversationCollection.updateOne(
                                conversationQuery,
                                new Document("$set", new Document("participantIds", currentParticipants))
                            );
                            
                            System.out.println("✅ Added user to existing conversation: " + conversationId);
                        }
                    }
                } else {
                    // Conversation doesn't exist - create new one with all group members
                    java.util.ArrayList<String> participantIds = new java.util.ArrayList<>();
                    
                    // Add all group members to participants (including the new user)
                    if (updatedGroupDoc != null && updatedGroupDoc.getList("users", String.class) != null) {
                        participantIds.addAll(updatedGroupDoc.getList("users", String.class));
                    }
                    
                    // Ensure current user is in the list
                    if (!participantIds.contains(UserSession.getUserId())) {
                        participantIds.add(UserSession.getUserId());
                    }
                    
                    conversationId = conversationDAO.createGroupConversation(
                        participantIds,
                        groupDoc.getString("group_name"),
                        groupDoc.getString("profile_picture"),
                        groupDoc.getObjectId("_id").toString() // Pass the group ID
                    );
                    
                    System.out.println("✅ Created new group conversation: " + conversationId);
                }
                
                // Create UI item and cache it
                if (conversationId != null && homeController != null) {
                    javafx.scene.layout.HBox newGroupItem = ConversationItemBuilder.createConversationItem(
                        groupDoc.getString("profile_picture") != null ? groupDoc.getString("profile_picture") : "genki/img/group-default.png",
                        groupDoc.getString("group_name"),
                        "",
                        "",
                        0,
                        false
                    );
                    
                    // Store conversation ID and group name in userData map
                    java.util.Map<String, Object> userData = new java.util.HashMap<>();
                    userData.put("conversationId", conversationId.toString());
                    userData.put("groupName", groupDoc.getString("group_name"));
                    newGroupItem.setUserData(userData);
                    
                    newGroupItem.setOnMouseClicked(e -> homeController.setCurrentConversation(conversationId, false));
                    
                    // 🔥 Cache the new group conversation
                    UserSession.addGroupConversationItem(newGroupItem);
                    
                    // 🔥 Display immediately in UI
                    homeController.addNewGroupToUI(newGroupItem);
                    
                    System.out.println("✅ Joined group cached and displayed: " + nameGroup);
                }

                AlertConstruct.alertConstructor(
                           "Success",
                        "",
                        "You have joined " + nameGroup,
                        Alert.AlertType.INFORMATION
                );
                
                // Close dialog
                Stage stage = (Stage) btnJoinGroup.getScene().getWindow();
                stage.close();

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
                   
                   // 🔔 NEW: Send notification via socket to group admin (if online)
                   if (joinGroupNotificationId != null) {
                       try {
                           genki.models.Notification notification = new genki.models.Notification(
                               joinGroupNotificationId,
                               groupAdminDoc.getObjectId("_id"),
                               "group_join_request",
                               "group_" + groupDoc.getObjectId("_id"),
                               UserSession.getUserId(),
                               UserSession.getUsername(),
                               UserSession.getUsername() + " wants to join " + nameGroup
                           );
                           notification.setStatus("pending");
                           
                           // Send notification request to server via socket instead of calling server method directly
                           JsonObject wrapper = new JsonObject();
                           wrapper.addProperty("messageType", "send_notification");
                           wrapper.addProperty("recipientId", groupAdminDoc.getObjectId("_id").toString());
                           wrapper.add("notification", GsonUtility.getGson().toJsonTree(notification));
                           
                           String message = GsonUtility.getGson().toJson(wrapper);
                           UserSession.getClientSocket().sendMessages(message);
                           
                           logger.info("✓ Group join notification sent to server");
                       } catch (Exception socketError) {
                           logger.warning("Could not send notification via socket: " + socketError.getMessage());
                       }
                   }

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