package GUI.Cards;

import Controllers.MainController;
import GUI.ColorsConstant;
import Model.Bacheca;
import Model.TitoloBacheca;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException; // Import per gestire l'errore di formato data
import java.util.List;
import java.util.stream.Collectors; // Import per Collectors

public class AddFrame extends JFrame {

    private final JTextField titoloField = new JTextField(20);
    private final JTextArea descrizioneArea = new JTextArea(5, 20);
    private final JTextField linkField = new JTextField(20);
    private final JTextField dataField = new JTextField(10);
    private JComboBox<TitoloBacheca> bachecaCombo;

    /**
     * Costruttore polimorfico per creare un ToDo o una Bacheca.
     * @param ctrl Il MainController.
     * @param isToDo Se true, crea un form per ToDo; se false, per Bacheca.
     * @param optionalDisponibili Array di liste di TitoloBacheca disponibili (solo per Bacheca).
     */
    public AddFrame(MainController ctrl, boolean isToDo, List<TitoloBacheca>... optionalDisponibili) {
        super(isToDo ? "Nuovo ToDo" : "Nuova Bacheca"); // Il titolo del frame cambia dinamicamente
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        if (!isToDo) {
            // Logica per aggiungere una bacheca
            List<TitoloBacheca> disponibili = optionalDisponibili.length > 0
                    ? optionalDisponibili[0]
                    : List.of(); // Se non passate, lista vuota

            if (disponibili.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Non ci sono bacheche disponibili per l'aggiunta.",
                        "Limite raggiunto",
                        JOptionPane.WARNING_MESSAGE);
                dispose(); // Chiudi la finestra se non ci sono bacheche disponibili
                return;
            }
            initFormBacheca(ctrl, disponibili);
        } else {
            // Logica per aggiungere un ToDo
            initFormToDo(ctrl);
        }
    }

    private void initFormBacheca(MainController ctrl, List<TitoloBacheca> disponibili) {
        // La lista 'disponibili' viene passata dal CardChoice
        bachecaCombo = new JComboBox<>(disponibili.toArray(new TitoloBacheca[0]));

        JButton aggiungiBtn = new JButton("Aggiungi Bacheca");
        aggiungiBtn.addActionListener(e -> {
            try {
                TitoloBacheca sel = (TitoloBacheca) bachecaCombo.getSelectedItem();
                if (sel == null) throw new IllegalArgumentException("Seleziona una bacheca.");

                // Aggiungi la bacheca senza descrizione (o puoi aggiungere un campo descrizione)
                ctrl.getBachecaController().aggiungiBacheca(sel, "");
                dispose(); // Chiudi la finestra dopo l'aggiunta
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Errore aggiunta Bacheca",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Seleziona Bacheca:"), BorderLayout.NORTH);
        panel.add(bachecaCombo, BorderLayout.CENTER);
        panel.add(aggiungiBtn, BorderLayout.SOUTH);

        add(panel);
        pack(); // Adatta la finestra ai componenti
        setVisible(true);
    }

    private void initFormToDo(MainController ctrl) {
        // Recupero dinamico delle bacheche utente (esclude SCADENZE_DI_OGGI)
        // Questo codice era già corretto, l'ho solo mantenuto qui.
        List<TitoloBacheca> disponibili = ctrl.getBachecaController()
                .getAllBacheche().stream()
                .map(Bacheca::getTitolo)
                .filter(t -> t != TitoloBacheca.SCADENZE_DI_OGGI)
                .collect(Collectors.toList()); // Usare collect(Collectors.toList()) invece di toList() per compatibilità

        // Se non c'è nessuna bacheca dove inserire il ToDo, chiudi.
        // Questo controllo è vitale per evitare NullPointerException se non ci sono bacheche.
        if (disponibili.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nessuna bacheca disponibile per l'inserimento del ToDo. Crea prima una bacheca.",
                    "Errore",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ColorsConstant.PinkFairy);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        // Titolo *
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel("Titolo *"), gbc);
        gbc.gridx = 1;
        panel.add(titoloField, gbc);

        // Descrizione
        y++;
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel("Descrizione"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descrizioneArea), gbc);

        // Data Scadenza * (tooltip formato)
        y++;
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel("Data scadenza (AAAA-MM-GG) *"), gbc); // Miglioro il testo per chiarezza
        gbc.gridx = 1;
        dataField.setToolTipText("Formato: AAAA-MM-GG");
        panel.add(dataField, gbc);

        // Link (opzionale)
        y++;
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel("Link (opzionale)"), gbc);
        gbc.gridx = 1;
        panel.add(linkField, gbc);

        // Selezione bacheca di destinazione *
        y++;
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel("Bacheca *"), gbc);
        gbc.gridx = 1;
        bachecaCombo = new JComboBox<>(disponibili.toArray(new TitoloBacheca[0]));
        panel.add(bachecaCombo, gbc);

        // Pulsante Salva ToDo
        y++;
        JButton salvaBtn = new JButton("Salva ToDo");
        salvaBtn.addActionListener(e -> {
            try {
                String titolo = titoloField.getText().trim();
                if (titolo.isEmpty()) {
                    throw new IllegalArgumentException("Il titolo è obbligatorio.");
                }

                LocalDate data;
                try {
                    data = LocalDate.parse(dataField.getText().trim());
                } catch (DateTimeParseException dtpe) {
                    throw new IllegalArgumentException("Formato data non valido. Usa AAAA-MM-GG.");
                }

                String desc = descrizioneArea.getText().trim();
                String link = linkField.getText().trim();
                if (link.isEmpty()) link = null; // Imposta a null se vuoto

                TitoloBacheca dest = (TitoloBacheca) bachecaCombo.getSelectedItem();
                if (dest == null) {
                    throw new IllegalArgumentException("Seleziona una bacheca di destinazione.");
                }

                ctrl.getToDoController().creaToDo(dest, titolo, data, link, desc, ColorsConstant.PinkFairy);
                dispose(); // Chiudi la finestra dopo l'aggiunta
            } catch (IllegalArgumentException ex) {
                // Per messaggi d'errore specifici generati dal codice
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Errore inserimento ToDo",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                // Per altri errori imprevisti
                JOptionPane.showMessageDialog(this,
                        "Si è verificato un errore inatteso: " + ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 1;
        gbc.gridy = y + 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(salvaBtn, gbc);

        add(panel);
        pack(); // Adatta la finestra ai componenti
        setVisible(true);
    }
}