package gui.dialogs;

import controllers.MainController;
import model.Bacheca;
import util.ColorsConstant;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Finestra di dialogo modale per la modifica della descrizione di una {@link Bacheca}.
 * <p>
 * Questa classe permette all'utente di aggiornare la descrizione associata a una bacheca esistente.
 * Include un meccanismo di validazione della lunghezza del testo in tempo reale,
 * impedendo il salvataggio (o segnalando l'errore) se il limite di caratteri viene superato.
 */
public class EditBachecaDialog extends JDialog {

    /**
     * Numero massimo di caratteri consentiti per la descrizione della bacheca.
     */
    private static final int MAX_DESC_CHARS = 50;

    /**
     * Costruisce e visualizza la finestra di modifica.
     * <p>
     * Inizializza i componenti grafici, pre-popola il campo di testo con la descrizione attuale,
     * configura il layout e imposta i listener per i pulsanti e per il conteggio dei caratteri.
     *
     * @param parent  La finestra proprietaria (owner) del dialog, per il posizionamento.
     * @param ctrl    Il controller principale per eseguire l'operazione di aggiornamento dati.
     * @param bacheca La bacheca di cui si sta modificando la descrizione.
     */
    public EditBachecaDialog(Window parent, MainController ctrl, Bacheca bacheca) {
        super(parent, "Modifica " + bacheca.getTitolo(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JTextArea descArea = new JTextArea(5, 30);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setText(bacheca.getDescrizione());

        JLabel descCountLabel = new JLabel("0 / " + MAX_DESC_CHARS);
        descCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBackground(ColorsConstant.LIGHT_GREY);
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        main.add(new JLabel("Descrizione:"), BorderLayout.NORTH);

        JPanel descPanel = new JPanel(new BorderLayout(0, 2));
        descPanel.setOpaque(false);
        descPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

        JPanel descCountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        descCountPanel.setOpaque(false);
        descCountPanel.add(descCountLabel);
        descPanel.add(descCountPanel, BorderLayout.SOUTH);

        main.add(descPanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        JButton salvaBtn = new JButton("Salva");
        JButton annullaBtn = new JButton("Annulla");
        btnRow.add(annullaBtn);
        btnRow.add(salvaBtn);
        main.add(btnRow, BorderLayout.SOUTH);

        annullaBtn.addActionListener(e -> dispose());

        salvaBtn.addActionListener(e -> {
            try {
                String nuovaDesc = descArea.getText();
                if (nuovaDesc.length() > MAX_DESC_CHARS) {
                    throw new Exception("La descrizione supera i " + MAX_DESC_CHARS + " caratteri.");
                }

                ctrl.onEditBachecaDescrizione(bacheca.getTitolo(), nuovaDesc.trim());
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

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