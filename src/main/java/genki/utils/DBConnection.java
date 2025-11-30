package genki.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class DBConnection {

    public static MongoClient initConnection(String connectionURI) throws RuntimeException {

            return MongoClients.create(connectionURI);
          }
}
