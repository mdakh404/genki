package genki.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;


public class HomeController {

      private static final Logger logger = Logger.getLogger(HomeController.class.getName());

      @FXML
      private Button btnSettings;
      

      @FXML
      private void handleSettingsBtnClick() {

                 try {

                     logger.log(Level.INFO, "Loading Settings.fxml");
                     FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/Settings.fxml"));
                     Parent root = loader.load();

 
                     
                     Stage settingsStage = new Stage();
                     settingsStage.setTitle("Settings");
                     settingsStage.setResizable(false);
                     settingsStage.initModality(Modality.APPLICATION_MODAL);


                     settingsStage.initOwner(btnSettings.getScene().getWindow());
                     settingsStage.setScene(new Scene(root));
                     settingsStage.centerOnScreen();
                     settingsStage.showAndWait();

                 } catch (IOException loadingException) {
                     logger.log(Level.WARNING, loadingException.getMessage());
                     Alert failedLoadingAlert = new Alert(Alert.AlertType.ERROR, "Failed to load settings.fxml file.");
                     failedLoadingAlert.showAndWait();
                 }

      }


}
