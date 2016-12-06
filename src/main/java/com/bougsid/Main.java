package com.bougsid;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("societe.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
//        ISocieteMetier societeMetier = (ISocieteMetier) Naming.lookup("rmi://localhost:1999/societeRmiService");
//        Societe societe = societeMetier.findSocieteByCode("MA0000012296");
//        System.out.println(societe.getName());

    }


    public static void main(String[] args) {
        launch(args);
    }
}
