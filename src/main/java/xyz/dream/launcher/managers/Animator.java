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

package xyz.dream.launcher.managers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import xyz.dream.launcher.MainScreen;

// TODO: rewrite to the Kotlin
public class Animator {
    /**
     * Creates an entrance animation for the starting screen elements.
     * @param logoView The ImageView for the logo.
     * @param nickNameField The Rectangle for the nickname field.
     * @param nickNameInput The TextField for nickname input.
     * @param rightArrowContainer The StackPane containing the right arrow (for navigation).
     */
    public static void playEntranceAnimation(ImageView logoView, Rectangle nickNameField, TextField nickNameInput, StackPane rightArrowContainer) {
        double offsetY = 300;
        double durationMillis = 850;
        int targetFps = 60;
        int totalFrames = (int) (durationMillis / 1000 * targetFps);

        double logoStartY = -offsetY;
        double logoEndY = 0;
        double fieldEndY = 0;

        if (logoView != null) logoView.setTranslateY(logoStartY);
        nickNameField.setTranslateY(offsetY);
        nickNameInput.setTranslateY(offsetY);
        if (rightArrowContainer != null) rightArrowContainer.setTranslateY(offsetY);

        Timeline timeline = new Timeline();
        for (int i = 0; i <= totalFrames; i++) {
            double t = (double) i / totalFrames;
            double ease = 0.5 - 0.5 * Math.cos(Math.PI * t);

            double logoY = logoStartY + (logoEndY - logoStartY) * ease;
            double fieldY = offsetY + (fieldEndY - offsetY) * ease;

            KeyFrame kf = new KeyFrame(Duration.millis(i * (1000.0 / targetFps)), e -> {
                if (logoView != null) logoView.setTranslateY(logoY);
                nickNameField.setTranslateY(fieldY);
                nickNameInput.setTranslateY(fieldY);
                if (rightArrowContainer != null) rightArrowContainer.setTranslateY(fieldY);
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
    }

    /**
     * Creates an exit animation and transitions to the main screen.
     * @param logoView The ImageView is the logo.
     * @param nickNameField The Rectangle for the nickname field.
     * @param nickNameInput The TextField for nickname.
     * @param rightArrowContainer The StackPane is the right arrow.
     * @param mainScreen The MainScreen instance to transition to.
     * @param primaryStage The primary stage.
     */
    public static void playExitAnimation(ImageView logoView, Rectangle nickNameField, TextField nickNameInput, StackPane rightArrowContainer, MainScreen mainScreen, javafx.stage.Stage primaryStage) {
        double offsetY = 600;
        double durationMillis = 850;

        Timeline exitTimeline = new Timeline();
        if (logoView != null) {
            KeyValue kvLogoUp = new KeyValue(logoView.translateYProperty(), -offsetY);
            KeyFrame kfLogoUp = new KeyFrame(Duration.millis(durationMillis), kvLogoUp);
            exitTimeline.getKeyFrames().add(kfLogoUp);
        }

        KeyValue kvFieldDown = new KeyValue(nickNameField.translateYProperty(), offsetY);
        KeyFrame kfFieldDown = new KeyFrame(Duration.millis(durationMillis), kvFieldDown);
        exitTimeline.getKeyFrames().add(kfFieldDown);

        KeyValue kvInputDown = new KeyValue(nickNameInput.translateYProperty(), offsetY);
        KeyFrame kfInputDown = new KeyFrame(Duration.millis(durationMillis), kvInputDown);
        exitTimeline.getKeyFrames().add(kfInputDown);

        if (rightArrowContainer != null) {
            KeyValue kvArrowDown = new KeyValue(rightArrowContainer.translateYProperty(), offsetY);
            KeyFrame kfArrowDown = new KeyFrame(Duration.millis(durationMillis), kvArrowDown);
            exitTimeline.getKeyFrames().add(kfArrowDown);
        }

        exitTimeline.setOnFinished(e -> {
            try {
                mainScreen.start(primaryStage);
            } catch (Exception ex) {
                System.err.println("Error transitioning to MainScreen: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        exitTimeline.play();
    }
}