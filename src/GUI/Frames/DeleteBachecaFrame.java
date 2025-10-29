package GUI.Frames;

import Controllers.MainController;
import Model.TitoloBacheca;

import javax.swing.*;
import java.awt.*;

public class DeleteBachecaFrame extends JFrame {

    public DeleteBachecaFrame(MainController ctrl, TitoloBacheca[] disponibili) {
        super("Elimina Bacheca");
        setSize(300,150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5,5));

        JPanel p = new JPanel();
        p.add(new JLabel("Seleziona bacheca:"));
        JComboBox<TitoloBacheca> combo = new JComboBox<>(disponibili);
        p.add(combo);
        add(p, BorderLayout.CENTER);

        JButton delete = new JButton("Elimina");
        delete.addActionListener(e -> {
            ctrl.onDeleteBacheca((TitoloBacheca) combo.getSelectedItem());
            dispose();
        });
        add(delete, BorderLayout.SOUTH);

        setVisible(true);
    }
}
