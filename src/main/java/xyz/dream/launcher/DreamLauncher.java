package xyz.dream.launcher;

import javafx.application.Application;
import javafx.stage.Stage;
import xyz.dream.launcher.managers.Nicknamer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DreamLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        String settingsPath = getSettingsPath();
        Path settingsFilePath = Paths.get(settingsPath, Nicknamer.SETTINGS_FILE);
        
        System.out.println("Looking for settings at: " + settingsFilePath);
        
        if (Files.exists(settingsFilePath)) {
            try {
                String content = new String(Files.readAllBytes(settingsFilePath));
                if (!content.isEmpty()) {
                    System.out.println("Settings file found, transitioning to MainScreen...");
                    MainScreen mainScreen = new MainScreen();
                    mainScreen.start(primaryStage);
                } else {
                    System.out.println("Settings file is empty, launching StartingScreen...");
                    StartingScreen startingScreen = new StartingScreen();
                    startingScreen.start(primaryStage);
                }
            } catch (Exception e) {
                System.err.println("Error reading settings file: " + e.getMessage());
                System.out.println("Launching StartingScreen due to error...");
                try {
                    StartingScreen startingScreen = new StartingScreen();
                    startingScreen.start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            System.out.println("Settings file not found, launching StartingScreen...");
            try {
                StartingScreen startingScreen = new StartingScreen();
                startingScreen.start(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Set JavaFX system properties to resolve rendering pipeline issues
        System.setProperty("javafx.verbose", "true");
        System.setProperty("prism.verbose", "true");
        System.setProperty("prism.order", "sw");
        
        // Launch the application
        launch(args);
    }
    
    private static String getSettingsPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            return System.getenv("APPDATA") + File.separator + Nicknamer.SETTINGS_FOLDER;
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/" + Nicknamer.SETTINGS_FOLDER;
        } else {
            return userHome + "/.config/" + Nicknamer.SETTINGS_FOLDER;
        }
    }
}