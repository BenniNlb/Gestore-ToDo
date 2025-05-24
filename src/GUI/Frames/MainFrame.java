package GUI.Frames;

import Controllers.MainController;
import GUI.Panels.BachecaPanel;
import GUI.Panels.InScadenzaPanel;
import GUI.Cards.CardChoice;
import Model.TitoloBacheca;
import Model.ToDo;
import GUI.Cards.ToDoCard;
import GUI.ColorsConstant;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final MainController mainCtrl;
    private final JPanel centerPanel;
    private final JTextField searchField;

    public MainFrame() {
        mainCtrl = new MainController();
        mainCtrl.getBachecaController().addChangeListener(this::refreshCenter);

        setTitle("Gestore ToDo");
        setSize(1200,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Color.WHITE);
        top.setOpaque(true);

        JLabel appTitle = new JLabel("Gestore ToDo");
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        appTitle.setForeground(Color.BLACK);
        appTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        appTitle.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));
        top.add(appTitle);

        JPanel searchRow = new JPanel(new BorderLayout(5,0));
        searchRow.setBackground(Color.WHITE);
        searchRow.setOpaque(true);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchField = new JTextField();
        searchField.setToolTipText("Cerca per titolo o descrizione");
        searchField.addActionListener(e -> doSearch());
        searchField.setBackground(ColorsConstant.PinkFairy);
        searchRow.add(searchField, BorderLayout.CENTER);

        JButton menuBtn = new JButton("☰");
        menuBtn.addActionListener(e -> showCardChoice(menuBtn));
        searchRow.add(menuBtn, BorderLayout.EAST);

        searchRow.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
        top.add(searchRow);

        add(top, BorderLayout.NORTH);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setOpaque(true);

        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        container.setBackground(Color.WHITE);
        container.setOpaque(true);
        container.add(centerPanel, BorderLayout.CENTER);

        add(new JScrollPane(
                container,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        ), BorderLayout.CENTER);

        refreshCenter();
        setVisible(true);
    }

    public void refreshCenter() {
        centerPanel.removeAll();

        InScadenzaPanel inScadenza = new InScadenzaPanel(mainCtrl);
        inScadenza.setAlignmentY(Component.TOP_ALIGNMENT);
        centerPanel.add(inScadenza);
        centerPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        mainCtrl.getBachecaController().getAllBacheche().stream()
                .filter(b -> b.getTitolo() != TitoloBacheca.SCADENZE_DI_OGGI)
                .forEach(b -> {
                    BachecaPanel bachecaPanel = new BachecaPanel(b);
                    bachecaPanel.setAlignmentY(Component.TOP_ALIGNMENT);
                    centerPanel.add(bachecaPanel);
                    centerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
                });

        centerPanel.add(Box.createHorizontalGlue());

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private int getNumberOfCols() {
        return 1 + (int) mainCtrl.getBachecaController().getAllBacheche().stream()
                .filter(b -> b.getTitolo() != TitoloBacheca.SCADENZE_DI_OGGI)
                .count();
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        List<ToDo> found = mainCtrl.getToDoController().searchToDo(q);

        centerPanel.removeAll();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(Box.createVerticalGlue(), BorderLayout.NORTH);
        centerPanel.add(Box.createVerticalGlue(), BorderLayout.SOUTH);

        if (found.isEmpty()) {
            JLabel no = new JLabel("La ricerca di \""+q+"\" non ha prodotto risultati");
            no.setHorizontalAlignment(SwingConstants.CENTER);
            no.setFont(new Font("SansSerif", Font.ITALIC, 14));
            no.setForeground(Color.GRAY);
            centerPanel.add(no, BorderLayout.CENTER);
        } else {
            JPanel results = new JPanel();
            results.setBackground(Color.WHITE);
            results.setOpaque(true);
            results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
            results.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            for (ToDo td : found) {
                ToDoCard card = new ToDoCard(td);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                results.add(card);
                results.add(Box.createRigidArea(new Dimension(0,5)));
            }
            // Aggiungi un glue anche qui per comprimere i risultati della ricerca
            results.add(Box.createVerticalGlue());

            JScrollPane resultsScrollPane = new JScrollPane(
                    results,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
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
        SwingUtilities.invokeLater(MainFrame::new);
    }
}