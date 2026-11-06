import ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application Entry Point for the Spiral Neural Network Interface Engine.
 * 
 * Jaypee Institute of Information Technology, Noida
 * B.Tech III-Semester Project
 * Course: OOPS Using Java Lab (Course Code: 24B15CS215)
 * Submitted to: Dr. K.Rajalakshmi
 * 
 * Submitted By:
 * - Aditya Saha    (2501030308)
 * - Aviral Jain    (2501030329)
 * - Divyansh Mittal (2501030335)
 */
public class Main {
    public static void main(String[] args) {
        // Set cross-platform look and feel with dark accents
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equalsIgnoreCase(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Default fallback
            }
        }

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
