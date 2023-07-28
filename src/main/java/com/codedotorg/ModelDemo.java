package com.codedotorg;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ModelDemo {

    private Stage window;
    private ImageView cameraView;
    private Label predictionLabel;
    private Button exitButton;
    private ModelManager model;
    private CameraController cameraController;

    public ModelDemo() {
        cameraController = new CameraController();
        model = new ModelManager();
        cameraView = new ImageView();
        predictionLabel = getPredictionLabel();
        exitButton = new Button("Exit");
    }
    
    public void startApp(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("Teachable Machine Example");

        // Shutdown hook to stop the camera capture when the app is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cameraController.stopCapture();
        }));

        showMainScreen();
    }

    public void showMainScreen() {
        setButtonActions();

        // Create a new layout
        VBox rootLayout = new VBox();

        // Set spacing of 10 pixels
        rootLayout.setSpacing(10);

        // Add the camera view, prediction label, and exit button to the layout
        rootLayout.getChildren().addAll(cameraView, predictionLabel, exitButton);

        // Create a new scene and set the layout as its root
        Scene mainScene = new Scene(rootLayout, 700, 700);

        // Set the scene to the window and show it
        window.setScene(mainScene);
        window.show();

        // Start capturing the webcam
        cameraController.captureCamera(cameraView, model);

        // Update the prediction label
        updatePredictionLabel();
    }

    private void setButtonActions() {
        exitButton.setOnAction(e -> {
            cameraController.stopCapture();
            System.exit(0);
        });
    }

    private Label getPredictionLabel() {
        Label label = new Label(cameraController.getPredictedClass() + " - " + cameraController.getPredictedScore());

        label.setLayoutX(50);
        label.setLayoutY(50);

        return label;
    }

    private void updatePredictionLabel() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            predictionLabel.setText(cameraController.getPredictedClass() + " - " + cameraController.getPredictedScore());
        }));
        
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

}
