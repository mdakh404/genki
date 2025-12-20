package genki.controllers;

import genki.models.AdminModel;
//import genki.utils.ScenesController;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import org.bson.Document;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;

public class AdminController {

    @FXML private TableView<Document> userTable;
    @FXML private TableColumn<Document, String> colName;
    @FXML private TableColumn<Document, String> colEmail;
    @FXML private TableColumn<Document, String> colRole;

    private final AdminModel adminModel = new AdminModel();

    @FXML
    public void initialize() {
        // Configuration des colonnes pour lire les documents BSON de MongoDB
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getString("username")));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getString("bio")));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getString("role")));

        loadUsersData();
    }

    private void loadUsersData() {
        // On transforme la liste de MongoDB en liste observable pour JavaFX
        ObservableList<Document> users = FXCollections.observableArrayList(adminModel.getAllUsers());
        userTable.setItems(users);
    }

    @FXML
    private void handleDeleteUser() {
        Document selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            adminModel.deleteUser(selected.getString("username"));
            loadUsersData(); // Rafraîchir le tableau
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        ScenesController.switchToScene("/genki/views/Login.fxml", "Genki - Login");
    }
}