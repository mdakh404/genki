package genki.models;

import genki.utils.UpdateResult;
import genki.utils.UpdateStatus;
import genki.utils.DBConnection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.MongoException;
import org.bson.Document;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsModel {

    private static final Logger logger = Logger.getLogger(SettingsModel.class.getName());
    private static final MongoClient mongoClient = DBConnection.initConnection("mongodb+srv://mdakh404:moaditatchi2020@genki.vu4rdeo.mongodb.net/?appName=Genki");


    private void updatePhoto() {

    }

    private UpdateResult updateUsername(String oldUsername, String newUsername) {

             try {
                 logger.log(Level.INFO, "Connection is initiated ...");

                 MongoDatabase db = mongoClient.getDatabase("genki_testing");
                 logger.log(Level.INFO, "Connected to database ...");

                 MongoCollection<Document> usersCollection = db.getCollection("users");
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

    private UpdateResult updateBio(String username, String bio) {

       try {
           logger.log(Level.INFO, "Updating bio ...");

           MongoDatabase db = mongoClient.getDatabase("genki_testing");

       } catch (MongoException mongoExc) {
           logger.log(Level.WARNING, "Error updating bio ", mongoExc);
           return new UpdateResult(UpdateStatus.DB_ERROR);
       }

    }
}
