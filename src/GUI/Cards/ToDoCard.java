package GUI.Cards;

import Controllers.MainController;
import Model.ToDo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class ToDoCard extends JPanel {
    private static final int MAX_WIDTH = 420; // massimo per estetica
    private static final int MIN_HEIGHT = 120;

    public ToDoCard(ToDo todo, MainController ctrl) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(todo.getColoreSfondo() != null ? todo.getColoreSfondo() : new Color(255, 228, 230));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // Titolo
        JLabel titolo = new JLabel(troncaTesto(todo.getTitolo(), 35));
        titolo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titolo.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titolo);

        // Descrizione (multiline)
        if (todo.getDescrizione() != null && !todo.getDescrizione().isBlank()) {
            JTextArea desc = new JTextArea(troncaTesto(todo.getDescrizione(), 500));
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            desc.setEditable(false);
            desc.setOpaque(false);
            desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            desc.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));
            add(desc);
        }

        // Link (se esiste)
        if (todo.getLinkURL() != null && !todo.getLinkURL().isBlank()) {
            JLabel linkLabel = new JLabel("<html><a href='" + todo.getLinkURL() + "'>" + todo.getLinkURL() + "</a></html>");
            linkLabel.setForeground(new Color(0, 102, 204));
            linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            linkLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            linkLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            linkLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(todo.getLinkURL()));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ToDoCard.this, "Impossibile aprire il link.", "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            add(linkLabel);
        }

        // Margine inferiore
        add(Box.createVerticalStrut(5));

        // Dimensioni dinamiche
        setMaximumSize(new Dimension(MAX_WIDTH, Integer.MAX_VALUE));
        setPreferredSize(new Dimension(MAX_WIDTH, Math.max(MIN_HEIGHT, getPreferredSize().height)));
    }

    private String troncaTesto(String testo, int maxLen) {
        if (testo == null) return "";
        return testo.length() > maxLen ? testo.substring(0, maxLen) + "…" : testo;
    }
}
