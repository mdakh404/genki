package com.ensiasd.genki;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;


public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));

        Scene scene = new Scene(root, 1220, 657);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Genki");
        primaryStage.show();
    }
}