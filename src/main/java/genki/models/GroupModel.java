package genki.models;

import genki.models.Group;
import genki.utils.DBConnection;
import genki.utils.UserSession;
import genki.utils.AddGroupResult;
import genki.utils.AddGroupStatus;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;

public class GroupModel {

       private static final DBConnection groupModelDBConnection = new DBConnection("genki_testing");
       private static final Logger logger = Logger.getLogger(GroupModel.class.getName());


       public static AddGroupResult addGroup(String groupName, String groupDescription, boolean isPublic, String groupAdmin) {

             logger.log(Level.INFO, "Adding group " + groupName + " ...");

              if (groupAdmin == null) {
                  logger.log(Level.WARNING, "UserSession was revoked.");
                  return new AddGroupResult(AddGroupStatus.GROUP_ADD_FAILURE);
              }

            try {

                MongoCollection<Document> groupsCollection = groupModelDBConnection.getCollection("groups");

                Document newGroupDoc = new Document("group_name", groupName)
                                       .append("description", groupDescription)
                                       .append("is_public", isPublic)
                                       .append("profile_picture", "")
                                       .append("group_admin", groupAdmin)
                                       .append("users", new ArrayList<>());

                InsertOneResult insertGroupResult = groupsCollection.insertOne(newGroupDoc);

                if (insertGroupResult.wasAcknowledged()) {
                    logger.log(Level.INFO, "Successfully added group " + groupName);

                    ObjectId nvGroupId = new ObjectId();
                    Group nvGroup = new Group(
                            nvGroupId.toHexString(),
                            groupName,
                            groupDescription,
                            isPublic,
                            "",
                            groupAdmin
                    );

                    UserSession.addGroup(nvGroup);
                    return new AddGroupResult(AddGroupStatus.GROUP_ADD_SUCCESS);
                }

                else {
                    logger.log(Level.WARNING, "Failed to add group " + groupName);
                    return new AddGroupResult(AddGroupStatus.GROUP_ADD_FAILURE);
                }

            } catch (MongoException ex) {
                logger.log(Level.WARNING, "DB Error while adding group", ex);
                return new AddGroupResult(AddGroupStatus.DB_ERROR);
            }

       }


       public ArrayList<String> getGroupNames() {

           ArrayList<String> groupNames = new ArrayList<>();

           try {

               MongoCollection<Document> groupsCollection = groupModelDBConnection.getCollection("groups");

               groupsCollection.find().forEach(groupDoc -> {
                   groupNames.add(groupDoc.getString("group_name"));
               });

               return groupNames;

           } catch (MongoException ex) {
               logger.warning("DB Error while getting group names" + ex);
               return new ArrayList<>();
           }


       }

}
