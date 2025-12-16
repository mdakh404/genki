package genki.services;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import genki.utils.DBConnection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupService {
    private static final Logger logger = Logger.getLogger(GroupService.class.getName());
    private final DBConnection dbConnection;
    private final MongoDatabase database;
    
    public GroupService() {
        this.dbConnection = new DBConnection("Genki");
        this.database = dbConnection.getDatabase();
    }
    
    // Ajouter un groupe
    public boolean addGroup(String groupName, String description) {
        try {
            MongoCollection<Document> collection = database.getCollection("groups");
            
            // Vérifier si le groupe existe déjà
            Document existingGroup = collection.find(new Document("groupName", groupName)).first();
            if (existingGroup != null) {
                logger.log(Level.WARNING, "Group " + groupName + " already exists");
                return false;
            }
            
            // Créer le document groupe
            Document groupDoc = new Document("groupName", groupName)
                    .append("description", description)
                    .append("createdAt", new java.util.Date())
                    .append("members", new ArrayList<String>())
                    .append("isActive", true);
            
            collection.insertOne(groupDoc);
            logger.log(Level.INFO, "Group " + groupName + " added successfully");
            return true;
            
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error adding group: " + e.getMessage());
            return false;
        }
    }
    
    // Ajouter un membre au groupe
    public boolean addMemberToGroup(String groupName, String username) {
        try {
            MongoCollection<Document> collection = database.getCollection("groups");
            
            Document filter = new Document("groupName", groupName);
            Document update = new Document("$addToSet", new Document("members", username));
            
            collection.updateOne(filter, update);
            logger.log(Level.INFO, "User " + username + " added to group " + groupName);
            return true;
            
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error adding member to group: " + e.getMessage());
            return false;
        }
    }
    
    // Récupérer tous les groupes
    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection("groups");
            for (Document doc : collection.find()) {
                groups.add(doc.getString("groupName"));
            }
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error retrieving groups: " + e.getMessage());
        }
        return groups;
    }
    
    // Supprimer un groupe
    public boolean deleteGroup(String groupName) {
        try {
            MongoCollection<Document> collection = database.getCollection("groups");
            collection.deleteOne(new Document("groupName", groupName));
            logger.log(Level.INFO, "Group " + groupName + " deleted successfully");
            return true;
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error deleting group: " + e.getMessage());
            return false;
        }
    }
}