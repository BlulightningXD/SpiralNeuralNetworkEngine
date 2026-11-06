package ui;

import data.SpiralDataset;
import neural.ActivationFunction;
import training.Trainer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Interactive parameter control panel for dataset synthesis,
 * deep neural network hyperparameters, and training lifecycle controls.
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public class ControlPanel extends JPanel {

    public interface ControlListener {
        void onRegenerateDataset(SpiralDataset.DatasetType type, int classes, int pointsPerClass, double noise, double rotations);
        void onRebuildNetwork(int[] hiddenLayers, ActivationFunction activation);
        void onStartTraining(double lr, double momentum, int epochs, int batchSize, int delayMs);
        void onPauseTraining();
        void onResumeTraining();
        void onStopTraining();
        void onResetWeights();
    }

    private ControlListener listener;

    // Dataset inputs
    private JComboBox<SpiralDataset.DatasetType> datasetTypeCombo;
    private JSpinner classesSpinner;
    private JSpinner pointsSpinner;
    private JSpinner noiseSpinner;
    private JSpinner rotationsSpinner;
    private JButton btnRegenerate;

    // Network inputs
    private JTextField hiddenLayersField;
    private JComboBox<ActivationFunction> activationCombo;
    private JButton btnRebuildNet;

    // Training inputs
    private JSpinner lrSpinner;
    private JSpinner momentumSpinner;
    private JSpinner epochsSpinner;
    private JSpinner batchSizeSpinner;
    private JSlider speedSlider;

    // Control buttons
    private JButton btnStart;
    private JButton btnPause;
    private JButton btnStop;
    private JButton btnReset;

    public ControlPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(340, 720));
        setMinimumSize(new Dimension(300, 500));
        setBackground(new Color(30, 32, 40));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        initComponents();
    }

    public void setControlListener(ControlListener listener) {
        this.listener = listener;
    }

    private void initComponents() {
        // Container panel wrapped in JScrollPane for flexible window resizing
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(30, 32, 40));

        container.add(createDatasetSection());
        container.add(Box.createVerticalStrut(10));
        container.add(createNetworkSection());
        container.add(Box.createVerticalStrut(10));
        container.add(createTrainingSection());
        container.add(Box.createVerticalStrut(10));
        container.add(createActionsSection());
        container.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(new Color(30, 32, 40));

        add(scrollPane);
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(38, 41, 52));
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 65, 82), 1),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(180, 195, 230)
        );
        panel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        return panel;
    }

    private JPanel createDatasetSection() {
        JPanel panel = createSectionPanel("1. Dataset Configuration");
        GridBagConstraints gbc = createGBC();

        // Pattern Type
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("Pattern:"), gbc);
        gbc.gridx = 1;
        datasetTypeCombo = new JComboBox<>(SpiralDataset.DatasetType.values());
        panel.add(datasetTypeCombo, gbc);

        // Classes
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(createLabel("Classes (Arms):"), gbc);
        gbc.gridx = 1;
        classesSpinner = new JSpinner(new SpinnerNumberModel(3, 2, 6, 1));
        panel.add(classesSpinner, gbc);

        // Points Per Class
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createLabel("Points / Class:"), gbc);
        gbc.gridx = 1;
        pointsSpinner = new JSpinner(new SpinnerNumberModel(150, 30, 600, 10));
        panel.add(pointsSpinner, gbc);

        // Noise
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(createLabel("Noise (σ):"), gbc);
        gbc.gridx = 1;
        noiseSpinner = new JSpinner(new SpinnerNumberModel(0.12, 0.0, 0.50, 0.02));
        panel.add(noiseSpinner, gbc);

        // Rotations
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(createLabel("Rotations:"), gbc);
        gbc.gridx = 1;
        rotationsSpinner = new JSpinner(new SpinnerNumberModel(1.25, 0.25, 3.5, 0.25));
        panel.add(rotationsSpinner, gbc);

        // Regenerate Button
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        btnRegenerate = createStyledButton("↻ Regenerate Data", new Color(41, 128, 185));
        btnRegenerate.addActionListener(e -> triggerRegenerate());
        panel.add(btnRegenerate, gbc);

        return panel;
    }

    private JPanel createNetworkSection() {
        JPanel panel = createSectionPanel("2. Neural Network Architecture");
        GridBagConstraints gbc = createGBC();

        // Hidden Layers
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("Hidden Neurons:"), gbc);
        gbc.gridx = 1;
        hiddenLayersField = new JTextField("32, 32", 8);
        hiddenLayersField.setToolTipText("Comma-separated neuron counts (e.g. 16, 16 or 32, 32)");
        panel.add(hiddenLayersField, gbc);

        // Activation Function
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(createLabel("Activation:"), gbc);
        gbc.gridx = 1;
        activationCombo = new JComboBox<>(ActivationFunction.values());
        activationCombo.setSelectedItem(ActivationFunction.RELU);
        panel.add(activationCombo, gbc);

        // Rebuild Button
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        btnRebuildNet = createStyledButton("⚡ Rebuild Architecture", new Color(108, 92, 231));
        btnRebuildNet.addActionListener(e -> triggerRebuildNetwork());
        panel.add(btnRebuildNet, gbc);

        return panel;
    }

    private JPanel createTrainingSection() {
        JPanel panel = createSectionPanel("3. Hyperparameters & Speed");
        GridBagConstraints gbc = createGBC();

        // Learning Rate
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("Learning Rate (α):"), gbc);
        gbc.gridx = 1;
        lrSpinner = new JSpinner(new SpinnerNumberModel(0.08, 0.001, 1.0, 0.01));
        panel.add(lrSpinner, gbc);

        // Momentum
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(createLabel("Momentum (β):"), gbc);
        gbc.gridx = 1;
        momentumSpinner = new JSpinner(new SpinnerNumberModel(0.90, 0.0, 0.99, 0.05));
        panel.add(momentumSpinner, gbc);

        // Epochs
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createLabel("Total Epochs:"), gbc);
        gbc.gridx = 1;
        epochsSpinner = new JSpinner(new SpinnerNumberModel(2500, 100, 15000, 250));
        panel.add(epochsSpinner, gbc);

        // Batch Size
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(createLabel("Batch Size:"), gbc);
        gbc.gridx = 1;
        batchSizeSpinner = new JSpinner(new SpinnerNumberModel(32, 4, 512, 16));
        panel.add(batchSizeSpinner, gbc);

        // Animation Delay
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(createLabel("Pacing / Delay:"), gbc);
        gbc.gridx = 1;
        speedSlider = new JSlider(0, 20, 0);
        speedSlider.setBackground(new Color(38, 41, 52));
        speedSlider.setToolTipText("Throttle training delay (0 = Maximum Speed)");
        panel.add(speedSlider, gbc);

        return panel;
    }

    private JPanel createActionsSection() {
        JPanel panel = createSectionPanel("4. Training Controls");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        row1.setBackground(new Color(38, 41, 52));

        btnStart = createStyledButton("▶ Start", new Color(39, 174, 96));
        btnPause = createStyledButton("⏸ Pause", new Color(243, 156, 18));
        btnStop = createStyledButton("⏹ Stop", new Color(192, 57, 43));
        btnReset = createStyledButton("↺ Reset", new Color(127, 140, 141));

        btnPause.setEnabled(false);
        btnStop.setEnabled(false);

        row1.add(btnStart);
        row1.add(btnPause);
        row1.add(btnStop);
        row1.add(btnReset);

        panel.add(row1);

        // Action listeners
        btnStart.addActionListener(e -> {
            if (listener != null) {
                double lr = ((Number) lrSpinner.getValue()).doubleValue();
                double momentum = ((Number) momentumSpinner.getValue()).doubleValue();
                int epochs = ((Number) epochsSpinner.getValue()).intValue();
                int batch = ((Number) batchSizeSpinner.getValue()).intValue();
                int delay = speedSlider.getValue();
                listener.onStartTraining(lr, momentum, epochs, batch, delay);
            }
        });

        btnPause.addActionListener(e -> {
            if (listener != null) {
                if ("⏸ Pause".equals(btnPause.getText())) {
                    listener.onPauseTraining();
                } else {
                    listener.onResumeTraining();
                }
            }
        });

        btnStop.addActionListener(e -> {
            if (listener != null) listener.onStopTraining();
        });

        btnReset.addActionListener(e -> {
            if (listener != null) listener.onResetWeights();
        });

        return panel;
    }

    public void updateTrainerState(Trainer.State state) {
        switch (state) {
            case RUNNING:
                btnStart.setEnabled(false);
                btnPause.setEnabled(true);
                btnPause.setText("⏸ Pause");
                btnStop.setEnabled(true);
                btnReset.setEnabled(false);
                btnRegenerate.setEnabled(false);
                btnRebuildNet.setEnabled(false);
                break;
            case PAUSED:
                btnStart.setEnabled(false);
                btnPause.setEnabled(true);
                btnPause.setText("▶ Resume");
                btnStop.setEnabled(true);
                btnReset.setEnabled(true);
                btnRegenerate.setEnabled(true);
                btnRebuildNet.setEnabled(true);
                break;
            case IDLE:
            case STOPPED:
            case COMPLETED:
            default:
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                btnPause.setText("⏸ Pause");
                btnStop.setEnabled(false);
                btnReset.setEnabled(true);
                btnRegenerate.setEnabled(true);
                btnRebuildNet.setEnabled(true);
                break;
        }
    }

    private void triggerRegenerate() {
        if (listener != null) {
            SpiralDataset.DatasetType type = (SpiralDataset.DatasetType) datasetTypeCombo.getSelectedItem();
            int classes = ((Number) classesSpinner.getValue()).intValue();
            int points = ((Number) pointsSpinner.getValue()).intValue();
            double noise = ((Number) noiseSpinner.getValue()).doubleValue();
            double rotations = ((Number) rotationsSpinner.getValue()).doubleValue();
            listener.onRegenerateDataset(type, classes, points, noise, rotations);
        }
    }

    private void triggerRebuildNetwork() {
        if (listener != null) {
            int[] hidden = parseHiddenLayers(hiddenLayersField.getText());
            ActivationFunction act = (ActivationFunction) activationCombo.getSelectedItem();
            listener.onRebuildNetwork(hidden, act);
        }
    }

    public int[] parseHiddenLayers(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new int[]{16, 16};
        }
        String[] tokens = text.split(",");
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (String t : tokens) {
            try {
                int val = Integer.parseInt(t.trim());
                if (val > 0) list.add(val);
            } catch (NumberFormatException ignored) {}
        }
        if (list.isEmpty()) return new int[]{16, 16};
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) result[i] = list.get(i);
        return result;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(new Color(210, 215, 225));
        return label;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }
}
