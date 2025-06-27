package xyz.dream.launcher.managers;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Nicknamer {

    public static final String SETTINGS_FOLDER = ".DreamLauncher";
    public static final String SETTINGS_FILE = "settings.json";

    /**
     * Loads the nickname from the settings file.
     * If the file does not exist or the nickname is not set, it returns null.
     *
     * @return The nickname as a String, or null if not set.
     */
    public String loadNickname() {
        try {
            Path settingsFile = Paths.get(getSettingsPath(), SETTINGS_FILE);
            if (Files.exists(settingsFile)) {
                String content = new String(Files.readAllBytes(settingsFile));
                return new JsonParser()
                        .parse(content)
                        .getAsJsonObject()
                        .get("nickname")
                        .getAsString();
            }
        } catch (Exception e) {
            System.err.println("Error loading settings: " + e.getMessage());
        }
        return null;
    }

    /**
     * Displays the Cubelius image if the nickname is "cubelius" (launcher's dev).
     * Deletes the settings file if it exists.
     * This method should be called on the JavaFX Application Thread.
     */
    public void showCubeliusImage() {
        String nickname = loadNickname();
        if (!"cubelius".equalsIgnoreCase(nickname)) {
            return;
        }

        try {
            // Delete settings file
            Path settingsFile = Paths.get(getSettingsPath(), SETTINGS_FILE);
            if (Files.exists(settingsFile)) {
                Files.delete(settingsFile);
                System.out.println("Settings file deleted successfully");
            }

            // Load and display image
            Path imagePath = Paths.get("/Users/cubelius/Launcher/DreamLauncher/src/main/resources/Images/cubelius.png");
            if (!Files.exists(imagePath)) {
                System.err.println("Cubelius image not found at: " + imagePath.toAbsolutePath());
                System.exit(1);
                return;
            }

            javafx.application.Platform.runLater(() -> {
                try {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(imagePath.toUri().toString());
                    javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);

                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

                    javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(imageView);
                    root.setStyle("-fx-background-color: transparent;");

                    javafx.scene.Scene scene = new javafx.scene.Scene(root);
                    scene.setFill(null);

                    stage.setScene(scene);
                    stage.show();

                    // Hide other windows                    
                    javafx.stage.Window.getWindows().forEach(window -> {
                        if (window instanceof javafx.stage.Stage && window != stage) {
                            ((javafx.stage.Stage) window).hide();
                        }
                    });

                    // Close after delay
                    javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
                    delay.setOnFinished(event -> {
                        stage.close();
                        System.exit(0);
                    });
                    delay.play();

                } catch (Exception e) {
                    System.err.println("Error displaying image: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
            });

        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Saves the nickname to the settings file.
     * If the settings directory does not exist, it creates it.
     *
     * @param nickname The nickname to save.
     */
    public void saveNickname(String nickname) {
        try {
            Path settingsDir = Paths.get(getSettingsPath());
            Files.createDirectories(settingsDir);

            Path settingsFile = settingsDir.resolve(SETTINGS_FILE);
            JsonObject json = new JsonObject();
            json.addProperty("nickname", nickname);

            Files.write(settingsFile, new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(json)
                    .getBytes());
        } catch (Exception e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    /**
     * @return The path to the settings directory.
     */
    private String getSettingsPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            return System.getenv("APPDATA") + File.separator + SETTINGS_FOLDER;
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/" + SETTINGS_FOLDER;
        } else {
            return userHome + "/.config/" + SETTINGS_FOLDER;
        }
    }
}