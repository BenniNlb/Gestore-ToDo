package main;

import gui.views.LoginView;
import javax.swing.*;

/**
 * Classe principale che funge da punto di ingresso (Entry Point) per l'applicazione "Gestore ToDo".
 * <p>
 * Questa classe ha la responsabilità di inizializzare l'ambiente grafico e avviare il flusso
 * dell'applicazione. Le sue operazioni principali sono:
 * <ul>
 * <li>Configurare il Look and Feel nativo del sistema operativo per una migliore integrazione UI.</li>
 * <li>Avviare la finestra di login ({@link LoginView}) all'interno dell'Event Dispatch Thread (EDT) di Swing.</li>
 * </ul>
 */
public class Main {

    /**
     * Costruttore privato per nascondere quello pubblico implicito.
     * <p>
     * Questa classe contiene solo un metodo statico main e non deve essere istanziata.
     */
    private Main() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Metodo main standard di Java.
     * <p>
     * Viene invocato dalla JVM all'avvio del programma. Gestisce la configurazione iniziale
     * dell'interfaccia grafica e lancia la prima schermata visibile all'utente.
     *
     * @param args Argomenti da riga di comando (attualmente non utilizzati).
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}