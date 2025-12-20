package genki.models;

import genki.utils.DBConnection;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class AdminModel {
    // On reautilise la connexion existante cls DBconnection
    private final DBConnection adminConnection = new DBConnection("genki_testing");

    /**
     * Get tous les utilisateurs de la collection
     */
    public List<Document> getAllUsers() {
        List<Document> usersList = new ArrayList<>();
        try {
            MongoCollection<Document> collection = adminConnection.getUsersCollection();
            // On récupère tout et on le met dans une liste
            collection.find().into(usersList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usersList;
    }

    /**
     * Supprime un utilisateur via son email (unique)
     */
    public void deleteUser(String username) {
        try {
            adminConnection.getUsersCollection().deleteOne(new Document("username", username));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}