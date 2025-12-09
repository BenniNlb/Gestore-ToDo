package gui.dialogs.tododialog;

import controllers.MainController;
import model.ToDo;
import model.TitoloBacheca;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Finestra di dialogo modale per la gestione completa di un'attività (ToDo).
 * <p>
 * Questa classe funge da contenitore principale e decide dinamicamente quale
 * pannello visualizzare in base al contesto:
 * <ul>
 * <li><b>Vista Dettagli:</b> Per creare un nuovo ToDo o modificarne i dati (titolo, descrizione, data, ecc.).
 * Viene delegata alla classe {@code ToDoDetailsView}.</li>
 * <li><b>Vista Condivisioni:</b> Per gestire gli utenti con cui il ToDo è condiviso e i relativi permessi.
 * Viene delegata alla classe {@code ToDoSharingView}.</li>
 * </ul>
 */
public class AddEditToDoDialog extends JDialog {

    /**
     * Riferimento al controller principale per l'accesso ai dati e alla logica di business.
     */
    private final MainController ctrl;

    /**
     * L'oggetto ToDo oggetto della modifica o condivisione.
     * Se è {@code null}, la dialog si predispone per la creazione di un nuovo ToDo.
     */
    private final ToDo toEdit;

    /**
     * Costruttore di convenienza per la modifica dei dettagli di un ToDo esistente.
     * <p>
     * Richiama il costruttore principale impostando la visualizzazione dei dettagli come predefinita.
     *
     * @param parent   La finestra proprietaria (owner) del dialog.
     * @param ctrl     Il controller principale.
     * @param toEdit   Il ToDo da modificare.
     */
    public AddEditToDoDialog(Window parent, MainController ctrl, ToDo toEdit) {
        this(parent, ctrl, toEdit, null, true);
    }

    /**
     * Costruttore di convenienza che permette di scegliere la vista iniziale.
     *
     * @param parent          La finestra proprietaria (owner) del dialog.
     * @param ctrl            Il controller principale.
     * @param toEdit          Il ToDo da gestire.
     * @param showDetailsView {@code true} per mostrare i dettagli (modifica),
     * {@code false} per mostrare le condivisioni.
     */
    public AddEditToDoDialog(Window parent, MainController ctrl, ToDo toEdit, boolean showDetailsView) {
        this(parent, ctrl, toEdit, null, showDetailsView);
    }

    /**
     * Costruttore principale della finestra di dialogo.
     * <p>
     * Esegue le seguenti operazioni:
     * <ol>
     * <li>Verifica che esistano bacheche disponibili; in caso contrario mostra un errore e chiude.</li>
     * <li>Determina se mostrare la vista di dettaglio o quella di condivisione.</li>
     * <li>Imposta il titolo della finestra in base all'operazione (Nuovo, Modifica, Condividi).</li>
     * <li>Istanzia e aggiunge il pannello specifico ({@code ToDoDetailsView} o {@code ToDoSharingView}).</li>
     * </ol>
     *
     * @param parent          La finestra proprietaria (owner) del dialog.
     * @param ctrl            Il controller principale.
     * @param toEdit          Il ToDo da gestire (o {@code null} per creazione).
     * @param defaultBacheca  La bacheca da preselezionare nel form (opzionale, usata in creazione).
     * @param showDetailsView {@code true} per visualizzare il form dettagli,
     * {@code false} per visualizzare il pannello condivisioni.
     */
    public AddEditToDoDialog(Window parent, MainController ctrl, ToDo toEdit, TitoloBacheca defaultBacheca, boolean showDetailsView) {
        super(parent, "", ModalityType.APPLICATION_MODAL);
        this.ctrl = ctrl;
        this.toEdit = toEdit;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        List<TitoloBacheca> disponibili = ctrl.getBachecaController()
                .getAllBacheche().stream().map(b -> b.getTitolo()).toList();

        if (disponibili.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Nessuna bacheca disponibile. Crea prima una bacheca.",
                    "Errore",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        // Se toEdit è null, stiamo creando, quindi forziamo la vista dettagli
        boolean showDetails = showDetailsView || toEdit == null;

        JPanel panelToShow;
        if (!showDetails) {
            setTitle("Gestisci Condivisioni");
            // Istanzia la vista separata per le condivisioni
            panelToShow = new ToDoSharingView(ctrl, toEdit);
        } else {
            setTitle(toEdit == null ? "Nuovo ToDo" : "Modifica ToDo");
            // Istanzia la vista separata per i dettagli
            panelToShow = new ToDoDetailsView(ctrl, toEdit, disponibili, defaultBacheca);
        }

        getContentPane().add(panelToShow);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}