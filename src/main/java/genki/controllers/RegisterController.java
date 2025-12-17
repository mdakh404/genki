package genki.controllers;

import genki.utils.RegisterResult;
import genki.utils.RegistrationTask;
import genki.utils.AlertConstruct;
import genki.utils.UserSession;
import genki.models.RegisterModel;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;

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

              AlertConstruct.alertConstructor(
                      "Registration error",
                      "Empty fields",
                      "Please enter username and password.",
                      Alert.AlertType.WARNING
              );
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

                             UserSession.startSession(
                                     result.getUsername(),
                                     result.getUserId(),
                                     result.getUserRole()
                             );

                             AlertConstruct.alertConstructor(
                                     "Registration",
                                     "Registration successful",
                                     "You have successfully registered.",
                                     Alert.AlertType.INFORMATION
                             );

                             // Switch Scene to Home after successful registration
                             ScenesController.switchToScene("/genki/views/Home.fxml", "Genki - Home");
                         } catch (IOException event) {
                             logger.log(Level.SEVERE, "Error while loading Home.fxml", event);
                         }
                         break;
                     case USERNAME_INVALID:

                         AlertConstruct.alertConstructor(
                                "Sign Up error",
                                "Invalid username",
                                "Your username must be 5–15 characters long and can only include letters, numbers, and underscores.",
                                Alert.AlertType.ERROR
                         );
                         break;

                     case PASSWORD_INVALID:

                         AlertConstruct.alertConstructor(
                                 "Sign Up error",
                                 "Invalid password",
                                 "Your password needs at least 8 characters, with at least one uppercase letter and one symbol.",
                                 Alert.AlertType.ERROR
                         );

                         break;

                     case USER_EXISTS:

                         AlertConstruct.alertConstructor(
                                 "Sign Up error",
                                 "User already exists",
                                 username + " already exists, please sign up using a new username.",
                                 Alert.AlertType.ERROR
                         );

                         break;

                     default:

                         AlertConstruct.alertConstructor(
                                 "Unexpected Error",
                                 "Something went wrong",
                                 "An unexpected error occurred, please ty again in a few minutes.",
                                 Alert.AlertType.ERROR
                         );

                 }

                 registerButton.setDisable(false);

             });

             task.setOnFailed(e -> {

                 AlertConstruct.alertConstructor(
                         "Network Error",
                         "Database Connection Error",
                         "Failed to connect to database, please try again in a few minutes.",
                         Alert.AlertType.ERROR
                 );


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
