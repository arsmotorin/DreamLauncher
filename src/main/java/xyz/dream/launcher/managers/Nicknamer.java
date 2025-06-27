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