import javax.swing.*;

/** Starts the Personal Finance Goal Planner. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
