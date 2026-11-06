package training;

/**
 * Mathematical loss functions and analytical gradient derivatives for multi-class optimization.
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public enum LossFunction {
    CROSS_ENTROPY("Categorical Cross-Entropy") {
        @Override
        public double calculateLoss(double[] predicted, double[] target) {
            double loss = 0.0;
            double eps = 1e-15; // prevent log(0)
            for (int i = 0; i < target.length; i++) {
                if (target[i] > 0.0) {
                    double p = Math.max(eps, Math.min(1.0 - eps, predicted[i]));
                    loss -= target[i] * Math.log(p);
                }
            }
            return loss;
        }

        @Override
        public double[] calculateGradient(double[] predicted, double[] target) {
            // For Softmax paired with Cross-Entropy, dL/dz = (predicted - target)
            double[] grad = new double[predicted.length];
            for (int i = 0; i < predicted.length; i++) {
                grad[i] = predicted[i] - target[i];
            }
            return grad;
        }
    },
    MEAN_SQUARED_ERROR("Mean Squared Error") {
        @Override
        public double calculateLoss(double[] predicted, double[] target) {
            double sum = 0.0;
            for (int i = 0; i < target.length; i++) {
                double diff = predicted[i] - target[i];
                sum += 0.5 * diff * diff;
            }
            return sum;
        }

        @Override
        public double[] calculateGradient(double[] predicted, double[] target) {
            double[] grad = new double[predicted.length];
            for (int i = 0; i < predicted.length; i++) {
                grad[i] = predicted[i] - target[i];
            }
            return grad;
        }
    };

    private final String displayName;

    LossFunction(String displayName) {
        this.displayName = displayName;
    }

    public abstract double calculateLoss(double[] predicted, double[] target);

    public abstract double[] calculateGradient(double[] predicted, double[] target);

    @Override
    public String toString() {
        return displayName;
    }
}
