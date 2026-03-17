package gui.panels;

import controllers.BachecaController;
import gui.cards.ToDoCard;
import util.ColorsConstant;
import model.Bacheca;
import model.ToDo;
import controllers.MainController;
import gui.dialogs.EditBachecaDialog;
import gui.dialogs.tododialog.AddEditToDoDialog;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pannello grafico che rappresenta una singola {@link Bacheca} nell'interfaccia utente.
 * <p>
 * Questa classe gestisce la visualizzazione di una colonna di ToDo, includendo:
 * <ul>
 * <li>L'intestazione con titolo, descrizione e menu di modifica.</li>
 * <li>Un menu nascosto (nella matita) per filtrare e ordinare le card.</li>
 * <li>La lista scrollabile dei {@link ToDoCard} associati alla bacheca.</li>
 * <li>Il supporto al Drag &amp; Drop per lo spostamento e il riordinamento manuale dei ToDo.</li>
 * </ul>
 */
public class BachecaPanel extends JPanel {

    /** Logger per la registrazione degli errori del pannello. */
    private static final Logger LOGGER = Logger.getLogger(BachecaPanel.class.getName());

    /** DataFlavor per il riconoscimento dell'oggetto ToDo durante il Drag & Drop. */
    private static final DataFlavor TODO_FLAVOR;
    static {
        try {
            TODO_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ToDo.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** Numero massimo di caratteri per la descrizione mostrata nell'header. */
    private static final int MAX_DESC_CHARS_VIEW = 50;

    /** Mappa statica per mantenere i filtri attivi tra i vari rinfreschi della UI. */
    private static final java.util.Map<String, String> filtriMemoria = new java.util.HashMap<>();

    /** Mappa statica per mantenere l'ordinamento attivo tra i vari rinfreschi della UI. */
    private static final java.util.Map<String, String> ordiniMemoria = new java.util.HashMap<>();

    /** La bacheca rappresentata dal pannello. */
    private final Bacheca bacheca;

    /** Il controller principale per le operazioni di business. */
    private final MainController mainCtrl;

    /** Larghezza interna calcolata per le card. */
    private final int cardInnerWidth;

    /** Altezza dell'area della lista (per allineamento pixel-perfect). */
    private final int listHeight;

    /** Pannello contenitore della lista di ToDo. */
    private JPanel listPanel;

    /** Stringa del filtro attualmente selezionato. */
    private String currentFiltro;

    /** Stringa dell'ordinamento attualmente selezionato. */
    private String currentOrdina;

    /**
     * Costruisce un nuovo pannello per la bacheca specificata.
     *
     * @param bacheca    L'entità bacheca da visualizzare.
     * @param mainCtrl   Il controller principale dell'applicazione.
     * @param width      La larghezza totale del pannello.
     * @param listHeight L'altezza dell'area scorrevole.
     */
    public BachecaPanel(Bacheca bacheca, MainController mainCtrl, int width, int listHeight) {
        this.bacheca = bacheca;
        this.mainCtrl = mainCtrl;
        this.cardInnerWidth = width - 16;
        this.listHeight = listHeight;

        // Recupera i filtri salvati per questa bacheca (o usa i default)
        String bachecaTitolo = bacheca.getTitolo().toString();
        this.currentFiltro = filtriMemoria.getOrDefault(bachecaTitolo, "Da fare");
        this.currentOrdina = ordiniMemoria.getOrDefault(bachecaTitolo, "Predefinito");

        setLayout(new BorderLayout());
        setBackground(ColorsConstant.LIGHT_GREY);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        //HEADER
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(ColorsConstant.LIGHT_GREY);
        header.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel titleDescPanel = new JPanel(new BorderLayout());
        titleDescPanel.setOpaque(false);

        JLabel title = new JLabel(bacheca.getTitolo().toString());
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        String descText = bacheca.getDescrizione() != null ? bacheca.getDescrizione() : "";
        descText = troncaTesto(descText, MAX_DESC_CHARS_VIEW);

        JTextArea desc = new JTextArea(descText);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setFocusable(false);
        desc.setBorder(null);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(UIManager.getColor("Label.foreground"));

        titleDescPanel.add(title, BorderLayout.NORTH);
        titleDescPanel.add(desc,  BorderLayout.SOUTH);

        JButton editBtn = new JButton("✏");
        editBtn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        editBtn.setToolTipText("Opzioni bacheca");
        editBtn.addActionListener(e -> apriMenuBacheca(editBtn));

        JPanel editBtnWrapper = new JPanel(new BorderLayout());
        editBtnWrapper.setOpaque(false);
        editBtnWrapper.add(editBtn, BorderLayout.NORTH);

        header.add(titleDescPanel, BorderLayout.CENTER);
        header.add(editBtnWrapper, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // CREAZIONE AREA DELLA LISTA
        listPanel = new JPanel();
        listPanel.setBackground(ColorsConstant.LIGHT_GREY);
        listPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel listContainer = new JPanel(new BorderLayout());
        listContainer.setBackground(ColorsConstant.LIGHT_GREY);
        listContainer.add(listPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(
                listContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ColorsConstant.LIGHT_GREY);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(width - 16, listHeight));
        add(scroll, BorderLayout.CENTER);

        //INIZIALIZZAZIONE DATI E DND
        aggiornaLista();
        configuraDragAndDrop(listContainer, listPanel);

        int totalHeight = listHeight + header.getPreferredSize().height + 32;
        setPreferredSize(new Dimension(width, totalHeight));
    }

    /**
     * Svuota il pannello, applica i filtri salvati in memoria e ridisegna le card.
     */
    private void aggiornaLista() {
        listPanel.removeAll();
        LocalDate oggi = LocalDate.now();

        List<ToDo> filtrati = bacheca.getToDos().stream()
                .filter(t -> {
                    boolean scaduto = t.getDataScadenza() != null && t.getDataScadenza().isBefore(oggi);
                    if ("Scaduti".equals(currentFiltro)) return scaduto;
                    if ("Da fare".equals(currentFiltro)) return !scaduto;
                    return true;
                })
                .sorted((t1, t2) -> {
                    if ("Predefinito".equals(currentOrdina)) return 0;

                    LocalDate d1 = t1.getDataScadenza();
                    LocalDate d2 = t2.getDataScadenza();

                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1;
                    if (d2 == null) return -1;

                    if ("Scadenza ↑".equals(currentOrdina)) {
                        return d1.compareTo(d2);
                    } else {
                        return d2.compareTo(d1);
                    }
                })
                .collect(Collectors.toList());

        if (filtrati.isEmpty()) {
            // Forza il pannello a prendere tutta l'altezza per centrare perfettamente il testo
            listPanel.setLayout(new GridBagLayout());
            listPanel.setPreferredSize(new Dimension(cardInnerWidth, listHeight - 40));

            JLabel empty = new JLabel("Nessun ToDo in programma");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(new Color(150, 150, 150));
            listPanel.add(empty);
        } else {
            // Ripristina l'altezza dinamica (null) per permettere lo scorrimento
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setPreferredSize(null);

            listPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            for (ToDo td : filtrati) {
                ToDoCard card = new ToDoCard(td, mainCtrl, cardInnerWidth, true);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);
                card.setMaximumSize(new Dimension(cardInnerWidth, Integer.MAX_VALUE));
                listPanel.add(card);
                listPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * Apre il popup per gestire i filtri della bacheca e salva la scelta in memoria.
     */
    private void apriDialogFiltri() {
        JComboBox<String> comboFiltro = new JComboBox<>(new String[]{"Tutti", "Da fare", "Scaduti"});
        comboFiltro.setSelectedItem(currentFiltro);

        JComboBox<String> comboOrdina = new JComboBox<>(new String[]{"Predefinito", "Scadenza ↑", "Scadenza ↓"});
        comboOrdina.setSelectedItem(currentOrdina);

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Mostra:"));
        panel.add(comboFiltro);
        panel.add(new JLabel("Ordina per:"));
        panel.add(comboOrdina);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Filtri ToDo - " + bacheca.getTitolo(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            currentFiltro = (String) comboFiltro.getSelectedItem();
            currentOrdina = (String) comboOrdina.getSelectedItem();

            String bachecaTitolo = bacheca.getTitolo().toString();
            filtriMemoria.put(bachecaTitolo, currentFiltro);
            ordiniMemoria.put(bachecaTitolo, currentOrdina);

            aggiornaLista();
        }
    }

    private void apriMenuBacheca(JButton editBtn) {
        JPopupMenu bachecaMenu = new JPopupMenu();

        JMenuItem addAction = new JMenuItem("Aggiungi ToDo");
        addAction.addActionListener(ev -> {
            new AddEditToDoDialog((Window) SwingUtilities.getWindowAncestor(this), mainCtrl, null, bacheca.getTitolo(), true);
        });
        bachecaMenu.add(addAction);

        JMenuItem editAction = new JMenuItem("Modifica Descrizione");
        editAction.addActionListener(ev -> {
            new EditBachecaDialog((Window) SwingUtilities.getWindowAncestor(this), mainCtrl, bacheca);
        });
        bachecaMenu.add(editAction);

        JMenuItem filtriAction = new JMenuItem("Filtri ToDo");
        filtriAction.addActionListener(ev -> apriDialogFiltri());
        bachecaMenu.add(filtriAction);

        bachecaMenu.addSeparator();

        JMenuItem deleteAction = new JMenuItem("Elimina Bacheca");
        deleteAction.setForeground(Color.RED);
        deleteAction.addActionListener(ev -> {
            int conf = JOptionPane.showConfirmDialog(
                    this,
                    "Eliminare la bacheca \"" + bacheca.getTitolo() + "\"?\n(Tutti i ToDo al suo interno saranno persi)",
                    "Conferma Eliminazione",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (conf == JOptionPane.YES_OPTION) {
                mainCtrl.onDeleteBacheca(bacheca.getTitolo());
            }
        });
        bachecaMenu.add(deleteAction);

        bachecaMenu.show(editBtn, 0, editBtn.getHeight());
    }

    private void configuraDragAndDrop(JPanel targetContainer, JPanel targetList) {
        final BachecaController bc = mainCtrl.getBachecaController();

        new DropTarget(targetContainer, DnDConstants.ACTION_MOVE, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (!dtde.isDataFlavorSupported(TODO_FLAVOR)) dtde.rejectDrag();
                else targetContainer.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                targetContainer.setBorder(null);
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    if (!dtde.isDataFlavorSupported(TODO_FLAVOR)) {
                        dtde.rejectDrop();
                        return;
                    }
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    Transferable tr = dtde.getTransferable();
                    ToDo td = (ToDo) tr.getTransferData(TODO_FLAVOR);

                    Point dropPointOnList = SwingUtilities.convertPoint(targetContainer, dtde.getLocation(), targetList);

                    Bacheca sorgente = null;
                    for (Bacheca b : bc.getAllBacheche()) {
                        if (b.getToDos().contains(td)) {
                            sorgente = b;
                            break;
                        }
                    }

                    boolean isFilteredOrSorted = !"Tutti".equals(currentFiltro) || !"Predefinito".equals(currentOrdina);
                    int insertIndex;

                    if (isFilteredOrSorted) {
                        insertIndex = bacheca.getToDos().size();
                    } else {
                        insertIndex = computeInsertIndex(targetList, dropPointOnList);
                    }

                    if (sorgente != null && sorgente.equals(bacheca)) {
                        int srcIndex = sorgente.getToDos().indexOf(td);
                        if (srcIndex != -1) {
                            sorgente.rimuoviToDo(td);
                            if (insertIndex > srcIndex) insertIndex--;
                        }
                    } else if (sorgente != null) {
                        sorgente.rimuoviToDo(td);
                    }

                    if (insertIndex < 0) insertIndex = 0;
                    if (insertIndex > bacheca.getToDos().size()) insertIndex = bacheca.getToDos().size();

                    bacheca.getToDos().add(insertIndex, td);

                    mainCtrl.onSalvaOrdineBacheca(bacheca);
                    if (sorgente != null && !sorgente.equals(bacheca)) {
                        mainCtrl.onSalvaOrdineBacheca(sorgente);
                    }

                    bc.notifyChange();
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Errore imprevisto durante il Drag and Drop del ToDo", ex);
                    try { dtde.dropComplete(false); } catch (Exception ignored) {}
                } finally {
                    targetContainer.setBorder(null);
                }
            }
        }, true);
    }

    private String troncaTesto(String testo, int maxLen) {
        if (testo == null) return "";
        return testo.length() > maxLen ? testo.substring(0, maxLen) + "…" : testo;
    }

    private int computeInsertIndex(JPanel listPanel, Point dropPoint) {
        Component[] comps = listPanel.getComponents();
        int index = 0;
        for (Component c : comps) {
            if (index == 0 && c instanceof Box.Filler) continue;
            if (!(c instanceof ToDoCard)) continue;

            Rectangle r = c.getBounds();
            int centerY = r.y + r.height / 2;
            if (dropPoint.y < centerY) return index;
            index++;
        }
        return index;
    }
}