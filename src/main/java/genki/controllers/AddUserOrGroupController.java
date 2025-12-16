package genki.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AddUserOrGroupController {
    
    @FXML
    private Button AddUser;
    
    @FXML
    private Button AddGroup;
    
    private HomeController homeController;
    
    public void setHomeController(HomeController controller) {
        this.homeController = controller;
    }
    
    @FXML
    private void AddUser() {
        
        closeCurrentWindow();
        
        
        if (homeController != null) {
            homeController.AddUser();
        }
    }
    
    @FXML
    private void AddGroup() {
        
        closeCurrentWindow();
        
        if (homeController != null) {
            homeController.AddGroup();
        }
    }
    
    private void closeCurrentWindow() {
        Stage stage = (Stage) AddUser.getScene().getWindow();
        stage.close();
    }
}