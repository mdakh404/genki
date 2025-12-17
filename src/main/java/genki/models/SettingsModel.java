package genki.models;

import genki.utils.UpdateResult;
import genki.utils.UpdateStatus;
import genki.utils.DBConnection;
import genki.utils.PasswordHasher;
import genki.utils.CredsValidator;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.MongoException;
import org.bson.Document;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;


import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.io.File;

public class SettingsModel {

    private static final Dotenv env = Dotenv.load();
    private static final Cloudinary cloudinary = new Cloudinary(env.get("CLOUDINARY_URL"));
    private static final Logger logger = Logger.getLogger(SettingsModel.class.getName());
    private static final DBConnection SettingsUpdateConnection = new DBConnection("genki_testing");
    private String uploadedPhotoURL;

    public String getUploadedPhotoURL() {
        return uploadedPhotoURL;
    }

    private boolean checkUsernameValidity(String username) {

        return CredsValidator.validateUser(username);
    }

    private boolean checkPasswordValidity(String password) {
        return CredsValidator.validatePass(password);
    }


    public UpdateResult updatePhoto(String username, File imageFile)  {
          logger.log(Level.INFO, "Uploading image to Cloudinary ...");
        try {
            Map<?, ?> result = SettingsModel.cloudinary.uploader().upload(
                    imageFile,
                    ObjectUtils.asMap(
                            "folder", "genki_production/profile_images",
                            "resource_type", "image"
                    )
            );

            MongoCollection<Document> usersCollection = SettingsUpdateConnection.getUsersCollection();
            logger.log(Level.INFO, "Updating photo_url field ...");

            uploadedPhotoURL = result.get("secure_url").toString();

            usersCollection.updateOne(
                 Filters.eq("username", username),
                 Updates.set("photo_url", uploadedPhotoURL)
            );

            logger.log(Level.INFO, "Updated photo_url field.");
            return new UpdateResult(UpdateStatus.PHOTO_UPDATED);

        } catch (Exception e) {
             logger.log(Level.WARNING, "Error uploading image to Cloudinary.", e);
             return new UpdateResult(UpdateStatus.IMG_UPLOAD_ERROR);
        }
    }

    public UpdateResult updateUsername(String oldUsername, String newUsername) {

        if (!checkUsernameValidity(newUsername)) {
            return new UpdateResult(UpdateStatus.INVALID_USERNAME);
        }

        else {
            try {

                MongoCollection<Document> usersCollection = SettingsUpdateConnection.getUsersCollection();

                usersCollection.updateOne(
                        Filters.eq("username", oldUsername),
                        Updates.set("username", newUsername)
                );

                logger.log(Level.INFO, "Updated username " + oldUsername + " to " + newUsername);
                return new UpdateResult(UpdateStatus.USERNAME_UPDATED);

            } catch (MongoException mongoExc) {
                logger.log(Level.WARNING, "Error updating username ", mongoExc);
                return new UpdateResult(UpdateStatus.DB_ERROR);
            }
        }
    }

    public UpdateResult updateBio(String username, String bio) {

        try {

            MongoCollection<Document> usersCollection = SettingsUpdateConnection.getUsersCollection();

            usersCollection.updateOne(
                    Filters.eq("username", username),
                    Updates.set("bio", bio)
            );

            logger.log(Level.INFO, "Updated bio ...");
            return new UpdateResult(UpdateStatus.BIO_UPDATED);


        } catch (Exception e) {
            logger.log(Level.WARNING, "Error updating bio ", e);
            return new UpdateResult(UpdateStatus.DB_ERROR);
        }

    }

    public UpdateResult updatePassword(String username, String oldPassword, String newPassword) {

        if (!checkPasswordValidity(newPassword)) {
            return new UpdateResult(UpdateStatus.INVALID_NEW_PASSWORD);
        }

        else {
            try {

                MongoCollection<Document> usersCollection = SettingsUpdateConnection.getUsersCollection();

                String hashedOldPassword = usersCollection.find(Filters.eq("username", username)).first().getString("password");

                if (!PasswordHasher.checkPassword(oldPassword, hashedOldPassword)) {
                    return new UpdateResult(UpdateStatus.INVALID_CURRENT_PASSWORD);
                } else {
                    usersCollection.updateOne(
                            Filters.eq("username", username),
                            Updates.set("password", PasswordHasher.hashPassword(newPassword))
                    );

                    logger.log(Level.INFO, "Updated password.");
                    return new UpdateResult(UpdateStatus.PASSWORD_UPDATED);

                }


            } catch (MongoException mongoExc) {
                logger.log(Level.WARNING, "Error updating password ", mongoExc);
                return new UpdateResult(UpdateStatus.DB_ERROR);
            } catch (NullPointerException nullExcept) {
                logger.log(Level.WARNING, "Error while accessing users collection", nullExcept);
                return new UpdateResult(UpdateStatus.DB_ERROR);
            }
        }

   }

}
