package genki.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoException;
import org.bson.Document;

import java.util.logging.Logger;
import java.util.logging.Level;

public class DBConnection {

    private static final Logger logger = Logger.getLogger(DBConnection.class.getName());
    private final String connectionURI = "mongodb+srv://moad:moad2020@cluster0.gtnn0nb.mongodb.net/?appName=Cluster0";

    private final String dbName;
    
    // Singleton instance - only ONE connection for the entire application
    private static DBConnection instance = null;
    private static final Object lock = new Object();


    private DBConnection(String dbName) {
        this.dbName = dbName;
        logger.log(Level.INFO, "✓ DBConnection singleton instance created for: " + dbName);
    }
    
    /**
     * Get the singleton instance of DBConnection
     * Thread-safe implementation
     * @param dbName The database name
     * @return The singleton DBConnection instance
     */
    public static DBConnection getInstance(String dbName) {
        if (instance == null) {
            synchronized(lock) {
                if (instance == null) {
                    instance = new DBConnection(dbName);
                }
            }
        }
        return instance;
    }

    public MongoDatabase getDatabase() throws MongoException{

       try {

           logger.log(Level.INFO, "Connecting to MongoDB ...");
           MongoClient mongoClient = MongoClients.create(connectionURI);
           logger.log(Level.INFO, "Connecting to " + dbName + " ...");
           return mongoClient.getDatabase(dbName);

       } catch (MongoException mongoEXCP) {
          logger.log(Level.WARNING, "Error connecting to database " + mongoEXCP.getMessage());
          throw new MongoException("Mongo Exception", mongoEXCP);
       }
    }

    public MongoCollection<Document> getUsersCollection() throws MongoException {
        try {
            logger.log(Level.INFO, "Connected to database ...");
            logger.log(Level.INFO, "Accessing users collection ...");
            return this.getDatabase().getCollection("users");
        } catch (MongoException mongoException) {
            throw new MongoException(mongoException.getMessage());
        }
    }
    
    // hamza add this
    public MongoCollection<Document> getCollection(String collectionName) throws MongoException {
        try {
            logger.log(Level.INFO, "Accessing collection: " + collectionName);
            return this.getDatabase().getCollection(collectionName);
        } catch (MongoException mongoException) {
            throw new MongoException("Failed to access collection: " + collectionName, mongoException);
        }
    }
    
}
