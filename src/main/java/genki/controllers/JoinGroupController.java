package genki.controllers;

import genki.models.GroupModel;
import genki.utils.DBConnection;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JoinGroupController {

    private static final GroupModel groupModel = new GroupModel();
    private static final DBConnection JoinGroupDBConnection = new DBConnection("genki_testing");

    @FXML
    private TextField CodeJoinGroup;
    
    @FXML
    private ListView<String> listSuggestions;
    
    @FXML
    private Button btnJoinGroup;
    
    @FXML
    private Button btnCancel;
    
    // Liste de tous les groupes disponibles
    private ObservableList<String> allGroups = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupAutoComplete();
        allGroups.addAll(groupModel.getGroupNames());
    }
    
    /**
     * Configure le système d'auto-complétion pour le TextField
     */
    private void setupAutoComplete() {
        FilteredList<String> filteredGroups = new FilteredList<>(allGroups, s -> true);
        listSuggestions.setItems(filteredGroups);
        
        // 🔥 Filtrage en temps réel
        CodeJoinGroup.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                listSuggestions.setVisible(false);
                return;
            }
            
            filteredGroups.setPredicate(group ->
                group.toLowerCase().contains(newText.toLowerCase())
            );
            
            listSuggestions.setVisible(!filteredGroups.isEmpty());
        });
        
        // 🔥 Cliquer sur une suggestion
        listSuggestions.setOnMouseClicked(e -> {
            String selected = listSuggestions.getSelectionModel().getSelectedItem();
            if (selected != null) {
                CodeJoinGroup.setText(selected);
                listSuggestions.setVisible(false);
            }
        });
        
        // 🔥 Masquer les suggestions si le champ perd le focus
        CodeJoinGroup.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                // Délai pour permettre le clic sur la suggestion
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                        javafx.application.Platform.runLater(() -> 
                            listSuggestions.setVisible(false)
                        );
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
    
    @FXML
    private void handleJoinGroup() {
        String codeGroup = CodeJoinGroup.getText().trim();
        
        if (codeGroup.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                     "Please enter a group code.");
            return;
        }
        
        // TODO: Ajouter la logique pour rejoindre le groupe dans votre base de données
        System.out.println("Joining group: " + codeGroup);
        
        showAlert(Alert.AlertType.INFORMATION, "Success", 
                 "Group '" + codeGroup + "' joined successfully!");
        
        closeWindow();
    }
    
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    
    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Ferme la fenêtre actuelle
     */
    private void closeWindow() {
        Stage stage = (Stage) btnJoinGroup.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
    
    /**
     * Méthode pour mettre à jour la liste des groupes disponibles
     * (à appeler depuis l'extérieur si nécessaire)
     */
    public void setAvailableGroups(ObservableList<String> groups) {
        this.allGroups.setAll(groups);
    }
}