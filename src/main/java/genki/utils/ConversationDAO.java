package genki.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import genki.models.Conversation;
import java.time.LocalDateTime;
import java.util.List;

public class ConversationDAO {
    
    private DBConnection DBConnect = DBConnection.getInstance("genki_testing");
    private MongoDatabase database = DBConnect.getDatabase();
    private MongoCollection<Document> conversations = database.getCollection("Conversation");
    
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
    
    public ObjectId createDirectConversation(String userId1, String userId2) {
        try {
            ObjectId existingId = findDirectConversation(userId1, userId2);
            if (existingId != null) {
                System.out.println("Conversation already exists: " + existingId);
                return existingId;
            }
            
            Conversation conversation = new Conversation("direct", List.of(userId1, userId2));
            return insertConversation(conversation);
        } catch (Exception e) {
            System.err.println("Error creating direct conversation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
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
    
    public ObjectId createGroupConversation(List<String> participantIds, String groupName, String photoUrl) {
        return createGroupConversation(participantIds, groupName, photoUrl, null);
    }

    public ObjectId createGroupConversation(List<String> participantIds, String groupName, String photoUrl, String groupId) {
        try {
            ObjectId existingId = findGroupConversation(groupName);
            if (existingId != null) {
                System.out.println("Group conversation already exists: " + existingId + " for group: " + groupName);
                return existingId;
            }
            
            Document doc = new Document()
                    .append("type", "group")
                    .append("participantIds", participantIds)
                    .append("groupName", groupName)
                    .append("groupId", groupId)
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
