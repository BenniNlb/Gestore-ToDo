package GUI.Cards;

import Controllers.MainController;
import GUI.Frames.MainFrame;   // importa MainFrame
import Model.Bacheca;
import Model.TitoloBacheca;
import GUI.ColorsConstant;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardChoice extends JPanel {
    private final MainController ctrl;
    private final MainFrame mainFrame;

    public CardChoice(MainController ctrl, MainFrame mainFrame) {
        this.ctrl      = ctrl;
        this.mainFrame = mainFrame;

        // Layout e stile
        setLayout(new GridLayout(3, 1, 0, 5));
        setBackground(ColorsConstant.PinkFairy);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Aggiungi ToDo
        add(makeLabel("Aggiungi ToDo", () -> {
            new AddFrame(ctrl, true);
            SwingUtilities.getWindowAncestor(this).setVisible(false);
        }));

        // Aggiungi Bacheca
        add(makeLabel("Aggiungi Bacheca", () -> {
            // Calcolo bacheche disponibili
            List<TitoloBacheca> tutte = Stream.of(
                    TitoloBacheca.UNIVERSITA,
                    TitoloBacheca.LAVORO,
                    TitoloBacheca.TEMPO_LIBERO
            ).collect(Collectors.toList());

            List<TitoloBacheca> esistenti = ctrl.getBachecaController()
                    .getAllBacheche().stream()
                    .map(Bacheca::getTitolo)
                    .collect(Collectors.toList());

            List<TitoloBacheca> disponibili = tutte.stream()
                    .filter(t -> !esistenti.contains(t))
                    .collect(Collectors.toList());

            if (disponibili.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Non è possibile aggiungere bacheche, massimo raggiunto.",
                        "Limite raggiunto",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                new AddFrame(ctrl, false, disponibili);
                SwingUtilities.getWindowAncestor(this).setVisible(false);
            }
        }));

        // Elimina Bacheca
        add(makeLabel("Elimina Bacheca", () -> {
            TitoloBacheca[] toDelete = ctrl.getBachecaController()
                    .getAllBacheche().stream()
                    .map(Bacheca::getTitolo)
                    .filter(t -> t != TitoloBacheca.SCADENZE_DI_OGGI)
                    .toArray(TitoloBacheca[]::new);
            new DeleteBachecaFrame(ctrl, toDelete);
            SwingUtilities.getWindowAncestor(this).setVisible(false);
        }));
    }

    private JLabel makeLabel(String text, Runnable action) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setForeground(Color.BLUE.darker());
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }
        });
        return lbl;
    }
}
