package genki.models;

import genki.utils.UpdateResult;
import genki.utils.UpdateStatus;
import genki.utils.DBConnection;
import genki.utils.PasswordHasher;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;


import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.io.File;

public class GroupSettingsModel {

    private static final Dotenv env = Dotenv.load();
    private static final Cloudinary cloudinary = new Cloudinary(env.get("CLOUDINARY_URL"));
    private static final Logger logger = Logger.getLogger(GroupSettingsModel.class.getName());
    private static final DBConnection GroupSettingsDBConnection = DBConnection.getInstance("genki_testing");
    private String uploadedPhotoURL;

    public String getUploadedPhotoURL() {
        return uploadedPhotoURL;
    }



    public UpdateResult updatePhoto(String groupId, File imageFile)  {
        logger.log(Level.INFO, "Uploading group image to Cloudinary ...");
        try {
            Map<?, ?> result = GroupSettingsModel.cloudinary.uploader().upload(
                    imageFile,
                    ObjectUtils.asMap(
                            "folder", "genki_production/group_images",
                            "resource_type", "image"
                    )
            );

            MongoCollection<Document> groupsCollection = GroupSettingsDBConnection.getCollection("groups");
            logger.log(Level.INFO, "Updating group profile_picture field ...");

            uploadedPhotoURL = result.get("secure_url").toString();

            groupsCollection.updateOne(
                    Filters.eq("_id", new ObjectId(groupId)),
                    Updates.set("profile_picture", uploadedPhotoURL)
            );

            logger.log(Level.INFO, "Updated profile_picture field.");
            return new UpdateResult(UpdateStatus.PHOTO_UPDATED);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error uploading image to Cloudinary.", e);
            return new UpdateResult(UpdateStatus.IMG_UPLOAD_ERROR);
        }
    }

    public UpdateResult updateGroupName(String groupId, String newGroupName) {

            try {

                MongoCollection<Document> groupsCollection = GroupSettingsDBConnection.getCollection("groups");

                groupsCollection.updateOne(
                        Filters.eq("_id", new ObjectId(groupId)),
                        Updates.set("group_name", newGroupName)
                );

                logger.log(Level.INFO, "Updated group name " + " to " + newGroupName);
                return new UpdateResult(UpdateStatus.GROUP_NAME_UPDATED);

            } catch (MongoException mongoExc) {
                logger.log(Level.WARNING, "Error updating group name ", mongoExc);
                return new UpdateResult(UpdateStatus.DB_ERROR);
            }
    }

    public UpdateResult updateGroupDescription(String groupId, String newDescription) {

        try {

            MongoCollection<Document> groupsCollection = GroupSettingsDBConnection.getCollection("groups");

            groupsCollection.updateOne(
                    Filters.eq("_id", new ObjectId(groupId)),
                    Updates.set("description", newDescription)
            );

            logger.log(Level.INFO, "Updated group description ...");
            return new UpdateResult(UpdateStatus.GROUP_DESCRIPTION_UPDATED);


        } catch (Exception e) {
            logger.log(Level.WARNING, "Error updating group description ", e);
            return new UpdateResult(UpdateStatus.DB_ERROR);
        }

    }

    public UpdateResult updateVisibility(String groupId, String newVisibility) {


            try {

                MongoCollection<Document> groupsCollection = GroupSettingsDBConnection.getCollection("groups");

                groupsCollection.updateOne(
                            Filters.eq("_id", new ObjectId(groupId)),
                            Updates.set("is_public", newVisibility.equals("Public"))
                    );

                    logger.log(Level.INFO, "Updated group visibility.");
                    return new UpdateResult(UpdateStatus.GROUP_VISIBILITY_UPDATED);


            } catch (MongoException mongoExc) {
                logger.log(Level.WARNING, "Error updating group visibility ", mongoExc);
                return new UpdateResult(UpdateStatus.DB_ERROR);
            } catch (NullPointerException nullExcept) {
                logger.log(Level.WARNING, "Error while accessing groups collection", nullExcept);
                return new UpdateResult(UpdateStatus.DB_ERROR);
            }


    }

    public UpdateResult deleteGroup(String groupId) throws MongoException {

        try {

            MongoCollection<Document> groupsCollection = GroupSettingsDBConnection.getCollection("groups");

            DeleteResult deleteResult = groupsCollection.deleteOne(
                    Filters.eq("_id", new ObjectId(groupId))
            );

            if (deleteResult.getDeletedCount() > 0) {
                return new UpdateResult(UpdateStatus.GROUP_DELETED);
            }
            else {
                return new UpdateResult(UpdateStatus.GROUP_DELETION_ERROR);
            }

        } catch (MongoException mongoExc) {
            logger.log(Level.WARNING, "DB error while deleting group", mongoExc);
            return new UpdateResult(UpdateStatus.DB_ERROR);
        }
    }

}
