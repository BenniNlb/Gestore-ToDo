package gui.views;

import controllers.MainController;
import gui.cards.ToDoCard;
import gui.dialogs.AddBachecaDialog;
import gui.panels.BachecaPanel;
import gui.panels.InScadenzaPanel;
import gui.panels.WrapPanel;
import model.Bacheca;
import model.ToDo;
import model.TitoloBacheca;
import model.Utente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Rappresenta la finestra principale (Dashboard) dell'applicazione.
 * <p>
 * Questa classe funge da vista primaria (Boundary) nel pattern MVC dopo il login.
 * È responsabile della visualizzazione delle bacheche dell'utente, del pannello
 * delle scadenze e di tutti i controlli di navigazione (ricerca, filtri, logout, aggiunta bacheche).
 * <p>
 * La classe gestisce anche un layout reattivo, ricalcolando la larghezza delle colonne
 * in base alle dimensioni della finestra.
 */
public class BoardView extends JFrame {

    /**
     * Riferimento al controller principale per delegare le azioni dell'utente.
     */
    private final MainController mainCtrl;

    /**
     * Pannello centrale a scorrimento orizzontale che contiene le bacheche.
     */
    private final JPanel centerPanel;

    /**
     * Pannello superiore (Header) contenente titolo e pulsanti di azione.
     */
    private final JPanel topPanel;

    /**
     * L'utente attualmente loggato nel sistema.
     */
    private final Utente utenteLoggato;

    /**
     * Larghezza minima in pixel per una colonna (bacheca).
     */
    private static final int MIN_BACHECA_WIDTH = 260;

    /**
     * Larghezza massima in pixel per una colonna (bacheca).
     */
    private static final int MAX_BACHECA_WIDTH = 520;

    /**
     * Spazio orizzontale (gap) in pixel tra le colonne delle bacheche.
     */
    private static final int GAP_BETWEEN = 8;

    /**
     * Flag che indica se il pannello delle "Scadenze di oggi" deve essere visibile.
     */
    private boolean showInScadenza = true;

    /**
     * Costruisce la finestra principale (Dashboard) dell'applicazione.
     * <p>
     * Inizializza il controller, configura il layout della finestra,
     * imposta i listener per il ridimensionamento e costruisce l'interfaccia grafica.
     * Registra inoltre la vista come listener per gli aggiornamenti del modello.
     *
     * @param utente L'utente che ha effettuato il login e di cui visualizzare i dati.
     */
    public BoardView(Utente utente) {
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

        topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(centerPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(BoardView.this::refreshCenter);
            }
        });

        refreshCenter();
        setVisible(true);
    }

    /**
     * Crea e configura il pannello superiore (Header).
     * <p>
     * Include il titolo di benvenuto e i pulsanti per le azioni globali:
     * Aggiungi Bacheca, Cerca, Filtri e Logout.
     *
     * @return Il pannello {@link JPanel} configurato per l'intestazione.
     */
    private JPanel createTopPanel() {
        JPanel top = new JPanel(new BorderLayout(0, 5));
        top.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12, 10, 8, 10));

        JLabel appTitle = new JLabel("Ciao, " + utenteLoggato.getUsername());
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        appTitle.setForeground(Color.BLACK);
        titlePanel.add(appTitle, BorderLayout.WEST);

        JPanel eastButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        eastButtons.setOpaque(false);

        JButton addBachecaBtn = new JButton("Aggiungi Bacheca");
        addBachecaBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        addBachecaBtn.setToolTipText("Aggiungi una nuova bacheca");
        addBachecaBtn.addActionListener(e -> showAddBachecaDialog());
        eastButtons.add(addBachecaBtn);

        JButton searchBtn = new JButton("Cerca");
        searchBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchBtn.setToolTipText("Cerca ToDo");
        searchBtn.addActionListener(e -> showSearchDialog());
        eastButtons.add(searchBtn);

        JButton filtriBtn = new JButton("Filtri");
        filtriBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        filtriBtn.setToolTipText("Filtri e opzioni");
        filtriBtn.addActionListener(e -> showFiltriMenu(filtriBtn));
        eastButtons.add(filtriBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        logoutBtn.setForeground(Color.RED);
        logoutBtn.addActionListener(e -> doLogout());
        eastButtons.add(logoutBtn);

        titlePanel.add(eastButtons, BorderLayout.EAST);
        top.add(titlePanel, BorderLayout.NORTH);

        return top;
    }

    /**
     * Aggiorna e ridisegna il pannello centrale della dashboard.
     * <p>
     * Questo metodo viene chiamato all'inizializzazione, al ridimensionamento della finestra
     * e ogni volta che i dati cambiano. Calcola dinamicamente la larghezza delle colonne
     * (bacheche) in base allo spazio disponibile e ridisegna i componenti {@link BachecaPanel}
     * e {@link InScadenzaPanel}.
     */
    public void refreshCenter() {
        centerPanel.removeAll();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));

        int containerWidth = Math.max(400, getContentPane().getWidth());
        List<Bacheca> bacheche = mainCtrl.getBachecaController().getAllBacheche();
        int columns = bacheche.size() + (showInScadenza ? 1 : 0);
        if (columns == 0) columns = 1;

        int totalGaps = Math.max(0, (columns - 1) * GAP_BETWEEN);
        int widthPer = (containerWidth - totalGaps) / columns;
        widthPer = Math.max(MIN_BACHECA_WIDTH, Math.min(MAX_BACHECA_WIDTH, widthPer));

        int totalHeight = getContentPane().getHeight();
        int topHeight = topPanel.getPreferredSize().height + 20;
        int availableHeight = Math.max(240, totalHeight - topHeight - 80);

        boolean firstColumn = false;

        if (showInScadenza) {
            InScadenzaPanel inScadenza = new InScadenzaPanel(mainCtrl, widthPer);
            inScadenza.setAlignmentY(Component.TOP_ALIGNMENT);
            inScadenza.setPreferredSize(new Dimension(widthPer, availableHeight + 80));
            inScadenza.setMaximumSize(new Dimension(widthPer, Integer.MAX_VALUE));
            centerPanel.add(inScadenza);
            firstColumn = true;
        }

        for (Bacheca b : bacheche) {
            if (firstColumn) centerPanel.add(Box.createRigidArea(new Dimension(GAP_BETWEEN, 0)));

            BachecaPanel panel = new BachecaPanel(b, mainCtrl, widthPer, availableHeight);
            panel.setAlignmentY(Component.TOP_ALIGNMENT);
            panel.setMaximumSize(new Dimension(widthPer, Integer.MAX_VALUE));
            centerPanel.add(panel);

            firstColumn = true;
        }

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    /**
     * Gestisce la procedura di logout dell'utente.
     * <p>
     * Mostra una finestra di conferma. Se confermato, chiude la finestra corrente
     * e riapre la schermata di login.
     */
    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Sei sicuro di voler effettuare il logout?",
                "Conferma Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new gui.views.LoginView().setVisible(true));
        }
    }

    /**
     * Mostra un dialog per l'inserimento di una query di ricerca testuale.
     */
    private void showSearchDialog() {
        String query = JOptionPane.showInputDialog(
                this, "Cerca per titolo o descrizione:", "Ricerca ToDo", JOptionPane.PLAIN_MESSAGE
        );
        if (query != null && !query.trim().isEmpty()) doSearch(query.trim());
    }

    /**
     * Esegue la ricerca testuale dei ToDo e visualizza i risultati.
     *
     * @param query La stringa da cercare nel titolo o nella descrizione.
     */
    public void doSearch(String query) {
        List<ToDo> found = mainCtrl.getToDoController().searchToDo(query);
        showSearchResults(found, "Risultati ricerca per: \"" + query + "\"");
    }

    /**
     * Esegue la ricerca dei ToDo per data di scadenza.
     * <p>
     * Effettua il parsing della data (formato AAAA-MM-GG) e gestisce eventuali errori di formato.
     *
     * @param dateQuery La data da cercare in formato stringa.
     */
    public void doDateSearch(String dateQuery) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateQuery.trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato data non valido. Usa AAAA-MM-GG.", "Errore Data", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<ToDo> found = mainCtrl.getScadenzePerData(date);
        showSearchResults(found, "ToDo in scadenza il: " + dateQuery);
    }

    /**
     * Sostituisce la visualizzazione delle bacheche con un pannello contenente i risultati della ricerca.
     * Correzione: Le card hanno larghezza fissa ma altezza dinamica.
     * Layout: WrapPanel (Orizzontale + a capo).
     *
     * @param found     La lista dei ToDo trovati.
     * @param titleText Il titolo da mostrare sopra i risultati.
     */
    private void showSearchResults(List<ToDo> found, String titleText) {
        centerPanel.removeAll();
        centerPanel.setLayout(new BorderLayout());

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        topRow.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        topRow.add(title, BorderLayout.WEST);

        JButton backBtn = new JButton("← Indietro");
        backBtn.addActionListener(e -> refreshCenter());
        topRow.add(backBtn, BorderLayout.EAST);

        centerPanel.add(topRow, BorderLayout.NORTH);

        if (found.isEmpty()) {
            JLabel no = new JLabel("Nessun risultato trovato", SwingConstants.CENTER);
            no.setFont(new Font("SansSerif", Font.ITALIC, 16));
            no.setForeground(Color.GRAY);
            no.setOpaque(true);
            no.setBackground(Color.WHITE);
            centerPanel.add(no, BorderLayout.CENTER);
        } else {
            gui.panels.WrapPanel listPanel = new gui.panels.WrapPanel();
            listPanel.setBackground(Color.WHITE);
            listPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            int fixedCardWidth = 400;

            for (ToDo td : found) {
                gui.cards.ToDoCard card = new gui.cards.ToDoCard(td, mainCtrl, fixedCardWidth, false);

                card.setAlignmentX(Component.LEFT_ALIGNMENT);

                card.setMaximumSize(new Dimension(fixedCardWidth, Integer.MAX_VALUE));

                listPanel.add(card);
            }

            // --- Pannello Contenitore ---
            JPanel container = new JPanel(new BorderLayout());
            container.setBackground(Color.WHITE);
            container.add(listPanel, BorderLayout.NORTH);

            JScrollPane scroll = new JScrollPane(container,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            scroll.getViewport().setBackground(Color.WHITE);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);

            centerPanel.add(scroll, BorderLayout.CENTER);
        }

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    /**
     * Mostra il menu contestuale con le opzioni di filtro (Toggle Scadenze, Filtro Data).
     *
     * @param invoker Il componente che ha invocato il menu.
     */
    private void showFiltriMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem toggleItem = new JMenuItem(showInScadenza ? "Nascondi Scadenze" : "Mostra Scadenze");
        toggleItem.addActionListener(e -> setShowInScadenza(!showInScadenza));
        menu.add(toggleItem);

        JMenuItem filterDateItem = new JMenuItem("Filtra per Data...");
        filterDateItem.addActionListener(e -> showDateFilterDialog());
        menu.add(filterDateItem);

        menu.show(invoker, 0, invoker.getHeight());
    }

    /**
     * Mostra il dialog per inserire una data di filtro.
     */
    private void showDateFilterDialog() {
        String dateQuery = JOptionPane.showInputDialog(this, "Inserisci la data (AAAA-MM-GG):", "Filtra per Data", JOptionPane.PLAIN_MESSAGE);
        if (dateQuery != null && !dateQuery.trim().isEmpty()) doDateSearch(dateQuery);
    }

    /**
     * Mostra il dialog per l'aggiunta di una nuova bacheca, filtrando quelle già esistenti.
     */
    private void showAddBachecaDialog() {
        List<TitoloBacheca> tutte = Stream.of(TitoloBacheca.UNIVERSITA, TitoloBacheca.LAVORO, TitoloBacheca.TEMPO_LIBERO).collect(Collectors.toList());
        List<TitoloBacheca> esistenti = mainCtrl.getBachecaController().getAllBacheche().stream().map(Bacheca::getTitolo).collect(Collectors.toList());
        List<TitoloBacheca> disponibili = tutte.stream().filter(t -> !esistenti.contains(t)).collect(Collectors.toList());
        new AddBachecaDialog(this, mainCtrl, disponibili);
    }

    /**
     * Restituisce lo stato di visibilità del pannello delle scadenze.
     *
     * @return {@code true} se visibile, {@code false} altrimenti.
     */
    public boolean isShowInScadenza() { return showInScadenza; }

    /**
     * Imposta la visibilità del pannello delle scadenze e aggiorna la vista.
     *
     * @param show {@code true} per mostrare il pannello, {@code false} per nasconderlo.
     */
    public void setShowInScadenza(boolean show) {
        this.showInScadenza = show;
        refreshCenter();
    }
}