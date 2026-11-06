package data;

import java.util.Arrays;

/**
 * Represents a single 2D data point with coordinates and its classification label.
 * Designed for the Spiral Neural Network Interface Engine.
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public class DataPoint {
    private final double x;
    private final double y;
    private final int label;
    private int predictedLabel;
    private double[] probabilities;

    /**
     * Constructs a DataPoint with 2D coordinates and true class label.
     * 
     * @param x X coordinate (feature 1)
     * @param y Y coordinate (feature 2)
     * @param label True class label (integer 0, 1, ..., numClasses - 1)
     */
    public DataPoint(double x, double y, int label) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.predictedLabel = -1;
        this.probabilities = new double[0];
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getLabel() {
        return label;
    }

    public int getPredictedLabel() {
        return predictedLabel;
    }

    public void setPredictedLabel(int predictedLabel) {
        this.predictedLabel = predictedLabel;
    }

    public double[] getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(double[] probabilities) {
        this.probabilities = probabilities != null ? Arrays.copyOf(probabilities, probabilities.length) : new double[0];
    }

    /**
     * Returns true if the predicted label matches the true label.
     */
    public boolean isCorrect() {
        return predictedLabel == label;
    }

    /**
     * Returns an array representation of the 2D input features [x, y].
     */
    public double[] toFeatures() {
        return new double[]{x, y};
    }

    @Override
    public String toString() {
        return String.format("DataPoint(x=%.4f, y=%.4f, label=%d, pred=%d)", x, y, label, predictedLabel);
    }
}
