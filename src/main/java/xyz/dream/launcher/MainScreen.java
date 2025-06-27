package xyz.dream.launcher;

import javafx.application.Application;

public class MainScreen extends Application {
    @Override
    public void start(javafx.stage.Stage primaryStage) {
        System.out.println("xyz.dream.launcher.MainScreen started successfully.");

        Pane root = new Pane();
        Scene scene = new Scene(root, 1040, 589);

        // Background color
        scene.setFill(javafx.scene.paint.Color.web("#1C1C1C"));
        root.setStyle("-fx-background-color: #1C1C1C;");

        // Application title
        primaryStage.setTitle("Dream Launcher");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        // Show the main screen
        primaryStage.show();
    }
}
