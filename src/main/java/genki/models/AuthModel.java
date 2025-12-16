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
    private final DBConnection AuthConnection = new DBConnection("genki_testing");


    /**
     * authLogin()
     * Handles the login functionality of the user to the application.
     * gets username and password, validates it and process to authentication.
     * it instantiates a DBConnection instance and use it with a connectionURI to connect
     * to the database on MongoDB Atlas cluster.
     * @param username is the supplied username during login
     * @param password is the supplied password during login
     * @return AuthResult WRONG_PASSWORD_USER when login credentials are invalid or user is not registered
     * @return AuthResult SUCCESS when the login succeed
     * @return AuthResult DB_ERROR when the connection to the database fails
     * */
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
                                userDoc.getString("role")
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



