package neural;

import java.util.Random;

/**
 * Represents a fully-connected (dense) neural network layer.
 * Manages weights, biases, activations, gradients, and momentum velocity tensors.
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public class Layer {
    private final int inputSize;
    private final int outputSize;
    private final double[][] weights;
    private final double[] biases;
    private final ActivationFunction activationFunction;
    private final boolean isSoftmax;

    // Cache for backpropagation
    private double[] lastInputs;
    private double[] lastZ;
    private double[] lastActivations;

    // Gradient accumulators
    private final double[][] weightGradients;
    private final double[] biasGradients;

    // Momentum velocity buffers (for accelerated gradient descent)
    private final double[][] weightVelocities;
    private final double[] biasVelocities;

    private static final Random random = new Random();

    /**
     * Constructs a Layer with specified dimensions, activation, and softmax flag.
     */
    public Layer(int inputSize, int outputSize, ActivationFunction activationFunction, boolean isSoftmax) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.activationFunction = activationFunction;
        this.isSoftmax = isSoftmax;

        this.weights = new double[inputSize][outputSize];
        this.biases = new double[outputSize];

        this.weightGradients = new double[inputSize][outputSize];
        this.biasGradients = new double[outputSize];

        this.weightVelocities = new double[inputSize][outputSize];
        this.biasVelocities = new double[outputSize];

        initializeWeights();
    }

    /**
     * Initializes weights using He or Xavier/Glorot variance scaling.
     */
    public void initializeWeights() {
        double stdDev;
        if (activationFunction == ActivationFunction.RELU) {
            // He initialization: std = sqrt(2 / inputSize)
            stdDev = Math.sqrt(2.0 / inputSize);
        } else {
            // Xavier / Glorot initialization: std = sqrt(2 / (inputSize + outputSize))
            stdDev = Math.sqrt(2.0 / (inputSize + outputSize));
        }

        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weights[i][j] = random.nextGaussian() * stdDev;
                weightGradients[i][j] = 0.0;
                weightVelocities[i][j] = 0.0;
            }
        }

        for (int j = 0; j < outputSize; j++) {
            biases[j] = (activationFunction == ActivationFunction.RELU) ? 0.01 : 0.0;
            biasGradients[j] = 0.0;
            biasVelocities[j] = 0.0;
        }
    }

    /**
     * Performs forward propagation through this dense layer.
     * Computes z = W * x + b, followed by activation or softmax.
     * 
     * @param inputs Array of length inputSize
     * @return Output activations of length outputSize
     */
    public double[] forward(double[] inputs) {
        this.lastInputs = inputs;
        this.lastZ = new double[outputSize];
        this.lastActivations = new double[outputSize];

        // Linear combination: z_j = sum_i (x_i * w_ij) + b_j
        for (int j = 0; j < outputSize; j++) {
            double sum = biases[j];
            for (int i = 0; i < inputSize; i++) {
                sum += inputs[i] * weights[i][j];
            }
            lastZ[j] = sum;
        }

        if (isSoftmax) {
            // Numerically stable softmax: p_j = exp(z_j - max(z)) / sum(exp(z_k - max(z)))
            double maxZ = Double.NEGATIVE_INFINITY;
            for (double zVal : lastZ) {
                if (zVal > maxZ) maxZ = zVal;
            }

            double sumExp = 0.0;
            for (int j = 0; j < outputSize; j++) {
                lastActivations[j] = Math.exp(lastZ[j] - maxZ);
                sumExp += lastActivations[j];
            }

            if (sumExp > 0.0) {
                for (int j = 0; j < outputSize; j++) {
                    lastActivations[j] /= sumExp;
                }
            }
        } else {
            for (int j = 0; j < outputSize; j++) {
                lastActivations[j] = activationFunction.activate(lastZ[j]);
            }
        }

        return lastActivations;
    }

    /**
     * Performs backward propagation through this dense layer.
     * 
     * @param upstreamGradients dL/da (for hidden layers) or (p - y) if softmax output
     * @return Gradients with respect to input features (dL/dx) for the previous layer
     */
    public double[] backward(double[] upstreamGradients) {
        double[] delta = new double[outputSize];

        if (isSoftmax) {
            // When combined with Cross-Entropy loss, upstreamGradients is already (p - y) = delta
            System.arraycopy(upstreamGradients, 0, delta, 0, outputSize);
        } else {
            // delta_j = (dL/da_j) * f'(z_j)
            for (int j = 0; j < outputSize; j++) {
                double derivative = activationFunction.derivative(lastZ[j], lastActivations[j]);
                delta[j] = upstreamGradients[j] * derivative;
            }
        }

        // Accumulate gradients for weights: dW_ij = x_i * delta_j
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weightGradients[i][j] += lastInputs[i] * delta[j];
            }
        }

        // Accumulate gradients for biases: db_j = delta_j
        for (int j = 0; j < outputSize; j++) {
            biasGradients[j] += delta[j];
        }

        // Compute input gradients for previous layer: dL/dx_i = sum_j (w_ij * delta_j)
        double[] inputGradients = new double[inputSize];
        for (int i = 0; i < inputSize; i++) {
            double sum = 0.0;
            for (int j = 0; j < outputSize; j++) {
                sum += weights[i][j] * delta[j];
            }
            inputGradients[i] = sum;
        }

        return inputGradients;
    }

    /**
     * Updates weights and biases using mini-batch gradient descent with momentum and optional L2 regularization.
     * 
     * @param learningRate Step size alpha
     * @param momentum Momentum coefficient beta (e.g. 0.9)
     * @param batchSize Number of samples over which gradients were accumulated
     */
    public void updateParameters(double learningRate, double momentum, int batchSize) {
        double scale = 1.0 / Math.max(1, batchSize);
        double l2Decay = 0.0001; // subtle L2 weight decay for regularization

        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                double grad = (weightGradients[i][j] * scale) + (l2Decay * weights[i][j]);
                // Velocity update: v = beta * v + alpha * grad
                weightVelocities[i][j] = (momentum * weightVelocities[i][j]) + (learningRate * grad);
                weights[i][j] -= weightVelocities[i][j];
                weightGradients[i][j] = 0.0; // reset accumulator
            }
        }

        for (int j = 0; j < outputSize; j++) {
            double grad = biasGradients[j] * scale;
            biasVelocities[j] = (momentum * biasVelocities[j]) + (learningRate * grad);
            biases[j] -= biasVelocities[j];
            biasGradients[j] = 0.0; // reset accumulator
        }
    }

    public int getInputSize() {
        return inputSize;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public double[][] getWeights() {
        return weights;
    }

    public double[] getBiases() {
        return biases;
    }

    public ActivationFunction getActivationFunction() {
        return activationFunction;
    }

    public boolean isSoftmax() {
        return isSoftmax;
    }

    public double[] getLastActivations() {
        return lastActivations;
    }
}
