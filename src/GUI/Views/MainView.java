package GUI.Views;

import Controllers.MainController;
import GUI.Panels.BachecaPanel;
import GUI.Panels.InScadenzaPanel;
import GUI.Cards.CardChoice;
import Model.ToDo;
import GUI.ColorsConstant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

public class MainView extends JFrame {
    private final MainController mainCtrl;
    private final JPanel centerPanel;
    private final JTextField searchField;
    private final JPanel top; // header panel

    // Parametri configurabili
    private static final int MIN_BACHECA_WIDTH = 260;   // non scendere sotto questo valore
    private static final int MAX_BACHECA_WIDTH = 520;   // (opzionale) massimo per colonna
    private static final int GAP_BETWEEN = 8;           // gap orizzontale tra bacheche

    public MainView() {
        mainCtrl = new MainController();
        try {
            mainCtrl.getBachecaController().addChangeListener(this::refreshCenter);
        } catch (Exception ignored) {}

        setTitle("Gestore ToDo");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // TOP
        top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Color.WHITE);

        JLabel appTitle = new JLabel("Gestore ToDo");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 28)); // titolo grande
        appTitle.setForeground(Color.BLACK);
        appTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        appTitle.setBorder(BorderFactory.createEmptyBorder(12, 10, 8, 10));
        top.add(appTitle);

        JPanel searchRow = new JPanel(new BorderLayout(5, 0));
        searchRow.setBackground(Color.WHITE);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        searchField = new JTextField();
        searchField.setToolTipText("Cerca per titolo o descrizione (premi Invio)");
        searchField.addActionListener(e -> doSearch());
        searchField.setBackground(ColorsConstant.PinkFairy);
        searchRow.add(searchField, BorderLayout.CENTER);

        JButton menuBtn = new JButton("☰");
        menuBtn.addActionListener(e -> showCardChoice(menuBtn));
        searchRow.add(menuBtn, BorderLayout.EAST);

        top.add(searchRow);
        add(top, BorderLayout.NORTH);

        // CENTER
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(centerPanel, BorderLayout.CENTER);

        // listener per resize: ricalcola e ridisegna le bacheche
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // esegui dopo che Swing aggiorna le dimensioni
                SwingUtilities.invokeLater(() -> refreshCenter());
            }
        });

        // mostra
        refreshCenter();
        setVisible(true);
    }

    /**
     * Ricostruisce la schermata principale con larghezze reattive.
     */
    public void refreshCenter() {
        centerPanel.removeAll();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));

        // Calcoli dimensione disponibili
        int containerWidth = getContentPane().getWidth();
        if (containerWidth <= 0) containerWidth = getWidth(); // fallback
        // togliamo i padding orizzontali del centerPanel (10 + 10)
        int horizontalPadding = 20;
        int availableWidth = Math.max(400, containerWidth - horizontalPadding);

        // numero colonne = 1 (Scadenze) + n bacheche
        List<?> bacheche = mainCtrl.getBachecaController().getAllBacheche();
        int columns = 1 + bacheche.size();

        // gap total tra colonne
        int totalGaps = Math.max(0, (columns - 1) * GAP_BETWEEN);

        // calcolo larghezza per colonna, limitata tra MIN e MAX
        int widthPer = (availableWidth - totalGaps) / columns;
        widthPer = Math.max(MIN_BACHECA_WIDTH, widthPer);
        if (MAX_BACHECA_WIDTH > 0) widthPer = Math.min(MAX_BACHECA_WIDTH, widthPer);

        // calcolo altezza disponibile per la lista (body) delle bacheche
        int totalHeight = getContentPane().getHeight();
        int topHeight = top.getPreferredSize().height + 20;
        int availableListHeight = Math.max(240, totalHeight - topHeight - 80); // fallback se 0

        // Sezione scadenze (a sinistra)
        InScadenzaPanel inScadenza = new InScadenzaPanel(mainCtrl);
        inScadenza.setAlignmentY(Component.TOP_ALIGNMENT);
        inScadenza.setPreferredSize(new Dimension(widthPer, availableListHeight + 80)); // include header area
        inScadenza.setMaximumSize(new Dimension(widthPer, Integer.MAX_VALUE));
        centerPanel.add(inScadenza);
        centerPanel.add(Box.createRigidArea(new Dimension(GAP_BETWEEN, 0)));

        // Bacheche: creamo ciascuna con la larghezza calcolata
        for (Object o : bacheche) {
            Model.Bacheca b = (Model.Bacheca) o;
            BachecaPanel bachecaPanel = new BachecaPanel(b, mainCtrl, widthPer, availableListHeight);
            bachecaPanel.setAlignmentY(Component.TOP_ALIGNMENT);
            bachecaPanel.setMaximumSize(new Dimension(widthPer, Integer.MAX_VALUE));
            centerPanel.add(bachecaPanel);
            centerPanel.add(Box.createRigidArea(new Dimension(GAP_BETWEEN, 0)));
        }

        // forza repaint/revalidate
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private void doSearch() {
        String q = searchField.getText().trim();

        if (q.isEmpty()) {
            refreshCenter();
            return;
        }

        java.util.List<ToDo> found = mainCtrl.getToDoController().searchToDo(q);

        centerPanel.removeAll();
        centerPanel.setLayout(new BorderLayout());

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        topRow.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Risultati ricerca per: \"" + q + "\"");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        topRow.add(title, BorderLayout.WEST);

        JButton backBtn = new JButton("← Torna alle bacheche");
        backBtn.addActionListener(ev -> refreshCenter());
        topRow.add(backBtn, BorderLayout.EAST);

        centerPanel.add(topRow, BorderLayout.NORTH);

        if (found.isEmpty()) {
            JLabel no = new JLabel("Nessun risultato trovato");
            no.setHorizontalAlignment(SwingConstants.CENTER);
            no.setFont(new Font("SansSerif", Font.ITALIC, 14));
            no.setForeground(Color.GRAY);
            centerPanel.add(no, BorderLayout.CENTER);
        } else {
            JPanel results = new JPanel();
            results.setBackground(Color.WHITE);
            results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
            results.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Calcolo widthPer coerente con il layout corrente
            int containerWidth = getContentPane().getWidth();
            int availableWidth = Math.max(400, containerWidth - 20);
            int columns = 1 + mainCtrl.getBachecaController().getAllBacheche().size();
            int totalGaps = Math.max(0, (columns - 1) * GAP_BETWEEN);
            int widthPer = (availableWidth - totalGaps) / columns;
            widthPer = Math.max(MIN_BACHECA_WIDTH, widthPer);
            if (MAX_BACHECA_WIDTH > 0) widthPer = Math.min(MAX_BACHECA_WIDTH, widthPer);
            int cardInnerWidth = widthPer - 32;

            for (ToDo td : found) {
                GUI.Cards.ToDoCard card = new GUI.Cards.ToDoCard(td, mainCtrl);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                Dimension pref = card.getPreferredSize();
                card.setMaximumSize(new Dimension(cardInnerWidth, pref.height));
                card.setPreferredSize(new Dimension(cardInnerWidth, pref.height));
                results.add(card);
                results.add(Box.createRigidArea(new Dimension(0, 8)));
            }
            results.add(Box.createVerticalGlue());

            JScrollPane resultsScrollPane = new JScrollPane(results, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            resultsScrollPane.getViewport().setBackground(Color.WHITE);
            centerPanel.add(resultsScrollPane, BorderLayout.CENTER);
        }

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private void showCardChoice(Component invoker) {
        CardChoice cc = new CardChoice(mainCtrl, this);
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        popup.setLayout(new BorderLayout());
        popup.add(cc, BorderLayout.CENTER);
        popup.show(invoker, 0, invoker.getHeight());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainView::new);
    }
}
