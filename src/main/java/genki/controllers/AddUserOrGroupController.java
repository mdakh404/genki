//package genki.controllers;
//
//import java.util.logging.Level;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Button;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//import javafx.scene.image.Image;
//import java.util.logging.Logger;
//
//public class AddUserOrGroupController {
//    
//	private static Logger logger = Logger.getLogger(AddUserOrGroupController.class.getName());
//	
//    @FXML
//    private Button AddUser;
//    
//    @FXML
//    private Button AddGroup;
//    
//    @FXML
//    public void AddUser() {
//        try {
//            logger.log(Level.INFO, "Loading AddUser.fxml");
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/AddUser.fxml"));
//            Parent root = loader.load();
//            Stage dialogStage = new Stage();
//            try {
//                Image logo = new Image(getClass().getResourceAsStream("/genki/img/icone_add_user.jpg"), 128, 128, true, true);
//                dialogStage.getIcons().add(logo);
//            } catch (Exception e) {
//                logger.log(Level.WARNING, "Failed to load application logo", e);
//            }
//            dialogStage.setTitle("Add New User");
//            dialogStage.initOwner(((Stage) AddUser.getScene().getWindow()));
//            dialogStage.initModality(Modality.APPLICATION_MODAL);
//            dialogStage.setResizable(false);
//            dialogStage.setScene(new Scene(root, 400, 300));
//            dialogStage.centerOnScreen();
//            dialogStage.showAndWait();
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "Error loading AddUser dialog", e);
//            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to load AddUser dialog.");
//            errorAlert.showAndWait();
//        }
//    }
//    
//    @FXML
//    private void AddGroup() {
//    	  try {
//              logger.log(Level.INFO, "Loading AddGroup.fxml");
//              FXMLLoader loader = new FXMLLoader(getClass().getResource("/genki/views/AddGroup.fxml"));
//              Parent root = loader.load();
//              
//              Stage dialogStage = new Stage();
//              try {
//                  Image logo = new Image(getClass().getResourceAsStream("/genki/img/icone_add_group.jpg"), 128, 128, true, true);
//                  dialogStage.getIcons().add(logo);
//              } catch (Exception e) {
//                  logger.log(Level.WARNING, "Failed to load application logo", e);
//              }
//              dialogStage.setTitle("Add New Group");
//              dialogStage.initModality(Modality.APPLICATION_MODAL);
//              dialogStage.setResizable(false);
//              dialogStage.setScene(new Scene(root));
//              dialogStage.centerOnScreen();
//              dialogStage.showAndWait();
//          } catch (Exception e) {
//              logger.log(Level.SEVERE, "Error loading AddGroup dialog", e);
//              Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to load AddGroup dialog.");
//              errorAlert.showAndWait();
//          }
//    }
//    
//    private void closeCurrentWindow() {
//        Stage stage = (Stage) AddUser.getScene().getWindow();
//        stage.close();
//    }
//}