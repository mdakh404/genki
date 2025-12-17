package genki.controllers;

import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;

import java.io.IOException;

public class ScenesController {

      private static Stage stage;

      public static void setStage(Stage stage) {
          ScenesController.stage = stage;
      }

      public static void switchToScene(String scene, String title) throws IOException {

                  FXMLLoader loader = new FXMLLoader(ScenesController.class.getResource(scene));
                  Parent root = (Parent) loader.load();

                  stage.setScene(new Scene(root));

                  //stage.setMaximized(true);
//                  stage.centerOnScreen();
                  stage.setTitle(title);
      }
}
