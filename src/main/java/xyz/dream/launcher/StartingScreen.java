package xyz.dream.launcher;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class StartingScreen extends Application {
    public static final String SETTINGS_FOLDER = ".DreamLauncher";
    public static final String SETTINGS_FILE = "settings.json";
    private Image logo;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Starting Dream Launcher...");
        Pane root = new Pane();
        Scene scene = new Scene(root, 1040, 589);

        // Background color
        scene.setFill(javafx.scene.paint.Color.web("#1C1C1C"));
        root.setStyle("-fx-background-color: #1C1C1C;");

        // Application title
        primaryStage.setTitle("Dream Launcher");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();

        // Load custom font
        Font gilroyMedium = null;
        try {
            gilroyMedium = Font.loadFont(getClass().getResourceAsStream("/Fonts/Gilroy-Medium.ttf"), 18);
            System.out.println("Font loaded successfully");
        } catch (Exception e) {
            System.err.println("Error when loading the font: " + e.getMessage());
            e.printStackTrace();
        }

        // Nickname field background
        Rectangle nickNameField = new Rectangle();
        nickNameField.setX(370);
        nickNameField.setY(372);
        nickNameField.setWidth(300);
        nickNameField.setHeight(56);
        nickNameField.setFill(Color.web("#2F2F2F"));
        nickNameField.setArcWidth(16);
        nickNameField.setArcHeight(16);

        // Add nickNameField to root first (background layer)
        root.getChildren().add(nickNameField);
        System.out.println("Added nickname field background");

        // Load logo
        ImageView logoView = null;
        try {
            logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/logo.png")));
            logoView = new ImageView(logo);
            logoView.setX(392);
            logoView.setY(161);
            root.getChildren().add(logoView);
            System.out.println("Logo added successfully");
        } catch (Exception e) {
            System.err.println("Error when loading the logo: " + e.getMessage());
            e.printStackTrace();
        }

        // TextField for nickname input
        TextField nickNameInput = new TextField();
        nickNameInput.setLayoutX(390);
        nickNameInput.setLayoutY(372);
        nickNameInput.setPrefWidth(195);
        nickNameInput.setPrefHeight(56);
        nickNameInput.setStyle("-fx-background-color: transparent; -fx-text-fill: #6F6F6F; -fx-background-radius: 8; -fx-border-radius: 8; -fx-prompt-text-fill: #575757;");

        if (gilroyMedium != null) {
            nickNameInput.setFont(gilroyMedium);
        } else {
            nickNameInput.setFont(Font.font("Arial", 18));
        }

        // Character limit (3 to 16 characters)
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.length() >= 3 && newText.length() <= 16) {
                return change;
            } else if (newText.length() < 3) {
                return change;
            }
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        nickNameInput.setTextFormatter(textFormatter);

        // Remove automatic focus traversal
        nickNameInput.setFocusTraversable(false);

        // Load saved nickname if available
        String savedNickname = loadNickname();
        if (savedNickname != null && !savedNickname.isEmpty()) {
            nickNameInput.setText(savedNickname);
            System.out.println("Loaded saved nickname: " + savedNickname);
        } else {
            nickNameInput.setPromptText("Введи свой ник");
        }

        // Remove focus when clicking elsewhere
        root.setOnMouseClicked(event -> {
            if (!nickNameInput.getBoundsInParent().contains(event.getX(), event.getY())) {
                nickNameInput.getParent().requestFocus();
                if (nickNameInput.getText().isEmpty()) {
                    nickNameInput.setPromptText("Введи свой ник");
                }
            }
        });

        // Add nickNameInput to the root
        root.getChildren().add(nickNameInput);
        System.out.println("Added nickname input field");

        // Right arrow
        StackPane rightArrowContainer = null;
        try {
            Image rightArrowImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/rightArrow.png")));
            ImageView rightArrowView = new ImageView(rightArrowImage);

            rightArrowContainer = new StackPane();

            rightArrowContainer.setLayoutX(610);
            rightArrowContainer.setLayoutY(376.5);

            Rectangle rightClickArea = new Rectangle();
            rightClickArea.setWidth(rightArrowImage.getWidth() + 30);
            rightClickArea.setHeight(rightArrowImage.getHeight() + 30);
            rightClickArea.setFill(Color.TRANSPARENT);

            StackPane finalRightArrowContainer1 = rightArrowContainer;
            rightArrowContainer.setOnMouseEntered(e -> {
                ColorAdjust highlight = new ColorAdjust();
                highlight.setBrightness(0.3);
                rightArrowView.setEffect(highlight);

                finalRightArrowContainer1.setCursor(Cursor.HAND);
            });

            rightArrowContainer.setOnMouseExited(e -> {
                rightArrowView.setEffect(null);
            });

            rightArrowContainer.getChildren().addAll(rightClickArea, rightArrowView);

            root.getChildren().add(rightArrowContainer);
            System.out.println("Right arrow with increased click area added to the scene");

            StackPane finalRightArrowContainer = rightArrowContainer;
            rightArrowContainer.setOnMouseClicked(clickEvent -> {
                String currentNickname = nickNameInput.getText().trim();

                // Check if the nickname is valid (3-16 characters)
                if (currentNickname.length() >= 3 && currentNickname.length() <= 16) {
                    // Save nickname before transitioning
                    saveNickname(currentNickname);
                    System.out.println("Valid nickname entered: " + currentNickname);

                    // Transition to xyz.dream.launcher.MainScreen
                    try {
                        root.getChildren().clear();
                        System.out.println("Transitioning to xyz.dream.launcher.MainScreen...");
                        MainScreen mainScreen = new MainScreen();
                        mainScreen.start(primaryStage);

                        System.gc();

                    } catch (Exception ex) {
                        System.err.println("Error transitioning to xyz.dream.launcher.MainScreen: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                } else {
                    // Invalid nickname - show feedback
                    System.out.println("Invalid nickname. Must be 3-16 characters");

                    // Red text
                    nickNameInput.setStyle("-fx-background-color: transparent; -fx-text-fill: #F6342D; -fx-background-radius: 8; -fx-border-radius: 8; -fx-prompt-text-fill: #575757;");
                    nickNameInput.requestFocus();

                    // Reset style after a short delay
                    Timeline timeline = new Timeline(
                            new KeyFrame(
                                    Duration.seconds(2),
                                    resetEvent -> nickNameInput.setStyle("-fx-background-color: transparent; -fx-text-fill: #6F6F6F; -fx-background-radius: 6; -fx-border-radius: 6; -fx-prompt-text-fill: #575757;")
                            )
                    );
                    timeline.play();
                }
            });

        } catch (Exception e) {
            System.err.println("Error loading right arrow: " + e.getMessage());
            e.printStackTrace();
        }

        // Show the stage
        primaryStage.show();
        System.out.println("Application started successfully");

        // Animation
        double offsetY = 300;
        double durationMillis = 850;
        int targetFps = 60;
        int totalFrames = (int) (durationMillis / 1000 * targetFps);

        double logoStartY = -offsetY;
        double logoEndY = 0;
        double fieldStartY = offsetY;
        double fieldEndY = 0;

        if (logoView != null) logoView.setTranslateY(logoStartY);
        nickNameField.setTranslateY(fieldStartY);
        nickNameInput.setTranslateY(fieldStartY);
        if (rightArrowContainer != null) rightArrowContainer.setTranslateY(fieldStartY);

        Timeline timeline = new Timeline();
        for (int i = 0; i <= totalFrames; i++) {
            double t = (double) i / totalFrames;
            double ease = 0.5 - 0.5 * Math.cos(Math.PI * t);

            double logoY = logoStartY + (logoEndY - logoStartY) * ease;
            double fieldY = fieldStartY + (fieldEndY - fieldStartY) * ease;

            ImageView finalLogoView = logoView;
            StackPane finalRightArrowContainer = rightArrowContainer;
            KeyFrame kf = new KeyFrame(Duration.millis(i * (1000.0 / targetFps)), e -> {
                if (finalLogoView != null) finalLogoView.setTranslateY(logoY);
                nickNameField.setTranslateY(fieldY);
                nickNameInput.setTranslateY(fieldY);
                if (finalRightArrowContainer != null) finalRightArrowContainer.setTranslateY(fieldY);
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
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

    private void saveNickname(String nickname) {
        try {
            Path settingsDir = Paths.get(getSettingsPath());
            Files.createDirectories(settingsDir);

            Path settingsFile = settingsDir.resolve(SETTINGS_FILE);
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("nickname", nickname);

            Files.write(settingsFile, new com.google.gson.GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(json)
                    .getBytes());
        } catch (Exception e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    private String loadNickname() {
        try {
            Path settingsFile = Paths.get(getSettingsPath(), SETTINGS_FILE);
            if (Files.exists(settingsFile)) {
                String content = new String(Files.readAllBytes(settingsFile));
                return new com.google.gson.JsonParser()
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
}