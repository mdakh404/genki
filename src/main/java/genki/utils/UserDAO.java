package genki.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import genki.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    private DBConnection DBConnect = new DBConnection("genki_testing");
    private MongoDatabase database = DBConnect.getDatabase();
    private MongoCollection<Document> users = database.getCollection("users");
    
    /**
     * Get user by username
     * @param username The username
     * @return User document or null if not found
     */
    public Document getUserByUsername(String username) {
        try {
            return users.find(Filters.eq("username", username)).first();
        } catch (Exception e) {
            System.err.println("Error getting user by username: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get user by ID
     * @param userId The user ObjectId
     * @return User document or null if not found
     */
    public Document getUserById(ObjectId userId) {
        try {
            return users.find(Filters.eq("_id", userId)).first();
        } catch (Exception e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get all friends of a user
     * @param username The username
     * @return List of friend documents
     */
    public List<Document> getFriendsForUser(String username) {
        try {
            List<Document> friendsList = new ArrayList<>();
            Document userDoc = getUserByUsername(username);
            
            if (userDoc == null) return friendsList;
            
            List<?> friendIds = userDoc.getList("friends", Object.class);
            if (friendIds == null || friendIds.isEmpty()) return friendsList;
            
            for (Object friendObj : friendIds) {
                ObjectId friendId;
                if (friendObj instanceof ObjectId) {
                    friendId = (ObjectId) friendObj;
                } else {
                    try {
                        friendId = new ObjectId(friendObj.toString());
                    } catch (Exception e) {
                        System.err.println("Invalid friend ID format: " + friendObj);
                        continue;
                    }
                }
                
                Document friendDoc = getUserById(friendId);
                if (friendDoc != null) {
                    friendsList.add(friendDoc);
                }
            }
            
            return friendsList;
        } catch (Exception e) {
            System.err.println("Error getting friends for user: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Convert Document to User object (with MongoDB _id)
     */
    public User documentToUser(Document doc) {
        if (doc == null) return null;
        
        User user = new User();
        // Set the ID from MongoDB _id field
        if (doc.getObjectId("_id") != null) {
            user.setId(doc.getObjectId("_id").toHexString());
        }
        user.setUsername(doc.getString("username"));
        user.setPassword(doc.getString("password"));
        user.setBio(doc.getString("bio"));
        user.setRole(doc.getString("role"));
        user.setPhotoUrl(doc.getString("photo_url"));
        
        @SuppressWarnings("unchecked")
        List<String> friends = (List<String>) (List<?>) doc.getList("friends", Object.class);
        user.setFriends(friends);
        
        return user;
    }
}
