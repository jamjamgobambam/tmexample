package com.codedotorg;

import java.io.ByteArrayInputStream;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.opencv.imgcodecs.Imgcodecs;

public class CameraController {

    private String predictedClass;
    private float predictedScore;
    private static volatile boolean running = true;

    public CameraController() {
        nu.pattern.OpenCV.loadLocally();

        predictedClass = null;
        predictedScore = 0;
    }

    public void captureCamera(ImageView imageView, ModelManager model) {
        new Thread(() -> {
            // Create a VideoCapture with the system default camera (0)
            VideoCapture camera = new VideoCapture(0);

            if (!camera.isOpened()) {
                System.out.println("Error! Camera can't be opened.");
                return;
            }

            // Create a new frame to host the image from the camera
            Mat frame = new Mat();

            while (running) {
                // Capture the frame
                if (camera.read(frame)) {
                    // Convert and display the image from the camera
                    Image img = matToImage(frame);

                    // Update the image displayed in the image view
                    Platform.runLater(() -> imageView.setImage(img));

                    // Get the predicted class from the model
                    predictedClass = model.predictClass(frame);

                    // Get the predicted score from the model
                    predictedScore = model.predictScore(frame);
                }
                else {
                    System.out.println("Cannot capture the frame.");
                    break;
                }
            }

            // Release the camera after usage
            camera.release();
        }).start();
    }

    public String getPredictedClass() {
        return predictedClass;
    }

    public float getPredictedScore() {
        return predictedScore;
    }

    public void stopCapture() {
        running = false;
    }

    private Image matToImage(Mat frame) {
        // Create a temporary buffer
        MatOfByte buffer = new MatOfByte();

        // Encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);

        // Build and return an Image created from the image encoded in the buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}
