package gui.panels;

import controllers.BachecaController;
import gui.cards.ToDoCard;
import util.ColorsConstant;
import model.Bacheca;
import model.ToDo;
import controllers.MainController;
import gui.dialogs.EditBachecaDialog;
import gui.dialogs.tododialog.AddEditToDoDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.List;

/**
 * Pannello grafico che rappresenta una singola {@link Bacheca} nell'interfaccia utente.
 * <p>
 * Questa classe è responsabile di:
 * <ul>
 * <li>Visualizzare il titolo e la descrizione della bacheca.</li>
 * <li>Mostrare la lista verticale dei {@link ToDo} contenuti.</li>
 * <li>Gestire l'interazione per aggiungere, modificare o eliminare la bacheca.</li>
 * <li><b>Gestire il Drag &amp; Drop:</b> funge da {@code DropTarget} per permettere
 * lo spostamento dei ToDo tra bacheche diverse o il riordinamento nella stessa lista.</li>
 * </ul>
 * <p>
 * <b>Nota UI:</b> La barra di scorrimento verticale è funzionale ma resa invisibile
 * per mantenere un design pulito e minimalista.
 */
public class BachecaPanel extends JPanel {

    /**
     * Il DataFlavor supportato per le operazioni di Drag &amp; Drop.
     * Identifica il trasferimento di oggetti di tipo {@link ToDo}.
     */
    private static final DataFlavor TODO_FLAVOR;
    static {
        try {
            TODO_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ToDo.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Numero massimo di caratteri visualizzabili nella descrizione della bacheca
     * prima che venga troncata.
     */
    private static final int MAX_DESC_CHARS_VIEW = 50;

    /**
     * Costruisce un nuovo pannello per una bacheca specifica.
     * <p>
     * Inizializza il layout, l'header con i controlli, la lista dei ToDo
     * e configura il listener per il Drag &amp; Drop.
     *
     * @param bacheca    L'oggetto {@link Bacheca} (model) da visualizzare.
     * @param mainCtrl   Il controller principale per gestire le azioni (modifica, eliminazione).
     * @param width      La larghezza prefissata del pannello (in pixel).
     * @param listHeight L'altezza dell'area scrollabile della lista (in pixel).
     */
    public BachecaPanel(Bacheca bacheca, MainController mainCtrl, int width, int listHeight) {
        setLayout(new BorderLayout());

        setBackground(ColorsConstant.LIGHT_GREY);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // --- Header (Titolo e Descrizione) ---
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

        // Pulsante Modifica (Matita)
        JButton editBtn = new JButton("✏");
        editBtn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        editBtn.setToolTipText("Opzioni bacheca");

        editBtn.addActionListener(e -> {
            JPopupMenu bachecaMenu = new JPopupMenu();

            JMenuItem addAction = new JMenuItem("Aggiungi ToDo");
            addAction.addActionListener(ev -> {
                new AddEditToDoDialog(
                        (Window) SwingUtilities.getWindowAncestor(this),
                        mainCtrl,
                        null, // Nuovo ToDo
                        bacheca.getTitolo(), // Bacheca di default
                        true // true = Dettagli
                );
            });
            bachecaMenu.add(addAction);

            JMenuItem editAction = new JMenuItem("Modifica Descrizione");
            editAction.addActionListener(ev -> {
                new EditBachecaDialog(
                        (Window) SwingUtilities.getWindowAncestor(this),
                        mainCtrl,
                        bacheca
                );
            });
            bachecaMenu.add(editAction);

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
        });

        JPanel editBtnWrapper = new JPanel(new BorderLayout());
        editBtnWrapper.setOpaque(false);
        editBtnWrapper.add(editBtn, BorderLayout.NORTH);

        header.add(titleDescPanel, BorderLayout.CENTER);
        header.add(editBtnWrapper, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // --- Body: Lista dei ToDo ---
        List<ToDo> todos = bacheca.getToDos();
        if (todos == null || todos.isEmpty()) {
            JLabel empty = new JLabel("Non ci sono ToDo da fare");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            empty.setBorder(new EmptyBorder(20, 20, 20, 20));
            empty.setPreferredSize(new Dimension(width - 16, listHeight));
            add(empty, BorderLayout.CENTER);
        } else {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(ColorsConstant.LIGHT_GREY);
            list.setBorder(new EmptyBorder(0, 0, 0, 0)); // Nessun margine laterale extra necessario

            list.add(Box.createRigidArea(new Dimension(0, 10)));

            // Calcolo larghezza: Width - (padding sx/dx del pannello)
            int cardInnerWidth = width - 16;

            for (ToDo td : todos) {
                ToDoCard card = new ToDoCard(td, mainCtrl, cardInnerWidth, true);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);
                // Forza la larghezza massima per evitare espansioni anomale
                card.setMaximumSize(new Dimension(cardInnerWidth, Integer.MAX_VALUE));

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

            // --- TRUCCO PER NASCONDERE LA SCROLLBAR ---
            // Imposta la dimensione della barra verticale a 0,0 rendendola invisibile.
            // Lo scorrimento tramite rotellina o trackpad continuerà a funzionare.
            scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
            scroll.getVerticalScrollBar().setUnitIncrement(16); // Scorrimento fluido

            scroll.setPreferredSize(new Dimension(width - 16, listHeight));
            add(scroll, BorderLayout.CENTER);

            // --- Gestione Drag & Drop ---
            final JPanel targetList = list;
            final JPanel targetContainer = listContainer;
            final BachecaController bc = mainCtrl.getBachecaController();

            new DropTarget(targetContainer, DnDConstants.ACTION_MOVE, new DropTargetAdapter() {
                @Override
                public void dragEnter(DropTargetDragEvent dtde) {
                    if (!dtde.isDataFlavorSupported(TODO_FLAVOR)) {
                        dtde.rejectDrag();
                    } else {
                        targetContainer.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                    }
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
                        Object obj = tr.getTransferData(TODO_FLAVOR);
                        if (!(obj instanceof ToDo)) {
                            dtde.dropComplete(false);
                            return;
                        }
                        ToDo td = (ToDo) obj;

                        Point dropPointOnList = SwingUtilities.convertPoint(
                                targetContainer,
                                dtde.getLocation(),
                                targetList
                        );

                        Bacheca sorgente = null;
                        for (Bacheca b : bc.getAllBacheche()) {
                            if (b.getToDos().contains(td)) {
                                sorgente = b;
                                break;
                            }
                        }

                        int insertIndex = computeInsertIndex(targetList, dropPointOnList);

                        if (sorgente != null && sorgente.equals(bacheca)) {
                            int srcIndex = sorgente.getToDos().indexOf(td);
                            if (srcIndex != -1) {
                                sorgente.rimuoviToDo(td);
                                if (insertIndex > srcIndex) insertIndex = insertIndex - 1;
                            }
                        } else {
                            if (sorgente != null) {
                                sorgente.rimuoviToDo(td);
                            }
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
                        ex.printStackTrace();
                        try { dtde.dropComplete(false); } catch (Exception ignored) {}
                    } finally {
                        targetContainer.setBorder(null);
                    }
                }
            }, true);
        }

        int totalHeight = listHeight + header.getPreferredSize().height + 32;
        setPreferredSize(new Dimension(width, totalHeight));
    }

    private String troncaTesto(String testo, int maxLen) {
        if (testo == null) return "";
        return testo.length() > maxLen ? testo.substring(0, maxLen) + "…" : testo;
    }

    /**
     * Calcola l'indice di inserimento nella lista in base alla posizione del cursore del mouse.
     * Utilizzato durante l'operazione di Drag &amp; Drop per determinare dove posizionare
     * la card rilasciata.
     *
     * @param listPanel Il pannello contenente la lista delle card.
     * @param dropPoint Le coordinate del punto di rilascio.
     * @return L'indice (0-based) dove inserire il nuovo elemento.
     */
    private int computeInsertIndex(JPanel listPanel, Point dropPoint) {
        Component[] comps = listPanel.getComponents();
        int index = 0;
        for (Component c : comps) {
            if (index == 0 && c instanceof Box.Filler) continue;
            if (!(c instanceof ToDoCard)) continue;

            Rectangle r = c.getBounds();
            int centerY = r.y + r.height / 2;
            if (dropPoint.y < centerY) {
                return index;
            }
            index++;
        }
        return index;
    }
}