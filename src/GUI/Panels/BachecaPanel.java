package GUI.Panels;

import GUI.Cards.ToDoCard;
import GUI.ColorsConstant;
import Model.Bacheca;
import Model.ToDo;
import Controllers.MainController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.List;

/**
 * Panel che mostra una singola bacheca e contiene il pannello "list" dove sono inserite
 * le ToDoCard. Su quel pannello è attivo un DropTarget che calcola la posizione di inserimento
 * in base alla y del drop, così si può riordinare all'interno della stessa bacheca oppure
 * spostare tra bacheche.
 */
public class BachecaPanel extends JPanel {

    // flavor JVM-local per ToDo (uguale a quello usato nelle ToDoCard)
    private static final DataFlavor TODO_FLAVOR;
    static {
        try {
            TODO_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ToDo.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public BachecaPanel(Bacheca bacheca, MainController mainCtrl, int width, int listHeight) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorsConstant.Murrey, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Header (fuori dallo scroll)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(6,6,6,6));
        JLabel title = new JLabel(bacheca.getTitolo().toString());
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel desc = new JLabel(bacheca.getDescrizione() != null ? bacheca.getDescrizione() : "");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        header.add(title, BorderLayout.NORTH);
        header.add(desc,  BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Body: pannello con BoxLayout che contiene ToDoCard (wrap in JScrollPane)
        List<ToDo> todos = bacheca.getToDos();
        if (todos == null || todos.isEmpty()) {
            JLabel empty = new JLabel("Non ci sono ToDo da fare");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            empty.setBorder(new EmptyBorder(20,20,20,20));
            // Impostiamo dimensione fissa per mantenere tutte le bacheche uguali
            empty.setPreferredSize(new Dimension(width - 16, listHeight));
            add(empty, BorderLayout.CENTER);
        } else {
            // Pannello "list" che contiene le card (sul quale registreremo il DropTarget)
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(Color.WHITE);
            list.setBorder(new EmptyBorder(10,10,10,10));

            int cardInnerWidth = width - 32; // consider padding and borders

            for (ToDo td : todos) {
                ToDoCard card = new ToDoCard(td, mainCtrl);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Adattiamo la larghezza della card, e fissiamo l'altezza al preferred (così non si allunga)
                Dimension pref = card.getPreferredSize();
                card.setPreferredSize(new Dimension(cardInnerWidth, pref.height));
                card.setMaximumSize(new Dimension(cardInnerWidth, pref.height));
                list.add(card);
                list.add(Box.createRigidArea(new Dimension(0,8)));
            }

            JScrollPane scroll = new JScrollPane(
                    list,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            scroll.setBorder(null);
            scroll.getViewport().setBackground(Color.WHITE);

            // Fissiamo la dimensione del viewport: larghezza fissa e altezza limitata
            scroll.setPreferredSize(new Dimension(width - 16, listHeight));
            add(scroll, BorderLayout.CENTER);

            // ====== DropTarget sul pannello "list" (gestisce reordering / spostamento) ======
            final JPanel targetList = list;
            final Controllers.BachecaController bc = mainCtrl.getBachecaController();

            new DropTarget(targetList, DnDConstants.ACTION_MOVE, new DropTargetAdapter() {
                @Override
                public void dragEnter(DropTargetDragEvent dtde) {
                    if (!dtde.isDataFlavorSupported(TODO_FLAVOR)) {
                        dtde.rejectDrag();
                    } else {
                        // evidenzia leggermente il pannello
                        targetList.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.BLUE, 2),
                                new EmptyBorder(8, 8, 8, 8)
                        ));
                    }
                }

                @Override
                public void dragExit(DropTargetEvent dte) {
                    // ripristina
                    targetList.setBorder(new EmptyBorder(10,10,10,10));
                    // il bordo esterno è gestito dal container BachecaPanel, quindi lo lasciamo semplice
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

                        // punto di drop relativo al pannello "list"
                        Point dropPoint = dtde.getLocation();

                        // Trova la bacheca sorgente che contiene td (se c'è)
                        Bacheca sorgente = null;
                        for (Bacheca b : bc.getAllBacheche()) {
                            if (b.getToDos().contains(td)) {
                                sorgente = b;
                                break;
                            }
                        }

                        // Calcola indice di inserimento nella bacheca di destinazione (bacheca)
                        int insertIndex = computeInsertIndex(targetList, dropPoint);

                        // Se sorgente == destinazione gestiamo l'offset dovuto alla rimozione
                        if (sorgente != null && sorgente.equals(bacheca)) {
                            int srcIndex = sorgente.getToDos().indexOf(td);
                            if (srcIndex != -1) {
                                // rimuoviamo temporaneamente
                                sorgente.rimuoviToDo(td);
                                // se spostiamo verso il basso e il nuovo indice > srcIndex,
                                // dobbiamo decrementare insertIndex perché la rimozione sposta gli elementi
                                if (insertIndex > srcIndex) insertIndex = insertIndex - 1;
                            }
                        } else {
                            // se td proveniva da altra bacheca, rimuovilo dalla sorgente
                            if (sorgente != null) {
                                sorgente.rimuoviToDo(td);
                            }
                        }

                        // Limiti
                        if (insertIndex < 0) insertIndex = 0;
                        if (insertIndex > bacheca.getToDos().size()) insertIndex = bacheca.getToDos().size();

                        // Inserisci nella posizione calcolata
                        bacheca.getToDos().add(insertIndex, td);

                        // Notifica e conclude drop
                        bc.notifyChange();
                        dtde.dropComplete(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        try { dtde.dropComplete(false); } catch (Exception ignored) {}
                    } finally {
                        // ripristina bordo
                        targetList.setBorder(new EmptyBorder(10,10,10,10));
                    }
                }
            }, true);
        }

        // Impostiamo dimensioni complessive della bacheca
        int totalHeight = listHeight + header.getPreferredSize().height + 32;
        setPreferredSize(new Dimension(width, totalHeight));
    }

    /**
     * Calcola l'indice di inserimento nella lista (basato sulle ToDoCard presenti)
     * confrontando la y del drop con i centri verticali delle card.
     */
    private int computeInsertIndex(JPanel listPanel, Point dropPoint) {
        Component[] comps = listPanel.getComponents();
        int index = 0;
        for (Component c : comps) {
            if (!(c instanceof ToDoCard)) continue; // ignora gli spacer
            Rectangle r = c.getBounds();
            int centerY = r.y + r.height / 2;
            if (dropPoint.y < centerY) {
                return index;
            }
            index++;
        }
        // se non trovato, inserisci in coda
        return index;
    }
}
