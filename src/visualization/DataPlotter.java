package visualization;

import data.DataPoint;
import data.SpiralDataset;
import neural.NeuralNetwork;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * High-performance 2D Graphics2D canvas for rendering spiral datasets,
 * dynamic neural decision boundary manifolds, and interactive probe queries.
 * 
 * @author Aditya Saha (2501030308), Aviral Jain (2501030329), Divyansh Mittal (2501030335)
 * Course: OOPS Using Java Lab (24B15CS215)
 */
public class DataPlotter extends JPanel {

    // Distinct modern palette for up to 8 classes
    public static final Color[] CLASS_COLORS = {
        new Color(255, 71, 87),   // 0: Coral Red
        new Color(30, 144, 255),  // 1: Dodger Blue
        new Color(46, 213, 115),  // 2: Emerald Green
        new Color(255, 165, 2),   // 3: Golden Amber
        new Color(155, 89, 182),  // 4: Amethyst Purple
        new Color(0, 210, 211),   // 5: Teal Cyan
        new Color(255, 107, 129), // 6: Wild Strawberry
        new Color(243, 156, 18)   // 7: Sun Yellow
    };

    private SpiralDataset dataset;
    private NeuralNetwork network;

    // View settings
    private final double xMin = -1.25;
    private final double xMax = 1.25;
    private final double yMin = -1.25;
    private final double yMax = 1.25;

    private boolean showDecisionBoundary = true;
    private boolean showGrid = true;
    private boolean showConfidence = true;
    private int gridResolution = 90; // resolution of decision boundary mesh

    // Decision boundary cached image
    private BufferedImage boundaryImage;
    private boolean boundaryNeedsRepaint = true;

    // Interactive probe
    private double probeX = Double.NaN;
    private double probeY = Double.NaN;
    private double[] probeProbabilities = null;

    public DataPlotter(SpiralDataset dataset, NeuralNetwork network) {
        this.dataset = dataset;
        this.network = network;
        setPreferredSize(new Dimension(550, 550));
        setBackground(new Color(24, 26, 32));

        // Enable tooltips
        ToolTipManager.sharedInstance().registerComponent(this);

        setupMouseListeners();
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                probeAtScreen(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                probeX = Double.NaN;
                probeY = Double.NaN;
                probeProbabilities = null;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                probeAtScreen(e.getX(), e.getY());
            }
        });
    }

    private void probeAtScreen(int screenX, int screenY) {
        double mathX = screenToMathX(screenX);
        double mathY = screenToMathY(screenY);

        if (mathX >= xMin && mathX <= xMax && mathY >= yMin && mathY <= yMax) {
            probeX = mathX;
            probeY = mathY;
            if (network != null) {
                probeProbabilities = network.forward(new double[]{mathX, mathY});
            }
            repaint();
        }
    }

    public void setDataset(SpiralDataset dataset) {
        this.dataset = dataset;
        invalidateBoundary();
        repaint();
    }

    public void setNetwork(NeuralNetwork network) {
        this.network = network;
        invalidateBoundary();
        repaint();
    }

    public void invalidateBoundary() {
        this.boundaryNeedsRepaint = true;
    }

    public void setShowDecisionBoundary(boolean show) {
        this.showDecisionBoundary = show;
        repaint();
    }

    public boolean isShowDecisionBoundary() {
        return showDecisionBoundary;
    }

    public void setShowGrid(boolean show) {
        this.showGrid = show;
        repaint();
    }

    public void setGridResolution(int resolution) {
        this.gridResolution = Math.max(30, Math.min(200, resolution));
        invalidateBoundary();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int width = getWidth();
        int height = getHeight();

        // 1. Draw Decision Boundary Heatmap (if enabled and network available)
        if (showDecisionBoundary && network != null) {
            renderDecisionBoundary(g2, width, height);
        }

        // 2. Draw Coordinate Grid & Axes
        if (showGrid) {
            drawGrid(g2, width, height);
        }

        // 3. Draw Dataset Points
        if (dataset != null) {
            drawDataPoints(g2, width, height);
        }

        // 4. Draw Interactive Probe Overlay (if active)
        if (!Double.isNaN(probeX) && probeProbabilities != null) {
            drawProbeHUD(g2, width, height);
        }

        // 5. Draw Legend
        drawLegend(g2, width, height);
    }

    /**
     * Computes and draws the decision boundary heatmap grid.
     */
    private void renderDecisionBoundary(Graphics2D g2, int width, int height) {
        if (boundaryNeedsRepaint || boundaryImage == null || 
            boundaryImage.getWidth() != gridResolution || boundaryImage.getHeight() != gridResolution) {
            
            boundaryImage = new BufferedImage(gridResolution, gridResolution, BufferedImage.TYPE_INT_ARGB);
            double[] input = new double[2];

            for (int gy = 0; gy < gridResolution; gy++) {
                double my = yMax - ((double) gy / (gridResolution - 1)) * (yMax - yMin);
                input[1] = my;

                for (int gx = 0; gx < gridResolution; gx++) {
                    double mx = xMin + ((double) gx / (gridResolution - 1)) * (xMax - xMin);
                    input[0] = mx;

                    double[] probs = network.forward(input);
                    int bestClass = 0;
                    double maxP = probs[0];
                    for (int c = 1; c < probs.length; c++) {
                        if (probs[c] > maxP) {
                            maxP = probs[c];
                            bestClass = c;
                        }
                    }

                    Color baseColor = CLASS_COLORS[bestClass % CLASS_COLORS.length];
                    // Alpha scale based on prediction confidence
                    int alpha = showConfidence ? (int) (40 + maxP * 90) : 75;
                    alpha = Math.max(30, Math.min(180, alpha));

                    Color blended = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
                    boundaryImage.setRGB(gx, gy, blended.getRGB());
                }
            }
            boundaryNeedsRepaint = false;
        }

        if (boundaryImage != null) {
            g2.drawImage(boundaryImage, 0, 0, width, height, null);
        }
    }

    /**
     * Draws subtle background coordinate grid and axis lines.
     */
    private void drawGrid(Graphics2D g2, int width, int height) {
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(255, 255, 255, 22));
        g2.setStroke(new BasicStroke(1.0f));

        // Sub-grid lines
        double[] ticks = {-1.0, -0.5, 0.0, 0.5, 1.0};
        for (double t : ticks) {
            int sx = mathToScreenX(t, width);
            int sy = mathToScreenY(t, height);

            g2.setColor(new Color(255, 255, 255, 20));
            g2.drawLine(sx, 0, sx, height);
            g2.drawLine(0, sy, width, sy);

            g2.setColor(new Color(160, 165, 180, 130));
            if (t != 0.0) {
                g2.drawString(String.format("%.1f", t), sx + 3, mathToScreenY(0.0, height) - 4);
                g2.drawString(String.format("%.1f", t), mathToScreenX(0.0, width) + 4, sy - 3);
            }
        }

        // Primary axes X = 0 and Y = 0
        int originX = mathToScreenX(0.0, width);
        int originY = mathToScreenY(0.0, height);
        g2.setColor(new Color(255, 255, 255, 70));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(originX, 0, originX, height);
        g2.drawLine(0, originY, width, originY);

        // Axis labels
        g2.setColor(new Color(200, 210, 230, 200));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.drawString("X₁", width - 20, originY - 6);
        g2.drawString("X₂", originX + 8, 16);
    }

    /**
     * Renders sample points with class colors, borders, and misclassification highlights.
     */
    private void drawDataPoints(Graphics2D g2, int width, int height) {
        List<DataPoint> points = dataset.getDataPoints();
        int radius = 5;
        int diameter = radius * 2;

        for (DataPoint pt : points) {
            int sx = mathToScreenX(pt.getX(), width);
            int sy = mathToScreenY(pt.getY(), height);

            Color classColor = CLASS_COLORS[pt.getLabel() % CLASS_COLORS.length];

            // Fill point
            g2.setColor(classColor);
            g2.fillOval(sx - radius, sy - radius, diameter, diameter);

            // Check if misclassified
            if (pt.getPredictedLabel() >= 0 && !pt.isCorrect()) {
                // Draw alert indicator: white ring with outer red notch
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(sx - radius, sy - radius, diameter, diameter);

                g2.setColor(new Color(255, 50, 50, 220));
                g2.drawOval(sx - radius - 2, sy - radius - 2, diameter + 4, diameter + 4);
            } else {
                // Subtle dark outline for high contrast
                g2.setColor(new Color(15, 15, 20, 180));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawOval(sx - radius, sy - radius, diameter, diameter);
            }
        }
    }

    /**
     * Draws interactive mouse probe HUD showing live neural activations.
     */
    private void drawProbeHUD(Graphics2D g2, int width, int height) {
        int sx = mathToScreenX(probeX, width);
        int sy = mathToScreenY(probeY, height);

        // Crosshair on probed location
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4, 4}, 0.0f));
        g2.drawLine(sx - 12, sy, sx + 12, sy);
        g2.drawLine(sx, sy - 12, sx, sy + 12);
        g2.drawOval(sx - 6, sy - 6, 12, 12);

        // HUD panel box
        int boxW = 160;
        int boxH = 30 + (probeProbabilities.length * 16);
        int boxX = (sx + 15 + boxW > width) ? sx - boxW - 15 : sx + 15;
        int boxY = Math.max(10, Math.min(height - boxH - 10, sy - 20));

        g2.setColor(new Color(20, 22, 28, 225));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g2.setColor(new Color(80, 120, 200, 180));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // Header
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.setColor(Color.WHITE);
        g2.drawString(String.format("Probe (%.2f, %.2f)", probeX, probeY), boxX + 8, boxY + 16);

        // Probabilities
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int c = 0; c < probeProbabilities.length; c++) {
            Color clr = CLASS_COLORS[c % CLASS_COLORS.length];
            int lineY = boxY + 32 + (c * 16);

            g2.setColor(clr);
            g2.fillRect(boxX + 8, lineY - 8, 8, 8);

            g2.setColor(new Color(230, 230, 235));
            double p = probeProbabilities[c];
            g2.drawString(String.format("Class %d: %5.1f%%", c, p * 100.0), boxX + 22, lineY);
        }
    }

    /**
     * Draws legend in top-left or bottom-left corner.
     */
    private void drawLegend(Graphics2D g2, int width, int height) {
        int numClasses = dataset != null ? dataset.getNumClasses() : 3;
        int legW = 110;
        int legH = 20 + (numClasses * 18);
        int legX = 12;
        int legY = 12;

        g2.setColor(new Color(18, 20, 26, 180));
        g2.fillRoundRect(legX, legY, legW, legH, 8, 8);
        g2.setColor(new Color(60, 65, 80, 150));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(legX, legY, legW, legH, 8, 8);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.setColor(new Color(200, 205, 220));
        g2.drawString("CLASSES", legX + 8, legY + 14);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int c = 0; c < numClasses; c++) {
            int yPos = legY + 30 + (c * 18);
            Color clr = CLASS_COLORS[c % CLASS_COLORS.length];

            g2.setColor(clr);
            g2.fillOval(legX + 8, yPos - 8, 8, 8);

            g2.setColor(new Color(220, 225, 235));
            g2.drawString("Class " + c, legX + 22, yPos);
        }
    }

    // Coordinate conversions
    private int mathToScreenX(double mx, int width) {
        return (int) (((mx - xMin) / (xMax - xMin)) * width);
    }

    private int mathToScreenY(double my, int height) {
        return (int) (((yMax - my) / (yMax - yMin)) * height);
    }

    private double screenToMathX(int sx) {
        return xMin + ((double) sx / getWidth()) * (xMax - xMin);
    }

    private double screenToMathY(int sy) {
        return yMax - ((double) sy / getHeight()) * (yMax - yMin);
    }
}
