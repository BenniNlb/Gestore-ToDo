package gui.panels;

import gui.cards.ToDoCard;
import controllers.MainController;
import model.ToDo;
import util.ColorsConstant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Pannello grafico dedicato alla visualizzazione delle attività in scadenza odierna.
 * <p>
 * Questo componente rappresenta una colonna all'interno della dashboard principale.
 * Recupera e mostra automaticamente tutti i {@link ToDo} la cui data di scadenza
 * corrisponde alla data odierna, filtrando eventuali duplicati.
 */
public class InScadenzaPanel extends JPanel {

    /**
     * Costruisce il pannello delle scadenze giornaliere.
     * <p>
     * Inizializza l'interfaccia grafica e popola la lista dei ToDo odierni tramite
     * il {@link MainController}. Nel caso in cui non ci siano scadenze, viene
     * mostrato un messaggio di stato vuoto con un offset verticale (margine inferiore di 45 pixel)
     * per garantire il perfetto allineamento visivo con le altre bacheche della dashboard.
     *
     * @param mainCtrl   Il controller principale dell'applicazione per il recupero dei dati.
     * @param panelWidth La larghezza prefissata del pannello, utilizzata per calcolare
     * la dimensione delle {@link ToDoCard} contenute.
     */
    public InScadenzaPanel(MainController mainCtrl, int panelWidth) {
        setLayout(new BorderLayout());

        setBackground(ColorsConstant.LIGHT_GREY);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel header = new JLabel("Scadenze di oggi");
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.setBorder(new EmptyBorder(6, 6, 6, 6));
        add(header, BorderLayout.NORTH);

        List<ToDo> scadOggiRaw = mainCtrl.getScadenzeOggi();
        List<ToDo> scadOggi = deduplicateById(scadOggiRaw);

        if (scadOggi == null || scadOggi.isEmpty()) {
            JLabel empty = new JLabel("Nessuna scadenza per oggi");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(new Color(150, 150, 150));

            empty.setBorder(new EmptyBorder(0, 0, 45, 0));

            add(empty, BorderLayout.CENTER);
        } else {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(ColorsConstant.LIGHT_GREY);
            list.setBorder(new EmptyBorder(0, 0, 0, 0));

            list.add(Box.createRigidArea(new Dimension(0, 10)));
            int cardWidth = panelWidth - 16;

            for (ToDo td : scadOggi) {
                ToDoCard card = new ToDoCard(td, mainCtrl, cardWidth, false);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);
                card.setMaximumSize(new Dimension(cardWidth, Integer.MAX_VALUE));
                list.add(card);
                list.add(Box.createRigidArea(new Dimension(0, 8)));
            }

            JPanel listContainer = new JPanel(new BorderLayout());
            listContainer.setBackground(ColorsConstant.LIGHT_GREY);
            listContainer.add(list, BorderLayout.NORTH);

            JScrollPane scroll = new JScrollPane(
                    listContainer,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            scroll.setBorder(null);
            scroll.getViewport().setBackground(ColorsConstant.LIGHT_GREY);
            scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

            add(scroll, BorderLayout.CENTER);
        }
    }

    /**
     * Rimuove i duplicati da una lista di ToDo preservandone l'ordine di inserimento originale.
     * <p>
     * Il filtraggio avviene tramite l'ID univoco del ToDo se presente, altrimenti tramite
     * l'identity hash code per i ToDo non ancora persistiti.
     *
     * @param list La lista grezza di {@link ToDo} da elaborare.
     * @return Una nuova lista contenente esclusivamente istanze uniche.
     */
    private List<ToDo> deduplicateById(List<ToDo> list) {
        if (list == null) return Collections.emptyList();
        Map<Integer, ToDo> map = new LinkedHashMap<>();
        for (ToDo td : list) {
            if (td == null) continue;
            int id = td.getIdToDo();
            if (id == 0) map.put(System.identityHashCode(td), td);
            else map.putIfAbsent(id, td);
        }
        return new ArrayList<>(map.values());
    }
}