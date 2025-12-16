module genki {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires java.logging;
    requires org.mongodb.bson;
    requires jbcrypt;
    requires cloudinary.core;
    requires dotenv.java;


    exports genki;
    opens genki to javafx.fxml;
    exports genki.utils;
    opens genki.utils to javafx.fxml;
    exports genki.controllers;
    opens genki.controllers to javafx.fxml;
}