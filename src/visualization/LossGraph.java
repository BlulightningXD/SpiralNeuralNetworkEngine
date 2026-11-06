package visualization;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Real-time dynamic graph component plotting dual-axis convergence curves:
 * Categorical Cross-Entropy Loss (left axis) and Classification Accuracy % (right axis).
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public class LossGraph extends JPanel {

    public static class MetricPoint {
        public final int epoch;
        public final double loss;
        public final double accuracy;

        public MetricPoint(int epoch, double loss, double accuracy) {
            this.epoch = epoch;
            this.loss = loss;
            this.accuracy = accuracy;
        }
    }

    private final List<MetricPoint> history = Collections.synchronizedList(new ArrayList<>());
    private double maxLoss = 1.0;
    private double minLoss = 0.0;
    private int maxEpoch = 100;

    private static final Color BG_COLOR = new Color(22, 24, 30);
    private static final Color GRID_COLOR = new Color(255, 255, 255, 18);
    private static final Color LOSS_COLOR = new Color(255, 121, 63);    // Vibrant Coral/Orange
    private static final Color ACC_COLOR = new Color(46, 213, 115);     // Neon Emerald Green
    private static final Color TEXT_MUTED = new Color(140, 145, 160);

    public LossGraph() {
        setPreferredSize(new Dimension(360, 180));
        setMinimumSize(new Dimension(280, 140));
        setBackground(BG_COLOR);
    }

    /**
     * Adds an epoch evaluation metric point and triggers a repaint.
     */
    public void addEntry(int epoch, double loss, double accuracy) {
        history.add(new MetricPoint(epoch, loss, accuracy));
        if (loss > maxLoss) {
            maxLoss = loss * 1.1;
        }
        if (epoch > maxEpoch) {
            maxEpoch = epoch;
        }
        repaint();
    }

    /**
     * Clears all historical data points and resets metric bounds.
     */
    public void clear() {
        history.clear();
        maxLoss = 1.0;
        minLoss = 0.0;
        maxEpoch = 100;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        int padLeft = 45;
        int padRight = 45;
        int padTop = 32;
        int padBottom = 26;

        int plotW = w - padLeft - padRight;
        int plotH = h - padTop - padBottom;

        if (plotW <= 10 || plotH <= 10) return;

        // Plot background container
        g2.setColor(new Color(17, 19, 24));
        g2.fillRoundRect(padLeft, padTop, plotW, plotH, 6, 6);
        g2.setColor(new Color(40, 44, 55));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(padLeft, padTop, plotW, plotH, 6, 6);

        // Grid lines (horizontal)
        int hLines = 4;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        for (int i = 0; i <= hLines; i++) {
            int y = padTop + (i * plotH / hLines);
            g2.setColor(GRID_COLOR);
            g2.drawLine(padLeft, y, padLeft + plotW, y);

            // Left Y-axis labels (Loss)
            double lossVal = maxLoss - (i * (maxLoss - minLoss) / hLines);
            g2.setColor(LOSS_COLOR.brighter());
            String lossStr = String.format("%.2f", lossVal);
            g2.drawString(lossStr, padLeft - 32, y + 3);

            // Right Y-axis labels (Accuracy %)
            double accVal = 100.0 - (i * 100.0 / hLines);
            g2.setColor(ACC_COLOR.brighter());
            String accStr = String.format("%.0f%%", accVal);
            g2.drawString(accStr, padLeft + plotW + 8, y + 3);
        }

        // Draw curves
        synchronized (history) {
            int n = history.size();
            if (n > 1) {
                int[] xPoints = new int[n];
                int[] yLoss = new int[n];
                int[] yAcc = new int[n];

                int effectiveMaxEpoch = Math.max(maxEpoch, history.get(n - 1).epoch);

                for (int i = 0; i < n; i++) {
                    MetricPoint pt = history.get(i);
                    double xFrac = (double) pt.epoch / effectiveMaxEpoch;
                    xPoints[i] = padLeft + (int) (xFrac * plotW);

                    // Loss Y coordinate
                    double lossFrac = (pt.loss - minLoss) / (maxLoss - minLoss + 1e-9);
                    lossFrac = Math.max(0.0, Math.min(1.0, lossFrac));
                    yLoss[i] = padTop + plotH - (int) (lossFrac * plotH);

                    // Accuracy Y coordinate (0% - 100%)
                    double accFrac = pt.accuracy / 100.0;
                    accFrac = Math.max(0.0, Math.min(1.0, accFrac));
                    yAcc[i] = padTop + plotH - (int) (accFrac * plotH);
                }

                // Draw Loss Curve (Orange)
                g2.setColor(LOSS_COLOR);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(xPoints, yLoss, n);

                // Draw Accuracy Curve (Green)
                g2.setColor(ACC_COLOR);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(xPoints, yAcc, n);
            }

            // Top Header: Title and Current Readings
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            if (!history.isEmpty()) {
                MetricPoint last = history.get(n - 1);

                g2.setColor(LOSS_COLOR);
                String lossText = String.format("Loss: %.4f", last.loss);
                g2.drawString(lossText, padLeft, padTop - 12);

                g2.setColor(ACC_COLOR);
                String accText = String.format("Accuracy: %.2f%%", last.accuracy);
                g2.drawString(accText, padLeft + 120, padTop - 12);

                g2.setColor(TEXT_MUTED);
                String epochText = String.format("Epoch: %d / %d", last.epoch, maxEpoch);
                g2.drawString(epochText, padLeft + plotW - 90, padTop - 12);
            } else {
                g2.setColor(TEXT_MUTED);
                g2.drawString("Training Convergence Metrics (Loss & Accuracy)", padLeft, padTop - 12);
            }
        }

        // X-axis label
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.setColor(TEXT_MUTED);
        g2.drawString("Epochs", padLeft + (plotW / 2) - 16, h - 8);
    }
}
