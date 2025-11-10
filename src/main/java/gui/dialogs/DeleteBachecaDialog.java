package gui.dialogs;

import controllers.MainController;
import model.TitoloBacheca;

import javax.swing.*;
import java.awt.*;

public class DeleteBachecaDialog extends JDialog {

    public DeleteBachecaDialog(Window parent, MainController ctrl, TitoloBacheca[] disponibili) {
        super(parent, "Elimina Bacheca", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel p = new JPanel();
        p.add(new JLabel("Seleziona bacheca:"));
        JComboBox<TitoloBacheca> combo = new JComboBox<>(disponibili);
        p.add(combo);
        getContentPane().add(p, BorderLayout.CENTER);

        JButton delete = new JButton("Elimina");
        delete.addActionListener(e -> {
            ctrl.onDeleteBacheca((TitoloBacheca) combo.getSelectedItem());
            dispose();
        });
        getContentPane().add(delete, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}