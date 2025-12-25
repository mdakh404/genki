package genki.controllers;

import genki.models.AdminModel;
import genki.utils.UserSession;
//import genki.utils.ScenesController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.io.IOException;
import java.util.List;

public class AdminController {

    // --- PANNEAUX DE NAVIGATION ---
    @FXML private VBox panelUsers, panelMessages, panelStats, panelSettings, panelNotif;
    @FXML private Label viewTitle, totalCountLabel, onlineCountLabel;

    // --- TABLEAU UTILISATEURS ---
    @FXML private TableView<Document> userTable;
    @FXML private TableColumn<Document, String> colName, colEmail, colRole, colStatus;

    // --- TABLEAU & CHAMP MESSAGE GLOBAL ---
    @FXML private TextArea globalMessageField;
    @FXML private TableView<Document> messageTable;
    @FXML private TableColumn<Document, String> colMsgDate, colMsgContent;

    private final AdminModel adminModel = new AdminModel();

    @FXML
    public void initialize() {
        // Configuration Table Utilisateurs
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getString("username")));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getString("bio")));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getString("role")));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrDefault("status", "actif").toString()));

        // Configuration Table Messages Globaux
        colMsgDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get("timestamp").toString()));
        colMsgContent.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getString("content")));

        loadUsersData();
    }

    // --- LOGIQUE DE NAVIGATION (SWITCH PANELS) ---
    private void hideAllPanels() {
        panelUsers.setVisible(false);
        panelMessages.setVisible(false);
        panelStats.setVisible(false);
        panelSettings.setVisible(false);
        panelNotif.setVisible(false);
    }

    @FXML public void showUsersPanel() { hideAllPanels(); panelUsers.setVisible(true); viewTitle.setText("Gestion des Utilisateurs"); loadUsersData(); }
    @FXML public void showMessagesPanel() { hideAllPanels(); panelMessages.setVisible(true); viewTitle.setText("Message Global"); loadMessagesHistory(); }
    @FXML public void showStatsPanel() { hideAllPanels(); panelStats.setVisible(true); }
    @FXML public void showSettingsPanel() { hideAllPanels(); panelSettings.setVisible(true); }
    @FXML public void showNotifPanel() { hideAllPanels(); panelNotif.setVisible(true); }

    // --- GESTION DES MESSAGES GLOBAUX ---
    @FXML
    public void sendGlobalMessage() {
        String content = globalMessageField.getText();
        if (content != null && !content.trim().isEmpty()) {
            
            // On envoie via le socket de la session admin
            if (UserSession.getClientSocket() != null) {
                // Le serveur va détecter "BROADCAST_MSG:"
                UserSession.getClientSocket().sendMessages("BROADCAST_MSG:" + content);
                
                // On garde une trace en base de données
                adminModel.broadcastMessage(content);

                globalMessageField.clear();
                loadMessagesHistory();
            }
        }
    }

    private void loadMessagesHistory() {
        List<Document> msgs = adminModel.getGlobalMessages();
        messageTable.setItems(FXCollections.observableArrayList(msgs));
    }

    // --- GESTION DES UTILISATEURS ---
    private void loadUsersData() {
        List<Document> users = adminModel.getAllUsers();
        userTable.setItems(FXCollections.observableArrayList(users));
        
        // Mise à jour des Stats
        totalCountLabel.setText(String.valueOf(users.size()));
        long online = users.stream().filter(u -> u.get("is_online") != null && (boolean) u.get("is_online")).count();
        onlineCountLabel.setText(online + " Actifs");
    }

    @FXML
    public void toggleStatus() {
        Document selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String newStatus = "bloqué".equals(selected.getString("status")) ? "actif" : "bloqué";
            adminModel.updateUserStatus(selected.getString("username"), newStatus);
            loadUsersData();
        }
    }

    @FXML
    public void handleDeleteUser() {
        Document selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            adminModel.deleteUser(selected.getString("username"));
            loadUsersData();
        }
    }

    @FXML public void handleLogout() throws IOException { ScenesController.switchToScene("/genki/views/Login.fxml", "Login"); }
    
    // Méthodes placeholder pour éviter les erreurs FXML
    @FXML public void addUser() {}
    @FXML public void deleteMessage() {}
    @FXML public void exportPDF() {}
    @FXML public void exportCSV() {}
}