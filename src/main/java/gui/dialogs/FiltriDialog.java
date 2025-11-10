package gui.dialogs;

import controllers.MainController;
import util.ColorsConstant;
import gui.views.MainView;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog (popup) per le opzioni di Filtro e Visualizzazione.
 */
public class FiltriDialog extends JDialog {

    private JTextField textSearchField;
    private JTextField dateSearchField;

    public FiltriDialog(MainView parentView, MainController ctrl) {
        super(parentView, "Filtri e Opzioni", false); // false = non modale
        setUndecorated(true); // Rimuove la cornice della finestra

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        mainPanel.setBackground(ColorsConstant.LIGHT_GREY);

        // --- 1. Ricerca Testuale ---
        mainPanel.add(new JLabel("Cerca per Testo:"));
        textSearchField = new JTextField(20);
        textSearchField.addActionListener(e -> applyFilters(parentView));
        mainPanel.add(textSearchField);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- 2. Ricerca per Data (MODIFICATA ETICHETTA) ---
        mainPanel.add(new JLabel("Cerca per data (AAAA-MM-GG):"));
        dateSearchField = new JTextField(20);
        dateSearchField.addActionListener(e -> applyFilters(parentView));
        mainPanel.add(dateSearchField);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- 3. Opzione Vista Scadenze ---
        JCheckBox toggleScadenze = new JCheckBox("Mostra Scadenze");
        toggleScadenze.setFont(new Font("SansSerif", Font.PLAIN, 14));
        toggleScadenze.setOpaque(false);
        toggleScadenze.setSelected(parentView.isShowInScadenza());
        toggleScadenze.addActionListener(e -> {
            parentView.setShowInScadenza(toggleScadenze.isSelected());
        });
        mainPanel.add(toggleScadenze);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- 4. Pulsanti ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);

        JButton applyBtn = new JButton("Applica");
        applyBtn.addActionListener(e -> applyFilters(parentView));
        buttonPanel.add(applyBtn);

        JButton closeBtn = new JButton("Chiudi");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        mainPanel.add(buttonPanel);

        // Aggiungi un listener per chiudere il popup se si clicca fuori
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                dispose();
            }
        });

        getContentPane().add(mainPanel);
        pack();
    }

    private void applyFilters(MainView parentView) {
        String textQuery = textSearchField.getText().trim();
        String dateQuery = dateSearchField.getText().trim();

        if (!textQuery.isEmpty()) {
            parentView.doSearch(textQuery);
        } else if (!dateQuery.isEmpty()) {
            parentView.doDateSearch(dateQuery);
        }

        dispose(); // Chiudi il popup dopo aver applicato un filtro
    }
}