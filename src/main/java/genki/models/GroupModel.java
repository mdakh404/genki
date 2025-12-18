package genki.models;

import genki.utils.DBConnection;
import genki.utils.AddGroupResult;
import genki.utils.AddGroupStatus;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.MongoException;
import org.bson.Document;

import java.util.logging.Logger;
import java.util.logging.Level;

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
                                       .append("group_admin", groupAdmin);

                InsertOneResult insertGroupResult = groupsCollection.insertOne(newGroupDoc);

                if (insertGroupResult.wasAcknowledged()) {
                    logger.log(Level.INFO, "Successfully added group " + groupName);
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


}
