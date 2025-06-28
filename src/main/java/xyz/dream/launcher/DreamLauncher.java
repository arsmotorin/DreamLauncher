// Copyright 2025 © Arsenii Motorin
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package xyz.dream.launcher;

import javafx.application.Application;
import javafx.stage.Stage;
import xyz.dream.launcher.managers.Nicknamer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DreamLauncher extends Application {

    /**
     * The main entry point for the JavaFX application.
     * It checks for the existence of a settings file and starts the screen.
     *
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        String settingsPath = getSettingsPath();
        Path settingsFilePath = Paths.get(settingsPath, Nicknamer.SETTINGS_FILE);
        
        System.out.println("Looking for settings at: " + settingsFilePath);
        
        if (Files.exists(settingsFilePath)) {
            try {
                String content = new String(Files.readAllBytes(settingsFilePath));
                if (!content.isEmpty()) {
                    System.out.println("Settings file found");
                    MainScreen mainScreen = new MainScreen();
                    mainScreen.start(primaryStage);
                } else {
                    System.out.println("Settings file is empty");
                    StartingScreen startingScreen = new StartingScreen();
                    startingScreen.start(primaryStage);
                }
            } catch (Exception e) {
                System.err.println("Error reading settings file: " + e.getMessage());
                System.out.println("Closing the application...");
                try {
                    System.exit(0);
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
        System.setProperty("javafx.verbose", "true");
        System.setProperty("prism.verbose", "true");
        System.setProperty("prism.order", "sw");
        
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