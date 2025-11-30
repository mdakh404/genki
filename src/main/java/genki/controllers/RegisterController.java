package genki.controllers;

import genki.utils.CredsValidator;
import genki.utils.DBConnection;
import genki.utils.PasswordHasher;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;

public class RegisterController implements Initializable{

    public static final Logger logger = Logger.getLogger(RegisterController.class.getName());

    @FXML
    private TextField regUserName;

    @FXML
    private TextField regPassword;

    @FXML
    private TextField bioField;

    public void setRegistrationInfo(String username, String password) {
        regUserName.setText(username);
        regPassword.setText(password);
    }

    public int validateRegistration(String username, String password) {

        if (username.isEmpty()) {
            return -1;
        }
        else if (password.isEmpty()) {
            return -2;
        }

        else {
            if (!CredsValidator.validateUser(username)) return -3;
            else if (!CredsValidator.validatePass(password)) return -4;
            else return 0;
        }
    }

    @FXML
    public boolean authRegister() {

           String userName = regUserName.getText();
           String password = regPassword.getText();
           String bio = bioField.getText();

           switch (validateRegistration(userName, password)) {
               case -1:
                   logger.log(Level.WARNING, "username field is empty");
                   break;
               case -2:
                   logger.log(Level.WARNING, "password field is empty");
               case -3:
                   logger.log(Level.WARNING, "username field is not valid");
                   break;
               case -4:
                   logger.log(Level.WARNING, "password field is not valid");
           }

           try (MongoClient mongoClient = DBConnection.initConnection("mongodb+srv://mdakh404:moaditatchi2020@genki.vu4rdeo.mongodb.net/?appName=Genki")) {

               logger.log(Level.WARNING, "Registration attempt by " + userName);

               MongoDatabase db = mongoClient.getDatabase("genki_testing");
               logger.log(Level.INFO, "Connected to the database");

               MongoCollection<Document> usersCollection = db.getCollection("users");
               logger.log(Level.INFO, "Accessing users collection");

               Document userDoc = usersCollection.find(Filters.eq("username", userName)).first();

               if (userDoc != null) {
                   logger.log(Level.SEVERE, "Error: " + userName + " is already registered");
                   return false;
               }

               else {

                   Document newUser = new Document()
                           .append("username", userName)
                           .append("password", PasswordHasher.hashPassword(password))
                           .append("bio",  (bio == null || bio.isEmpty()) ? "" : bio)
                           .append("role", "user")
                           .append("photo_url", "")
                           .append("created_at", new Date());

                   usersCollection.insertOne(newUser);
                   logger.log(Level.INFO, userName + " account has been created");
                   return true;
               }


           } catch (RuntimeException e) {
               logger.log(Level.SEVERE, "Failed to connect to database", e);
           }

           return true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        regUserName.focusedProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal) {
                regUserName.setStyle("-fx-border-color: #374151");
            }
            else {
                regUserName.setStyle("-fx-border-color: #F2F2F2");
            }
        } );

        regPassword.focusedProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal) {
                regPassword.setStyle("-fx-border-color: #374151");
            }
            else {
                regPassword.setStyle("-fx-border-color: #F2F2F2");
            }
        } );

        bioField.focusedProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal) {
                bioField.setStyle("-fx-border-color: #374151");
            }
            else {
                bioField.setStyle("-fx-border-color: #F2F2F2");
            }
        } );
    }

}
