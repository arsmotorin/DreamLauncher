package xyz.dream.launcher;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import xyz.dream.launcher.managers.Fonter;
import xyz.dream.launcher.managers.Nicknamer;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;

public class MainScreen extends Application {
    private Image logo;
    private final int playtime = 100;

    @Override
    public void start(javafx.stage.Stage primaryStage) {
        System.out.println("MainScreen started successfully");

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

        // Logo
        ImageView logoView = null;
        try {
            logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/logo.png")));
            logoView = new ImageView(logo);
            logoView.setX(68);
            logoView.setY(62);
            logoView.setFitWidth(128);
            logoView.setFitHeight(80);
            root.getChildren().add(logoView);
        } catch (Exception e) {
            System.err.println("Error when loading the logo: " + e.getMessage());
            e.printStackTrace();
        }

        // Text
        Text launcherText = new Text("Dream Launcher");
        launcherText.setX(231);
        launcherText.setY(99);

        Fonter fonter = new Fonter();

        if (fonter.getGilroyBold() != null) {
            launcherText.setFont(fonter.getGilroyBold());
            launcherText.setStyle("-fx-font-size: 24;");
            launcherText.setFill(javafx.scene.paint.Color.web("#5AC40F"));
        } else {
            launcherText.setFont(Font.font("Arial", 24));
        }

        root.getChildren().add(launcherText);

        // Description text
        Text desctiptionText = new Text("Launcher for Frogdream players");
        desctiptionText.setX(231);
        desctiptionText.setY(122);

        if (fonter.getGilroyMedium() != null) {
            desctiptionText.setFont(fonter.getGilroyMedium());
            desctiptionText.setStyle("-fx-font-size: 18;");
            desctiptionText.setFill(javafx.scene.paint.Color.web("#6F6F6F"));
        } else {
            desctiptionText.setFont(Font.font("Arial", 18));
        }

        root.getChildren().add(desctiptionText);

        // Play button
        Rectangle playButton = new Rectangle();
        playButton.setX(68);
        playButton.setY(184);
        playButton.setWidth(288);
        playButton.setHeight(56);
        playButton.setFill(Color.web("#5AC40F"));
        playButton.setArcWidth(24);
        playButton.setArcHeight(24);

        root.getChildren().add(playButton);

        // Play icon
        ImageView playIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/play.png"))));
        playIcon.setFitWidth(24);
        playIcon.setFitHeight(24);
        playIcon.setLayoutX(84);
        playIcon.setLayoutY(200);

        root.getChildren().add(playIcon);

        // Game folder button
        Rectangle gameFolderButton = new Rectangle();
        gameFolderButton.setX(372);
        gameFolderButton.setY(184);
        gameFolderButton.setWidth(56);
        gameFolderButton.setHeight(56);
        gameFolderButton.setFill(Color.web("#2B2B2B"));
        gameFolderButton.setArcWidth(24);
        gameFolderButton.setArcHeight(24);

        root.getChildren().add(gameFolderButton);

        // Game folder icon
        ImageView gameFolderIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/folder.png"))));
        gameFolderIcon.setFitWidth(24);
        gameFolderIcon.setFitHeight(24);
        gameFolderIcon.setLayoutX(388);
        gameFolderIcon.setLayoutY(200);

        root.getChildren().add(gameFolderIcon);

        // Settings button
        Rectangle settingsButton = new Rectangle();
        settingsButton.setX(444);
        settingsButton.setY(184);
        settingsButton.setWidth(56);
        settingsButton.setHeight(56);
        settingsButton.setFill(Color.web("#2B2B2B"));
        settingsButton.setArcWidth(24);
        settingsButton.setArcHeight(24);

        root.getChildren().add(settingsButton);

        // Settings icon
        ImageView settingsIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/settings.png"))));
        settingsIcon.setFitWidth(24);
        settingsIcon.setFitHeight(24);
        settingsIcon.setLayoutX(460);
        settingsIcon.setLayoutY(200);

        root.getChildren().add(settingsIcon);

        // Big rectangle
        Rectangle bigRectangle = new Rectangle();
        bigRectangle.setX(593);
        bigRectangle.setY(62);
        bigRectangle.setWidth(379);
        bigRectangle.setHeight(466);
        bigRectangle.setFill(Color.web("#232323"));
        bigRectangle.setArcWidth(32);
        bigRectangle.setArcHeight(32);

        root.getChildren().add(bigRectangle);

        // Player Head
        String nickname = new Nicknamer().loadNickname();
        ImageView headImage = new ImageView(new Image("https://minotar.net/helm/" + nickname + "/100.png"));
        headImage.setFitWidth(43);
        headImage.setFitHeight(43);
        headImage.setLayoutX(68);
        headImage.setLayoutY(485);

        Rectangle clip = new Rectangle(43, 43);
        clip.setArcWidth(16);
        clip.setArcHeight(16);

        headImage.setClip(clip);

        Rectangle playerHead = new Rectangle(43, 43);
        playerHead.setArcWidth(16);
        playerHead.setArcHeight(16);
        playerHead.setLayoutX(68);
        playerHead.setLayoutY(485);

        root.getChildren().addAll(playerHead, headImage);

        // Nickname field
        Text nicknameText = new Text(nickname != null ? nickname : "Player");
        nicknameText.setX(123);
        nicknameText.setY(503);
        if (fonter.getGilroyMedium() != null) {
            nicknameText.setFont(fonter.getGilroyMedium());
            nicknameText.setStyle("-fx-font-size: 16;");
            nicknameText.setFill(javafx.scene.paint.Color.web("#6F6F6F"));
        } else {
            nicknameText.setFont(Font.font("Arial", 16));
        }

        root.getChildren().add(nicknameText);

        String playtime = String.valueOf(this.playtime);
        Text playtimeText = new Text("Наиграно: " + playtime + " ч.");
        playtimeText.setX(124);
        playtimeText.setY(523);
        if (fonter.getGilroyMedium() != null) {
            playtimeText.setFont(fonter.getGilroyMedium());
            playtimeText.setFill(javafx.scene.paint.Color.web("#6F6F6F"));
            playtimeText.setStyle("-fx-font-size: 16;");
        } else {
            playtimeText.setFont(Font.font("Arial", 16));
        }

        root.getChildren().add(playtimeText);

        // Show the main screen
        primaryStage.show();
    }
}