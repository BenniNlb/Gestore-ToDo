package gui.views;

import controllers.MainController;
import gui.panels.*;
import gui.dialogs.AddBachecaDialog;
import model.Bacheca;
import model.TitoloBacheca;
import model.ToDo;
import gui.ColorsConstant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainView extends JFrame {
    private final MainController mainCtrl;
    private final JPanel centerPanel;
    private final JTextField searchField;
    private final JPanel top; // header panel

    // Parametri configurabili
    private static final int MIN_BACHECA_WIDTH = 260;
    private static final int MAX_BACHECA_WIDTH = 520;
    private static final int GAP_BETWEEN = 8;

    // Stato per la visibilità del pannello Scadenze
    private boolean showInScadenza = true;

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

        // --- Layout del pannello TOP ---
        top = new JPanel();
        top.setLayout(new BorderLayout(0, 5));
        top.setBackground(Color.WHITE);

        // 1. Pannello Titolo (con Bottone +)
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 8, 10));

        JLabel appTitle = new JLabel("Gestore ToDo");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        appTitle.setForeground(Color.BLACK);
        titlePanel.add(appTitle, BorderLayout.WEST);

        JButton addBachecaBtn = new JButton("+");
        addBachecaBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        addBachecaBtn.setMargin(new Insets(0, 6, 0, 6));
        addBachecaBtn.addActionListener(e -> showAddBachecaDialog());

        JPanel plusButtonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        plusButtonWrapper.setOpaque(false);
        plusButtonWrapper.add(addBachecaBtn);

        titlePanel.add(plusButtonWrapper, BorderLayout.EAST);

        top.add(titlePanel, BorderLayout.NORTH);

        // 2. Pannello Ricerca (con Bottone Filtri ⛶)
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        searchField = new JTextField();
        searchField.setToolTipText("Cerca per titolo o descrizione (premi Invio)");
        searchField.addActionListener(e -> doSearch(searchField.getText()));
        searchField.setBackground(ColorsConstant.LIGHT_GREY);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton filtriBtn = new JButton("Filtri ⛶");
        filtriBtn.addActionListener(e -> showFiltriMenu(filtriBtn));
        searchPanel.add(filtriBtn, BorderLayout.EAST);

        top.add(searchPanel, BorderLayout.CENTER);
        // --- FINE MODIFICA TOP ---

        add(top, BorderLayout.NORTH);

        // CENTER
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(centerPanel, BorderLayout.CENTER);

        // listener per resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
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

        searchField.setText("");

        int containerWidth = getContentPane().getWidth();
        if (containerWidth <= 0) containerWidth = getWidth();
        int horizontalPadding = 20;
        int availableWidth = Math.max(400, containerWidth - horizontalPadding);

        List<?> bacheche = mainCtrl.getBachecaController().getAllBacheche();
        int bachecheCount = bacheche.size();
        int columns = bachecheCount + (showInScadenza ? 1 : 0);

        if (columns == 0) columns = 1;

        int totalGaps = Math.max(0, (columns - 1) * GAP_BETWEEN);

        int widthPer = (availableWidth - totalGaps) / columns;
        widthPer = Math.max(MIN_BACHECA_WIDTH, widthPer);
        if (MAX_BACHECA_WIDTH > 0) widthPer = Math.min(MAX_BACHECA_WIDTH, widthPer);

        int totalHeight = getContentPane().getHeight();
        int topHeight = top.getPreferredSize().height + 20;
        int availableListHeight = Math.max(240, totalHeight - topHeight - 80);

        boolean isFirstColumnAdded = false;

        if (showInScadenza) {
            InScadenzaPanel inScadenza = new InScadenzaPanel(mainCtrl, widthPer);
            inScadenza.setAlignmentY(Component.TOP_ALIGNMENT);
            inScadenza.setPreferredSize(new Dimension(widthPer, availableListHeight + 80));
            inScadenza.setMaximumSize(new Dimension(widthPer, Integer.MAX_VALUE));
            centerPanel.add(inScadenza);
            isFirstColumnAdded = true;
        }

        for (int i = 0; i < bacheche.size(); i++) {
            if (isFirstColumnAdded) {
                centerPanel.add(Box.createRigidArea(new Dimension(GAP_BETWEEN, 0)));
            }

            model.Bacheca b = (model.Bacheca) bacheche.get(i);
            BachecaPanel bachecaPanel = new BachecaPanel(b, mainCtrl, widthPer, availableListHeight);
            bachecaPanel.setAlignmentY(Component.TOP_ALIGNMENT);
            bachecaPanel.setMaximumSize(new Dimension(widthPer, Integer.MAX_VALUE));
            centerPanel.add(bachecaPanel);

            isFirstColumnAdded = true;
        }

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // --- METODI DI RICERCA ---

    public void doSearch(String query) {
        String q = query.trim();
        if (q.isEmpty()) {
            refreshCenter();
            return;
        }
        java.util.List<ToDo> found = mainCtrl.getToDoController().searchToDo(q);
        showSearchResults(found, "Risultati ricerca per: \"" + q + "\"");
    }

    public void doDateSearch(String dateQuery) {
        String q = dateQuery.trim();
        if (q.isEmpty()) {
            refreshCenter();
            return;
        }

        LocalDate endDate;
        try {
            endDate = LocalDate.parse(q);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato data non valido. Usa AAAA-MM-GG.", "Errore Data", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.util.List<ToDo> found = mainCtrl.getScadenzePerData(endDate);
        showSearchResults(found, "ToDo in scadenza il: " + q);
    }
    // --- FINE METODI RICERCA ---

    // Metodo helper per mostrare i risultati
    private void showSearchResults(java.util.List<ToDo> found, String titleText) {
        centerPanel.removeAll();
        centerPanel.setLayout(new BorderLayout());

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        topRow.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel(titleText);
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
            // --- MODIFICATO: Usa il nuovo WrapPanel ---

            WrapPanel results = new WrapPanel();
            results.setBackground(Color.WHITE);
            results.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            int cardWidth = 300;
            if (cardWidth < MIN_BACHECA_WIDTH) cardWidth = MIN_BACHECA_WIDTH;

            // --- NUOVA RIGA: Aggiunge spazio in cima (per coerenza) ---
            // results.add(Box.createRigidArea(new Dimension(0, 10))); // In realtà non serve in FlowLayout
            // --- FINE NUOVA RIGA ---

            for (ToDo td : found) {
                gui.cards.ToDoCard card = new gui.cards.ToDoCard(td, mainCtrl, cardWidth, false);

                Dimension pref = card.getPreferredSize();
                card.setPreferredSize(new Dimension(cardWidth, pref.height));

                results.add(card);
            }

            JScrollPane resultsScrollPane = new JScrollPane(
                    results,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            resultsScrollPane.getViewport().setBackground(Color.WHITE);
            resultsScrollPane.setBorder(null);

            resultsScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

            centerPanel.add(resultsScrollPane, BorderLayout.CENTER);
            // --- FINE MODIFICA ---
        }

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // --- METODI PER I POPUP ---

    private void showAddBachecaDialog() {
        List<TitoloBacheca> tutte = Stream.of(
                TitoloBacheca.UNIVERSITA,
                TitoloBacheca.LAVORO,
                TitoloBacheca.TEMPO_LIBERO
        ).collect(Collectors.toList());

        List<TitoloBacheca> esistenti = mainCtrl.getBachecaController()
                .getAllBacheche().stream()
                .map(Bacheca::getTitolo)
                .collect(Collectors.toList());

        List<TitoloBacheca> disponibili = tutte.stream()
                .filter(t -> !esistenti.contains(t))
                .collect(Collectors.toList());

        new AddBachecaDialog(this, mainCtrl, disponibili);
    }

    // Dialog per il filtro data
    private void showDateFilterDialog() {
        String dateQuery = JOptionPane.showInputDialog(
                this,
                "Inserisci la data (AAAA-MM-GG):",
                "Filtra per Data",
                JOptionPane.PLAIN_MESSAGE
        );

        if (dateQuery != null && !dateQuery.trim().isEmpty()) {
            doDateSearch(dateQuery.trim());
        }
    }

    // Metodo per il menu "Filtri ⛶"
    private void showFiltriMenu(Component invoker) {
        JPopupMenu filtriMenu = new JPopupMenu();

        // Opzione 1: Toggle Scadenze
        String toggleText = isShowInScadenza() ? "Nascondi Scadenze" : "Mostra Scadenze";
        JMenuItem toggleItem = new JMenuItem(toggleText);
        toggleItem.addActionListener(e -> {
            setShowInScadenza(!isShowInScadenza());
        });
        filtriMenu.add(toggleItem);

        // Opzione 2: Filtra per Data
        JMenuItem filterDateItem = new JMenuItem("Filtra per Data...");
        filterDateItem.addActionListener(e -> {
            showDateFilterDialog();
        });
        filtriMenu.add(filterDateItem);

        // Mostra il menu
        filtriMenu.show(invoker, 0, invoker.getHeight());
    }

    // --- METODI HELPER PER OPZIONI VISTA ---

    public boolean isShowInScadenza() {
        return showInScadenza;
    }

    public void setShowInScadenza(boolean showInScadenza) {
        this.showInScadenza = showInScadenza;
        refreshCenter();
    }

    // --- FINE METODI HELPER ---

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(MainView::new);
    }
}