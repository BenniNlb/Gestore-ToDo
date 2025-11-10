package gui.dialogs;

import controllers.MainController;
import model.TitoloBacheca;
import util.ColorsConstant; // Importa i colori

import javax.swing.*;
import java.awt.*;

public class AddBachecaDialog extends JDialog {

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

        // --- Componenti ---
        JComboBox<TitoloBacheca> combo = new JComboBox<>(disponibili.toArray(new TitoloBacheca[0]));

        // NUOVO: Area di testo per la descrizione
        JTextArea descArea = new JTextArea(4, 25);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JButton addBtn = new JButton("Aggiungi");

        addBtn.addActionListener(e -> {
            TitoloBacheca sel = (TitoloBacheca) combo.getSelectedItem();
            // NUOVO: Leggi la descrizione
            String descrizione = descArea.getText().trim();

            if (sel != null) {
                // MODIFICATO: Passa anche la descrizione al controller
                ctrl.getBachecaController().aggiungiBacheca(sel, descrizione);
                dispose();
            }
        });

        // --- Layout ---
        JPanel panel = new JPanel(new GridBagLayout()); // Modificato in GridBagLayout
        panel.setBackground(ColorsConstant.LIGHT_GREY); // Sfondo grigio
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Riga 0: Titolo
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Seleziona Bacheca:"), gbc);

        gbc.gridx = 1;
        panel.add(combo, gbc);

        // Riga 1: Descrizione
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Allinea l'etichetta in alto
        panel.add(new JLabel("Descrizione (opz.):"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST; // Reset
        panel.add(new JScrollPane(descArea), gbc);

        // Riga 2: Pulsante
        gbc.gridy = 2;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE; // Non riempire
        gbc.anchor = GridBagConstraints.EAST; // Allinea a destra
        panel.add(addBtn, gbc);

        getContentPane().add(panel);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}