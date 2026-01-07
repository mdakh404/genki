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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.util.logging.Logger;
import java.util.List;

public class JoinGroupController {

    private static final NotificationDAO notificationDAO = new NotificationDAO();
    private static final GroupModel groupModel = new GroupModel();
    private static final Logger logger = Logger.getLogger(JoinGroupController.class.getName());
    private static final DBConnection JoinGroupDBConnection = DBConnection.getInstance("genki_testing");

    @FXML
    private TextField nameJoinGroup;
    
    @FXML
    private ListView<String> listSuggestions;
    
    @FXML
    private Button btnJoinGroup;
    
    @FXML
    private Button btnCancel;
    
    @FXML
    private VBox rootContainer;
    
    private ObservableList<String> allGroups = FXCollections.observableArrayList();
    private HomeController homeController;
    
    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
    
    @FXML
    public void initialize() {
    	configureCompactLayout();
        setupAutoComplete();
        allGroups.addAll(groupModel.getGroupNames());
    }
    
    private void configureCompactLayout() {
        listSuggestions.setVisible(false);
        listSuggestions.setManaged(false);
    }
    
    private void setupAutoComplete() {
        FilteredList<String> filteredGroups = new FilteredList<>(allGroups, s -> true);
        listSuggestions.setItems(filteredGroups);
        
        nameJoinGroup.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                listSuggestions.setVisible(false);
                listSuggestions.setManaged(false);
                return;
            }
            
            filteredGroups.setPredicate(group ->
                group.toLowerCase().contains(newText.toLowerCase())
            );
            
            boolean hasResults = !filteredGroups.isEmpty();
            listSuggestions.setVisible(hasResults);
            listSuggestions.setManaged(hasResults);
        });
        
        listSuggestions.setOnMouseClicked(e -> {
            String selected = listSuggestions.getSelectionModel().getSelectedItem();
            if (selected != null) {
                nameJoinGroup.setText(selected);
                listSuggestions.setVisible(false);
            }
        });
        
        nameJoinGroup.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
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
                
                if (groupDoc.getList("users", String.class) != null) {
                    for (String userId : groupDoc.getList("users", String.class)) {
                        nvGroup.addUser(userId);
                    }
                }

                UserSession.addGroup(nvGroup);
                
                Document updatedGroupDoc = groupsCollection.find(
                        Filters.eq("_id", groupDoc.getObjectId("_id"))
                ).first();
                
                ConversationDAO conversationDAO = new ConversationDAO();
                ObjectId conversationId;
                
                ObjectId existingConversationId = conversationDAO.findGroupConversation(groupDoc.getString("group_name"));
                
                if (existingConversationId != null) {
                    conversationId = existingConversationId;
                    
                    MongoCollection<Document> conversationCollection = JoinGroupDBConnection.getCollection("Conversation");
                    Document conversationQuery = new Document("_id", existingConversationId);
                    Document conversationDoc = conversationCollection.find(conversationQuery).first();
                    
                    if (conversationDoc != null) {
                        List<String> currentParticipants = conversationDoc.getList("participantIds", String.class);
                        if (currentParticipants == null) {
                            currentParticipants = new java.util.ArrayList<>();
                        }
                        
                        if (!currentParticipants.contains(UserSession.getUserId())) {
                            currentParticipants.add(UserSession.getUserId());
                            
                            conversationCollection.updateOne(
                                conversationQuery,
                                new Document("$set", new Document("participantIds", currentParticipants))
                            );
                            
                            System.out.println("✅ Added user to existing conversation: " + conversationId);
                        }
                    }
                } else {
                    java.util.ArrayList<String> participantIds = new java.util.ArrayList<>();
                    
                    if (updatedGroupDoc != null && updatedGroupDoc.getList("users", String.class) != null) {
                        participantIds.addAll(updatedGroupDoc.getList("users", String.class));
                    }
                    
                    if (!participantIds.contains(UserSession.getUserId())) {
                        participantIds.add(UserSession.getUserId());
                    }
                    
                    conversationId = conversationDAO.createGroupConversation(
                        participantIds,
                        groupDoc.getString("group_name"),
                        groupDoc.getString("profile_picture"),
                        groupDoc.getObjectId("_id").toString()
                    );
                    
                    System.out.println("✅ Created new group conversation: " + conversationId);
                }
                
                if (conversationId != null && homeController != null) {
                    javafx.scene.layout.HBox newGroupItem = ConversationItemBuilder.createConversationItem(
                        groupDoc.getString("profile_picture") != null ? groupDoc.getString("profile_picture") : "genki/img/group-default.png",
                        groupDoc.getString("group_name"),
                        "",
                        "",
                        0,
                        false
                    );
                    
                    java.util.Map<String, Object> userData = new java.util.HashMap<>();
                    userData.put("conversationId", conversationId.toString());
                    userData.put("groupName", groupDoc.getString("group_name"));
                    newGroupItem.setUserData(userData);
                    
                    newGroupItem.setOnMouseClicked(e -> homeController.setCurrentConversation(conversationId, false));
                    
                    UserSession.addGroupConversationItem(newGroupItem);
                    
                    homeController.addNewGroupToUI(newGroupItem);
                    
                    System.out.println("✅ Joined group cached and displayed: " + nameGroup);
                }

                AlertConstruct.alertConstructor(
                           "Success",
                        "",
                        "You have joined " + nameGroup,
                        Alert.AlertType.INFORMATION
                );
                
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
        
        closeWindow();
    }
    
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) btnJoinGroup.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
    
    public void setAvailableGroups(ObservableList<String> groups) {
        this.allGroups.setAll(groups);
    }
}