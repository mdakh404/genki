package genki.models;


import genki.utils.UserSession;
import genki.utils.DBConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.types.ObjectId;

public class GroupsModel {

     private final ArrayList<Group> listGroups = new ArrayList<>();
     private final Logger logger = Logger.getLogger(GroupsModel.class.getName());
     private final DBConnection GroupsModelDBConnection = DBConnection.getInstance("genki_testing");

     public void loadGroups(String username) {

         logger.info("Loading groups for user " + username);

         try {

             MongoCollection<Document> usersCollection = GroupsModelDBConnection.getCollection("users");
             Document userDoc  = usersCollection.find(
                   Filters.eq("username", username)
             ).first();

             if (userDoc != null) {

                 List<String> groupsIds = userDoc.getList("groups", String.class);

                 if (groupsIds != null && !groupsIds.isEmpty()) {

                     for (String groupId : groupsIds) {

                         MongoCollection<Document> groupsCollection = GroupsModelDBConnection.getCollection("groups");
                         Document groupDoc = groupsCollection.find(
                                 Filters.eq("_id", new ObjectId(groupId))
                         ).first();

                         if (groupDoc != null) {

                             Group nvGroup = new Group(
                                     groupId,
                                     groupDoc.getString("group_name"),
                                     groupDoc.getString("description"),
                                     groupDoc.getBoolean("is_public"),
                                     groupDoc.getString("profile_picture"),
                                     groupDoc.getString("group_admin")
                             );
                             
                             // 🔥 CRITICAL: Populate group members from database
                             List<String> users = groupDoc.getList("users", String.class);
                             if (users != null) {
                                 for (String userId : users) {
                                     nvGroup.addUser(userId);
                                 }
                             }

                             UserSession.addGroup(nvGroup);
                         }
                     }
                 }

             }

         } catch (MongoException ex) {
             logger.log(Level.WARNING, ex.getMessage(), ex);
         }

     }


}
