package GUI.Dialogs;

import Controllers.MainController;
import Model.TitoloBacheca;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class AddBachecaDialog extends JDialog {

    public AddBachecaDialog(Window parent, MainController ctrl, java.util.List<TitoloBacheca> disponibili) {
        super(parent, "Nuova Bacheca", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        if (disponibili == null || disponibili.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
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

        JPanel panel = new JPanel(new BorderLayout(8,8));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel.add(new JLabel("Seleziona Bacheca:"), BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);
        panel.add(addBtn, BorderLayout.SOUTH);

        getContentPane().add(panel);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}