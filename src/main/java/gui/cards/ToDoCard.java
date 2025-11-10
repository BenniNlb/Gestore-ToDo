package gui.cards;

import controllers.MainController;
import model.ToDo;
import gui.dialogs.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;

import java.time.LocalDate;
import java.util.Map;

public class ToDoCard extends JPanel {
    private static final int MIN_HEIGHT = 90;

    private static final DataFlavor TODO_FLAVOR;
    static {
        try {
            TODO_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ToDo.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // MODIFICATO: Aggiunto boolean draggable
    public ToDoCard(ToDo todo, MainController ctrl, int cardWidth, boolean draggable) {
        setLayout(new BorderLayout(0, 4)); // 4px di gap verticale
        setBackground(todo.getColoreSfondo() != null ? todo.getColoreSfondo() : Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        Insets insets = getBorder().getBorderInsets(this);
        int contentWidth = cardWidth - insets.left - insets.right;
        if (contentWidth <= 0) contentWidth = 100; // Fallback

        // --- MODIFICATO: Tutta la logica DnD è ora condizionale ---
        if (draggable) {
            // Drag handler
            setTransferHandler(new TransferHandler() {
                @Override
                protected Transferable createTransferable(JComponent c) {
                    return new Transferable() {
                        @Override
                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[]{TODO_FLAVOR};
                        }
                        @Override
                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                            return TODO_FLAVOR.equals(flavor);
                        }
                        @Override
                        public Object getTransferData(DataFlavor flavor) {
                            return todo;
                        }
                    };
                }
                @Override
                public int getSourceActions(JComponent c) {
                    return MOVE;
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    TransferHandler th = getTransferHandler();
                    if (th != null) {
                        th.exportAsDrag(ToDoCard.this, e, TransferHandler.MOVE);
                    }
                }
            });
        }
        // --- FINE MODIFICA ---

        // ===== TOP ROW (CHECKBOX + TITOLO) [BorderLayout.NORTH] =====
        JPanel topRow = new JPanel(new BorderLayout(5, 0));
        topRow.setOpaque(false);

        JLabel titolo = new JLabel(troncaTesto(todo.getTitolo(), 20));
        titolo.setFont(new Font("SansSerif", Font.BOLD, 15));

        JCheckBox checkCompletato = new JCheckBox();
        checkCompletato.setOpaque(false);
        checkCompletato.setSelected(todo.isCompletato());

        boolean scaduto = todo.getDataScadenza() != null && todo.getDataScadenza().isBefore(LocalDate.now());

        if (scaduto && !todo.isCompletato()) {
            titolo.setForeground(Color.RED);
        }

        checkCompletato.addActionListener(e -> {
            boolean isSelected = checkCompletato.isSelected();
            ctrl.onToggleCompletato(todo, isSelected);

            if (scaduto) {
                titolo.setForeground(isSelected ? Color.BLACK : Color.RED);
            } else {
                titolo.setForeground(Color.BLACK);
            }
        });

        topRow.add(checkCompletato, BorderLayout.WEST);
        topRow.add(titolo, BorderLayout.CENTER);
        add(topRow, BorderLayout.NORTH);

        // ===== CONTENT PANEL (Descrizione e Immagine) [BorderLayout.CENTER] =====
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // ===== DESCRIZIONE (opzionale) =====
        if (todo.getDescrizione() != null && !todo.getDescrizione().isBlank()) {
            JTextArea desc = new JTextArea(troncaTesto(todo.getDescrizione(), 500));
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            desc.setEditable(false);
            desc.setOpaque(false);
            desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            desc.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

            contentPanel.add(desc);
        }

        // ===== IMMAGINE (opzionale) =====
        if (todo.getImmagine() != null) {
            ImageIcon img = todo.getImmagine();
            ImageIcon scaled = scaleIconToWidth(img, contentWidth);
            JLabel imgLabel = new JLabel(scaled);
            imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            imgLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

            contentPanel.add(imgLabel);
        }

        if (contentPanel.getComponentCount() == 0) {
            contentPanel.add(Box.createVerticalStrut(MIN_HEIGHT / 3));
        }

        add(contentPanel, BorderLayout.CENTER);

        // ===== FOOTER (Data + Pulsanti) [BorderLayout.SOUTH] =====
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        footer.setOpaque(false);

        JLabel dateLabel = new JLabel(todo.getDataScadenza() != null ? todo.getDataScadenza().toString() : "Senza data");
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);

        JButton linksBtn = new JButton("🔗");
        linksBtn.addActionListener(e -> mostraLinks(todo));
        right.add(linksBtn);

        JButton menuBtn = new JButton("⋯");
        menuBtn.addActionListener(e -> mostraMenu(ctrl, todo, menuBtn));
        right.add(menuBtn);

        footer.add(dateLabel);
        footer.add(Box.createHorizontalGlue());
        footer.add(right);

        Dimension footerPref = footer.getPreferredSize();
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, footerPref.height));

        add(footer, BorderLayout.SOUTH);
    }

    // ===== FUNZIONI DI SUPPORTO =====

    private void mostraLinks(ToDo todo) {
        List<String> links = todo.getLinkURLs();
        if (links == null || links.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessun link associato a questo ToDo", "Links", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Link ToDo", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(6,6));
        DefaultListModel<String> m = new DefaultListModel<>();
        for (String l : links) m.addElement(l);
        JList<String> list = new JList<>(m);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dlg.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton openBtn = new JButton("Apri");
        JButton closeBtn = new JButton("Chiudi");
        row.add(openBtn);
        row.add(closeBtn);
        dlg.add(row, BorderLayout.SOUTH);

        openBtn.addActionListener(a -> {
            String sel = list.getSelectedValue();
            if (sel != null && !sel.isBlank()) {
                try {
                    Desktop.getDesktop().browse(new URI(sel));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Impossibile aprire il link.", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        closeBtn.addActionListener(a -> dlg.dispose());

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void mostraMenu(MainController ctrl, ToDo todo, JButton anchor) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Modifica");
        JMenuItem del = new JMenuItem("Elimina");

        edit.addActionListener(ev -> {
            Window owner = SwingUtilities.getWindowAncestor(ToDoCard.this);
            new AddEditToDoDialog(owner, ctrl, todo);
        });
        del.addActionListener(ev -> {
            int conf = JOptionPane.showConfirmDialog(this, "Eliminare questo ToDo?", "Conferma", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                ctrl.onDeleteToDo(todo);
            }
        });
        popup.add(edit);
        popup.add(del);
        popup.show(anchor, 0, anchor.getHeight());
    }

    private String troncaTesto(String testo, int maxLen) {
        if (testo == null) return "";
        return testo.length() > maxLen ? testo.substring(0, maxLen) + "…" : testo;
    }

    private ImageIcon scaleIconToWidth(ImageIcon src, int maxWidth) {
        if (src == null) return null;
        if (maxWidth <= 0) maxWidth = 100;

        int w = src.getIconWidth();
        int h = src.getIconHeight();
        if (w <= maxWidth) return src;

        int newW = maxWidth;
        int newH = (int) ((double) h / w * newW);

        if (newH <= 0) newH = 1;

        Image scaled = src.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}