package gui.panels;

import gui.cards.ToDoCard;
import util.ColorsConstant;
import model.Bacheca;
import model.ToDo;
import controllers.*;
import gui.dialogs.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.List;

public class BachecaPanel extends JPanel {

    private static final DataFlavor TODO_FLAVOR;
    static {
        try {
            TODO_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ToDo.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Costante per troncare la descrizione
    private static final int MAX_DESC_CHARS_VIEW = 50;

    public BachecaPanel(Bacheca bacheca, MainController mainCtrl, int width, int listHeight) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorsConstant.GREY, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // --- Header con pulsante Modifica ---
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(6,6,6,6));

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
        editBtn.setMargin(new Insets(2, 6, 2, 6));

        editBtn.addActionListener(e -> {
            JPopupMenu bachecaMenu = new JPopupMenu();

            // 1. Aggiungi ToDo
            JMenuItem addAction = new JMenuItem("Aggiungi ToDo...");
            addAction.addActionListener(ev -> {
                new AddEditToDoDialog(
                        (Window) SwingUtilities.getWindowAncestor(this),
                        mainCtrl,
                        null,
                        bacheca.getTitolo()
                );
            });
            bachecaMenu.add(addAction);

            // 2. Modifica Descrizione
            JMenuItem editAction = new JMenuItem("Modifica Descrizione...");
            editAction.addActionListener(ev -> {
                new EditBachecaDialog(
                        (Window) SwingUtilities.getWindowAncestor(this),
                        mainCtrl,
                        bacheca
                );
            });
            bachecaMenu.add(editAction);

            bachecaMenu.addSeparator(); // Separatore

            // 3. Elimina Bacheca
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
        // --- FINE MODIFICA HEADER ---

        // Body: pannello con BoxLayout
        List<ToDo> todos = bacheca.getToDos();
        if (todos == null || todos.isEmpty()) {
            JLabel empty = new JLabel("Non ci sono ToDo da fare");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            empty.setBorder(new EmptyBorder(20,20,20,20));
            empty.setPreferredSize(new Dimension(width - 16, listHeight));
            add(empty, BorderLayout.CENTER);
        } else {
            // Pannello "list" che contiene le card
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(Color.WHITE);
            list.setBorder(new EmptyBorder(0, 0, 0, 0));

            // --- NUOVA RIGA: Aggiunge spazio in cima per il Drop ---
            list.add(Box.createRigidArea(new Dimension(0, 10)));
            // --- FINE NUOVA RIGA ---

            int cardInnerWidth = width - 16 - 0;

            for (ToDo td : todos) {
                ToDoCard card = new ToDoCard(td, mainCtrl, cardInnerWidth, true);
                card.setAlignmentX(Component.CENTER_ALIGNMENT);

                Dimension pref = card.getPreferredSize();
                card.setMaximumSize(new Dimension(cardInnerWidth, pref.height));

                list.add(card);
                list.add(Box.createRigidArea(new Dimension(0,8)));
            }

            JPanel listContainer = new JPanel(new BorderLayout());
            listContainer.setBackground(Color.WHITE);
            listContainer.add(list, BorderLayout.NORTH);

            JScrollPane scroll = new JScrollPane(
                    listContainer,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            scroll.setBorder(null);
            scroll.getViewport().setBackground(Color.WHITE);

            scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

            scroll.setPreferredSize(new Dimension(width - 16, listHeight));
            add(scroll, BorderLayout.CENTER);

            // ====== DropTarget sul pannello "list" ======
            final JPanel targetList = list;
            final JPanel targetContainer = listContainer;
            final controllers.BachecaController bc = mainCtrl.getBachecaController();

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

    private int computeInsertIndex(JPanel listPanel, Point dropPoint) {
        Component[] comps = listPanel.getComponents();
        int index = 0;
        for (Component c : comps) {
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