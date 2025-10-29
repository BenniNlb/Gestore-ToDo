package GUI.Cards;

import Controllers.MainController;
import GUI.Frames.*;
import Model.Bacheca;
import Model.TitoloBacheca;
import GUI.ColorsConstant;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardChoice extends JPanel {
    public CardChoice(MainController ctrl, JFrame parent) {
        setLayout(new GridLayout(3, 1, 0, 5));
        setBackground(ColorsConstant.PinkFairy);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(makeLabel("Aggiungi ToDo", () -> new AddEditToDoFrame(ctrl)));

        add(makeLabel("Aggiungi Bacheca", () -> {
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

            new AddBachecaFrame(ctrl, disponibili);
        }));

        add(makeLabel("Elimina Bacheca", () -> {
            List<TitoloBacheca> presenti = ctrl.getBachecaController()
                    .getAllBacheche().stream()
                    .map(Bacheca::getTitolo)
                    .collect(Collectors.toList());

            if (presenti.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nessuna bacheca da eliminare.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            new DeleteBachecaFrame(ctrl, presenti.toArray(new TitoloBacheca[0]));
        }));
    }

    private JLabel makeLabel(String text, Runnable action) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setForeground(Color.BLUE.darker());
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
                SwingUtilities.getWindowAncestor(lbl).dispose();
            }
        });
        return lbl;
    }
}
