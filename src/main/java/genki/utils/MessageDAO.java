package genki.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import genki.models.Message;
import java.time.LocalDateTime;

public class MessageDAO {
    
    private DBConnection DBConnect = new DBConnection("genki_testing");
    private MongoDatabase database = DBConnect.getDatabase();
    private MongoCollection<Document> messages = database.getCollection("Message");
    
    /**
     * Insert a new message into the database
     * @param message The message object to insert
     * @return The ObjectId of the inserted message
     */
    public ObjectId insertMessage(Message message) {
        try {
            Document doc = new Document()
                    .append("conversationId", message.getConversationId())
                    .append("senderId", message.getSenderId())
                    .append("senderName", message.getSenderName())
                    .append("content", message.getContent())
                    .append("timestamp", message.getTimestamp())
                    .append("isRead", message.isRead());
            
            var result = messages.insertOne(doc);
            return result.getInsertedId().asObjectId().getValue();
        } catch (Exception e) {
            System.err.println("Error inserting message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create and insert a new message
     * @param conversationId The ID of the conversation
     * @param senderId The ID of the sender
     * @param senderName The name of the sender
     * @param content The message content
     * @return The ObjectId of the created message
     */
    public ObjectId sendMessage(ObjectId conversationId, String senderId, String senderName, String content) {
        try {
            Message message = new Message(conversationId, senderId, senderName, content);
            ObjectId messageId = insertMessage(message);
            
            // Update the conversation's last message
            if (messageId != null) {
                ConversationDAO conversationDAO = new ConversationDAO();
                conversationDAO.updateLastMessage(conversationId, content, senderId);
            }
            
            return messageId;
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Mark a message as read
     * @param messageId The message ID
     */
    public void markAsRead(ObjectId messageId) {
        try {
            Document update = new Document("$set", new Document()
                    .append("isRead", true));
            
            messages.updateOne(
                    new Document("_id", messageId),
                    update
            );
        } catch (Exception e) {
            System.err.println("Error marking message as read: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Mark all messages in a conversation as read
     * @param conversationId The conversation ID
     */
    public void markAllAsRead(ObjectId conversationId) {
        try {
            Document update = new Document("$set", new Document()
                    .append("isRead", true));
            
            messages.updateMany(
                    new Document("conversationId", conversationId)
                            .append("isRead", false),
                    update
            );
        } catch (Exception e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
