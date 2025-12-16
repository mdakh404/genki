package genki.services;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import genki.models.User;
import genki.utils.DBConnection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private final DBConnection dbConnection;
    private final MongoDatabase database;
    
    public UserService() {
        this.dbConnection = new DBConnection("Genki");
        this.database = dbConnection.getDatabase();
    }
    
    // Ajouter un utilisateur
    public boolean addUser(String username) {
        try {
            MongoCollection<Document> collection = database.getCollection("users");
            
            // Vérifier si l'utilisateur existe déjà
            Document existingUser = collection.find(new Document("username", username)).first();
            if (existingUser != null) {
                logger.log(Level.WARNING, "User " + username + " already exists");
                return false;
            }
            
            // Créer le document utilisateur
            Document userDoc = new Document("username", username)
                    .append("createdAt", new java.util.Date())
                    .append("isActive", true);
            
            collection.insertOne(userDoc);
            logger.log(Level.INFO, "User " + username + " added successfully");
            return true;
            
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error adding user: " + e.getMessage());
            return false;
        }
    }
    
    // Récupérer tous les utilisateurs
    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection("users");
            for (Document doc : collection.find()) {
                users.add(doc.getString("username"));
            }
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error retrieving users: " + e.getMessage());
        }
        return users;
    }
    
    // Supprimer un utilisateur
    public boolean deleteUser(String username) {
        try {
            MongoCollection<Document> collection = database.getCollection("users");
            collection.deleteOne(new Document("username", username));
            logger.log(Level.INFO, "User " + username + " deleted successfully");
            return true;
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error deleting user: " + e.getMessage());
            return false;
        }
    }
    
    // Vérifier si un utilisateur existe
    public boolean userExists(String username) {
        try {
            MongoCollection<Document> collection = database.getCollection("users");
            Document user = collection.find(new Document("username", username)).first();
            return user != null;
        } catch (MongoException e) {
            logger.log(Level.SEVERE, "Error checking user existence: " + e.getMessage());
            return false;
        }
    }
}