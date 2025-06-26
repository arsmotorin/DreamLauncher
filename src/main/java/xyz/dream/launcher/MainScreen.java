package xyz.dream.launcher;

import javafx.application.Application;

public class MainScreen extends Application {
    @Override
    public void start(javafx.stage.Stage primaryStage) {
        System.out.println("xyz.dream.launcher.MainScreen started successfully.");

        // Window
        primaryStage.setTitle("Dream Launcher");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setResizable(false);

        // Show the main screen
        primaryStage.show();
    }
}
