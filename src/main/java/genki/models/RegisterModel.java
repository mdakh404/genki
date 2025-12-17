package genki.models;

import genki.utils.CredsValidator;
import genki.utils.DBConnection;
import genki.utils.PasswordHasher;
import genki.utils.RegisterStatus;
import genki.utils.RegisterResult;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.MongoException;
import org.bson.Document;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDate;

public class RegisterModel {

    private static final Logger logger = Logger.getLogger(RegisterModel.class.getName());
    private final DBConnection RegisterConnection = new DBConnection("genki_testing");


    public RegisterStatus validateRegistration(String username, String password) {

            if (!CredsValidator.validateUser(username)) return RegisterStatus.USERNAME_INVALID;
            else if (!CredsValidator.validatePass(password)) return RegisterStatus.PASSWORD_INVALID;
            return RegisterStatus.SUCCESS;
    }


    /**
     * Register()
     * Handles the registration process of the application.
     * gets username, password and bio, validates them and then create a new account for the user.
     * it instantiates a DBConnection instance and use it with a connectionURI to connect
     * to the database on MongoDB Atlas cluster.
     * @param username is the supplied username during registration
     * @param password is the supplied password during registration
     * @param bio is the supplied bio during registration
     * @return RegisterStatus USERNAME_INVALID when supplied username contain symbols (_ doesn't count), less than 5 chars or greater than 15 chars
     *                        PASSWORD_INVALID when supplied password doesn't match Genki password policy (See genki.utils.CredsValidator.validatePass())
     *                        DB_ERROR when the connection to the database fails
     *                        SUCCESS when account is successfully created and registered on the database
     * */
    public RegisterResult Register(String username, String password, String bio) {

        switch (validateRegistration(username, password)) {
            case USERNAME_INVALID:
                logger.log(Level.WARNING, "username field is invalid");
                return new RegisterResult(RegisterStatus.USERNAME_INVALID);
            case PASSWORD_INVALID:
                logger.log(Level.WARNING, "password field is invalid");
                return new RegisterResult(RegisterStatus.PASSWORD_INVALID);
        }

        //TODO update the code to get credentials from ENV_VARS or config files
        try {

            MongoDatabase db = RegisterConnection.getDatabase();
            logger.log(Level.INFO, "Registration attempt");
            logger.log(Level.INFO, "Connected to the database");

            MongoCollection<Document> usersCollection = db.getCollection("users");
            logger.log(Level.INFO, "Accessing users collection");

            Document userDoc = usersCollection.find(Filters.eq("username", username)).first();

            if (userDoc != null) {
                logger.log(Level.SEVERE, "Error: " + username + " is already registered");
                return new RegisterResult(RegisterStatus.USER_EXISTS);
            }

            else {

                Document newUser = new Document()
                        .append("username", username)
                        .append("password", PasswordHasher.hashPassword(password))
                        .append("bio",  (bio == null || bio.isEmpty()) ? "" : bio)
                        .append("role", "user")
                        .append("photo_url", "")
                        .append("created_at", LocalDate.now());

                usersCollection.insertOne(newUser);
                logger.log(Level.INFO, username + " account has been created");
                return new RegisterResult(
                        RegisterStatus.SUCCESS,
                        newUser.getString("username"),
                        newUser.getObjectId("_id").toHexString(),
                        newUser.getString("role")
                );
            }


        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            return new RegisterResult(RegisterStatus.DB_ERROR);
        }
    }
}
