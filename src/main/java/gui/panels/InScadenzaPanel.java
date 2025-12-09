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
 * Questo componente funge da colonna speciale nella dashboard principale.
 * Aggrega e visualizza automaticamente tutti i {@link ToDo} (sia creati dall'utente
 * che condivisi con esso) la cui data di scadenza coincide con la data corrente.
 * <p>
 * Caratteristiche principali:
 * <ul>
 * <li>Visualizzazione in lista verticale scrollabile.</li>
 * <li>Filtraggio automatico dei duplicati.</li>
 * <li>Visualizzazione di un messaggio di cortesia se non ci sono scadenze.</li>
 * </ul>
 */
public class InScadenzaPanel extends JPanel {

    /**
     * Costruisce il pannello delle scadenze.
     * <p>
     * Inizializza il layout, recupera la lista dei ToDo in scadenza oggi tramite
     * il {@link MainController}, rimuove eventuali duplicati e costruisce l'interfaccia
     * grafica (lista di card o messaggio "vuoto").
     *
     * @param mainCtrl   Il controller principale utilizzato per recuperare i dati.
     * @param panelWidth La larghezza prefissata del pannello, utilizzata per calcolare
     * la dimensione corretta delle {@link ToDoCard} interne.
     */
    public InScadenzaPanel(MainController mainCtrl, int panelWidth) {
        setLayout(new BorderLayout());

        setBackground(ColorsConstant.LIGHT_GREY);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

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
            list.setBackground(ColorsConstant.LIGHT_GREY);
            list.setBorder(new EmptyBorder(0, 0, 0, 0));

            list.add(Box.createRigidArea(new Dimension(0, 10)));

            int cardWidth = panelWidth - 16 - 0;

            for (ToDo td : scadOggi) {
                ToDoCard card = new ToDoCard(td, mainCtrl, cardWidth, false);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);

                card.setMaximumSize(new Dimension(cardWidth, Integer.MAX_VALUE));

                list.add(card);
                list.add(Box.createRigidArea(new Dimension(0,8)));
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
     * Rimuove i duplicati da una lista di ToDo basandosi sul loro ID univoco.
     * <p>
     * Utilizza una {@link LinkedHashMap} per mantenere l'ordine di inserimento originale
     * (preservando l'ordinamento restituito dal database/controller) garantendo al
     * contempo che ogni ID compaia una sola volta nella lista finale.
     *
     * @param list La lista grezza di ToDo (potenzialmente contenente duplicati).
     * @return Una nuova lista contenente solo istanze uniche di {@link ToDo}.
     */
    private List<ToDo> deduplicateById(List<ToDo> list) {
        if (list == null) return Collections.emptyList();

        Map<Integer, ToDo> map = new LinkedHashMap<>();

        for (ToDo td : list) {
            if (td == null) continue;

            int id = td.getIdToDo();

            if (id == 0) {
                map.put(new java.util.Random().nextInt(), td);
            } else {
                map.putIfAbsent(id, td);
            }
        }
        return new ArrayList<>(map.values());
    }
}