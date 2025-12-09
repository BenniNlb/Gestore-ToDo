package gui.dialogs.tododialog;

import controllers.MainController;
import model.ToDo;
import model.TitoloBacheca;
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

/**
 * Pannello grafico (View) per la visualizzazione e modifica dei dettagli di un {@link ToDo}.
 * <p>
 * Questo componente gestisce l'inserimento e la validazione di tutti i dati relativi a un'attività:
 * <ul>
 * <li>Titolo (obbligatorio, con limite caratteri).</li>
 * <li>Descrizione (opzionale, con limite caratteri).</li>
 * <li>Data di scadenza (formato rigoroso AAAA-MM-GG).</li>
 * <li>Link URL esterni.</li>
 * <li>Immagine allegata (con ridimensionamento automatico e limite di peso).</li>
 * <li>Colore di sfondo personalizzato.</li>
 * <li>Bacheca di appartenenza.</li>
 * </ul>
 * Interagisce direttamente con il {@link MainController} per salvare le modifiche.
 */
public class ToDoDetailsView extends JPanel {

    /**
     * Riferimento al controller principale per eseguire le operazioni di salvataggio.
     */
    private final MainController ctrl;

    /**
     * L'oggetto ToDo da modificare. Se {@code null}, il pannello è in modalità "creazione".
     */
    private final ToDo toEdit;

    /**
     * Etichetta che mostra il conteggio dei caratteri per il titolo.
     */
    private JLabel titoloCountLabel;

    /**
     * Etichetta che mostra il conteggio dei caratteri per la descrizione.
     */
    private JLabel descCountLabel;

    /**
     * Etichetta che mostra il nome del file immagine selezionato.
     */
    private JLabel selectedImageLabel;

    /**
     * Pulsante per rimuovere l'immagine attualmente selezionata.
     */
    private JButton removeImageBtn;

    /**
     * L'immagine (icona) attualmente selezionata o caricata dal ToDo esistente.
     */
    private ImageIcon selectedImage;

    /**
     * Il nome del file dell'immagine selezionata.
     */
    private String selectedImageName;

    private static final int MAX_TITOLO_CHARS = 35;
    private static final int MAX_DESC_CHARS = 350;
    private static final long MAX_IMAGE_BYTES = 2 * 1024 * 1024; // 2MB
    private static final int MAX_FILENAME_CHARS = 25;
    private static final int MAX_IMAGE_WIDTH = 400;
    private static final int MAX_IMAGE_HEIGHT = 400;

    /**
     * Costruisce il pannello dei dettagli.
     *
     * @param ctrl           Il controller principale.
     * @param toEdit         Il ToDo da modificare (o {@code null}).
     * @param disponibili    La lista delle bacheche disponibili per la selezione.
     * @param defaultBacheca La bacheca da preselezionare (utile in fase di creazione).
     */
    public ToDoDetailsView(MainController ctrl, ToDo toEdit, List<TitoloBacheca> disponibili, TitoloBacheca defaultBacheca) {
        this.ctrl = ctrl;
        this.toEdit = toEdit;
        setLayout(new BorderLayout());
        setBackground(ColorsConstant.LIGHT_GREY);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createDetailsPanel(disponibili, defaultBacheca), BorderLayout.CENTER);
    }

    /**
     * Crea e configura il pannello contenente tutti i campi di input del form.
     *
     * @param disponibili    Lista delle bacheche selezionabili.
     * @param defaultBacheca Bacheca di default.
     * @return Il pannello {@link JPanel} configurato.
     */
    private JPanel createDetailsPanel(List<TitoloBacheca> disponibili, TitoloBacheca defaultBacheca) {
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

        final Color[] selectedColor = {Color.WHITE};

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

        if (toEdit != null) {
            titoloField.setText(toEdit.getTitolo());
            descrizioneArea.setText(toEdit.getDescrizione() != null ? toEdit.getDescrizione() : "");
            dataField.setText(toEdit.getDataScadenza() != null ? toEdit.getDataScadenza().toString() : "");
            if (toEdit.getLinkURLs() != null) toEdit.getLinkURLs().forEach(linksModel::addElement);
            if (toEdit.getImmagine() != null) {
                selectedImage = toEdit.getImmagine();
                selectedImageName = "Immagine caricata";
                selectedImageLabel.setText(troncaTesto(selectedImageName, MAX_FILENAME_CHARS));
                removeImageBtn.setVisible(true);
            }
            if (toEdit.getColoreSfondo() != null) selectedColor[0] = toEdit.getColoreSfondo();
            bachecaCombo.setSelectedItem(defaultBacheca);
        } else if (defaultBacheca != null) {
            bachecaCombo.setSelectedItem(defaultBacheca);
        }

        colorPreview.setBackground(selectedColor[0]);

        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(ColorsConstant.LIGHT_GREY);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        gbc.gridx = 0;
        gbc.gridy = y;
        main.add(new JLabel("Titolo *"), gbc);
        gbc.gridx = 1;
        JPanel titoloPanel = new JPanel(new BorderLayout(0, 2));
        titoloPanel.setOpaque(false);
        titoloPanel.add(titoloField, BorderLayout.CENTER);
        JPanel titoloCountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        titoloCountPanel.setOpaque(false);
        titoloCountPanel.add(titoloCountLabel);
        titoloPanel.add(titoloCountPanel, BorderLayout.SOUTH);
        main.add(titoloPanel, gbc);

        y++;
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        main.add(new JLabel("Descrizione"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPanel descPanel = new JPanel(new BorderLayout(0, 2));
        descPanel.setOpaque(false);
        descPanel.add(new JScrollPane(descrizioneArea), BorderLayout.CENTER);
        JPanel descCountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        descCountPanel.setOpaque(false);
        descCountPanel.add(descCountLabel);
        descPanel.add(descCountPanel, BorderLayout.SOUTH);
        main.add(descPanel, gbc);

        y++;
        gbc.gridx = 0;
        gbc.gridy = y;
        main.add(new JLabel("Data (AAAA-MM-GG) *"), gbc);
        gbc.gridx = 1;
        main.add(dataField, gbc);

        y++;
        gbc.gridx = 0;
        gbc.gridy = y;
        main.add(new JLabel("Link (opzionali)"), gbc);
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
        gbc.gridx = 1;
        gbc.gridy = y;
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

        y++;
        gbc.gridx = 0;
        gbc.gridy = y;
        main.add(new JLabel("Immagine (opzionale)"), gbc);
        gbc.gridx = 1;
        JPanel imgRow = new JPanel(new BorderLayout(6, 6));
        imgRow.setOpaque(false);
        imgRow.add(chooseImgBtn, BorderLayout.WEST);
        selectedImageLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        imgRow.add(selectedImageLabel, BorderLayout.CENTER);
        imgRow.add(removeImageBtn, BorderLayout.EAST);
        main.add(imgRow, gbc);

        chooseImgBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Immagini (max 2MB)", "jpg", "jpeg", "png", "gif"));
            int ret = fc.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    File f = fc.getSelectedFile();
                    if (f.length() > MAX_IMAGE_BYTES) throw new Exception("File troppo grande");
                    ImageIcon raw = new ImageIcon(f.getAbsolutePath());
                    int w = raw.getIconWidth(), h = raw.getIconHeight();
                    ImageIcon scaled;
                    if (w <= MAX_IMAGE_WIDTH && h <= MAX_IMAGE_HEIGHT) scaled = raw;
                    else {
                        double ratio = Math.max((double) w / MAX_IMAGE_WIDTH, (double) h / MAX_IMAGE_HEIGHT);
                        int newW = (int) (w / ratio), newH = (int) (h / ratio);
                        scaled = new ImageIcon(raw.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH));
                    }
                    selectedImage = scaled;
                    selectedImageName = f.getName();
                    selectedImageLabel.setText(troncaTesto(selectedImageName, MAX_FILENAME_CHARS));
                    removeImageBtn.setVisible(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Impossibile caricare immagine: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                    selectedImage = null;
                    selectedImageName = null;
                    selectedImageLabel.setText("Nessuna immagine.");
                    removeImageBtn.setVisible(false);
                }
            }
        });

        removeImageBtn.addActionListener(e -> {
            selectedImage = null;
            selectedImageName = null;
            selectedImageLabel.setText("Nessuna immagine.");
            removeImageBtn.setVisible(false);
        });

        y++;
        gbc.gridx = 0;
        gbc.gridy = y;
        main.add(new JLabel("Colore Sfondo"), gbc);
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

        y++;
        gbc.gridx = 0;
        gbc.gridy = y;
        main.add(new JLabel("Bacheca *"), gbc);
        gbc.gridx = 1;
        main.add(bachecaCombo, gbc);

        y++;
        gbc.gridx = 1;
        gbc.gridy = y;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        JButton salva = new JButton(toEdit == null ? "Salva ToDo" : "Salva modifiche");
        JButton annulla = new JButton("Annulla");
        btnRow.add(annulla);
        btnRow.add(salva);
        main.add(btnRow, gbc);

        annulla.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
        salva.addActionListener(e -> {
            try {
                String titolo = titoloField.getText().trim();
                String desc = descrizioneArea.getText();
                if (titolo.isEmpty()) throw new IllegalArgumentException("Il titolo è obbligatorio.");
                if (titolo.length() > MAX_TITOLO_CHARS) throw new IllegalArgumentException("Titolo troppo lungo.");
                if (desc.length() > MAX_DESC_CHARS) throw new IllegalArgumentException("Descrizione troppo lunga.");
                LocalDate data;
                try {
                    data = LocalDate.parse(dataField.getText().trim());
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException("Formato data non valido.");
                }
                List<String> links = new ArrayList<>();
                for (int i = 0; i < linksModel.size(); i++) links.add(linksModel.get(i));
                TitoloBacheca dest = (TitoloBacheca) bachecaCombo.getSelectedItem();
                String descToSave = desc.trim();
                if (toEdit == null)
                    ctrl.onAddToDo(titolo, data, links, descToSave, selectedColor[0], dest, selectedImage);
                else
                    ctrl.onEditToDo(toEdit, titolo, data, links, descToSave, selectedImage, dest, selectedColor[0]);
                SwingUtilities.getWindowAncestor(this).dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        setupDocumentListener(titoloField, titoloCountLabel, MAX_TITOLO_CHARS);
        setupDocumentListener(descrizioneArea, descCountLabel, MAX_DESC_CHARS);
        updateCharCount(titoloField.getText().length(), titoloCountLabel, MAX_TITOLO_CHARS);
        updateCharCount(descrizioneArea.getText().length(), descCountLabel, MAX_DESC_CHARS);

        return main;
    }

    /**
     * Configura un listener su un campo di testo per aggiornare il contatore dei caratteri in tempo reale.
     *
     * @param field Il campo di testo da monitorare.
     * @param label L'etichetta dove mostrare il conteggio.
     * @param max   Il numero massimo di caratteri consentiti.
     */
    private void setupDocumentListener(JTextField field, JLabel label, int max) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                updateCharCount(field.getText().length(), label, max);
            }
        });
    }

    /**
     * Configura un listener su un'area di testo per aggiornare il contatore dei caratteri in tempo reale.
     *
     * @param area  L'area di testo da monitorare.
     * @param label L'etichetta dove mostrare il conteggio.
     * @param max   Il numero massimo di caratteri consentiti.
     */
    private void setupDocumentListener(JTextArea area, JLabel label, int max) {
        area.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                updateCharCount(area.getText().length(), label, max);
            }
        });
    }

    /**
     * Aggiorna visivamente il contatore dei caratteri e cambia colore se il limite viene superato.
     *
     * @param length Lunghezza attuale del testo.
     * @param label  Etichetta da aggiornare.
     * @param max    Limite massimo.
     */
    private void updateCharCount(int length, JLabel label, int max) {
        label.setText(length + " / " + max);
        label.setForeground(length > max ? Color.RED : Color.GRAY);
    }

    /**
     * Tronca una stringa per scopi di visualizzazione (anteprima).
     *
     * @param testo  Il testo da troncare.
     * @param maxLen Lunghezza massima.
     * @return La stringa troncata con ellissi finale, o l'originale se entro i limiti.
     */
    private String troncaTesto(String testo, int maxLen) {
        if (testo == null) return "";
        return testo.length() > maxLen ? testo.substring(0, maxLen) + "…" : testo;
    }
}