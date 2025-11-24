package genki;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable{

    @FXML
    private TextField reguserName;

    @FXML
    private TextField regPassword;

    @FXML
    private TextField bioField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        reguserName.focusedProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal) {
                reguserName.setStyle("-fx-border-color: #374151");
            }
            else {
                reguserName.setStyle("-fx-border-color: #9CA3AF");
            }
        } );

        regPassword.focusedProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal) {
                regPassword.setStyle("-fx-border-color: #374151");
            }
            else {
                regPassword.setStyle("-fx-border-color: #9CA3AF");
            }
        } );

        bioField.focusedProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal) {
                bioField.setStyle("-fx-border-color: #374151");
            }
            else {
                bioField.setStyle("-fx-border-color: #9CA3AF");
            }
        } );
    }

}
