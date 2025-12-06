package genki.controllers;

import genki.models.AuthModel;
import genki.utils.AuthResult;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

/**
 * LoginController is the controller responsible for handling the login functionality exposed on the Login scene
 * Implements Initializable interface to use the init method after fully loading the Login.fxml file components
 */
public class LoginController implements Initializable {

     private static final Logger logger = Logger.getLogger(LoginController.class.getName());

     private static final AuthModel authModel = new AuthModel();

     @FXML
     private TextField userName;
     @FXML
     private TextField password;

     @FXML
     public void redirectToRegister() {
         logger.log(Level.INFO, "Redirecting user to register");

         // calling ScenesController.switchToScene() to switch to Register view
         try {
             ScenesController.switchToScene("/genki/views/Register.fxml", "Genki - Sign Up");
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Error while loading Register.fxml", e);
         }
     }

     @FXML
     public void onLogin() {

            String user = userName.getText().trim();
            String pass = password.getText().trim();

             if (user.isEmpty()) {
                    logger.log(Level.WARNING, "Username field is empty");
                    userName.setStyle("-fx-border-color: #FF6347;");
             }

             if (pass.isEmpty()) {
                 logger.log(Level.WARNING, "Password field is empty");
                 password.setStyle("-fx-border-color: #FF6347");
              }

             if (user.isEmpty() || pass.isEmpty()) {

                 Alert alertEmpty = new Alert(AlertType.WARNING);
                 alertEmpty.setTitle("Login error");
                 alertEmpty.setHeaderText("Empty fields");
                 alertEmpty.setContentText("Please enter username and password");
                 alertEmpty.showAndWait();
             }

             else {
                 AuthResult loginResult = authModel.authLogin(user, pass);

                 switch (loginResult.getStatus()) {

                     case SUCCESS:
                         try {
                             logger.log(Level.INFO, "Login successful by " + user);
                             ScenesController.switchToScene("/genki/views/Home.fxml", "Genki - Home");
                         } catch (IOException e) {
                             logger.log(Level.SEVERE, "Error while loading Home.fxml", e);
                         }
                         break;

                     case WRONG_PASSWORD_USER:
                          Alert alertCreds = new Alert(AlertType.ERROR);
                          alertCreds.setTitle("Authentication Failed");
                          alertCreds.setHeaderText("Invalid Credentials");
                          alertCreds.setContentText("Wrong username or password");
                          alertCreds.showAndWait();
                          break;

                     case DB_ERROR:
                         Alert alertDB = new Alert(AlertType.ERROR);
                         alertDB.setTitle("Network Error");
                         alertDB.setHeaderText("Connection Error");
                         alertDB.setContentText("Failed to connect to database, please try again in a few minutes");
                         alertDB.showAndWait();
                         break;
                     default:
                         Alert alertUnknown = new Alert(AlertType.ERROR);
                         alertUnknown.setTitle("Unexpected Error");
                         alertUnknown.setHeaderText("Something went wrong");
                         alertUnknown.setContentText("An unexpected error occurred, please ty again in a few minutes");
                         alertUnknown.showAndWait();
                 }

             }
     }


     @Override
     public void initialize(URL location, ResourceBundle resources) {
         userName.focusedProperty().addListener((obs, oldVal, newVal) -> {
               if (newVal) {
                   userName.setStyle("-fx-border-color: #374151");
               }
               else {
                   userName.setStyle("-fx-border-color: #F2F2F2");
               }
         });

         password.focusedProperty().addListener((obs, oldVal, newVal) -> {
             if (newVal) {
                 password.setStyle("-fx-border-color: #374151");
             }
             else {
                 password.setStyle("-fx-border-color: #F2F2F2");
             }
         });
     }

}
