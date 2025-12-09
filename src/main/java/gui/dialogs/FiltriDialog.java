package gui.dialogs;

import controllers.MainController;
import util.ColorsConstant;
import gui.views.BoardView;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog (popup) per le opzioni di Filtro e Visualizzazione.
 */
public class FiltriDialog extends JDialog {

    /**
     * Campo per inserire il testo da cercare.
     */
    private JTextField textSearchField;

    /**
     * Campo per inserire la data da cercare (formato AAAA-MM-GG).
     */
    private JTextField dateSearchField;

    /**
     * Costruisce il dialog dei filtri.
     *
     * @param parentView La vista principale (BoardView) su cui applicare i filtri.
     * @param ctrl       Il controller principale (non utilizzato direttamente ma utile per coerenza).
     */
    public FiltriDialog(BoardView parentView, MainController ctrl) {
        super(parentView, "Filtri e Opzioni", false); // false = non modale
        setUndecorated(true); // Rimuove la cornice della finestra

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        mainPanel.setBackground(ColorsConstant.LIGHT_GREY);

        mainPanel.add(new JLabel("Cerca per Testo:"));
        textSearchField = new JTextField(20);
        textSearchField.addActionListener(e -> applyFilters(parentView));
        mainPanel.add(textSearchField);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        mainPanel.add(new JLabel("Cerca per data (AAAA-MM-GG):"));
        dateSearchField = new JTextField(20);
        dateSearchField.addActionListener(e -> applyFilters(parentView));
        mainPanel.add(dateSearchField);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JCheckBox toggleScadenze = new JCheckBox("Mostra Scadenze");
        toggleScadenze.setFont(new Font("SansSerif", Font.PLAIN, 14));
        toggleScadenze.setOpaque(false);
        toggleScadenze.setSelected(parentView.isShowInScadenza());
        toggleScadenze.addActionListener(e -> {
            parentView.setShowInScadenza(toggleScadenze.isSelected());
        });
        mainPanel.add(toggleScadenze);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);

        JButton applyBtn = new JButton("Applica");
        applyBtn.addActionListener(e -> applyFilters(parentView));
        buttonPanel.add(applyBtn);

        JButton closeBtn = new JButton("Chiudi");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        mainPanel.add(buttonPanel);

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                dispose();
            }
        });

        getContentPane().add(mainPanel);
        pack();
    }

    /**
     * Applica i filtri inseriti chiamando i metodi della vista principale.
     *
     * @param parentView La vista principale su cui agire.
     */
    private void applyFilters(BoardView parentView) {
        String textQuery = textSearchField.getText().trim();
        String dateQuery = dateSearchField.getText().trim();

        if (!textQuery.isEmpty()) {
            parentView.doSearch(textQuery);
        } else if (!dateQuery.isEmpty()) {
            parentView.doDateSearch(dateQuery);
        }

        dispose();
    }
}