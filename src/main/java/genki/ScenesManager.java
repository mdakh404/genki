package genki;

import genki.controllers.ScenesController;
import genki.controllers.ServerSocketController;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

public class ScenesManager extends Application {
	
	private ServerSocketController server = new ServerSocketController();

    private static final Logger logger = Logger.getLogger(ScenesManager.class.getName());

    @Override
    public void start(Stage primaryStage) throws IOException {
   	try {
   	server.startServer();}
   	catch(Exception e) {
   		System.out.println("There is already a Server Running...");
   	}

        ScenesController.setStage(primaryStage);
        
        // Ajouter le logo de l'application
        try {
            Image logo = new Image(getClass().getResourceAsStream("/genki/img/logo.png"), 128, 128, true, true);
            primaryStage.getIcons().add(logo);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load application logo", e);
        }
        
//        primaryStage.setOnCloseRequest(e -> {
//        	server.stopServer();
//        });
        
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
    
    public static void main(String[] args) {
    	launch(args);
    }
    
}