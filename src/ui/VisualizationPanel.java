package ui;

import data.SpiralDataset;
import neural.NeuralNetwork;
import visualization.DataPlotter;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Visualization workspace container embedding the DataPlotter canvas
 * and providing interactive layer toggle controls (decision boundary, grid, resolution).
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public class VisualizationPanel extends JPanel {

    private final DataPlotter dataPlotter;
    private final JCheckBox chkBoundary;
    private final JCheckBox chkGrid;
    private final JComboBox<String> resolutionCombo;
    private final JLabel infoLabel;

    public VisualizationPanel(SpiralDataset dataset, NeuralNetwork network) {
        setLayout(new BorderLayout());
        setBackground(new Color(24, 26, 32));

        dataPlotter = new DataPlotter(dataset, network);

        // Top Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        toolbar.setBackground(new Color(30, 33, 42));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 55, 70)));

        chkBoundary = new JCheckBox("Decision Boundary", true);
        styleCheckBox(chkBoundary);
        chkBoundary.addActionListener(e -> dataPlotter.setShowDecisionBoundary(chkBoundary.isSelected()));

        chkGrid = new JCheckBox("Grid & Axes", true);
        styleCheckBox(chkGrid);
        chkGrid.addActionListener(e -> dataPlotter.setShowGrid(chkGrid.isSelected()));

        JLabel resLabel = new JLabel("Mesh Res:");
        resLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resLabel.setForeground(new Color(180, 185, 200));

        resolutionCombo = new JComboBox<>(new String[]{"Fast (60x60)", "Standard (90x90)", "High-Res (130x130)"});
        resolutionCombo.setSelectedIndex(1);
        resolutionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resolutionCombo.addActionListener(e -> {
            int idx = resolutionCombo.getSelectedIndex();
            int res = (idx == 0) ? 60 : (idx == 1) ? 90 : 130;
            dataPlotter.setGridResolution(res);
        });

        infoLabel = new JLabel("Hint: Hover or click canvas to probe neural network activation");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(130, 150, 180));

        toolbar.add(chkBoundary);
        toolbar.add(chkGrid);
        toolbar.add(resLabel);
        toolbar.add(resolutionCombo);
        toolbar.add(infoLabel);

        add(toolbar, BorderLayout.NORTH);
        add(dataPlotter, BorderLayout.CENTER);
    }

    private void styleCheckBox(JCheckBox cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cb.setForeground(new Color(220, 225, 235));
        cb.setBackground(new Color(30, 33, 42));
        cb.setFocusPainted(false);
    }

    public DataPlotter getDataPlotter() {
        return dataPlotter;
    }

    public void updateInfo(String text) {
        infoLabel.setText(text);
    }
}
