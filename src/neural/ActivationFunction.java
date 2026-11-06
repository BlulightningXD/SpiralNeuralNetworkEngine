package neural;

/**
 * Encapsulates non-linear activation functions and their analytical derivatives
 * used in feed-forward neural networks.
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public enum ActivationFunction {
    RELU("ReLU") {
        @Override
        public double activate(double z) {
            // Leaky ReLU (slope 0.01 for negative z) to prevent dying neuron problem
            return z > 0 ? z : 0.01 * z;
        }

        @Override
        public double derivative(double z, double a) {
            return z > 0 ? 1.0 : 0.01;
        }
    },
    TANH("Tanh") {
        @Override
        public double activate(double z) {
            return Math.tanh(z);
        }

        @Override
        public double derivative(double z, double a) {
            // d/dz tanh(z) = 1 - tanh^2(z) = 1 - a^2
            return 1.0 - (a * a);
        }
    },
    SIGMOID("Sigmoid") {
        @Override
        public double activate(double z) {
            // Numerically stable sigmoid
            if (z >= 45.0) return 1.0;
            if (z <= -45.0) return 0.0;
            return 1.0 / (1.0 + Math.exp(-z));
        }

        @Override
        public double derivative(double z, double a) {
            // d/dz sigmoid(z) = sigmoid(z) * (1 - sigmoid(z)) = a * (1 - a)
            return a * (1.0 - a);
        }
    },
    LINEAR("Linear") {
        @Override
        public double activate(double z) {
            return z;
        }

        @Override
        public double derivative(double z, double a) {
            return 1.0;
        }
    };

    private final String displayName;

    ActivationFunction(String displayName) {
        this.displayName = displayName;
    }

    public abstract double activate(double z);

    /**
     * Calculates derivative with respect to pre-activation input z or post-activation a.
     * 
     * @param z pre-activation value
     * @param a post-activation value (activation(z))
     * @return derivative df/dz
     */
    public abstract double derivative(double z, double a);

    @Override
    public String toString() {
        return displayName;
    }
}
