package genki.models;

import genki.utils.DBConnection;
import genki.utils.PasswordHasher;
import genki.utils.AuthResult;
import genki.utils.AuthStatus;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.logging.Logger;
import java.util.logging.Level;

public class UserModel {

    private static final Logger logger = Logger.getLogger(UserModel.class.getName());

    /*
     * authLogin()
     * Handles the login functionality of the user to the application.
     * gets username and password, validate it and process to authentication.
     * it instantiates a DBConnection instance and use it with a connectionURI to connect
     * to the database on MongoDB Atlas cluster.
     * @return true if login successful, else otherwise
     * @return false will be used to redirect user to registration
     * */
    public AuthResult authLogin(String username, String password) {


        try (MongoClient mongoClient = DBConnection.initConnection("mongodb+srv://mdakh404:moaditatchi2020@genki.vu4rdeo.mongodb.net/?appName=Genki")) {
                logger.log(Level.INFO, "Login attempt by " + username);

                MongoDatabase database = mongoClient.getDatabase("genki_testing");
                logger.log(Level.INFO, "Connected to database");

                MongoCollection<Document> users = database.getCollection("users");
                logger.log(Level.INFO, "Accessing users collection");

                Document userDoc = users.find(Filters.eq("username", username)).first();

                if (userDoc == null) {
                    logger.log(Level.SEVERE, "Authentication failed, " + username + " was not found");
                    return new AuthResult(AuthStatus.WRONG_PASSWORD_USER, "The username or password you entered is incorrect");
                }
                else {

                    if (PasswordHasher.checkPassword(password, userDoc.getString("password"))) {
                        logger.log(Level.INFO, "Authentication succeed");
                        return new AuthResult(AuthStatus.SUCCESS, "Your are successfully logged in");
                    }
                    else {
                        logger.log(Level.SEVERE, "Authentication failed for " + username + ", password is not valid");
                        return new AuthResult(AuthStatus.WRONG_PASSWORD_USER, "The username or password you entered is incorrect");

                    }

                }


            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Failed to connect to the database", e);
                return new AuthResult(AuthStatus.DB_ERROR, "Connection failed");
            }
        }

}



