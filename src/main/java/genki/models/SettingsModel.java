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

import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsModel {

    private static final Logger logger = Logger.getLogger(SettingsModel.class.getName());
    private static final DBConnection SettingsUpdateConnection = new DBConnection("genki_testing");


    private boolean checkUsernameValidity(String username) {

        return CredsValidator.validateUser(username);
    }

    private boolean checkPasswordValidity(String password) {
        return CredsValidator.validatePass(password);
    }

    private static MongoCollection<Document> getUsersCollection() throws MongoException {
        try {
            logger.log(Level.INFO, "Connected to database ...");
            return SettingsUpdateConnection.getDatabase().getCollection("users");
        } catch (MongoException mongoException) {
            throw new MongoException(mongoException.getMessage());
        }

    }

    public UpdateResult updatePhoto() {
         return new UpdateResult(UpdateStatus.INVALID_PHOTO);
    }

    public UpdateResult updateUsername(String oldUsername, String newUsername) {

        if (!checkUsernameValidity(newUsername)) {
            return new UpdateResult(UpdateStatus.INVALID_USERNAME);
        }

        else {
            try {

                MongoCollection<Document> usersCollection = SettingsModel.getUsersCollection();
                logger.log(Level.INFO, "Accessing users collection ...");

                usersCollection.updateOne(
                        Filters.eq("username", oldUsername),
                        Updates.set("username", newUsername)
                );

                logger.log(Level.INFO, "Updated username ...");
                return new UpdateResult(UpdateStatus.USERNAME_UPDATED);

            } catch (MongoException mongoExc) {
                logger.log(Level.WARNING, "Error updating username ", mongoExc);
                return new UpdateResult(UpdateStatus.DB_ERROR);
            }
        }
    }

    public UpdateResult updateBio(String username, String bio) {

        try {

            MongoCollection<Document> usersCollection = SettingsModel.getUsersCollection();
            logger.log(Level.INFO, "Accessing users collection ...");

            usersCollection.updateOne(
                    Filters.eq("username", username),
                    Updates.set("bio", bio)
            );

            logger.log(Level.INFO, "Updated bio ...");
            return new UpdateResult(UpdateStatus.BIO_UPDATED);


        } catch (MongoException mongoExc) {
            logger.log(Level.WARNING, "Error updating bio ", mongoExc);
            return new UpdateResult(UpdateStatus.DB_ERROR);
        }

    }

    private UpdateResult updatePassword(String username, String oldPassword, String newPassword) {

        if (!checkPasswordValidity(newPassword)) {
            return new UpdateResult(UpdateStatus.INVALID_NEW_PASSWORD);
        }

        else {
            try {

                MongoCollection<Document> usersCollection = SettingsModel.getUsersCollection();

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
