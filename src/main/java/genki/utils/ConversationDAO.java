package genki.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import genki.models.Conversation;
import java.time.LocalDateTime;
import java.util.List;

public class ConversationDAO {
    
    private DBConnection DBConnect = new DBConnection("genki_testing");
    private MongoDatabase database = DBConnect.getDatabase();
    private MongoCollection<Document> conversations = database.getCollection("Conversation");
    
    /**
     * Insert a new conversation into the database
     * @param conversation The conversation object to insert
     * @return The ObjectId of the inserted conversation
     */
    public ObjectId insertConversation(Conversation conversation) {
        try {
            Document doc = new Document()
                    .append("type", conversation.getType())
                    .append("participantIds", conversation.getParticipantIds())
                    .append("lastMessageContent", conversation.getLastMessageContent())
                    .append("lastMessageSenderId", conversation.getLastMessageSenderId())
                    .append("lastMessageTime", conversation.getLastMessageTime())
                    .append("createdAt", conversation.getCreatedAt())
                    .append("updatedAt", conversation.getUpdatedAt());
            
            var result = conversations.insertOne(doc);
            return result.getInsertedId().asObjectId().getValue();
        } catch (Exception e) {
            System.err.println("Error inserting conversation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Find an existing direct conversation between two users
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return The ObjectId of the existing conversation, or null if not found
     */
    public ObjectId findDirectConversation(String userId1, String userId2) {
        try {
            Document query = new Document("type", "direct")
                    .append("participantIds", new Document("$all", List.of(userId1, userId2)));
            
            Document result = conversations.find(query).first();
            
            if (result != null) {
                return result.getObjectId("_id");
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error finding direct conversation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create a new direct conversation between two users, or return existing one
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return The ObjectId of the conversation (existing or newly created)
     */
    public ObjectId createDirectConversation(String userId1, String userId2) {
        try {
            // First check if conversation already exists
            ObjectId existingId = findDirectConversation(userId1, userId2);
            if (existingId != null) {
                System.out.println("Conversation already exists: " + existingId);
                return existingId;
            }
            
            // If not, create a new one
            Conversation conversation = new Conversation("direct", List.of(userId1, userId2));
            return insertConversation(conversation);
        } catch (Exception e) {
            System.err.println("Error creating direct conversation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Find an existing group conversation by group name
     * @param groupName Name of the group
     * @return The ObjectId of the existing conversation, or null if not found
     */
    public ObjectId findGroupConversation(String groupName) {
        try {
            Document query = new Document("type", "group")
                    .append("groupName", groupName);
            
            Document result = conversations.find(query).first();
            
            if (result != null) {
                return result.getObjectId("_id");
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error finding group conversation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create a new group conversation with group details, or return existing one
     * @param participantIds List of participant IDs
     * @param groupName Name of the group
     * @param photoUrl Profile picture URL of the group
     * @return The ObjectId of the conversation (existing or newly created)
     */
    public ObjectId createGroupConversation(List<String> participantIds, String groupName, String photoUrl) {
        try {
            // First check if conversation already exists for this group
            ObjectId existingId = findGroupConversation(groupName);
            if (existingId != null) {
                System.out.println("Group conversation already exists: " + existingId + " for group: " + groupName);
                return existingId;
            }
            
            // If not, create a new one
            Document doc = new Document()
                    .append("type", "group")
                    .append("participantIds", participantIds)
                    .append("groupName", groupName)
                    .append("photo_url", photoUrl)
                    .append("lastMessageContent", "")
                    .append("lastMessageSenderId", "")
                    .append("lastMessageTime", LocalDateTime.now())
                    .append("createdAt", LocalDateTime.now())
                    .append("updatedAt", LocalDateTime.now());
            
            var result = conversations.insertOne(doc);
            return result.getInsertedId().asObjectId().getValue();
        } catch (Exception e) {
            System.err.println("Error creating group conversation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create a new group conversation
     * @param participantIds List of participant IDs
     * @return The ObjectId of the created conversation
     */
    public ObjectId createGroupConversation(List<String> participantIds) {
        try {
            Conversation conversation = new Conversation("group", participantIds);
            return insertConversation(conversation);
        } catch (Exception e) {
            System.err.println("Error creating group conversation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Update the last message in a conversation
     * @param conversationId The conversation ID
     * @param messageContent The content of the last message
     * @param senderId The ID of the sender
     */
    public void updateLastMessage(ObjectId conversationId, String messageContent, String senderId) {
        try {
            Document update = new Document("$set", new Document()
                    .append("lastMessageContent", messageContent)
                    .append("lastMessageSenderId", senderId)
                    .append("lastMessageTime", LocalDateTime.now())
                    .append("updatedAt", LocalDateTime.now()));
            
            conversations.updateOne(
                    new Document("_id", conversationId),
                    update
            );
        } catch (Exception e) {
            System.err.println("Error updating last message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get a conversation by its ID
     * @param conversationId The conversation ID
     * @return The Conversation object, or null if not found
     */
    public Conversation getConversationById(ObjectId conversationId) {
        try {
            Document doc = conversations.find(new Document("_id", conversationId)).first();
            
            if (doc != null) {
                Conversation conversation = new Conversation();
                conversation.setId(doc.getObjectId("_id"));
                conversation.setType(doc.getString("type"));
                conversation.setParticipantIds(doc.getList("participantIds", String.class));
                conversation.setLastMessageContent(doc.getString("lastMessageContent"));
                conversation.setLastMessageSenderId(doc.getString("lastMessageSenderId"));
                
                // Handle LocalDateTime if stored as Date or string
                Object lastMessageTime = doc.get("lastMessageTime");
                if (lastMessageTime != null) {
                    if (lastMessageTime instanceof LocalDateTime) {
                        conversation.setLastMessageTime((LocalDateTime) lastMessageTime);
                    }
                }
                
                conversation.setGroupName(doc.getString("groupName"));
                conversation.setPhotoUrl(doc.getString("photo_url"));
                
                return conversation;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting conversation by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
