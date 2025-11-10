package main;

import gui.views.LoginView;
import javax.swing.*;

/**
 * Punto di avvio principale dell'applicazione.
 */
public class Main {

    public static void main(String[] args) {
        try {
            // Imposta il Look and Feel nativo del sistema operativo
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Avvia la finestra di Login
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}