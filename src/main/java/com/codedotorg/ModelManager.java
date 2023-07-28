package com.codedotorg;

import org.opencv.core.Mat;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Session;
import org.tensorflow.Shape;
import org.tensorflow.Tensor;
import org.tensorflow.SavedModelBundle;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Iterator;

public class ModelManager {
    
    private static final String MODEL_PATH = "src\\main\\java\\com\\codedotorg\\model\\";
    private static final String LABELS_PATH = MODEL_PATH + "labels.txt";

    private SavedModelBundle bundle;
    private Graph graph;
    private Session session;
    private List<String> labels;

    public ModelManager() {
        bundle = null;
        loadModel();
        loadLabels();
    }

    public void loadModel() {
        try {
            bundle = SavedModelBundle.load(MODEL_PATH, "serve");
            session = bundle.session();
            System.out.println("Model loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load the model");
            e.printStackTrace();
        }
    }

    public void loadLabels() {
        try {
            labels = Files.readAllLines(Paths.get(LABELS_PATH));
            System.out.println("Labels loaded successfully");
            System.out.println(labels);
        } catch (IOException e) {
            System.err.println("Failed to load the labels");
            e.printStackTrace();
        }
    }

    public void inspectModel(Graph graph) {
        try {
            graph = bundle.graph();
        } catch (Exception e) {
            System.err.println("Failed to load the model");
            e.printStackTrace();
        }

        Iterator<Operation> operationIterator = graph.operations();
        
        while (operationIterator.hasNext()) {
            Operation operation = operationIterator.next();
            System.out.println("Operation name: " + operation.name());
    
            int numOutputs = operation.numOutputs();
            for (int i = 0; i < numOutputs; i++) {
                try {
                    Shape shape = operation.output(i).shape();
                    System.out.print("Output " + i + " shape: [");
                    for (int j = 0; j < shape.numDimensions(); j++) {
                        System.out.print(shape.size(j));
                        if (j < shape.numDimensions() - 1) {
                            System.out.print(", ");
                        }
                    }
                    System.out.println("]");
                } catch (Exception e) {
                    System.out.println("Output " + i + " shape: unknown");
                }
            }
            
            System.out.println();
        }
    }

    public String predictClass(Mat frame) {
        byte[] bytesFromFrame = convertFrameToBytes(frame);
        float[] normalizedPixels = normalizeImage(bytesFromFrame);
        Tensor<Float> inputTensor = createInputTensor(frame, bytesFromFrame, normalizedPixels);
        List<Tensor<?>> outputTensors = createOutputTensors(inputTensor);
        return predictClassAndRelease(inputTensor, outputTensors);
    }

    public float predictScore(Mat frame) {
        byte[] bytesFromFrame = convertFrameToBytes(frame);
        float[] normalizedPixels = normalizeImage(bytesFromFrame);
        Tensor<Float> inputTensor = createInputTensor(frame, bytesFromFrame, normalizedPixels);
        List<Tensor<?>> outputTensors = createOutputTensors(inputTensor);
        return predictScoreAndRelease(inputTensor, outputTensors);
    }

    private byte[] convertFrameToBytes(Mat frame) {
        byte[] bytes = new byte[(int) (frame.total() * frame.channels())];
        frame.get(0, 0, bytes);
        return bytes;
    }

    private float[] normalizeImage(byte[] bytes) {
        float[] floatArray = new float[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            floatArray[i] = (bytes[i] & 0xff) / 255.0f;  // normalize pixel values to [0, 1]
        }

        return floatArray;
    }

    private float[][] convertOutputToSoftmax(List<Tensor<?>> output) {
        // Assuming output is softmax and has shape [1, num_classes]
        float[][] softmax = new float[1][labels.size()];
        output.get(0).copyTo(softmax);
        return softmax;
    }

    private Tensor<Float> createInputTensor(Mat frame, byte[] bytes, float[] floatArray) {
        int width = frame.width();
        int height = frame.height();
        int channels = frame.channels();
        FloatBuffer floatBuffer = FloatBuffer.wrap(floatArray);

        try {
            Tensor<Float> inputTensor = Tensor.create(new long[]{1, height, width, channels}, floatBuffer);
            return inputTensor;
        } catch (Exception e) {
            System.err.println("Failed to create input tensor.");
            e.printStackTrace();
            return null;
        }
    }

    private List<Tensor<?>> createOutputTensors(Tensor<Float> inputTensor) {
        try {
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

    private String predictClassAndRelease(Tensor<Float> inputTensor, List<Tensor<?>> output) {
        float[][] softmax = convertOutputToSoftmax(output);
        String result = findPredictedClass(softmax);
        releaseTensor(inputTensor, output);
        return result;
    }

    private float predictScoreAndRelease(Tensor<Float> inputTensor, List<Tensor<?>> output) {
        float[][] softmax = convertOutputToSoftmax(output);
        float result = findPredictedScore(softmax);
        releaseTensor(inputTensor, output);
        return result;
    }

    private String findPredictedClass(float[][] softmax) {
        float maxScore = -Float.MAX_VALUE;
        int maxScoreIdx = -1;

        // Find the class with the highest softmax score
        for (int i = 0; i < softmax[0].length; i++) {
            if (softmax[0][i] > maxScore) {
                maxScore = softmax[0][i];
                maxScoreIdx = i;
            }
        }

        return labels.get(maxScoreIdx);
    }

    private float findPredictedScore(float[][] softmax) {
        float maxScore = -Float.MAX_VALUE;

        // Find the class with the highest softmax score
        for (int i = 0; i < softmax[0].length; i++) {
            if (softmax[0][i] > maxScore) {
                maxScore = softmax[0][i];
            }
        }

        return maxScore;
    }

    private void releaseTensor(Tensor<Float> inputTensor, List<Tensor<?>> output) {
        inputTensor.close();
        output.forEach(Tensor::close);
    }

}
