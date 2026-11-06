package ui;

import data.SpiralDataset;
import neural.ActivationFunction;
import neural.NeuralNetwork;
import training.Trainer;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Main application window for the Spiral Neural Network Interface Engine.
 * Integrates MVC components, controls lifecycle events, and coordinates threading between
 * the numerical engine and Swing UI.
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 * Submitted to: Dr. K.Rajalakshmi
 */
public class MainFrame extends JFrame implements ControlPanel.ControlListener, Trainer.TrainingListener {

    private SpiralDataset dataset;
    private NeuralNetwork network;
    private Trainer trainer;

    // UI Modules
    private ControlPanel controlPanel;
    private VisualizationPanel visualizationPanel;
    private TrainingPanel trainingPanel;
    private JLabel statusBarLabel;

    public MainFrame() {
        super("Spiral Neural Network Interface Engine - JIIT OOPS Java Lab");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 720));
        setPreferredSize(new Dimension(1200, 800));

        initEngine();
        initUI();
        initMenuBar();

        pack();
        setLocationRelativeTo(null);
    }

    private void initEngine() {
        // Default: 3-arm spiral with 150 points per class
        dataset = new SpiralDataset(3, 150, 0.12, 1.25);

        // Default: 2 inputs -> [32, 32] hidden neurons -> 3 output classes
        network = new NeuralNetwork(2, new int[]{32, 32}, dataset.getNumClasses(), ActivationFunction.RELU);

        trainer = new Trainer(network, dataset);
        trainer.addListener(this);
    }

    private void initUI() {
        controlPanel = new ControlPanel();
        controlPanel.setControlListener(this);

        visualizationPanel = new VisualizationPanel(dataset, network);

        trainingPanel = new TrainingPanel();
        trainingPanel.setParameterCount(network.getTotalParameters());

        // Right side: Visualization on top, Training Telemetry / LossGraph below
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, visualizationPanel, trainingPanel);
        rightSplit.setResizeWeight(0.65);
        rightSplit.setDividerSize(5);
        rightSplit.setBorder(null);

        // Main layout: Control panel on left, Split on right
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, rightSplit);
        mainSplit.setResizeWeight(0.28);
        mainSplit.setDividerSize(5);
        mainSplit.setBorder(null);

        // Status bar at bottom
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(20, 22, 28));
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        statusBarLabel = new JLabel("System Ready | Architecture: " + network.getArchitectureDescription());
        statusBarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBarLabel.setForeground(new Color(160, 165, 180));

        JLabel collegeTag = new JLabel("JIIT CSE | OOPS Java Lab (24B15CS215)");
        collegeTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        collegeTag.setForeground(new Color(110, 150, 200));

        statusBar.add(statusBarLabel, BorderLayout.WEST);
        statusBar.add(collegeTag, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainSplit, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // Initial evaluation
        Trainer.EvaluationResult initEval = trainer.evaluate(dataset.getDataPoints());
        trainingPanel.updateMetrics(0, trainer.getTotalEpochs(), initEval.loss, initEval.accuracy);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(30, 32, 40));

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(Color.WHITE);

        JMenuItem resetItem = new JMenuItem("Reset Weights");
        resetItem.addActionListener(e -> onResetWeights());
        fileMenu.add(resetItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Presets Menu
        JMenu presetsMenu = new JMenu("Presets");
        presetsMenu.setForeground(Color.WHITE);

        JMenuItem p3Spiral = new JMenuItem("Classic 3-Arm Spiral (Recommended)");
        p3Spiral.addActionListener(e -> applyPreset(SpiralDataset.DatasetType.SPIRAL, 3, 160, 0.12, 1.25, new int[]{32, 32}));

        JMenuItem p2Spiral = new JMenuItem("2-Arm Yin-Yang Spiral");
        p2Spiral.addActionListener(e -> applyPreset(SpiralDataset.DatasetType.SPIRAL, 2, 200, 0.10, 1.5, new int[]{32, 32}));

        JMenuItem pCircles = new JMenuItem("Concentric Circles");
        pCircles.addActionListener(e -> applyPreset(SpiralDataset.DatasetType.CIRCLES, 2, 200, 0.10, 1.0, new int[]{16, 16}));

        JMenuItem pXor = new JMenuItem("XOR Quadrants");
        pXor.addActionListener(e -> applyPreset(SpiralDataset.DatasetType.XOR, 2, 200, 0.10, 1.0, new int[]{16, 16}));

        presetsMenu.add(p3Spiral);
        presetsMenu.add(p2Spiral);
        presetsMenu.add(pCircles);
        presetsMenu.add(pXor);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(Color.WHITE);

        JMenuItem aboutItem = new JMenuItem("About & Project Synopsis");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(presetsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void applyPreset(SpiralDataset.DatasetType type, int classes, int points, double noise, double rotations, int[] hidden) {
        if (trainer.getState() == Trainer.State.RUNNING) {
            trainer.stop();
        }
        dataset.setDatasetType(type);
        dataset.setNumClasses(classes);
        dataset.setPointsPerClass(points);
        dataset.setNoise(noise);
        dataset.setRotations(rotations);
        dataset.generate();

        network = new NeuralNetwork(2, hidden, classes, ActivationFunction.RELU);
        trainer = new Trainer(network, dataset);
        trainer.addListener(this);

        visualizationPanel.getDataPlotter().setDataset(dataset);
        visualizationPanel.getDataPlotter().setNetwork(network);
        trainingPanel.setParameterCount(network.getTotalParameters());
        trainingPanel.reset();

        Trainer.EvaluationResult eval = trainer.evaluate(dataset.getDataPoints());
        trainingPanel.updateMetrics(0, trainer.getTotalEpochs(), eval.loss, eval.accuracy);
        visualizationPanel.getDataPlotter().invalidateBoundary();
        visualizationPanel.getDataPlotter().repaint();
        statusBarLabel.setText("Applied Preset: " + type + " (" + classes + " classes) | Arch: " + network.getArchitectureDescription());
    }

    private void showAboutDialog() {
        String msg = "Spiral Neural Network Interface Engine Using Java\n\n"
            + "Jaypee Institute of Information Technology, Noida\n"
            + "B.Tech III-Semester CSE\n"
            + "Course: OOPS Using Java Lab (24B15CS215)\n"
            + "Submitted to: Dr. K.Rajalakshmi\n\n"
            + "Developed By:\n"
            + "• Aditya Saha (Enrollment: 2501030308)\n"
            + "• Aviral Jain (Enrollment: 2501030329)\n"
            + "• Divyansh Mittal (Enrollment: 2501030335)\n\n"
            + "Engine Highlights:\n"
            + "• Custom Pure Java Feed-Forward Neural Network (No external ML libraries)\n"
            + "• Softmax Output Head + Categorical Cross-Entropy Loss\n"
            + "• Analytical Backpropagation with Momentum SGD Optimization\n"
            + "• Dynamic Real-Time Decision Boundary Mesh Heatmap\n"
            + "• Real-time Dual-Axis Loss and Accuracy Convergence Graph\n"
            + "• Multithreaded Background Engine with Interactive Canvas Probing";

        JOptionPane.showMessageDialog(this, msg, "About Project", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- ControlPanel.ControlListener Callbacks ---

    @Override
    public void onRegenerateDataset(SpiralDataset.DatasetType type, int classes, int pointsPerClass, double noise, double rotations) {
        boolean classesChanged = (classes != dataset.getNumClasses());
        dataset.setDatasetType(type);
        dataset.setNumClasses(classes);
        dataset.setPointsPerClass(pointsPerClass);
        dataset.setNoise(noise);
        dataset.setRotations(rotations);
        dataset.generate();

        if (classesChanged) {
            network = new NeuralNetwork(2, network.getHiddenLayers(), classes, network.getHiddenActivation());
            trainer = new Trainer(network, dataset);
            trainer.addListener(this);
            visualizationPanel.getDataPlotter().setNetwork(network);
            trainingPanel.setParameterCount(network.getTotalParameters());
        }

        visualizationPanel.getDataPlotter().setDataset(dataset);
        visualizationPanel.getDataPlotter().invalidateBoundary();
        visualizationPanel.getDataPlotter().repaint();

        Trainer.EvaluationResult eval = trainer.evaluate(dataset.getDataPoints());
        trainingPanel.reset();
        trainingPanel.updateMetrics(0, trainer.getTotalEpochs(), eval.loss, eval.accuracy);
        statusBarLabel.setText("Dataset regenerated: " + dataset.getTotalPoints() + " points across " + classes + " classes.");
    }

    @Override
    public void onRebuildNetwork(int[] hiddenLayers, ActivationFunction activation) {
        if (trainer.getState() == Trainer.State.RUNNING) {
            trainer.stop();
        }
        network = new NeuralNetwork(2, hiddenLayers, dataset.getNumClasses(), activation);
        trainer = new Trainer(network, dataset);
        trainer.addListener(this);

        visualizationPanel.getDataPlotter().setNetwork(network);
        visualizationPanel.getDataPlotter().invalidateBoundary();
        visualizationPanel.getDataPlotter().repaint();

        trainingPanel.setParameterCount(network.getTotalParameters());
        trainingPanel.reset();

        Trainer.EvaluationResult eval = trainer.evaluate(dataset.getDataPoints());
        trainingPanel.updateMetrics(0, trainer.getTotalEpochs(), eval.loss, eval.accuracy);
        statusBarLabel.setText("Network rebuilt: " + network.getArchitectureDescription());
    }

    @Override
    public void onStartTraining(double lr, double momentum, int epochs, int batchSize, int delayMs) {
        trainer.setLearningRate(lr);
        trainer.setMomentum(momentum);
        trainer.setTotalEpochs(epochs);
        trainer.setBatchSize(batchSize);
        trainer.setDelayMs(delayMs);

        trainingPanel.reset();
        trainer.start();
        statusBarLabel.setText("Training started...");
    }

    @Override
    public void onPauseTraining() {
        trainer.pause();
        statusBarLabel.setText("Training paused.");
    }

    @Override
    public void onResumeTraining() {
        trainer.resume();
        statusBarLabel.setText("Training resumed.");
    }

    @Override
    public void onStopTraining() {
        trainer.stop();
        statusBarLabel.setText("Training stopped.");
    }

    @Override
    public void onResetWeights() {
        network.resetWeights();
        trainingPanel.reset();
        visualizationPanel.getDataPlotter().invalidateBoundary();
        visualizationPanel.getDataPlotter().repaint();

        Trainer.EvaluationResult eval = trainer.evaluate(dataset.getDataPoints());
        trainingPanel.updateMetrics(0, trainer.getTotalEpochs(), eval.loss, eval.accuracy);
        statusBarLabel.setText("Weights re-initialized to initial distributions.");
    }

    // --- Trainer.TrainingListener Callbacks ---

    @Override
    public void onEpochCompleted(int epoch, int totalEpochs, double loss, double accuracy) {
        trainingPanel.updateMetrics(epoch, totalEpochs, loss, accuracy);
        visualizationPanel.getDataPlotter().invalidateBoundary();
        visualizationPanel.getDataPlotter().repaint();
        statusBarLabel.setText(String.format("Training Epoch %d / %d | Loss: %.4f | Accuracy: %.2f%%", epoch, totalEpochs, loss, accuracy));
    }

    @Override
    public void onTrainingStateChanged(Trainer.State newState) {
        controlPanel.updateTrainerState(newState);
        trainingPanel.updateState(newState);

        if (newState == Trainer.State.COMPLETED) {
            statusBarLabel.setText("Training completed! Final convergence reached.");
        }
    }
}
