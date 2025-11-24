package genki;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
     @FXML
     private TextField userName;
     @FXML
     private TextField password;

     @Override
     public void initialize(URL location, ResourceBundle resources) {
         userName.focusedProperty().addListener((obs, oldVal, newVal) -> {
               if (newVal) {
                   userName.setStyle("-fx-border-color: #374151");
               }
               else {
                   userName.setStyle("-fx-border-color: #9CA3AF");
               }
         });

         password.focusedProperty().addListener((obs, oldVal, newVal) -> {
             if (newVal) {
                 password.setStyle("-fx-border-color: #374151");
             }
             else {
                 password.setStyle("-fx-border-color: #9CA3AF");
             }
         });
     }

}
