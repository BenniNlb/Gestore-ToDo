package gui.panels;

import gui.cards.ToDoCard;
import controllers.MainController;
import model.ToDo;
import gui.ColorsConstant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class InScadenzaPanel extends JPanel {

    public InScadenzaPanel(MainController mainCtrl, int panelWidth) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorsConstant.GREY, 1),
                BorderFactory.createEmptyBorder(8,8,8,8)
        ));

        JLabel header = new JLabel("Scadenze di oggi");
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(6,6,6,6));
        add(header, BorderLayout.NORTH);

        List<ToDo> scadOggiRaw = mainCtrl.getScadenzeOggi();
        List<ToDo> scadOggi = deduplicateById(scadOggiRaw);

        if (scadOggi == null || scadOggi.isEmpty()) {
            JLabel empty = new JLabel("Non ci sono scadenze per oggi");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            empty.setBorder(new EmptyBorder(20,20,20,20));
            add(empty, BorderLayout.CENTER);
        } else {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(Color.WHITE);
            list.setBorder(new EmptyBorder(0, 0, 0, 0));

            // --- NUOVA RIGA: Aggiunge spazio in cima ---
            list.add(Box.createRigidArea(new Dimension(0, 10)));
            // --- FINE NUOVA RIGA ---

            int cardWidth = panelWidth - 16 - 0;

            for (ToDo td : scadOggi) {
                ToDoCard card = new ToDoCard(td, mainCtrl, cardWidth, false);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);

                Dimension pref = card.getPreferredSize();
                card.setMaximumSize(new Dimension(cardWidth, pref.height));

                list.add(card);
                list.add(Box.createRigidArea(new Dimension(0,8)));
            }

            JPanel listContainer = new JPanel(new BorderLayout());
            listContainer.setBackground(Color.WHITE);
            listContainer.add(list, BorderLayout.NORTH);

            JScrollPane scroll = new JScrollPane(
                    listContainer,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            scroll.setBorder(null);
            scroll.getViewport().setBackground(Color.WHITE);

            scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

            add(scroll, BorderLayout.CENTER);
        }
    }

    private List<ToDo> deduplicateById(List<ToDo> list) {
        if (list == null) return Collections.emptyList();
        Map<UUID, ToDo> map = new LinkedHashMap<>();
        for (ToDo td : list) {
            if (td == null) continue;
            UUID id = td.getIdToDo();
            if (id == null) {
                map.put(UUID.randomUUID(), td);
            } else {
                map.putIfAbsent(id, td);
            }
        }
        return new ArrayList<>(map.values());
    }
}