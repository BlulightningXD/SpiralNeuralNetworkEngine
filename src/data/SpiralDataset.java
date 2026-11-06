package data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generates and manages 2D spiral datasets for non-linear classification benchmarks.
 * Uses parametric trigonometric relations with class-specific angular offsets and Gaussian noise.
 * 
 * Formula:
 *   For class c in {0, ..., C-1} and sample i in {0, ..., N-1}:
 *   r = (i / N) * maxRadius
 *   theta = 4 * PI * rotations * (i / N) + (2 * PI * c / C) + noise * Gaussian()
 *   x = r * sin(theta)
 *   y = r * cos(theta)
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public class SpiralDataset {
    public enum DatasetType {
        SPIRAL("Multi-Arm Spiral"),
        CIRCLES("Concentric Circles"),
        XOR("XOR Problem"),
        CLUSTERS("Gaussian Clusters");

        private final String displayName;
        DatasetType(String displayName) {
            this.displayName = displayName;
        }
        @Override
        public String toString() {
            return displayName;
        }
    }

    private final List<DataPoint> dataPoints = new ArrayList<>();
    private int numClasses = 3;
    private int pointsPerClass = 150;
    private double noise = 0.12;
    private double rotations = 1.25;
    private DatasetType datasetType = DatasetType.SPIRAL;
    private final Random random = new Random();

    public SpiralDataset() {
        generate();
    }

    public SpiralDataset(int numClasses, int pointsPerClass, double noise, double rotations) {
        this.numClasses = numClasses;
        this.pointsPerClass = pointsPerClass;
        this.noise = noise;
        this.rotations = rotations;
        this.datasetType = DatasetType.SPIRAL;
        generate();
    }

    /**
     * Generates data points according to current settings.
     */
    public synchronized void generate() {
        dataPoints.clear();
        switch (datasetType) {
            case CIRCLES:
                generateCircles();
                break;
            case XOR:
                generateXOR();
                break;
            case CLUSTERS:
                generateClusters();
                break;
            case SPIRAL:
            default:
                generateSpiral();
                break;
        }
        // Shuffle dataset so mini-batches or sequential training doesn't bias by class
        Collections.shuffle(dataPoints, random);
    }

    /**
     * Generates a multi-arm spiral dataset.
     */
    private void generateSpiral() {
        for (int c = 0; c < numClasses; c++) {
            for (int i = 0; i < pointsPerClass; i++) {
                // Radius increases linearly from near center outward
                double t = (double) i / (double) pointsPerClass;
                double r = t * 0.95; // bound within unit sphere

                // Angle includes rotation rate, class phase offset, and controlled noise
                double theta = t * (rotations * 2.0 * Math.PI) + (2.0 * Math.PI * c / numClasses);
                theta += random.nextGaussian() * noise;

                double x = r * Math.sin(theta);
                double y = r * Math.cos(theta);

                dataPoints.add(new DataPoint(x, y, c));
            }
        }
    }

    /**
     * Generates concentric circles dataset.
     */
    private void generateCircles() {
        int classes = Math.max(2, numClasses);
        for (int c = 0; c < classes; c++) {
            double baseRadius = 0.25 + (c * 0.70 / (classes - 1));
            for (int i = 0; i < pointsPerClass; i++) {
                double angle = random.nextDouble() * 2.0 * Math.PI;
                double r = baseRadius + random.nextGaussian() * (noise * 0.4);
                double x = r * Math.cos(angle);
                double y = r * Math.sin(angle);
                dataPoints.add(new DataPoint(x, y, c));
            }
        }
    }

    /**
     * Generates XOR quadrant data points.
     */
    private void generateXOR() {
        int total = pointsPerClass * numClasses;
        for (int i = 0; i < total; i++) {
            double x = (random.nextDouble() * 2.0 - 1.0) * 0.85;
            double y = (random.nextDouble() * 2.0 - 1.0) * 0.85;
            x += random.nextGaussian() * (noise * 0.2);
            y += random.nextGaussian() * (noise * 0.2);
            int label = ((x > 0 && y > 0) || (x < 0 && y < 0)) ? 1 : 0;
            dataPoints.add(new DataPoint(x, y, label % numClasses));
        }
    }

    /**
     * Generates Gaussian clusters.
     */
    private void generateClusters() {
        for (int c = 0; c < numClasses; c++) {
            double angle = (2.0 * Math.PI * c) / numClasses;
            double cx = 0.6 * Math.cos(angle);
            double cy = 0.6 * Math.sin(angle);
            for (int i = 0; i < pointsPerClass; i++) {
                double x = cx + random.nextGaussian() * (0.15 + noise * 0.3);
                double y = cy + random.nextGaussian() * (0.15 + noise * 0.3);
                dataPoints.add(new DataPoint(x, y, c));
            }
        }
    }

    public List<DataPoint> getDataPoints() {
        return Collections.unmodifiableList(dataPoints);
    }

    public int getTotalPoints() {
        return dataPoints.size();
    }

    public int getNumClasses() {
        return numClasses;
    }

    public void setNumClasses(int numClasses) {
        this.numClasses = Math.max(2, numClasses);
    }

    public int getPointsPerClass() {
        return pointsPerClass;
    }

    public void setPointsPerClass(int pointsPerClass) {
        this.pointsPerClass = Math.max(10, pointsPerClass);
    }

    public double getNoise() {
        return noise;
    }

    public void setNoise(double noise) {
        this.noise = Math.max(0.0, noise);
    }

    public double getRotations() {
        return rotations;
    }

    public void setRotations(double rotations) {
        this.rotations = Math.max(0.1, rotations);
    }

    public DatasetType getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType != null ? datasetType : DatasetType.SPIRAL;
    }

    /**
     * Returns input feature matrix of size [N, 2].
     */
    public double[][] getInputs() {
        int n = dataPoints.size();
        double[][] inputs = new double[n][2];
        for (int i = 0; i < n; i++) {
            DataPoint pt = dataPoints.get(i);
            inputs[i][0] = pt.getX();
            inputs[i][1] = pt.getY();
        }
        return inputs;
    }

    /**
     * Returns true class integer labels of size [N].
     */
    public int[] getLabels() {
        int n = dataPoints.size();
        int[] labels = new int[n];
        for (int i = 0; i < n; i++) {
            labels[i] = dataPoints.get(i).getLabel();
        }
        return labels;
    }

    /**
     * Returns one-hot encoded targets of size [N, numClasses].
     */
    public double[][] getOneHotTargets() {
        int n = dataPoints.size();
        double[][] targets = new double[n][numClasses];
        for (int i = 0; i < n; i++) {
            int label = dataPoints.get(i).getLabel();
            if (label >= 0 && label < numClasses) {
                targets[i][label] = 1.0;
            }
        }
        return targets;
    }
}
