package genki;

import genki.controllers.ScenesController;

import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.text.Font;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

public class ScenesManager extends Application {

    private static final Logger logger = Logger.getLogger(ScenesManager.class.getName());

    @Override
    public void start(Stage primaryStage) throws IOException {

        ScenesController.setStage(primaryStage);
        primaryStage.show();
        try {
            ScenesController.switchToScene("/genki/views/Login.fxml", "Genki - Sign in");
        } catch (IOException e) {
              // TODO alert an error of configuration to user informing Login.fxml is not found
              logger.log(Level.WARNING, "Login.fxml is not found on views folder");
        }



    }

    //TODO load a proper font here for use in application
    @Override
    public void init() throws Exception {
         super.init();
         try {
             Font.loadFont(getClass().getResourceAsStream("/genki/fonts/Roboto.ttf"), 10);
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Failed to load Roboto.ttf", e);
         }
    }
}