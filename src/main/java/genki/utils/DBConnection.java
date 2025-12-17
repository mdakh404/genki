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
    private final String connectionURI = "mongodb+srv://mdakh404:moaditatchi2020@genki.vu4rdeo.mongodb.net/?appName=Genki";

    private final String dbName;


    public DBConnection(String dbName) {
        this.dbName= dbName;
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
    
//  private static Connection connection;
//  
//  public static Connection getConnection() throws SQLException {
//      if (connection == null || connection.isClosed()) {
//          String url = "jdbc:mysql://localhost:3306/votre_db";
//          String user = "root";
//          String password = "password";
//          connection = DriverManager.getConnection(url, user, password);
//      }
//      return connection;
//  }
//}

    public MongoCollection<Document> getUsersCollection() throws MongoException {
        try {
            logger.log(Level.INFO, "Connected to database ...");
            logger.log(Level.INFO, "Accessing users collection ...");
            return this.getDatabase().getCollection("users");
        } catch (MongoException mongoException) {
            throw new MongoException(mongoException.getMessage());
        }

    }
}
