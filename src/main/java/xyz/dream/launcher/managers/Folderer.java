package xyz.dream.launcher.managers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Folderer {

    /**
     * Creates a folder for Minecraft files.
     * The folder is created in the user's home directory or the appropriate application data directory.
     *
     * @return The path to the created folder as a String.
     */
    public static String createTheFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        String folderPath;
        
        if (os.contains("mac")) {
            folderPath = userHome + "/Library/Application Support/DreamLauncher";
        } else if (os.contains("win")) {
            folderPath = System.getenv("APPDATA") + "\\DreamLauncher";
        } else {
            folderPath = userHome + "/DreamLauncher";
        }
        
        try {
            Path path = Paths.get(folderPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("DreamLauncher folder created at: " + folderPath);
            }
        } catch (Exception e) {
            System.err.println("Error creating DreamLauncher directory: " + e.getMessage());
        }
        
        return folderPath;
    }
}
