package xyz.dream.launcher;

import javafx.application.Application;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DreamLauncher {

    public static void main(String[] args) {
        System.setProperty("javafx.verbose", "true");

        String settingsPath = getSettingsPath();
        Path settingsFilePath = Paths.get(settingsPath, StartingScreen.SETTINGS_FILE);
        
        System.out.println("Looking for settings at: " + settingsFilePath);
        
        if (Files.exists(settingsFilePath)) {
            try {
                String content = new String(Files.readAllBytes(settingsFilePath));
                if (!content.isEmpty()) {
                    System.out.println("Settings file found, transitioning to MainScreen...");
                    Application.launch(MainScreen.class, args);
                } else {
                    System.out.println("Settings file is empty, launching StartingScreen...");
                    Application.launch(StartingScreen.class, args);
                }
            } catch (Exception e) {
                System.err.println("Error reading settings file: " + e.getMessage());
                System.out.println("Launching StartingScreen due to error...");
                Application.launch(StartingScreen.class, args);
            }
        } else {
            System.out.println("Settings file not found, launching StartingScreen...");
            Application.launch(StartingScreen.class, args);
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
            return System.getenv("APPDATA") + File.separator + StartingScreen.SETTINGS_FOLDER;
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/" + StartingScreen.SETTINGS_FOLDER;
        } else {
            return userHome + "/.config/" + StartingScreen.SETTINGS_FOLDER;
        }
    }
}