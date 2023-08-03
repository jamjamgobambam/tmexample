package com.codedotorg;

import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BinarySearch {

    /** The main window of the app */
    private Stage window;

    /** Displays the camera feed in the app */
    private ImageView cameraView;

    /** Displays the predicted class and confidence score */
    private Label predictionLabel;

    private Label promptLabel;
    private int left = 0;
    private int right = 100;
    private int guess = (left + right) / 2;

    /** Button to exit the app */
    private Button exitButton;

    /** Manages the TensorFlow model used for image classification */
    private ModelManager model;

    /** Controls the camera capture and provides frames to the TensorFlow model for classification */
    private CameraController cameraController;

    /**
     * Constructor for the ModelDemo class.
     * Initializes the camera controller, model manager, image view, prediction label, and exit button.
     */
    public BinarySearch() {
        cameraController = new CameraController();
        model = new ModelManager();
        cameraView = new ImageView();
        predictionLabel = getPredictionLabel();
        exitButton = new Button("Exit");
        promptLabel = new Label("Think of a number between 1 and 100:");
    }
    
    /**
     * Starts the Teachable Machine Example application.
     * Sets the title of the primary stage to "Teachable Machine Example".
     * Adds a shutdown hook to stop the camera capture when the app is closed.
     * Calls the showMainScreen() method to display the main screen.
     *
     * @param primaryStage the primary stage of the application
     */
    public void startApp(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("Teachable Machine Example");

        // Shutdown hook to stop the camera capture when the app is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cameraController.stopCapture();
        }));

        showMainScreen();
    }

    /**
     * Displays the main screen of the application, which includes the camera view,
     * prediction label, and exit button. Starts capturing the webcam and updates
     * the prediction label.
     */
    public void showMainScreen() {
        setButtonActions();

        // Create a new layout
        VBox rootLayout = new VBox();

        // Set spacing of 10 pixels
        rootLayout.setSpacing(10);

        // Add the camera view, prediction label, and exit button to the layout
        rootLayout.getChildren().addAll(promptLabel, cameraView, predictionLabel, exitButton);

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

    public int binarySearch(String predictedClass) {
        if (predictedClass.equals("0 thumbsup")) {
            left = guess;
            guess = (left + right) / 2;
            return guess;
        }
        else if (predictedClass.equals("1 thumbsdown")) {
            right = guess;
            guess = (left + right) / 2;
            return guess;
        }
        else if (predictedClass.equals("stop")) {
            return guess;
        }
        else {
            return -1;
        }
    }

    /**
     * Sets the action for the exit button. When clicked, it stops the camera capture and exits the program.
     */
    private void setButtonActions() {
        exitButton.setOnAction(e -> {
            cameraController.stopCapture();
            System.exit(0);
        });
    }

    /**
     * Returns a Label object that displays the predicted class and score obtained from the camera controller.
     * The Label object is positioned at (50, 50) on the screen.
     *
     * @return a Label object that displays the predicted class and score
     */
    private Label getPredictionLabel() {
        Label label = new Label(cameraController.getPredictedClass() + " - " + cameraController.getPredictedScore());

        label.setLayoutX(50);
        label.setLayoutY(50);

        return label;
    }

    /**
     * Updates the prediction label with the predicted class and score from the camera controller.
     * Uses a timeline to update the label every second.
     */
    private void updatePredictionLabel() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            // Get the predicted class and score from the CameraController
            String predictedClass = cameraController.getPredictedClass();
            double predictedScore = cameraController.getPredictedScore();

            // Update the prediction label with the guess, predicted class, and score
            if (predictedClass != null) {
                // Get the guess from the binary search
                int guess = binarySearch(predictedClass);

                Platform.runLater(() -> predictionLabel.setText("Guess: " + guess + " - " + predictedClass + " - " + predictedScore));
            }
        }));
        
        // Specify that the animation should repeat indefinitely
        timeline.setCycleCount(Timeline.INDEFINITE);

        // Start the animation
        timeline.play();
    }

}
