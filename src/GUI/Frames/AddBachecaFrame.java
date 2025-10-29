package GUI.Frames;

import Controllers.MainController;
import Model.TitoloBacheca;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AddBachecaFrame extends JFrame {

    public AddBachecaFrame(MainController ctrl, List<TitoloBacheca> disponibili) {
        super("Nuova Bacheca");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        if (disponibili.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nessuna bacheca disponibile da aggiungere.",
                    "Limite raggiunto",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        JComboBox<TitoloBacheca> combo = new JComboBox<>(disponibili.toArray(new TitoloBacheca[0]));
        JButton addBtn = new JButton("Aggiungi");

        addBtn.addActionListener(e -> {
            TitoloBacheca sel = (TitoloBacheca) combo.getSelectedItem();
            if (sel != null) {
                ctrl.getBachecaController().aggiungiBacheca(sel, "");
                dispose();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Seleziona Bacheca:"), BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);
        panel.add(addBtn, BorderLayout.SOUTH);

        add(panel);
        pack();
        setVisible(true);
    }
}
