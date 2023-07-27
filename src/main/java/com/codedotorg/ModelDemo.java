package com.codedotorg;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ModelDemo {

    private Stage window;
    private Button exitButton;

    public ModelDemo() {
        exitButton = new Button("Exit");
    }
    
    public void startApp(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("Teachable Machine Example");
        showMainScreen();
    }

    public void showMainScreen() {
        setButtonActions();

        // Create a new layout and add the exit button to it
        StackPane rootLayout = new StackPane();
        rootLayout.getChildren().add(exitButton);

        // Create a new scene and set the layout as its root
        Scene mainScene = new Scene(rootLayout, 500, 500);

        // Set the scene to the window and show it
        window.setTitle("Controls");
        window.setScene(mainScene);
        window.show();

        // Start capturing the webcam
        // CameraController.captureCamera();
    }

    private void setButtonActions() {
        exitButton.setOnAction(e -> System.exit(0));
    }

    private ImageView getCameraFeed() {
        // Create a new ImageView object to display the camera feed
        ImageView cameraView = new ImageView();

        // Set the dimensions of the ImageView to match the dimensions of the scene
        cameraView.fitWidthProperty().bind(window.widthProperty());
        cameraView.fitHeightProperty().bind(window.heightProperty());

        // Set the image of the ImageView to the camera feed
        // Replace this with your own code to capture the camera feed
        Image cameraImage = new Image("https://dummyimage.com/500x500/000/fff");
        cameraView.setImage(cameraImage);

        return cameraView;
    }

}
