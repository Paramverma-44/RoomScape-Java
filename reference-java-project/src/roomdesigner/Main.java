package roomdesigner;

import javax.swing.*;

/** Entry point of the Virtual Interior Room Designer. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            DesignerFrame f = new DesignerFrame();
            f.setVisible(true);
        });
    }
}
