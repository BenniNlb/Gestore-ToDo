package gui.dialogs;

import controllers.MainController;
import model.Bacheca;
import gui.ColorsConstant;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Dialog per modificare la descrizione di una Bacheca.
 */
public class EditBachecaDialog extends JDialog {

    // MODIFICATO: Limite ridotto a 50
    private static final int MAX_DESC_CHARS = 50;

    public EditBachecaDialog(Window parent, MainController ctrl, Bacheca bacheca) {
        super(parent, "Modifica " + bacheca.getTitolo(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Componenti
        JTextArea descArea = new JTextArea(5, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setText(bacheca.getDescrizione());

        JLabel descCountLabel = new JLabel("0 / " + MAX_DESC_CHARS);
        descCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

        // Layout
        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBackground(ColorsConstant.LIGHT_GREY);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        main.add(new JLabel("Descrizione:"), BorderLayout.NORTH);

        // Pannello per area di testo e contatore
        JPanel descPanel = new JPanel(new BorderLayout(0, 2));
        descPanel.setOpaque(false);
        descPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        JPanel descCountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        descCountPanel.setOpaque(false);
        descCountPanel.add(descCountLabel);
        descPanel.add(descCountPanel, BorderLayout.SOUTH);

        main.add(descPanel, BorderLayout.CENTER);

        // Pulsanti
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        JButton salvaBtn = new JButton("Salva");
        JButton annullaBtn = new JButton("Annulla");
        btnRow.add(annullaBtn);
        btnRow.add(salvaBtn);
        main.add(btnRow, BorderLayout.SOUTH);

        // Azioni
        annullaBtn.addActionListener(e -> dispose());

        salvaBtn.addActionListener(e -> {
            try {
                String nuovaDesc = descArea.getText();
                if (nuovaDesc.length() > MAX_DESC_CHARS) {
                    throw new Exception("La descrizione supera i " + MAX_DESC_CHARS + " caratteri.");
                }

                // Chiama il controller
                ctrl.onEditBachecaDescrizione(bacheca.getTitolo(), nuovaDesc.trim());
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Setup contatore
        descArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { update(); }
            @Override
            public void removeUpdate(DocumentEvent e) { update(); }
            @Override
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                int len = descArea.getText().length();
                descCountLabel.setText(len + " / " + MAX_DESC_CHARS);
                if (len > MAX_DESC_CHARS) {
                    descCountLabel.setForeground(Color.RED);
                } else {
                    descCountLabel.setForeground(Color.GRAY);
                }
            }
        });
        // Aggiorna contatore iniziale
        descCountLabel.setText(descArea.getText().length() + " / " + MAX_DESC_CHARS);

        getContentPane().add(main);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}