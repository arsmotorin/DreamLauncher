package xyz.dream.launcher.managers;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Iconer {
    private static final String[] ICON_SIZES = {"16", "32", "48", "64", "128"};
    private static final String ICON_PATH = "/Images/icon";

    public static void setApplicationIcon(Stage stage, String appName) {
        List<Image> icons = loadIcons();
        if (!icons.isEmpty()) {
            stage.getIcons().addAll(icons);
        }
    }

    private static List<Image> loadIcons() {
        List<Image> icons = new ArrayList<>();

        for (String size : ICON_SIZES) {
            try {
                InputStream stream = Iconer.class.getResourceAsStream(ICON_PATH + "-" + size + ".png");
                if (stream != null) {
                    icons.add(new Image(stream));
                }
            } catch (Exception e) {
                System.err.println("Icon not found for size: " + size + ". Error: " + e.getMessage());}
        }

        if (icons.isEmpty()) {
            try {
                InputStream stream = Iconer.class.getResourceAsStream(ICON_PATH + ".png");
                if (stream != null) {
                    icons.add(new Image(stream));
                }
            } catch (Exception e) {
                System.err.println("No application icons found");
            }
        }

        return icons;
    }
}