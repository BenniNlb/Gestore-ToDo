package GUI.Panels;

import GUI.Cards.ToDoCard;
import GUI.ColorsConstant;
import Model.Bacheca;
import Model.ToDo;
import Controllers.MainController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class BachecaPanel extends JPanel {

    public BachecaPanel(Bacheca bacheca, MainController mainCtrl, int width, int listHeight) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorsConstant.Murrey, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Header (fuori dallo scroll)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(6,6,6,6));
        JLabel title = new JLabel(bacheca.getTitolo().toString());
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel desc = new JLabel(bacheca.getDescrizione() != null ? bacheca.getDescrizione() : "");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        header.add(title, BorderLayout.NORTH);
        header.add(desc,  BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Body: pannello con BoxLayout che contiene ToDoCard (wrap in JScrollPane)
        List<ToDo> todos = bacheca.getToDos();
        if (todos == null || todos.isEmpty()) {
            JLabel empty = new JLabel("Non ci sono ToDo da fare");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            empty.setBorder(new EmptyBorder(20,20,20,20));
            // Impostiamo dimensione fissa per mantenere tutte le bacheche uguali
            empty.setPreferredSize(new Dimension(width - 16, listHeight));
            add(empty, BorderLayout.CENTER);
        } else {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(Color.WHITE);
            list.setBorder(new EmptyBorder(10,10,10,10));

            int cardInnerWidth = width - 32; // consider padding and borders

            for (ToDo td : todos) {
                ToDoCard card = new ToDoCard(td, mainCtrl);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Adattiamo la larghezza della card, e fissiamo l'altezza al preferred (così non si allunga)
                Dimension pref = card.getPreferredSize();
                card.setPreferredSize(new Dimension(cardInnerWidth, pref.height));
                card.setMaximumSize(new Dimension(cardInnerWidth, pref.height));
                list.add(card);
                list.add(Box.createRigidArea(new Dimension(0,8)));
            }

            JScrollPane scroll = new JScrollPane(
                    list,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            scroll.setBorder(null);
            scroll.getViewport().setBackground(Color.WHITE);

            // Fissiamo la dimensione del viewport: larghezza fissa e altezza limitata
            scroll.setPreferredSize(new Dimension(width - 16, listHeight));
            add(scroll, BorderLayout.CENTER);
        }

        // Impostiamo dimensioni complessive della bacheca
        int totalHeight = listHeight + header.getPreferredSize().height + 32;
        setPreferredSize(new Dimension(width, totalHeight));
    }
}
