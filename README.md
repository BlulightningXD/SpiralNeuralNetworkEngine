# Spiral Neural Network Interface Engine Using Java

**Jaypee Institute of Information Technology (JIIT), Noida**  
**B.Tech III-Semester Project**  
**Course Name:** OOPS Using Java Lab  
**Course Code:** 24B15CS215  
**Submitted to:** Dr. K.Rajalakshmi  

### Submitted By:
- **Aditya Saha** (Enrollment: `2501030308`)
- **Aviral Jain** (Enrollment: `2501030329`)
- **Divyansh Mittal** (Enrollment: `2501030335`)

---

## 1. Project Overview & Aim

The **Spiral Neural Network Interface Engine** is an educational, interactive desktop application developed in pure Java (JDK 17+) that demonstrates how a deep neural network learns to separate and classify complex non-linear patterns (such as multi-arm spirals) without relying on any external machine learning frameworks.

The application integrates:
- **Object-Oriented Programming (OOP)** principles (Encapsulation, Polymorphism, Strategy & Observer patterns).
- **Custom Feed-Forward Deep Neural Network** built from scratch using matrix mathematics.
- **Analytical Backpropagation** with Momentum-accelerated Stochastic Gradient Descent (SGD) and Softmax Categorical Cross-Entropy.
- **Interactive Java Swing & Graphics2D GUI** featuring live decision-boundary heatmaps, coordinate grids, misclassification indicators, real-time dual-axis convergence curves, and interactive mouse-probe activation testing.

---

## 2. Project Architecture & Package Structure

```
SpiralNeuralNetworkEngine/
├── src/
│   ├── Main.java                        # Application entry point & Look-and-Feel launcher
│   ├── ui/
│   │   ├── MainFrame.java               # Main window coordinating MVC, menus, and split views
│   │   ├── ControlPanel.java            # Parameter inputs for data, network, and training
│   │   ├── VisualizationPanel.java      # Canvas container with toolbar toggles
│   │   └── TrainingPanel.java           # Progress telemetry, parameter metrics, and loss graph
│   ├── data/
│   │   ├── DataPoint.java               # 2D coordinates, features, true & predicted labels
│   │   └── SpiralDataset.java           # Parametric trigonometric multi-arm spiral generator
│   ├── neural/
│   │   ├── NeuralNetwork.java           # Feed-forward network architecture & propagation
│   │   ├── Layer.java                   # Fully-connected dense layer with He/Xavier weights
│   │   └── ActivationFunction.java      # ReLU, Tanh, Sigmoid, and Linear with derivatives
│   ├── training/
│   │   ├── Trainer.java                 # Asynchronous background training loop with pause/stop
│   │   └── LossFunction.java            # Categorical Cross-Entropy and Mean Squared Error
│   └── visualization/
│       ├── DataPlotter.java             # Graphics2D canvas for points & decision boundary mesh
│       └── LossGraph.java               # Real-time dual-axis graph (Loss & Accuracy vs Epoch)
├── bin/                                 # Compiled Java bytecode (.class files)
├── run.bat                              # One-click Windows batch launcher
├── run.ps1                              # PowerShell runner script
└── README.md                            # Comprehensive project documentation
```

---

## 3. Mathematical & Algorithmic Foundations

### 3.1 Parametric Spiral Generation
For class $c \in \{0, \dots, C-1\}$ and sample $i \in \{0, \dots, N-1\}$:
$$r = \frac{i}{N} \cdot r_{\max}$$
$$\theta = 2\pi \cdot \text{rotations} \cdot \frac{i}{N} + \frac{2\pi c}{C} + \text{noise} \cdot \mathcal{N}(0, 1)$$
$$x = r \cdot \sin(\theta), \quad y = r \cdot \cos(\theta)$$

### 3.2 Feed-Forward Propagation
For each layer $l$:
$$z^{[l]} = W^{[l]} a^{[l-1]} + b^{[l]}$$
$$a^{[l]} = g(z^{[l]})$$
where $g(z)$ is the activation function (ReLU, Tanh, or Sigmoid).

At the classification output layer, Softmax normalizes activations into a probability distribution:
$$p_k = \frac{e^{z_k - \max(z)}}{\sum_{j=1}^K e^{z_j - \max(z)}}$$

### 3.3 Loss & Analytical Backpropagation
Using **Categorical Cross-Entropy Loss**:
$$L = -\sum_{k=1}^K y_k \ln(p_k + \epsilon)$$

When combined with Softmax, the analytical output gradient simplifies to:
$$\delta^{[L]} = p - y$$

The error propagates backward through layer $l$:
$$\delta^{[l]} = \left( (W^{[l+1]})^T \delta^{[l+1]} \right) \odot g'(z^{[l]})$$
$$\frac{\partial L}{\partial W^{[l]}} = a^{[l-1]} (\delta^{[l]})^T, \quad \frac{\partial L}{\partial b^{[l]}} = \delta^{[l]}$$

Weights and biases are updated using **Momentum SGD**:
$$v_W \leftarrow \beta \cdot v_W + \alpha \cdot \frac{\partial L}{\partial W}, \quad W \leftarrow W - v_W$$

---

## 4. Key Features

- **Custom Machine Learning Core**: Zero external dependencies (no PyTorch, TensorFlow, or DL4J). 100% standard Java SE.
- **Dynamic Decision Boundary Visualization**: Computes a fine-grained 2D mesh grid over the entire feature space, dynamically painting confidence-shaded decision manifolds as the network trains.
- **Dual-Axis Real-Time Graph**: Plots both loss convergence (left axis) and classification accuracy % (right axis) on an auto-scaling graph.
- **Multithreaded Asynchronous Training**: The training loop runs on a daemon background worker thread, ensuring the Swing GUI never freezes. Controls include Start, Pause, Resume, Stop, and Weight Reset.
- **Interactive Mouse HUD / Activation Probe**: Hover or click anywhere on the visualization canvas to inspect live $(x, y)$ coordinates and the network's predicted class probability distribution.
- **Presets & Extensibility**: Includes presets for 2-Arm Spiral, 3-Arm Classic Spiral, Concentric Circles, and XOR logic patterns.

---

## 5. How to Build & Run

### Prerequisites
- JDK 17 or higher (JDK 21/25 supported).

### Windows One-Click Execution
Double-click `run.bat` or execute in PowerShell:
```powershell
.\run.ps1
```

### Manual Compilation & Execution via Terminal
```bash
# 1. Compile source files
javac -d bin -sourcepath src src/Main.java src/data/*.java src/neural/*.java src/training/*.java src/ui/*.java src/visualization/*.java

# 2. Run the application
java -cp bin Main
```
