package com.codedotorg;

import org.opencv.core.Mat;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.SavedModelBundle;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ModelManager {
    
    /** The path to the directory containing the model */
    private static final String MODEL_PATH = "src\\main\\java\\com\\codedotorg\\model\\";

    /** The path to the labels.txt file (should be in the root of the model directory) */
    private static final String LABELS_PATH = MODEL_PATH + "labels.txt";

    /** Represents the TensorFlow model and its associated variables */
    private SavedModelBundle bundle;

    /** Represents a TensorFlow session, which is used to run the model and make predictions */
    private Session session;

    /** The list of class labels for the model */
    private List<String> labels;

    /**
     * Constructs a new ModelManager object.
     * Initializes the bundle to null, loads the model, and loads the labels.
     */
    public ModelManager() {
        bundle = null;
        loadModel();
        loadLabels();
    }

    /**
     * Loads a saved model from the specified path and creates a session.
     * Prints a message to the console if the model is loaded successfully.
     * Prints an error message and stack trace to the console if the model fails to load.
     */
    public void loadModel() {
        try {
            // Load the TensorFlow model from the MODEL_PATH directory and creates a new SavedModelBundle object
            // "serve" specifies the model signature name
            bundle = SavedModelBundle.load(MODEL_PATH, "serve");

            // Sets the session to a new Session object to run the TensorFlow model and make predictions
            session = bundle.session();
            System.out.println("Model loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load the model");
            e.printStackTrace();
        }
    }

    /**
     * Reads all the lines from the file specified by LABELS_PATH and stores them in the labels list.
     * Prints a success message and the labels list if the operation is successful.
     * Prints an error message and the stack trace if the operation fails.
     */
    public void loadLabels() {
        try {
            // Read all the lines from the labels.txt file and returns them as a list of strings
            // Paths.get() creates a Path object representing the path to the file containing the class labels
            labels = Files.readAllLines(Paths.get(LABELS_PATH));
            
            System.out.println("Labels loaded successfully");
            System.out.println(labels);
        } catch (IOException e) {
            System.err.println("Failed to load the labels");
            e.printStackTrace();
        }
    }

    /**
     * Predicts the class of a given frame using the loaded TensorFlow model.
     *
     * @param frame The input frame to predict the class for.
     * @return The predicted class as a string.
     */
    public String predictClass(Mat frame) {
        // Convert the image frame to a byte array
        byte[] bytesFromFrame = convertFrameToBytes(frame);

        // Normalize the pixel values of the image and return a float array
        float[] normalizedPixels = normalizeImage(bytesFromFrame);

        // Create a list of Tensor objects representing the input to the TensorFlow model
        Tensor<Float> inputTensor = createInputTensor(frame, bytesFromFrame, normalizedPixels);

        // Create a list of Tensor objects representing the output of the TensorFlow model
        List<Tensor<?>> outputTensors = createOutputTensors(inputTensor);

        // Run the TensorFlow model and make a prediction
        return predictClassAndRelease(inputTensor, outputTensors);
    }

    public float predictScore(Mat frame) {
        // Convert the image frame to a byte array
        byte[] bytesFromFrame = convertFrameToBytes(frame);

        // Normalize the pixel values of the image and return a float array
        float[] normalizedPixels = normalizeImage(bytesFromFrame);

        // Create a list of Tensor objects representing the input to the TensorFlow model
        Tensor<Float> inputTensor = createInputTensor(frame, bytesFromFrame, normalizedPixels);

        // Create a list of Tensor objects representing the output of the TensorFlow model
        List<Tensor<?>> outputTensors = createOutputTensors(inputTensor);

        // Run the TensorFlow model and make a prediction
        return predictScoreAndRelease(inputTensor, outputTensors);
    }

    /**
     * Converts a given OpenCV Mat frame to a byte array.
     * 
     * @param frame the OpenCV Mat frame to be converted
     * @return the byte array representation of the given frame
     */
    private byte[] convertFrameToBytes(Mat frame) {
        // Create a byte array containing the total number of elements in the frame times the number of channels
        byte[] bytes = new byte[(int) (frame.total() * frame.channels())];

        // Copy the pixel values from the frame to a byte array
        frame.get(0, 0, bytes);

        // Return the byte array
        return bytes;
    }

    /**
     * Normalizes an image represented as a byte array to a float array with pixel values in the range [0, 1].
     *
     * @param bytes the byte array representing the image
     * @return a float array with pixel values normalized to [0, 1]
     */
    private float[] normalizeImage(byte[] bytes) {
        // Create a float array that is the same length as the input byte array
        float[] floatArray = new float[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            // Normalize pixel values to the range [0, 1] by converting the byte value to an integer
            // using a bitwise AND operation with 0xff, which sets the most significant bits of the
            // integer to 0. The resulting integer is then divided by 255.0f to normalize its value
            // to the range [0, 1]. The normalized value is then stored in the float array.
            floatArray[i] = (bytes[i] & 0xff) / 255.0f;  // normalize pixel values to [0, 1]
        }

        // Return the float array
        return floatArray;
    }

    /**
     * Converts the output tensor to a softmax array.
     * Assumes that the output tensor is softmax and has shape [1, num_classes].
     * @param output The output tensor to convert.
     * @return A 2D float array representing the softmax values.
     */
    private float[][] convertOutputToSoftmax(List<Tensor<?>> output) {
        // Create a 2D float array with 1 row and labels.size() columns
        float[][] softmax = new float[1][labels.size()];

        // Copy the values from the first element of the output list to the array
        // The output list contains one Tensor object for each output of the TensorFlow model. In this case,
        // the first Tensor object is used to represent the predicted class probabilities for the input.
        output.get(0).copyTo(softmax);

        // Return the float array
        return softmax;
    }

    /**
     * Creates an input tensor from a given OpenCV Mat object, byte array, and float array.
     * 
     * @param frame The OpenCV Mat object to create the input tensor from.
     * @param bytes The byte array to use for the input tensor.
     * @param floatArray The float array to wrap in a FloatBuffer and use for the input tensor.
     * @return The created input tensor, or null if an exception occurred.
     */
    private Tensor<Float> createInputTensor(Mat frame, byte[] bytes, float[] floatArray) {
        // The width of the frame
        int width = frame.width();

        // The height of the frame
        int height = frame.height();

        // The number of channels in the frame
        int channels = frame.channels();

        // Wrap the float array containing the pixel values of the image frame
        // FloatBuffer class provides a way to create a buffer of float values that can be used
        // as the data buffer for a Tensor object
        FloatBuffer floatBuffer = FloatBuffer.wrap(floatArray);

        try {
            // Create a new Tensor object representing the input to the TensorFlow object
            // The shape of the Tensor object is specified as [1, height, width, channels], which represents a
            // batch of one image with the specified height, width, and number of channels
            Tensor<Float> inputTensor = Tensor.create(new long[]{1, height, width, channels}, floatBuffer);

            // Return the input Tensor
            return inputTensor;
        } catch (Exception e) {
            System.err.println("Failed to create input tensor.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates output tensors for a given input tensor using the TensorFlow session runner.
     * @param inputTensor the input tensor to feed to the session runner
     * @return a list of output tensors
     */
    private List<Tensor<?>> createOutputTensors(Tensor<Float> inputTensor) {
        try {
            // Create the Session.runner to run the TensorFlow model and fetch the output
            // feed() sets the input to the TensorFlow object
            // fetch() specifies the output of the TensorFlow model to fetch
            // run() runs the TensorFlow model, fetches the output, and returns a list of Tensor objects
            // representing the output of the TensorFlow model
            List<Tensor<?>> output = session.runner()
                .feed("serving_default_sequential_1_input", inputTensor)
                .fetch("StatefulPartitionedCall:0")
                .run();
            return output;
        } catch (Exception e) {
            System.err.println("Failed to create output tensors.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Predicts the class and releases the input tensor and output list.
     * 
     * @param inputTensor the input tensor to predict the class from
     * @param output the list of output tensors
     * @return the predicted class as a string
     */
    private String predictClassAndRelease(Tensor<Float> inputTensor, List<Tensor<?>> output) {
        // Convert the output of the TensorFlow model to a float array representing
        // the predicted class probabilities
        float[][] softmax = convertOutputToSoftmax(output);

        // Determine the most likely class for the input
        String result = findPredictedClass(softmax);

        // Release the input and output Tensor objects to free up memory used by the Tensor objects
        releaseTensor(inputTensor, output);

        // Return the predicted class
        return result;
    }

    /**
     * Predicts the score and releases the input tensor and output list.
     * 
     * @param inputTensor the input tensor to be used for prediction
     * @param output the list of output tensors
     * @return the predicted score
     */
    private float predictScoreAndRelease(Tensor<Float> inputTensor, List<Tensor<?>> output) {
        // Convert the output of the TensorFlow model to a float array representing
        // the predicted class probabilities
        float[][] softmax = convertOutputToSoftmax(output);

        // Determine the most likely class for the input
        float result = findPredictedScore(softmax);

        // Release the input and output Tensor objects to free up memory used by the Tensor objects
        releaseTensor(inputTensor, output);

        // Return the predicted score
        return result;
    }

    /**
     * Finds the predicted class based on the highest softmax score.
     * 
     * @param softmax a 2D float array containing softmax scores
     * @return the predicted class label
     */
    private String findPredictedClass(float[][] softmax) {
        // Set maxScore to the smallest possible negative float value
        float maxScore = -Float.MAX_VALUE;

        // Set maxScoreIdx to -1
        int maxScoreIdx = -1;

        // Find the class with the highest softmax score
        for (int i = 0; i < softmax[0].length; i++) {
            // Check if value is greater than the current value of maxScore
            if (softmax[0][i] > maxScore) {
                // Set maxScore to the value of the current element
                maxScore = softmax[0][i];

                // Set maxScoreIdx to the index of the current element
                maxScoreIdx = i;
            }
        }

        // Return the class label corresponding to the index of the highest predicted probability
        return labels.get(maxScoreIdx);
    }

    /**
     * Finds the predicted score from a 2D softmax array.
     * @param softmax the 2D softmax array
     * @return the predicted score
     */
    private float findPredictedScore(float[][] softmax) {
        // Set maxScore to the smallest possible negative float value
        float maxScore = -Float.MAX_VALUE;

        // Find the class with the highest softmax score
        for (int i = 0; i < softmax[0].length; i++) {
            // Check if value is greater than the current value of maxScore
            if (softmax[0][i] > maxScore) {
                // Set maxScore to the value of the current element
                maxScore = softmax[0][i];
            }
        }

        // Return the value of maxScore
        return maxScore;
    }

    /**
     * Releases the input tensor and output tensors by closing them.
     *
     * @param inputTensor The input tensor to be released.
     * @param output The list of output tensors to be released.
     */
    private void releaseTensor(Tensor<Float> inputTensor, List<Tensor<?>> output) {
        // Release the memory used by the input Tensor object
        inputTensor.close();

        // Call the close() method on each Tensor object in the output list
        output.forEach(Tensor::close);
    }

}
