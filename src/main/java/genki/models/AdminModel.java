package genki.models;

import genki.utils.DBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class AdminModel {
    private final DBConnection adminConnection = new DBConnection("genki_testing");

    /**
     * Récupère tous les utilisateurs
     */
    public List<Document> getAllUsers() {
        List<Document> usersList = new ArrayList<>();
        try {
            adminConnection.getUsersCollection().find().into(usersList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usersList;
    }

    /**
     * Supprime un utilisateur par son username
     */
    public void deleteUser(String username) {
        try {
            adminConnection.getUsersCollection().deleteOne(new Document("username", username));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Modifie le statut (bloqué/actif) d'un utilisateur
     */
    public void updateUserStatus(String username, String newStatus) {
        try {
            adminConnection.getUsersCollection().updateOne(
                new Document("username", username),
                new Document("$set", new Document("status", newStatus))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Envoie un message à tous les utilisateurs (Broadcast)
     * On stocke cela dans une collection dédiée "messages"
     */
    public void broadcastMessage(String content) {
        try {
            Document message = new Document("content", content)
                    .append("sender", "ADMIN")
                    .append("type", "GLOBAL")
                    .append("timestamp", new Date());
            
            adminConnection.getDatabase().getCollection("messages").insertOne(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère l'historique des messages globaux (du plus récent au plus ancien)
     */
    public List<Document> getGlobalMessages() {
        List<Document> messages = new ArrayList<>();
        try {
            adminConnection.getDatabase().getCollection("messages")
                .find(new Document("type", "GLOBAL"))
                .sort(Sorts.descending("timestamp"))
                .into(messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }
    
    
    
    
}