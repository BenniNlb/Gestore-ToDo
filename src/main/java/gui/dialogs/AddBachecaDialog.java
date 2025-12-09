package gui.dialogs;

import controllers.MainController;
import model.TitoloBacheca;
import util.ColorsConstant;

import javax.swing.*;
import java.awt.*;

/**
 * Finestra di dialogo modale per la creazione di una nuova bacheca.
 * <p>
 * Questa classe permette all'utente di selezionare uno dei titoli di bacheca
 * ancora disponibili (tra Università, Lavoro, Tempo Libero) e di associare una
 * descrizione opzionale. Interagisce con il {@link MainController} per persistere
 * la nuova bacheca nel sistema.
 */
public class AddBachecaDialog extends JDialog {

    /**
     * Costruisce e inizializza la finestra di dialogo per l'aggiunta di una bacheca.
     * <p>
     * Il costruttore verifica preliminarmente se esistono titoli disponibili.
     * Se l'utente ha già creato tutte le bacheche possibili, viene mostrato un
     * messaggio di avviso e il dialog viene chiuso immediatamente.
     *
     * @param parent      La finestra proprietaria (owner) del dialog, per centrare la finestra.
     * @param ctrl        Il controller principale utilizzato per eseguire l'azione di aggiunta.
     * @param disponibili La lista dei {@link TitoloBacheca} non ancora utilizzati dall'utente.
     */
    public AddBachecaDialog(Window parent, MainController ctrl, java.util.List<TitoloBacheca> disponibili) {
        super(parent, "Nuova Bacheca", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        if (disponibili == null || disponibili.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Nessuna bacheca disponibile da aggiungere.",
                    "Limite raggiunto",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        JComboBox<TitoloBacheca> combo = new JComboBox<>(disponibili.toArray(new TitoloBacheca[0]));

        JTextArea descArea = new JTextArea(4, 25);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JButton addBtn = new JButton("Aggiungi");

        addBtn.addActionListener(e -> {
            TitoloBacheca sel = (TitoloBacheca) combo.getSelectedItem();
            String descrizione = descArea.getText().trim();

            if (sel != null) {
                ctrl.getBachecaController().aggiungiBacheca(sel, descrizione);
                dispose();
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ColorsConstant.LIGHT_GREY);
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Seleziona Bacheca:"), gbc);

        gbc.gridx = 1;
        panel.add(combo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Descrizione (opz.):"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JScrollPane(descArea), gbc);

        gbc.gridy = 2;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(addBtn, gbc);

        getContentPane().add(panel);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}