package genki.controllers;

import genki.models.Group;
import genki.models.GroupsModel;
import genki.models.AuthModel;
import genki.utils.AuthResult;
import genki.utils.UserSession;
import genki.utils.AlertConstruct;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

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
	
	private clientSocketController client;

     private static final Logger logger = Logger.getLogger(LoginController.class.getName());

     private static final AuthModel authModel = new AuthModel();
     private static final GroupsModel groupsModel = new GroupsModel();

     @FXML
     private TextField userName;
     @FXML
     private TextField password;

     @FXML
     private Button loginButton;


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

             AlertConstruct.alertConstructor(
                   "Login Error",
                   "Empty Fields",
                   "Please enter username and password.",
                   Alert.AlertType.WARNING
             );
             return;
         }

         loginButton.setDisable(true);

         javafx.concurrent.Task<AuthResult> loginTask = new javafx.concurrent.Task<>() {
             @Override
             protected AuthResult call() throws Exception {
                 return authModel.authLogin(user, pass);
             }
         };

         loginTask.setOnSucceeded(e -> {
             AuthResult loginResult = loginTask.getValue();

             switch (loginResult.getStatus()) {
                 case SUCCESS:
                     try {

                         logger.log(Level.INFO, "Login successful by " + user);
                         
                         //Creating Client Socket
                         


                         UserSession.startSession(
                               loginResult.getUsername(),
                               loginResult.getUserId(),
<<<<<<< HEAD
                               loginResult.getUserRole()  
=======
                               loginResult.getUserRole(),
                               loginResult.getImageUrl()
>>>>>>> 51a5877af693044164268e81dd22a34c8872e58e
                         );
                         
                         

                         groupsModel.loadGroups(loginResult.getUsername());

                         if (groupsModel.getGroups().isEmpty()) {
                             logger.warning("Empty groups list for user " + user);
                         }

                         else {
                             for (Group group: groupsModel.getGroups()) {
                                 UserSession.addGroup(group);
                             }
                         }


<<<<<<< HEAD
                         AlertConstruct.alertConstructor(
                           "Login",
                           "Login Successful",
                           "You have successfully logged in.",
                           Alert.AlertType.INFORMATION
                         );
                      // --- LOGIQUE DE REDIRECTION ---
                         if ("admin".equals(loginResult.getUserRole())) {
                             logger.log(Level.INFO, "Redirection vers l'espace ADMIN");
                             ScenesController.switchToScene("/genki/views/AdminDashboard.fxml", "Genki - Admin Dashboard");
                         } else {
                             logger.log(Level.INFO, "Redirection vers l'espace UTILISATEUR");
                             ScenesController.switchToScene("/genki/views/Home.fxml", "Genki - Home");
                         }
      

                        // ScenesController.switchToScene("/genki/views/Home.fxml", "Genki - Home");
                     } catch (IOException ex) {
                         logger.log(Level.SEVERE, "Error while loading Home.fxml", ex);
                         loginButton.setDisable(false);
                     }
                     break;

                 case WRONG_PASSWORD_USER:
                     AlertConstruct.alertConstructor(
                             "Authentication Failed",
                             "Invalid Credentials",
                             "Wrong username or password.",
                             Alert.AlertType.ERROR
                     );
                     loginButton.setDisable(false);
                     break;

                 case DB_ERROR:
                     AlertConstruct.alertConstructor(
                       "Network Error",
                       "Database Connection Error",
                       "Failed to connect to database, please try again in a few minutes.",
                       Alert.AlertType.ERROR
                     );
                     loginButton.setDisable(false);
                     break;

                 default:
                     AlertConstruct.alertConstructor(
                       "Unexpected Error",
                       "Something went wrong",
                       "An unexpected error occurred, please try again in a few minutes.",
                       Alert.AlertType.ERROR
                     );
                     loginButton.setDisable(false);
             }
         });

         loginTask.setOnFailed(e -> {
             logger.log(Level.SEVERE, "Login task failed", loginTask.getException());
             AlertConstruct.alertConstructor(
               "Error",
               "Unexpected Error",
               "An unexpected error occurred. Please try again.",
               Alert.AlertType.ERROR
             );
             loginButton.setDisable(false);
         });

         new Thread(loginTask).start();
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
