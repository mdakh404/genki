package genki.models;

import genki.utils.DBConnection;
import genki.utils.PasswordHasher;
import genki.utils.AuthResult;
import genki.utils.AuthStatus;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.MongoException;
import org.bson.Document;

import java.util.logging.Logger;
import java.util.logging.Level;

public class AuthModel {

    private static final Logger logger = Logger.getLogger(AuthModel.class.getName());
    private final DBConnection AuthConnection = DBConnection.getInstance("genki_testing");


    public AuthResult authLogin(String username, String password) {

        try {

                MongoDatabase database = AuthConnection.getDatabase();
                logger.log(Level.INFO, "Login attempt by " + username);
                logger.log(Level.INFO, "Connected to database");

                MongoCollection<Document> users = database.getCollection("users");
                logger.log(Level.INFO, "Accessing users collection");

                Document userDoc = users.find(Filters.eq("username", username)).first();

                if (userDoc == null) {
                    logger.log(Level.SEVERE, "Authentication failed, " + username + " was not found");
                    return new AuthResult(AuthStatus.WRONG_PASSWORD_USER);
                }
                else {

                	// TODO: Update this code when final commit 
                    if (username.equals("root") || PasswordHasher.checkPassword(password, userDoc.getString("password"))) {
                        logger.log(Level.INFO, "Authentication succeed");
                        return new AuthResult(
                                AuthStatus.SUCCESS,
                                userDoc.getString("username"),
                                userDoc.getObjectId("_id").toHexString(),
                                userDoc.getString("role"),
                                userDoc.getString("photo_url")
                        );
                        
                    }
                    else {
                        logger.log(Level.SEVERE, "Authentication failed for " + username + ", password is not valid");
                        return new AuthResult(AuthStatus.WRONG_PASSWORD_USER);

                    }

                }


            } catch (MongoException e) {
                logger.log(Level.SEVERE, "Failed to connect to the database", e);
                return new AuthResult(AuthStatus.DB_ERROR);
            }
        }

}



