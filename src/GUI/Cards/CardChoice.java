package GUI.Cards;

import Controllers.MainController;
import GUI.Dialogs.AddBachecaDialog;
import GUI.Dialogs.AddEditToDoDialog;
import GUI.Dialogs.DeleteBachecaDialog;
import Model.Bacheca;
import Model.TitoloBacheca;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardChoice extends JPanel {

    public CardChoice(MainController ctrl, Window parent) {
        setLayout(new GridLayout(3, 1, 0, 5));
        setBackground(new Color(236, 198, 214));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Aggiungi ToDo
        JLabel l1 = makeLabel("Aggiungi ToDo", () -> new AddEditToDoDialog(parent, ctrl));
        add(l1);

        // Aggiungi Bacheca
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

            new AddBachecaDialog(parent, ctrl, disponibili);
        }));

        // Elimina Bacheca
        add(makeLabel("Elimina Bacheca", () -> {
            List<TitoloBacheca> presenti = ctrl.getBachecaController()
                    .getAllBacheche().stream()
                    .map(Bacheca::getTitolo)
                    .collect(Collectors.toList());

            if (presenti.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nessuna bacheca da eliminare.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            new DeleteBachecaDialog(parent, ctrl, presenti.toArray(new TitoloBacheca[0]));
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
                // se questo pannello è dentro un popup, chiudilo
                java.awt.Container c = lbl.getParent();
                while (c != null && !(c instanceof JPopupMenu)) c = c.getParent();
                if (c instanceof JPopupMenu) ((JPopupMenu) c).setVisible(false);
            }
        });
        return lbl;
    }
}