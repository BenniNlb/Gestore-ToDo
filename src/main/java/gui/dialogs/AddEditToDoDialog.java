package gui.dialogs;

import controllers.MainController;
import model.Bacheca;
import model.PermessoCondivisione;
import model.TitoloBacheca;
import model.ToDo;
import model.Utente;
import util.ColorsConstant;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddEditToDoDialog extends JDialog {

    private final MainController ctrl;
    private final ToDo toEdit;

    // --- MODIFICA: Rimosso l'enum ViewMode ---

    // Campi per la nuova logica
    private ImageIcon selectedImage;
    private String selectedImageName;
    private JLabel titoloCountLabel;
    private JLabel descCountLabel;
    private JLabel selectedImageLabel;
    private JButton removeImageBtn;

    // --- MODIFICA: Limiti caratteri aumentati ---
    private static final int MAX_TITOLO_CHARS = 35; // Era 30
    private static final int MAX_DESC_CHARS = 350; // Era 250
    // --- FINE MODIFICA ---

    private static final long MAX_IMAGE_BYTES = 2 * 1024 * 1024; // 2MB
    private static final int MAX_FILENAME_CHARS = 25;
    private static final int MAX_IMAGE_WIDTH = 400;
    private static final int MAX_IMAGE_HEIGHT = 400;

    // --- Componenti per la Condivisione ---
    private DefaultListModel<Map.Entry<Utente, PermessoCondivisione>> sharedListModel;
    private JList<Map.Entry<Utente, PermessoCondivisione>> sharedList;
    private DefaultListModel<Utente> searchListModel;
    private JList<Utente> searchList;
    private JTextField searchField;
    private JComboBox<PermessoCondivisione> permessoCombo;
    // --- FINE ---

    /**
     * Costruttore vecchio (per retrocompatibilità, anche se non usato)
     */
    public AddEditToDoDialog(Window parent, MainController ctrl, ToDo toEdit) {
        this(parent, ctrl, toEdit, null, true);
    }

    /**
     * Costruttore per Modifica (da ToDoCard)
     * --- MODIFICA: Sostituito ViewMode con boolean ---
     */
    public AddEditToDoDialog(Window parent, MainController ctrl, ToDo toEdit, boolean showDetailsView) {
        this(parent, ctrl, toEdit, null, showDetailsView);
    }

    /**
     * Costruttore per Nuovo ToDo (da BachecaPanel)
     * --- MODIFICA: Sostituito ViewMode con boolean ---
     */
    public AddEditToDoDialog(Window parent, MainController ctrl, ToDo toEdit, TitoloBacheca defaultBacheca, boolean showDetailsView) {
        super(parent, "", ModalityType.APPLICATION_MODAL); // Titolo impostato dopo
        this.ctrl = ctrl;
        this.toEdit = toEdit;
        this.selectedImage = null;
        this.selectedImageName = null;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        List<TitoloBacheca> disponibili = ctrl.getBachecaController()
                .getAllBacheche().stream().map(Bacheca::getTitolo).toList();

        if (disponibili.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Nessuna bacheca disponibile. Crea prima una bacheca.",
                    "Errore",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        // --- MODIFICA: Logica basata sul boolean ---
        boolean showDetails = showDetailsView;
        if (toEdit == null) {
            showDetails = true; // Se è un nuovo ToDo, mostra sempre i dettagli
        }

        JPanel panelToShow;

        if (!showDetails) { // Se showDetails è false, mostra condivisioni
            setTitle("Gestisci Condivisioni");
            panelToShow = createSharingPanel();
            loadSharedUsers(); // Popola la lista
        } else {
            // Default è DETTAGLI
            setTitle(toEdit == null ? "Nuovo ToDo" : "Modifica ToDo");
            panelToShow = createDetailsPanel(disponibili, defaultBacheca);
        }
        // --- FINE MODIFICA ---

        getContentPane().add(panelToShow);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Crea il pannello "Dettagli" (il form originale).
     */
    private JPanel createDetailsPanel(List<TitoloBacheca> disponibili, TitoloBacheca defaultBacheca) {
        // Componenti
        JTextField titoloField = new JTextField(30);
        titoloCountLabel = new JLabel("0 / " + MAX_TITOLO_CHARS);
        titoloCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

        JTextArea descrizioneArea = new JTextArea(5, 30);
        descrizioneArea.setLineWrap(true);
        descrizioneArea.setWrapStyleWord(true);
        descCountLabel = new JLabel("0 / " + MAX_DESC_CHARS);
        descCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

        JTextField dataField = new JTextField(12);

        DefaultListModel<String> linksModel = new DefaultListModel<>();
        JList<String> linksList = new JList<>(linksModel);
        JTextField linkInput = new JTextField(20);

        final Color[] selectedColor = { Color.WHITE };

        JButton colorBtn = new JButton("Scegli Colore");
        JPanel colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(24, 24));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JButton chooseImgBtn = new JButton("Scegli immagine...");
        selectedImageLabel = new JLabel("Nessuna immagine.");
        removeImageBtn = new JButton("x");
        removeImageBtn.setToolTipText("Rimuovi immagine");
        removeImageBtn.setVisible(false);

        JComboBox<TitoloBacheca> bachecaCombo = new JComboBox<>(disponibili.toArray(new TitoloBacheca[0]));

        // Se modifica
        if (toEdit != null) {
            titoloField.setText(toEdit.getTitolo());
            descrizioneArea.setText(toEdit.getDescrizione() != null ? toEdit.getDescrizione() : "");
            dataField.setText(toEdit.getDataScadenza() != null ? toEdit.getDataScadenza().toString() : "");

            if (toEdit.getLinkURLs() != null) {
                for (String l : toEdit.getLinkURLs()) linksModel.addElement(l);
            }

            if (toEdit.getImmagine() != null) {
                this.selectedImage = toEdit.getImmagine();
                this.selectedImageName = "Immagine caricata";
                selectedImageLabel.setText(troncaTesto(this.selectedImageName, MAX_FILENAME_CHARS));
                removeImageBtn.setVisible(true);
            }

            if (toEdit.getColoreSfondo() != null) {
                selectedColor[0] = toEdit.getColoreSfondo();
            }

            bachecaCombo.setSelectedItem(ctrl.getBachecaController().getAllBacheche().stream()
                    .filter(b -> b.getToDos().contains(toEdit))
                    .map(Bacheca::getTitolo)
                    .findFirst()
                    .orElse(disponibili.get(0)));

        } else if (defaultBacheca != null) {
            bachecaCombo.setSelectedItem(defaultBacheca);
        }

        colorPreview.setBackground(selectedColor[0]);

        // Layout
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(ColorsConstant.LIGHT_GREY);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        // --- Titolo con contatore
        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Titolo *"), gbc);
        gbc.gridx = 1;
        JPanel titoloPanel = new JPanel(new BorderLayout(0,2));
        titoloPanel.setOpaque(false);
        titoloPanel.add(titoloField, BorderLayout.CENTER);

        JPanel titoloCountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        titoloCountPanel.setOpaque(false);
        titoloCountPanel.add(titoloCountLabel);

        titoloPanel.add(titoloCountPanel, BorderLayout.SOUTH);
        main.add(titoloPanel, gbc);

        // --- Descrizione con contatore
        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.NORTHWEST;
        main.add(new JLabel("Descrizione"), gbc);
        gbc.anchor = GridBagConstraints.WEST; // Reset
        gbc.gridx = 1;
        JPanel descPanel = new JPanel(new BorderLayout(0,2));
        descPanel.setOpaque(false);
        descPanel.add(new JScrollPane(descrizioneArea), BorderLayout.CENTER);

        JPanel descCountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        descCountPanel.setOpaque(false);
        descCountPanel.add(descCountLabel);

        descPanel.add(descCountPanel, BorderLayout.SOUTH);
        main.add(descPanel, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Data (AAAA-MM-GG) *"), gbc);
        gbc.gridx = 1; main.add(dataField, gbc);

        // --- Link
        y++;
        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Link (opzionali)"), gbc);
        gbc.gridx = 1;
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        linkRow.setOpaque(false);
        JButton addLinkBtn = new JButton("Aggiungi");
        JButton removeLinkBtn = new JButton("Rimuovi");
        linkRow.add(linkInput);
        linkRow.add(addLinkBtn);
        linkRow.add(removeLinkBtn);
        main.add(linkRow, gbc);

        y++;
        gbc.gridx = 1; gbc.gridy = y;
        JScrollPane linksScroll = new JScrollPane(linksList);
        linksScroll.setPreferredSize(new Dimension(320, 80));
        main.add(linksScroll, gbc);

        addLinkBtn.addActionListener(e -> {
            String txt = linkInput.getText().trim();
            if (!txt.isEmpty()) {
                linksModel.addElement(txt);
                linkInput.setText("");
            }
        });
        removeLinkBtn.addActionListener(e -> {
            int sel = linksList.getSelectedIndex();
            if (sel >= 0) linksModel.remove(sel);
        });

        // --- Immagine (Nuova UI)
        y++;
        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Immagine (opzionale)"), gbc);
        gbc.gridx = 1;
        JPanel imgRow = new JPanel(new BorderLayout(6,6));
        imgRow.setOpaque(false);
        imgRow.add(chooseImgBtn, BorderLayout.WEST);
        selectedImageLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        imgRow.add(selectedImageLabel, BorderLayout.CENTER);
        imgRow.add(removeImageBtn, BorderLayout.EAST);
        main.add(imgRow, gbc);

        chooseImgBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Immagini (max 2MB)", "jpg","jpeg","png","gif"));
            int ret = fc.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    File f = fc.getSelectedFile();

                    if (f.length() > MAX_IMAGE_BYTES) {
                        throw new Exception("File troppo grande. Limite: 2MB.");
                    }

                    ImageIcon raw = new ImageIcon(f.getAbsolutePath());

                    int w = raw.getIconWidth();
                    int h = raw.getIconHeight();
                    ImageIcon scaled;

                    if (w <= MAX_IMAGE_WIDTH && h <= MAX_IMAGE_HEIGHT) {
                        scaled = raw;
                    } else {
                        double widthRatio = (double) w / MAX_IMAGE_WIDTH;
                        double heightRatio = (double) h / MAX_IMAGE_HEIGHT;
                        double ratio = Math.max(widthRatio, heightRatio);

                        int newW = (int) (w / ratio);
                        int newH = (int) (h / ratio);

                        Image scaledImg = raw.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                        scaled = new ImageIcon(scaledImg);
                    }

                    this.selectedImage = scaled;
                    this.selectedImageName = f.getName();
                    selectedImageLabel.setText(troncaTesto(this.selectedImageName, MAX_FILENAME_CHARS));
                    removeImageBtn.setVisible(true);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Impossibile caricare immagine: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                    this.selectedImage = null;
                    this.selectedImageName = null;
                    selectedImageLabel.setText("Nessuna immagine.");
                    removeImageBtn.setVisible(false);
                }
            }
        });

        removeImageBtn.addActionListener(e -> {
            this.selectedImage = null;
            this.selectedImageName = null;
            selectedImageLabel.setText("Nessuna immagine.");
            removeImageBtn.setVisible(false);
        });

        // --- Colore
        y++;
        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Colore Sfondo"), gbc);
        gbc.gridx = 1;
        JPanel colorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        colorRow.setOpaque(false);
        colorRow.add(colorBtn);
        colorRow.add(colorPreview);
        main.add(colorRow, gbc);

        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Scegli un colore", selectedColor[0]);
            if (c != null) {
                selectedColor[0] = c;
                colorPreview.setBackground(c);
            }
        });

        // --- Bacheca
        y++;
        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Bacheca *"), gbc);
        gbc.gridx = 1; main.add(bachecaCombo, gbc);

        // --- Pulsanti
        y++;
        gbc.gridx = 1; gbc.gridy = y;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false); // Sfondo trasparente
        JButton salva = new JButton(toEdit == null ? "Salva ToDo" : "Salva modifiche");
        JButton annulla = new JButton("Annulla");
        btnRow.add(annulla);
        btnRow.add(salva);
        main.add(btnRow, gbc);

        annulla.addActionListener(e -> dispose());

        salva.addActionListener(e -> {
            try {
                String titolo = titoloField.getText().trim();
                String desc = descrizioneArea.getText();

                if (titolo.isEmpty()) throw new IllegalArgumentException("Il titolo è obbligatorio.");
                if (titolo.length() > MAX_TITOLO_CHARS) {
                    throw new IllegalArgumentException("Il titolo supera i " + MAX_TITOLO_CHARS + " caratteri.");
                }
                if (desc.length() > MAX_DESC_CHARS) {
                    throw new IllegalArgumentException("La descrizione supera i " + MAX_DESC_CHARS + " caratteri.");
                }

                LocalDate data;
                try {
                    data = LocalDate.parse(dataField.getText().trim());
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException("Formato data non valido. Usa AAAA-MM-GG.");
                }

                List<String> links = new ArrayList<>();
                for (int i = 0; i < linksModel.getSize(); i++) links.add(linksModel.getElementAt(i));

                TitoloBacheca dest = (TitoloBacheca) bachecaCombo.getSelectedItem();

                String descToSave = desc.trim();

                if (toEdit == null) {
                    ctrl.onAddToDo(titolo, data, links, descToSave, selectedColor[0], dest, this.selectedImage);
                } else {
                    ctrl.onEditToDo(toEdit, titolo, data, links, descToSave, this.selectedImage, dest, selectedColor[0]);
                }

                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Listener per contatore caratteri
        setupDocumentListener(titoloField, titoloCountLabel, MAX_TITOLO_CHARS);
        setupDocumentListener(descrizioneArea, descCountLabel, MAX_DESC_CHARS);

        updateCharCount(titoloField.getText().length(), titoloCountLabel, MAX_TITOLO_CHARS);
        updateCharCount(descrizioneArea.getText().length(), descCountLabel, MAX_DESC_CHARS);

        return main;
    }

    /**
     * NUOVO: Crea il pannello "Condivisioni".
     */
    private JPanel createSharingPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(ColorsConstant.LIGHT_GREY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. Sezione: Utenti Attuali ---
        JPanel currentUsersPanel = new JPanel(new BorderLayout(0, 5));
        currentUsersPanel.setOpaque(false);
        currentUsersPanel.setBorder(BorderFactory.createTitledBorder("Attualmente Condiviso Con"));

        sharedListModel = new DefaultListModel<>();
        sharedList = new JList<>(sharedListModel);

        sharedList.setCellRenderer(new SharedUserRenderer());

        JScrollPane sharedScroll = new JScrollPane(sharedList);
        sharedScroll.setPreferredSize(new Dimension(300, 150));
        currentUsersPanel.add(sharedScroll, BorderLayout.CENTER);

        // Pulsanti per la lista utenti
        JPanel sharedButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        sharedButtons.setOpaque(false);
        JButton modificaPermessoBtn = new JButton("Modifica Permesso");
        JButton rimuoviCondivisioneBtn = new JButton("Rimuovi");
        sharedButtons.add(modificaPermessoBtn);
        sharedButtons.add(rimuoviCondivisioneBtn);
        currentUsersPanel.add(sharedButtons, BorderLayout.SOUTH);

        mainPanel.add(currentUsersPanel, BorderLayout.CENTER);

        // --- 2. Sezione: Aggiungi Utente ---
        JPanel addUserPanel = new JPanel(new BorderLayout(0, 5));
        addUserPanel.setOpaque(false);
        addUserPanel.setBorder(BorderFactory.createTitledBorder("Aggiungi Condivisione"));

        // Ricerca
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField();
        JButton searchBtn = new JButton("Cerca");
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);
        addUserPanel.add(searchPanel, BorderLayout.NORTH);

        // Risultati ricerca
        searchListModel = new DefaultListModel<>();
        searchList = new JList<>(searchListModel);
        searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane searchScroll = new JScrollPane(searchList);
        searchScroll.setPreferredSize(new Dimension(300, 100));
        addUserPanel.add(searchScroll, BorderLayout.CENTER);

        // Azione di Aggiunta
        JPanel addActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        addActionPanel.setOpaque(false);
        permessoCombo = new JComboBox<>(PermessoCondivisione.values());
        JButton aggiungiCondivisioneBtn = new JButton("Aggiungi");
        addActionPanel.add(new JLabel("Permesso:"));
        addActionPanel.add(permessoCombo);
        addActionPanel.add(aggiungiCondivisioneBtn);
        addUserPanel.add(addActionPanel, BorderLayout.SOUTH);

        mainPanel.add(addUserPanel, BorderLayout.SOUTH);

        // --- 3. LOGICA (Listeners) ---

        // Cerca Utente
        searchBtn.addActionListener(e -> cercaUtenti());
        searchField.addActionListener(e -> cercaUtenti()); // Cerca anche con Invio

        // Aggiungi Condivisione
        aggiungiCondivisioneBtn.addActionListener(e -> aggiungiCondivisione());

        // Rimuovi Condivisione
        rimuoviCondivisioneBtn.addActionListener(e -> rimuoviCondivisione());

        // Modifica Permesso
        modificaPermessoBtn.addActionListener(e -> modificaPermesso());

        return mainPanel;
    }

    /**
     * NUOVO: Popola la lista degli utenti condivisi
     */
    private void loadSharedUsers() {
        if (toEdit == null) return;

        sharedListModel.clear();
        Map<Utente, PermessoCondivisione> condivisioni = toEdit.getCondivisioni();
        if (condivisioni != null) {
            sharedListModel.addAll(condivisioni.entrySet());
        }
    }

    /**
     * NUOVO: Logica per il pulsante "Cerca"
     */
    private void cercaUtenti() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        searchListModel.clear();
        List<Utente> utentiTrovati = ctrl.cercaUtenti(query);

        // Filtra utenti già presenti nella lista
        Map<Utente, PermessoCondivisione> giaCondivisi = toEdit.getCondivisioni();
        List<Integer> idGiaCondivisi = giaCondivisi.keySet().stream()
                .map(Utente::getIdUtente)
                .toList();

        for (Utente u : utentiTrovati) {
            if (!idGiaCondivisi.contains(u.getIdUtente())) {
                searchListModel.addElement(u);
            }
        }
    }

    /**
     * NUOVO: Logica per il pulsante "Aggiungi"
     */
    private void aggiungiCondivisione() {
        Utente utenteSelezionato = searchList.getSelectedValue();
        if (utenteSelezionato == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un utente dai risultati della ricerca.", "Errore", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PermessoCondivisione permesso = (PermessoCondivisione) permessoCombo.getSelectedItem();

        // Chiamiamo il controller (che aggiorna DB e modello)
        ctrl.onAggiungiCondivisione(toEdit, utenteSelezionato, permesso);

        // Aggiorniamo la UI
        loadSharedUsers(); // Ricarica la lista "Attualmente Condiviso"
        searchListModel.removeElement(utenteSelezionato); // Rimuovi dalla lista "Risultati"
    }

    /**
     * NUOVO: Logica per il pulsante "Rimuovi"
     */
    private void rimuoviCondivisione() {
        Map.Entry<Utente, PermessoCondivisione> selezionato = sharedList.getSelectedValue();
        if (selezionato == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un utente dalla lista \"Attualmente Condiviso Con\".", "Errore", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Utente utenteDaRimuovere = selezionato.getKey();

        int conf = JOptionPane.showConfirmDialog(this,
                "Rimuovere " + utenteDaRimuovere.getUsername() + " dalla condivisione?",
                "Conferma Rimozione",
                JOptionPane.YES_NO_OPTION);

        if (conf == JOptionPane.YES_OPTION) {
            // Chiamiamo il controller
            ctrl.onRimuoviCondivisione(toEdit, utenteDaRimuovere);
            // Aggiorniamo la UI
            loadSharedUsers();
        }
    }

    /**
     * NUOVO: Logica per il pulsante "Modifica Permesso"
     */
    private void modificaPermesso() {
        Map.Entry<Utente, PermessoCondivisione> selezionato = sharedList.getSelectedValue();
        if (selezionato == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un utente dalla lista \"Attualmente Condiviso Con\".", "Errore", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Utente utente = selezionato.getKey();
        PermessoCondivisione permessoAttuale = selezionato.getValue();

        // Chiedi all'utente il nuovo permesso
        PermessoCondivisione nuovoPermesso = (PermessoCondivisione) JOptionPane.showInputDialog(
                this,
                "Seleziona il nuovo permesso per " + utente.getUsername() + ":",
                "Modifica Permesso",
                JOptionPane.QUESTION_MESSAGE,
                null,
                PermessoCondivisione.values(), // Array di opzioni
                permessoAttuale // Opzione preselezionata
        );

        if (nuovoPermesso != null && nuovoPermesso != permessoAttuale) {
            // Chiamiamo il controller
            ctrl.onModificaPermesso(toEdit, utente, nuovoPermesso);
            // Aggiorniamo la UI
            loadSharedUsers();
        }
    }


    // --- Metodo helper per troncare il testo ---
    private String troncaTesto(String testo, int maxLen) {
        if (testo == null) return "";
        return testo.length() > maxLen ? testo.substring(0, maxLen) + "…" : testo;
    }

    // --- Metodi helper per contatore caratteri ---

    private void setupDocumentListener(JTextField field, JLabel label, int max) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { update(); }
            @Override
            public void removeUpdate(DocumentEvent e) { update(); }
            @Override
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                updateCharCount(field.getText().length(), label, max);
            }
        });
    }

    private void setupDocumentListener(JTextArea area, JLabel label, int max) {
        area.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { update(); }
            @Override
            public void removeUpdate(DocumentEvent e) { update(); }
            @Override
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                updateCharCount(area.getText().length(), label, max);
            }
        });
    }

    private void updateCharCount(int length, JLabel label, int max) {
        label.setText(length + " / " + max);
        if (length > max) {
            label.setForeground(Color.RED);
        } else {
            label.setForeground(Color.GRAY);
        }
    }

    /**
     * NUOVO: Classe interna per renderizzare la lista degli utenti condivisi.
     * Mostra "Username (Permesso)"
     */
    private static class SharedUserRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // Inizia con il rendering di default
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Map.Entry) {
                try {
                    Map.Entry<?, ?> entry = (Map.Entry<?, ?>) value;
                    Utente utente = (Utente) entry.getKey();
                    PermessoCondivisione permesso = (PermessoCondivisione) entry.getValue();

                    // Imposta il testo personalizzato
                    setText(utente.getUsername() + " (" + permesso.toString() + ")");

                } catch (Exception e) {
                    setText("Errore nel rendering");
                }
            }
            return c;
        }
    }
}