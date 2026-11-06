package ui;

import training.Trainer;
import visualization.LossGraph;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Real-time telemetry dashboard presenting training progress,
 * live loss/accuracy metrics, architecture parameter counts, and the convergence loss graph.
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public class TrainingPanel extends JPanel {

    private final LossGraph lossGraph;

    private JLabel statusLabel;
    private JLabel epochLabel;
    private JLabel lossLabel;
    private JLabel accuracyLabel;
    private JLabel paramsLabel;
    private JProgressBar progressBar;

    private long trainingStartTime = 0;

    public TrainingPanel() {
        setLayout(new BorderLayout(6, 6));
        setBackground(new Color(26, 28, 36));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        setPreferredSize(new Dimension(360, 260));

        lossGraph = new LossGraph();

        initComponents();
    }

    private void initComponents() {
        // Top telemetry statistics grid
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 8, 6));
        statsPanel.setBackground(new Color(33, 36, 46));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 60, 75), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        statusLabel = createMetricLabel("Status:", "Ready", new Color(46, 213, 115));
        epochLabel = createMetricLabel("Epoch:", "0 / 0", Color.WHITE);
        lossLabel = createMetricLabel("Loss:", "0.0000", new Color(255, 121, 63));
        accuracyLabel = createMetricLabel("Accuracy:", "0.00%", new Color(46, 213, 115));
        paramsLabel = createMetricLabel("Weights:", "0 params", new Color(200, 205, 220));

        statsPanel.add(wrapMetric("Status", statusLabel));
        statsPanel.add(wrapMetric("Epoch", epochLabel));
        statsPanel.add(wrapMetric("Loss (Cross-Entropy)", lossLabel));
        statsPanel.add(wrapMetric("Accuracy", accuracyLabel));
        statsPanel.add(wrapMetric("Architecture", paramsLabel));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(39, 174, 96));
        progressBar.setBackground(new Color(20, 22, 28));
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 10));
        progressBar.setPreferredSize(new Dimension(100, 18));

        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.setBackground(new Color(26, 28, 36));
        topBox.add(statsPanel);
        topBox.add(Box.createVerticalStrut(6));
        topBox.add(progressBar);

        add(topBox, BorderLayout.NORTH);

        // Center: Convergence Graph
        JPanel graphContainer = new JPanel(new BorderLayout());
        graphContainer.setBackground(new Color(26, 28, 36));
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(55, 60, 75), 1),
            "Convergence History",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 11),
            new Color(180, 195, 230)
        );
        graphContainer.setBorder(border);
        graphContainer.add(lossGraph, BorderLayout.CENTER);

        add(graphContainer, BorderLayout.CENTER);
    }

    private JPanel wrapMetric(String title, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout(2, 2));
        p.setBackground(new Color(33, 36, 46));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        titleLbl.setForeground(new Color(150, 155, 170));
        p.add(titleLbl, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private JLabel createMetricLabel(String prefix, String initial, Color color) {
        JLabel lbl = new JLabel(initial);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(color);
        return lbl;
    }

    public void updateMetrics(int currentEpoch, int totalEpochs, double loss, double accuracy) {
        epochLabel.setText(String.format("%d / %d", currentEpoch, totalEpochs));
        lossLabel.setText(String.format("%.4f", loss));
        accuracyLabel.setText(String.format("%.2f%%", accuracy));

        int percent = (int) (((double) currentEpoch / Math.max(1, totalEpochs)) * 100);
        progressBar.setValue(percent);
        progressBar.setString(percent + "%");

        lossGraph.addEntry(currentEpoch, loss, accuracy);
    }

    public void updateState(Trainer.State state) {
        switch (state) {
            case RUNNING:
                statusLabel.setText("Training...");
                statusLabel.setForeground(new Color(30, 144, 255));
                break;
            case PAUSED:
                statusLabel.setText("Paused");
                statusLabel.setForeground(new Color(243, 156, 18));
                break;
            case STOPPED:
                statusLabel.setText("Stopped");
                statusLabel.setForeground(new Color(231, 76, 60));
                break;
            case COMPLETED:
                statusLabel.setText("Completed");
                statusLabel.setForeground(new Color(46, 213, 115));
                break;
            case IDLE:
            default:
                statusLabel.setText("Ready");
                statusLabel.setForeground(new Color(180, 185, 200));
                break;
        }
    }

    public void setParameterCount(int params) {
        paramsLabel.setText(params + " weights");
    }

    public void reset() {
        statusLabel.setText("Ready");
        epochLabel.setText("0 / 0");
        lossLabel.setText("0.0000");
        accuracyLabel.setText("0.00%");
        progressBar.setValue(0);
        progressBar.setString("0%");
        lossGraph.clear();
    }

    public LossGraph getLossGraph() {
        return lossGraph;
    }
}
