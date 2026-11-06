package neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Multilayer Feed-Forward Neural Network implementation with configurable
 * depth, layer dimensions, activation functions, and output classification head.
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public class NeuralNetwork {
    private final int inputSize;
    private final int[] hiddenLayers;
    private final int outputSize;
    private final ActivationFunction hiddenActivation;
    private final List<Layer> layers = new ArrayList<>();

    /**
     * Constructs a feed-forward neural network.
     * 
     * @param inputSize Number of input dimensions (2 for 2D classification)
     * @param hiddenLayers Array of neuron counts for each hidden layer (e.g. [32, 32])
     * @param outputSize Number of output classes
     * @param hiddenActivation Non-linear activation for hidden layers (ReLU, Tanh, etc.)
     */
    public NeuralNetwork(int inputSize, int[] hiddenLayers, int outputSize, ActivationFunction hiddenActivation) {
        this.inputSize = inputSize;
        this.hiddenLayers = hiddenLayers != null ? hiddenLayers.clone() : new int[]{16, 16};
        this.outputSize = outputSize;
        this.hiddenActivation = hiddenActivation != null ? hiddenActivation : ActivationFunction.RELU;
        buildNetwork();
    }

    /**
     * Assembles the layers of the network.
     */
    private void buildNetwork() {
        layers.clear();
        int prevSize = inputSize;

        // Hidden layers
        for (int hiddenSize : hiddenLayers) {
            if (hiddenSize <= 0) continue;
            layers.add(new Layer(prevSize, hiddenSize, hiddenActivation, false));
            prevSize = hiddenSize;
        }

        // Output layer: uses Softmax for multi-class classification
        layers.add(new Layer(prevSize, outputSize, ActivationFunction.LINEAR, true));
    }

    /**
     * Propagates input vector through all layers.
     * 
     * @param input Input feature vector [x, y]
     * @return Output class probability distribution
     */
    public double[] forward(double[] input) {
        double[] current = input;
        for (Layer layer : layers) {
            current = layer.forward(current);
        }
        return current;
    }

    /**
     * Computes the argmax predicted class index for an input vector.
     */
    public int predict(double[] input) {
        double[] probs = forward(input);
        int maxIndex = 0;
        double maxProb = probs[0];
        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Propagates gradients backwards from the output layer to the input layer.
     * 
     * @param outputDelta Gradient delta from loss function (e.g. p - y for Softmax Cross-Entropy)
     */
    public void backward(double[] outputDelta) {
        double[] currentGrad = outputDelta;
        for (int i = layers.size() - 1; i >= 0; i--) {
            currentGrad = layers.get(i).backward(currentGrad);
        }
    }

    /**
     * Applies accumulated gradients to all layer parameters.
     */
    public void updateParameters(double learningRate, double momentum, int batchSize) {
        for (Layer layer : layers) {
            layer.updateParameters(learningRate, momentum, batchSize);
        }
    }

    /**
     * Resets all network weights and biases to freshly initialized distributions.
     */
    public void resetWeights() {
        for (Layer layer : layers) {
            layer.initializeWeights();
        }
    }

    /**
     * Calculates total trainable parameters (weights + biases) across all layers.
     */
    public int getTotalParameters() {
        int total = 0;
        for (Layer layer : layers) {
            total += (layer.getInputSize() * layer.getOutputSize()) + layer.getOutputSize();
        }
        return total;
    }

    public List<Layer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    public int getInputSize() {
        return inputSize;
    }

    public int[] getHiddenLayers() {
        return hiddenLayers.clone();
    }

    public int getOutputSize() {
        return outputSize;
    }

    public ActivationFunction getHiddenActivation() {
        return hiddenActivation;
    }

    public String getArchitectureDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(inputSize).append(" -> ");
        for (int h : hiddenLayers) {
            sb.append(h).append(" -> ");
        }
        sb.append(outputSize).append(" (").append(hiddenActivation.name()).append(" + Softmax)");
        return sb.toString();
    }
}
