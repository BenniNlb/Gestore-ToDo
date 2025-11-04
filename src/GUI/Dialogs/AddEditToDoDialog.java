package GUI.Dialogs;

import Controllers.MainController;
import Model.Bacheca;
import Model.TitoloBacheca;
import Model.ToDo;
import GUI.ColorsConstant;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog per creare / modificare ToDo.
 */
public class AddEditToDoDialog extends JDialog {

    private final MainController ctrl;
    private final ToDo toEdit;

    public AddEditToDoDialog(Window parent, MainController ctrl) {
        this(parent, ctrl, null);
    }

    public AddEditToDoDialog(Window parent, MainController ctrl, ToDo toEdit) {
        super(parent, toEdit == null ? "Nuovo ToDo" : "Modifica ToDo", ModalityType.APPLICATION_MODAL);
        this.ctrl = ctrl;
        this.toEdit = toEdit;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Recupera titoli bacheche
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
        JTextArea descrizioneArea = new JTextArea(5, 30);
        descrizioneArea.setLineWrap(true);
        descrizioneArea.setWrapStyleWord(true);

        JTextField dataField = new JTextField(12);

        DefaultListModel<String> linksModel = new DefaultListModel<>();
        JList<String> linksList = new JList<>(linksModel);
        JTextField linkInput = new JTextField(20);

        JLabel imagePreview = new JLabel();
        final ImageIcon[] selectedImage = {null}; // <-- fix per uso in lambda

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
                selectedImage[0] = toEdit.getImmagine();
                imagePreview.setIcon(selectedImage[0]);
            }
            bachecaCombo.setSelectedItem(ctrl.getBachecaController().getAllBacheche().stream()
                    .filter(b -> b.getToDos().contains(toEdit))
                    .map(Bacheca::getTitolo)
                    .findFirst()
                    .orElse(disponibili.get(0)));
        }

        // Layout
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(ColorsConstant.PinkFairy);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Titolo *"), gbc);
        gbc.gridx = 1; main.add(titoloField, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Descrizione"), gbc);
        gbc.gridx = 1; main.add(new JScrollPane(descrizioneArea), gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Data (AAAA-MM-GG) *"), gbc);
        gbc.gridx = 1; main.add(dataField, gbc);

        // link input + buttons
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

        // immagine chooser
        y++;
        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Immagine (opzionale)"), gbc);
        gbc.gridx = 1;
        JPanel imgRow = new JPanel(new BorderLayout(6,6));
        imgRow.setOpaque(false);
        JButton chooseImgBtn = new JButton("Scegli immagine...");
        imgRow.add(chooseImgBtn, BorderLayout.WEST);
        imagePreview.setPreferredSize(new Dimension(200,120));
        imagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imgRow.add(imagePreview, BorderLayout.EAST);
        main.add(imgRow, gbc);

        chooseImgBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Immagini", "jpg","jpeg","png","gif"));
            int ret = fc.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File f = fc.getSelectedFile();
                    javax.swing.ImageIcon raw = new javax.swing.ImageIcon(f.getAbsolutePath());
                    int maxW = 400;
                    int w = raw.getIconWidth();
                    int h = raw.getIconHeight();
                    javax.swing.ImageIcon scaled;
                    if (w > maxW) {
                        int newW = maxW;
                        int newH = (int) ((double) h / w * newW);
                        java.awt.Image scaledImg = raw.getImage().getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
                        scaled = new javax.swing.ImageIcon(scaledImg);
                    } else {
                        scaled = raw;
                    }
                    selectedImage[0] = scaled;
                    imagePreview.setIcon(selectedImage[0]);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Impossibile caricare immagine: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // bacheca combo
        y++;
        gbc.gridx = 0; gbc.gridy = y; main.add(new JLabel("Bacheca *"), gbc);
        gbc.gridx = 1; main.add(bachecaCombo, gbc);

        // pulsanti salva / annulla
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

                List<String> links = new ArrayList<>();
                for (int i = 0; i < linksModel.getSize(); i++) links.add(linksModel.getElementAt(i));

                TitoloBacheca dest = (TitoloBacheca) bachecaCombo.getSelectedItem();

                if (toEdit == null) {
                    // Creazione nuovo ToDo: passa la bacheca di destinazione
                    ctrl.onAddToDo(titolo, data, links, desc, ColorsConstant.PinkFairy, dest, selectedImage[0]);
                } else {
                    // Modifica: PASSIAMO ANCHE LA BACHECA DI DESTINAZIONE
                    ctrl.onEditToDo(toEdit, titolo, data, links, desc, selectedImage[0], dest);
                }

                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        getContentPane().add(main);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
