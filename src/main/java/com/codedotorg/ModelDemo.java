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

    /** The main window of the app */
    private Stage window;

    /** Displays the camera feed in the app */
    private ImageView cameraView;

    /** Displays the predicted class and confidence score */
    private Label predictionLabel;

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
    public ModelDemo() {
        cameraController = new CameraController();
        model = new ModelManager();
        cameraView = new ImageView();
        predictionLabel = getPredictionLabel();
        exitButton = new Button("Exit");
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
        // Timeline is part of the JavaFX library to provide a way to create animations and other time-based events.
        // KeyFrame is used to define a single frame of an animation.
        // Duration.seconds(1) specifies that this should be executed every 1 second.
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // Sets the text of the predictionLabel to the predicted class label and score from the CameraController every second
            predictionLabel.setText(cameraController.getPredictedClass() + " - " + cameraController.getPredictedScore());
        }));
        
        // Specify that the animation should repeat indefinitely
        timeline.setCycleCount(Timeline.INDEFINITE);

        // Start the animation
        timeline.play();
    }

}
