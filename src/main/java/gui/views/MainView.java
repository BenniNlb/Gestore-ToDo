package gui.views;

import controllers.*;
import gui.panels.*;
import gui.dialogs.AddBachecaDialog;
import model.*;

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
    private final JPanel top; // header panel

    private final Utente utenteLoggato;

    // Parametri configurabili
    private static final int MIN_BACHECA_WIDTH = 260;
    private static final int MAX_BACHECA_WIDTH = 520;
    private static final int GAP_BETWEEN = 8;

    // Stato per la visibilità del pannello Scadenze
    private boolean showInScadenza = true;

    public MainView(Utente utente) {
        this.utenteLoggato = utente;
        mainCtrl = new MainController(utenteLoggato);

        try {
            mainCtrl.getBachecaController().addChangeListener(this::refreshCenter);
        } catch (Exception ignored) {}

        setTitle("Dashboard di " + utenteLoggato.getUsername());
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        JLabel appTitle = new JLabel("Ciao, " + utenteLoggato.getUsername());

        appTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        appTitle.setForeground(Color.BLACK);
        titlePanel.add(appTitle, BorderLayout.WEST);

        // --- NUOVA INTESTAZIONE DESTRA (con testo al posto delle icone) ---
        JPanel eastHeaderButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        eastHeaderButtons.setOpaque(false);

        // Pulsante Aggiungi Bacheca (Testo)
        JButton addBachecaBtn = new JButton("Aggiungi Bacheca");
        addBachecaBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        addBachecaBtn.setToolTipText("Aggiungi una nuova bacheca");
        addBachecaBtn.addActionListener(e -> showAddBachecaDialog());
        eastHeaderButtons.add(addBachecaBtn);

        // Pulsante Cerca (Testo)
        JButton searchButton = new JButton("Cerca");
        searchButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchButton.setToolTipText("Cerca ToDo");
        searchButton.addActionListener(e -> showSearchDialog());
        eastHeaderButtons.add(searchButton);

        // Pulsante Filtri (Testo)
        JButton filtriBtn = new JButton("Filtri");
        filtriBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        filtriBtn.setToolTipText("Filtri e Opzioni");
        filtriBtn.addActionListener(e -> showFiltriMenu(filtriBtn));
        eastHeaderButtons.add(filtriBtn);

        // Pulsante Logout in Rosso con Conferma
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        logoutButton.setForeground(Color.RED);
        logoutButton.addActionListener(e -> doLogout());
        eastHeaderButtons.add(logoutButton);

        titlePanel.add(eastHeaderButtons, BorderLayout.EAST);
        // --- FINE NUOVA INTESTAZIONE ---

        top.add(titlePanel, BorderLayout.NORTH);

        add(top, BorderLayout.NORTH);

        // CENTER
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));

        // --- MODIFICA: Sfondo Dashboard ripristinato a bianco ---
        centerPanel.setBackground(Color.WHITE);
        // --- FINE MODIFICA ---

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

    // Metodo Logout con conferma
    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler effettuare il logout?",
                "Conferma Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
        }
    }

    // Metodo per Dialog Ricerca
    private void showSearchDialog() {
        String query = JOptionPane.showInputDialog(
                this,
                "Cerca per titolo o descrizione:",
                "Ricerca ToDo",
                JOptionPane.PLAIN_MESSAGE
        );
        if (query != null && !query.trim().isEmpty()) {
            doSearch(query.trim());
        }
    }

    /**
     * Ricostruisce la schermata principale con larghezze reattive.
     */
    public void refreshCenter() {
        centerPanel.removeAll();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));

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

    // Metodo helper per mostrare i risultati
    private void showSearchResults(java.util.List<ToDo> found, String titleText) {
        centerPanel.removeAll();
        centerPanel.setLayout(new BorderLayout());

        JPanel topRow = new JPanel(new BorderLayout());
        // --- MODIFICA: Sfondo Risultati ripristinato a bianco ---
        topRow.setBackground(Color.WHITE);
        // --- FINE MODIFICA ---
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
            // --- MODIFICA: Sfondo Risultati ripristinato a bianco ---
            no.setOpaque(true); // Rendi opaco per mostrare lo sfondo bianco
            no.setBackground(Color.WHITE);
            // --- FINE MODIFICA ---
            centerPanel.add(no, BorderLayout.CENTER);
        } else {
            WrapPanel results = new WrapPanel();
            // --- MODIFICA: Sfondo Risultati ripristinato a bianco ---
            results.setBackground(Color.WHITE);
            // --- FINE MODIFICA ---
            results.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            int cardWidth = 300;
            if (cardWidth < MIN_BACHECA_WIDTH) cardWidth = MIN_BACHECA_WIDTH;

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
            // --- MODIFICA: Sfondo Risultati ripristinato a bianco ---
            resultsScrollPane.getViewport().setBackground(Color.WHITE);
            // --- FINE MODIFICA ---
            resultsScrollPane.setBorder(null);

            resultsScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

            centerPanel.add(resultsScrollPane, BorderLayout.CENTER);
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
}