package training;

import data.DataPoint;
import data.SpiralDataset;
import neural.NeuralNetwork;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages asynchronous multi-threaded neural network training execution,
 * mini-batch mini-epochs, metric tracking, and GUI event notification.
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public class Trainer implements Runnable {

    public enum State {
        IDLE,
        RUNNING,
        PAUSED,
        STOPPED,
        COMPLETED
    }

    public interface TrainingListener {
        void onEpochCompleted(int epoch, int totalEpochs, double loss, double accuracy);
        void onTrainingStateChanged(State newState);
    }

    private final NeuralNetwork network;
    private final SpiralDataset dataset;
    private final LossFunction lossFunction;

    // Hyperparameters
    private double learningRate = 0.08;
    private double momentum = 0.90;
    private int totalEpochs = 2500;
    private int batchSize = 32;
    private int uiUpdateInterval = 5; // notify UI every N epochs for smooth 60fps rendering
    private int delayMs = 0; // optional delay for slow-motion educational view

    // Runtime state
    private volatile State state = State.IDLE;
    private volatile boolean shouldStop = false;
    private volatile boolean isPaused = false;
    private Thread workerThread;
    private int currentEpoch = 0;

    private final List<TrainingListener> listeners = new ArrayList<>();
    private final Random random = new Random();

    public Trainer(NeuralNetwork network, SpiralDataset dataset) {
        this(network, dataset, LossFunction.CROSS_ENTROPY);
    }

    public Trainer(NeuralNetwork network, SpiralDataset dataset, LossFunction lossFunction) {
        this.network = network;
        this.dataset = dataset;
        this.lossFunction = lossFunction != null ? lossFunction : LossFunction.CROSS_ENTROPY;
    }

    public void addListener(TrainingListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(TrainingListener listener) {
        listeners.remove(listener);
    }

    private void notifyStateChanged(State newState) {
        this.state = newState;
        for (TrainingListener l : listeners) {
            SwingUtilities.invokeLater(() -> l.onTrainingStateChanged(newState));
        }
    }

    private void notifyEpoch(int epoch, int total, double loss, double acc) {
        for (TrainingListener l : listeners) {
            SwingUtilities.invokeLater(() -> l.onEpochCompleted(epoch, total, loss, acc));
        }
    }

    /**
     * Starts training asynchronously in a dedicated worker thread.
     */
    public synchronized void start() {
        if (state == State.RUNNING) return;

        if (state == State.PAUSED) {
            resume();
            return;
        }

        shouldStop = false;
        isPaused = false;
        currentEpoch = 0;
        workerThread = new Thread(this, "NN-Trainer-Worker");
        workerThread.setDaemon(true);
        notifyStateChanged(State.RUNNING);
        workerThread.start();
    }

    public synchronized void pause() {
        if (state == State.RUNNING) {
            isPaused = true;
            notifyStateChanged(State.PAUSED);
        }
    }

    public synchronized void resume() {
        if (state == State.PAUSED) {
            isPaused = false;
            notifyStateChanged(State.RUNNING);
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public synchronized void stop() {
        shouldStop = true;
        isPaused = false;
        synchronized (this) {
            notifyAll();
        }
        if (workerThread != null && workerThread.isAlive()) {
            workerThread.interrupt();
        }
        notifyStateChanged(State.STOPPED);
    }

    @Override
    public void run() {
        List<DataPoint> dataList = new ArrayList<>(dataset.getDataPoints());
        int totalSamples = dataList.size();
        if (totalSamples == 0) {
            notifyStateChanged(State.COMPLETED);
            return;
        }

        int effectiveBatchSize = (batchSize <= 0 || batchSize > totalSamples) ? totalSamples : batchSize;

        for (int epoch = 1; epoch <= totalEpochs && !shouldStop; epoch++) {
            currentEpoch = epoch;

            // Handle pause state
            synchronized (this) {
                while (isPaused && !shouldStop) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        shouldStop = true;
                        break;
                    }
                }
            }
            if (shouldStop) break;

            // Shuffle data per epoch for mini-batch stochasticity
            Collections.shuffle(dataList, random);

            // Mini-batch training pass
            for (int i = 0; i < totalSamples; i += effectiveBatchSize) {
                int end = Math.min(i + effectiveBatchSize, totalSamples);
                int currentBatchCount = end - i;

                for (int b = i; b < end; b++) {
                    DataPoint pt = dataList.get(b);
                    double[] features = pt.toFeatures();
                    double[] target = new double[network.getOutputSize()];
                    if (pt.getLabel() >= 0 && pt.getLabel() < target.length) {
                        target[pt.getLabel()] = 1.0;
                    }

                    // Forward pass
                    double[] predicted = network.forward(features);

                    // Compute error / loss gradient
                    double[] grad = lossFunction.calculateGradient(predicted, target);

                    // Backward pass accumulates gradients
                    network.backward(grad);
                }

                // Update weights after accumulating gradients over the batch
                network.updateParameters(learningRate, momentum, currentBatchCount);
            }

            // Periodic evaluation and UI notification
            boolean isLast = (epoch == totalEpochs);
            if (epoch % uiUpdateInterval == 0 || isLast || epoch == 1) {
                EvaluationResult eval = evaluate(dataset.getDataPoints());
                notifyEpoch(epoch, totalEpochs, eval.loss, eval.accuracy);

                if (delayMs > 0) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        shouldStop = true;
                        break;
                    }
                }
            }
        }

        if (!shouldStop) {
            notifyStateChanged(State.COMPLETED);
        }
    }

    /**
     * Evaluates loss and classification accuracy across all dataset samples.
     */
    public EvaluationResult evaluate(List<DataPoint> points) {
        if (points == null || points.isEmpty()) {
            return new EvaluationResult(0.0, 0.0);
        }

        double totalLoss = 0.0;
        int correctCount = 0;
        int numClasses = network.getOutputSize();

        for (DataPoint pt : points) {
            double[] features = pt.toFeatures();
            double[] target = new double[numClasses];
            if (pt.getLabel() >= 0 && pt.getLabel() < numClasses) {
                target[pt.getLabel()] = 1.0;
            }

            double[] probs = network.forward(features);
            pt.setProbabilities(probs);

            int predictedClass = 0;
            double maxP = probs[0];
            for (int c = 1; c < probs.length; c++) {
                if (probs[c] > maxP) {
                    maxP = probs[c];
                    predictedClass = c;
                }
            }
            pt.setPredictedLabel(predictedClass);

            if (pt.isCorrect()) {
                correctCount++;
            }

            totalLoss += lossFunction.calculateLoss(probs, target);
        }

        double avgLoss = totalLoss / points.size();
        double accuracy = ((double) correctCount / points.size()) * 100.0;

        return new EvaluationResult(avgLoss, accuracy);
    }

    public static class EvaluationResult {
        public final double loss;
        public final double accuracy;

        public EvaluationResult(double loss, double accuracy) {
            this.loss = loss;
            this.accuracy = accuracy;
        }
    }

    // Getters and Setters
    public State getState() { return state; }
    public double getLearningRate() { return learningRate; }
    public void setLearningRate(double learningRate) { this.learningRate = learningRate; }
    public double getMomentum() { return momentum; }
    public void setMomentum(double momentum) { this.momentum = momentum; }
    public int getTotalEpochs() { return totalEpochs; }
    public void setTotalEpochs(int totalEpochs) { this.totalEpochs = totalEpochs; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public int getDelayMs() { return delayMs; }
    public void setDelayMs(int delayMs) { this.delayMs = delayMs; }
    public int getUiUpdateInterval() { return uiUpdateInterval; }
    public void setUiUpdateInterval(int uiUpdateInterval) { this.uiUpdateInterval = Math.max(1, uiUpdateInterval); }
    public int getCurrentEpoch() { return currentEpoch; }
}
