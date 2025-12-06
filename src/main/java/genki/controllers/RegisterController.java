package genki.controllers;

import genki.utils.RegisterResult;
import genki.utils.RegistrationTask;
import genki.models.RegisterModel;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.io.IOException;

public class RegisterController implements Initializable{

    public static final Logger logger = Logger.getLogger(RegisterController.class.getName());
    private static final RegisterModel registerModel = new RegisterModel();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    private TextField regUserName;

    @FXML
    private TextField regPassword;

    @FXML
    private TextField bioField;

    @FXML
    private Button registerButton;

    @FXML
    public void onRegister() {

          String username = regUserName.getText();
          String password = regPassword.getText();
          String bio = bioField.getText();

          if (username.isEmpty()) {
                logger.log(Level.WARNING, "Username field is empty");
                regUserName.setStyle("-fx-border-color: #FF6347");
          }

          if (password.isEmpty()) {
               logger.log(Level.WARNING, "Password field is empty");
               regPassword.setStyle("-fx-border-color: #FF6347");
          }

          if (username.isEmpty() || password.isEmpty()) {

              Alert alertEmpty = new Alert(AlertType.WARNING);
              alertEmpty.setTitle("Registration error");
              alertEmpty.setHeaderText("Empty fields");
              alertEmpty.setContentText("Please enter username and password");
              alertEmpty.showAndWait();
          }

         else {

             RegistrationTask task =  new RegistrationTask(
                     registerModel,
                     username,
                     password,
                     bio
             );

             task.setOnSucceeded(e -> {

                 RegisterResult result = task.getValue();

                 switch (result.getStatus()) {

                     case SUCCESS:
                         try {

                             Alert alertSuccess = new Alert(AlertType.INFORMATION);
                             alertSuccess.setTitle("Registration");
                             alertSuccess.setHeaderText("Registration successful");
                             alertSuccess.setContentText("You have successfully registered");
                             alertSuccess.showAndWait();
                             // Switch Scene to Home after successful registration
                             ScenesController.switchToScene("/genki/views/Home.fxml", "Genki - Home");
                         } catch (IOException event) {
                             logger.log(Level.SEVERE, "Error while loading Home.fxml", event);
                         }
                         break;
                     case USERNAME_INVALID:
                         Alert alertUserInvalid = new Alert(AlertType.ERROR);
                         alertUserInvalid.setTitle("Sign Up error");
                         alertUserInvalid.setHeaderText("Invalid username");
                         alertUserInvalid.setContentText("Your username must be 5–15 characters long and can only include letters, numbers, and underscores.");
                         alertUserInvalid.showAndWait();
                         break;

                     case PASSWORD_INVALID:
                         Alert alertPassInvalid = new Alert(AlertType.ERROR);
                         alertPassInvalid.setTitle("Sign Up error");
                         alertPassInvalid.setHeaderText("Invalid password");
                         alertPassInvalid.setContentText("Your password needs at least 8 characters, with at least one uppercase letter and one symbol.");
                         alertPassInvalid.showAndWait();
                         break;

                     case USER_EXISTS:
                         Alert alertUserExists = new Alert(AlertType.ERROR);
                         alertUserExists.setTitle("Sign Up error");
                         alertUserExists.setHeaderText("User already exists");
                         alertUserExists.setContentText(username + " already exists, please sign up using a new username");
                         alertUserExists.showAndWait();
                         break;

                     default:
                         Alert alertUnknown = new Alert(AlertType.ERROR);
                         alertUnknown.setTitle("Unexpected Error");
                         alertUnknown.setHeaderText("Something went wrong");
                         alertUnknown.setContentText("An unexpected error occurred, please ty again in a few minutes");
                         alertUnknown.showAndWait();

                 }

                 registerButton.setDisable(false);

             });

             task.setOnFailed(e -> {

                 Alert alertDB = new Alert(AlertType.ERROR);
                 alertDB.setTitle("Network Error");
                 alertDB.setHeaderText("Connection Error");
                 alertDB.setContentText("Failed to connect to database, please try again in a few minutes");
                 alertDB.showAndWait();

                 registerButton.setDisable(false);
             });

             executor.execute(task);
             registerButton.setDisable(true);

          }

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
