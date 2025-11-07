package gui.dialogs;

import controllers.MainController;
import model.Bacheca;
import model.TitoloBacheca;
import model.ToDo;
import gui.ColorsConstant;

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

public class AddEditToDoDialog extends JDialog {

    private final MainController ctrl;
    private final ToDo toEdit;

    // Campi per la nuova logica
    private ImageIcon selectedImage;
    private String selectedImageName;
    private JLabel titoloCountLabel;
    private JLabel descCountLabel;
    private JLabel selectedImageLabel;
    private JButton removeImageBtn;

    // Costanti per i limiti
    private static final int MAX_TITOLO_CHARS = 20;
    private static final int MAX_DESC_CHARS = 250;
    private static final long MAX_IMAGE_BYTES = 2 * 1024 * 1024; // 2MB
    private static final int MAX_FILENAME_CHARS = 25;
    private static final int MAX_IMAGE_WIDTH = 400;
    private static final int MAX_IMAGE_HEIGHT = 400;

    // MODIFICATO: Costruttore a 3 argomenti (chiama quello a 4)
    public AddEditToDoDialog(Window parent, MainController ctrl, ToDo toEdit) {
        this(parent, ctrl, toEdit, null); // Chiama il costruttore principale con defaultBacheca = null
    }

    // NUOVO: Costruttore a 4 argomenti
    public AddEditToDoDialog(Window parent, MainController ctrl, ToDo toEdit, TitoloBacheca defaultBacheca) {
        super(parent, toEdit == null ? "Nuovo ToDo" : "Modifica ToDo", ModalityType.APPLICATION_MODAL);
        this.ctrl = ctrl;
        this.toEdit = toEdit;
        this.selectedImage = null;
        this.selectedImageName = null;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        java.util.List<TitoloBacheca> disponibili = ctrl.getBachecaController()
                .getAllBacheche().stream().map(Bacheca::getTitolo).toList();

        if (disponibili.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Nessuna bacheca disponibile. Crea prima una bacheca.",
                    "Errore",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

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

        final Color[] selectedColor = { ColorsConstant.LIGHT_GREY };
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

            // Logica di selezione bacheca per MODIFICA
            bachecaCombo.setSelectedItem(ctrl.getBachecaController().getAllBacheche().stream()
                    .filter(b -> b.getToDos().contains(toEdit))
                    .map(Bacheca::getTitolo)
                    .findFirst()
                    .orElse(disponibili.get(0)));

        } else if (defaultBacheca != null) {
            // --- NUOVA LOGICA ---
            // Se è un NUOVO ToDo E abbiamo una bacheca di default
            bachecaCombo.setSelectedItem(defaultBacheca);
        }
        // --- FINE NUOVA LOGICA ---

        colorPreview.setBackground(selectedColor[0]);

        // Layout
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(ColorsConstant.LIGHT_GREY);
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

        getContentPane().add(main);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
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
}