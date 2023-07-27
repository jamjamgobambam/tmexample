package com.codedotorg;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

public class CameraController {
    
    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public static void captureCamera() {
        // Create a VideoCapture with the system default camera (0)
        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Error! Camera can't be opened.");
            return;
        }

        // Create a new frame to host the image from the camera
        Mat frame = new Mat();

        while (true) {
            // Capture the frame
            if (camera.read(frame)) {
                // Display the image from the camera
                HighGui.imshow("Camera", frame);
                HighGui.waitKey(1);
            }
            else {
                System.out.println("Cannot capture the frame.");
                break;
            }
        }

        // Release the camera after usage
        camera.release();

        // Destroy the window
        HighGui.destroyAllWindows();
    }
}
