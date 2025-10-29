package GUI.Frames;

import Controllers.MainController;
import Model.Bacheca;
import Model.TitoloBacheca;
import Model.ToDo;
import GUI.ColorsConstant;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AddEditToDoFrame extends JFrame {

    private final JTextField titoloField = new JTextField(20);
    private final JTextArea descrizioneArea = new JTextArea(5, 20);
    private final JTextField linkField = new JTextField(20);
    private final JTextField dataField = new JTextField(10);
    private JComboBox<TitoloBacheca> bachecaCombo;

    // Etichette contatori
    private final JLabel titoloCountLabel = new JLabel("0 / 35");
    private final JLabel descCountLabel = new JLabel("0 / 500");

    private final ToDo toEdit;

    public AddEditToDoFrame(MainController ctrl) {
        this(ctrl, null);
    }

    public AddEditToDoFrame(MainController ctrl, ToDo toEdit) {
        super(toEdit == null ? "Nuovo ToDo" : "Modifica ToDo");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        this.toEdit = toEdit;

        // Recupera tutte le bacheche disponibili
        List<TitoloBacheca> disponibili = ctrl.getBachecaController()
                .getAllBacheche().stream()
                .map(Bacheca::getTitolo)
                .toList();

        if (disponibili.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nessuna bacheca disponibile. Crea prima una bacheca.",
                    "Errore",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        // Impostazioni base pannello
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ColorsConstant.PinkFairy);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        // Titolo
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("Titolo *"), gbc);
        gbc.gridx = 1;
        panel.add(titoloField, gbc);

        // Contatore titolo
        y++;
        gbc.gridx = 1; gbc.gridy = y;
        titoloCountLabel.setForeground(Color.GRAY);
        titoloCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        panel.add(titoloCountLabel, gbc);

        // Descrizione
        y++;
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("Descrizione"), gbc);
        gbc.gridx = 1;
        JScrollPane scrollDesc = new JScrollPane(descrizioneArea);
        panel.add(scrollDesc, gbc);

        // Contatore descrizione
        y++;
        gbc.gridx = 1; gbc.gridy = y;
        descCountLabel.setForeground(Color.GRAY);
        descCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        panel.add(descCountLabel, gbc);

        // Data
        y++;
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("Data scadenza (AAAA-MM-GG) *"), gbc);
        gbc.gridx = 1;
        dataField.setToolTipText("Formato: AAAA-MM-GG");
        panel.add(dataField, gbc);

        // Link
        y++;
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("Link (opzionale)"), gbc);
        gbc.gridx = 1;
        panel.add(linkField, gbc);

        // Bacheca
        y++;
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel("Bacheca *"), gbc);
        gbc.gridx = 1;
        bachecaCombo = new JComboBox<>(disponibili.toArray(new TitoloBacheca[0]));
        panel.add(bachecaCombo, gbc);

        // Precompilazione se modifica
        if (toEdit != null) {
            titoloField.setText(toEdit.getTitolo());
            descrizioneArea.setText(toEdit.getDescrizione() != null ? toEdit.getDescrizione() : "");
            linkField.setText(toEdit.getLinkURL() != null ? toEdit.getLinkURL() : "");
            dataField.setText(toEdit.getDataScadenza() != null ? toEdit.getDataScadenza().toString() : "");
            bachecaCombo.setSelectedItem(ctrl.getBachecaController().getAllBacheche().stream()
                    .filter(b -> b.getToDos().contains(toEdit))
                    .map(Bacheca::getTitolo)
                    .findFirst()
                    .orElse(disponibili.get(0)));
        }

        // Bottone Salva
        y++;
        JButton salvaBtn = new JButton(toEdit == null ? "Salva ToDo" : "Modifica ToDo");
        salvaBtn.addActionListener(e -> salvaToDo(ctrl));
        gbc.gridx = 1; gbc.gridy = y + 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(salvaBtn, gbc);

        // Listener per contatori
        addCharCountListeners();

        add(panel);
        pack();
        setVisible(true);
    }

    private void addCharCountListeners() {
        // Aggiorna contatori in tempo reale
        titoloField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                int len = titoloField.getText().length();
                titoloCountLabel.setText(len + " / 35");
                titoloCountLabel.setForeground(len > 35 ? Color.RED : Color.GRAY);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        descrizioneArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                int len = descrizioneArea.getText().length();
                descCountLabel.setText(len + " / 500");
                descCountLabel.setForeground(len > 500 ? Color.RED : Color.GRAY);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });
    }

    private void salvaToDo(MainController ctrl) {
        try {
            String titolo = titoloField.getText().trim();
            String desc = descrizioneArea.getText().trim();

            if (titolo.isEmpty()) throw new IllegalArgumentException("Il titolo è obbligatorio.");
            if (titolo.length() > 35) throw new IllegalArgumentException("Il titolo può contenere al massimo 35 caratteri.");
            if (desc.length() > 500) throw new IllegalArgumentException("La descrizione può contenere al massimo 500 caratteri.");

            LocalDate data;
            try {
                data = LocalDate.parse(dataField.getText().trim());
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Formato data non valido. Usa AAAA-MM-GG.");
            }

            String link = linkField.getText().trim().isEmpty() ? null : linkField.getText().trim();
            TitoloBacheca dest = (TitoloBacheca) bachecaCombo.getSelectedItem();

            if (toEdit == null) {
                // Nuovo ToDo
                ctrl.getToDoController().creaToDo(dest, titolo, data, link, desc, ColorsConstant.PinkFairy);
            } else {
                // Modifica esistente
                toEdit.setTitolo(titolo);
                toEdit.setDescrizione(desc);
                toEdit.setLinkURL(link);
                toEdit.setDataScadenza(data);

                // Se cambia bacheca → sposta
                Bacheca corrente = ctrl.getBachecaController().getAllBacheche().stream()
                        .filter(b -> b.getToDos().contains(toEdit))
                        .findFirst()
                        .orElse(null);

                if (corrente != null && dest != null && !corrente.getTitolo().equals(dest)) {
                    corrente.rimuoviToDo(toEdit);
                    ctrl.getBachecaController().getBacheca(dest).aggiungiToDo(toEdit);
                }

                ctrl.getBachecaController().notifyChange();
            }

            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
}
