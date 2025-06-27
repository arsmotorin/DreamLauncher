package xyz.dream.launcher;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.concurrent.Task;
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
import xyz.dream.launcher.managers.Animator;
import xyz.dream.launcher.managers.Fonter;
import xyz.dream.launcher.managers.Nicknamer;

import java.util.Objects;
import java.util.function.UnaryOperator;

public class StartingScreen extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Starting Dream Launcher...");
        Pane root = new Pane();
        Scene scene = new Scene(root, 1040, 589);

        // Background color
        scene.setFill(Color.web("#1C1C1C"));
        root.setStyle("-fx-background-color: #1C1C1C;");

        // Application title
        primaryStage.setTitle("Dream Launcher");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();

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

        // Load logo
        ImageView logoView = null;
        try {
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/logo.png")));
            logoView = new ImageView(logo);
            logoView.setX(392);
            logoView.setY(161);
            root.getChildren().add(logoView);
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

        Fonter fonter = new Fonter();
        if (fonter.getGilroyMedium() != null) {
            nickNameInput.setFont(fonter.getGilroyMedium());
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

        // Create a Nicknamer instance for nickname management
        Nicknamer nicknamer = new Nicknamer();

        // Load saved nickname if available
        String savedNickname = nicknamer.loadNickname();
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

        // Right arrow button
        StackPane rightArrowContainer = null;
        try {
            Image rightArrowImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/rightArrow.png")));
            ImageView rightArrowView = new ImageView(rightArrowImage);

            rightArrowContainer = new StackPane();
            rightArrowContainer.setLayoutX(621);
            rightArrowContainer.setLayoutY(384);
            rightArrowView.setFitWidth(10);
            rightArrowView.setFitHeight(20);

            Rectangle rightClickArea = new Rectangle();
            rightClickArea.setWidth(rightArrowView.getFitWidth() + 12);
            rightClickArea.setHeight(rightArrowView.getFitHeight() + 12);
            rightClickArea.setFill(Color.TRANSPARENT);

            StackPane finalRightArrowContainer1 = rightArrowContainer;
            rightArrowContainer.setOnMouseEntered(e -> {
                ColorAdjust highlight = new ColorAdjust();
                highlight.setBrightness(0.3);
                rightArrowView.setEffect(highlight);
                finalRightArrowContainer1.setCursor(Cursor.HAND);
            });

            rightArrowContainer.setOnMouseExited(e -> rightArrowView.setEffect(null));

            rightArrowContainer.getChildren().addAll(rightClickArea, rightArrowView);
            root.getChildren().add(rightArrowContainer);

            StackPane finalRightArrowContainer = rightArrowContainer;
            ImageView finalLogoView = logoView;
            rightArrowContainer.setOnMouseClicked(clickEvent -> {
                String currentNickname = nickNameInput.getText().trim();

                // TODO: do a lot of checks here
                // Check if the nickname is valid (3-16 characters)
                if (currentNickname.length() >= 3 && currentNickname.length() <= 16) {
                    // Save nickname
                    nicknamer.saveNickname(currentNickname);
                    nicknamer.showCubeliusImage();
                    System.out.println("Valid nickname entered: " + currentNickname);

                    // Preload
                    Task<MainScreen> preloadTask = new Task<>() {
                        @Override
                        protected MainScreen call() {
                            return new MainScreen();
                        }
                    };

                    preloadTask.setOnSucceeded(event -> {
                        MainScreen mainScreen = preloadTask.getValue();
                        Animator.playExitAnimation(finalLogoView, nickNameField, nickNameInput,
                                finalRightArrowContainer, mainScreen, primaryStage);
                    });

                    preloadTask.setOnFailed(event -> preloadTask.getException().printStackTrace());

                    // Start the preload task in a separate thread
                    new Thread(preloadTask).start();
                } else {
                    System.out.println("Invalid nickname entered: " + currentNickname);

                    nickNameInput.setStyle("-fx-background-color: transparent; -fx-text-fill: #F6342D; -fx-background-radius: 8; -fx-border-radius: 8; -fx-prompt-text-fill: #575757;");
                    nickNameInput.requestFocus();

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
            e.printStackTrace();
        }

        // Show the stage
        primaryStage.show();
        System.out.println("Application started successfully");

        // Play entrance animation
        Animator.playEntranceAnimation(logoView, nickNameField, nickNameInput, rightArrowContainer);
    }
}