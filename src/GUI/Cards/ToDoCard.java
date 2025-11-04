package GUI.Cards;

import Controllers.MainController;
import Model.ToDo;
import GUI.Dialogs.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;

public class ToDoCard extends JPanel {
    private static final int MAX_WIDTH = 420;
    private static final int MIN_HEIGHT = 90;
    private static final int INNER_PADDING = 20;


    private static final DataFlavor TODO_FLAVOR;
    static {
        try {
            TODO_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ToDo.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ToDoCard(ToDo todo, MainController ctrl) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(todo.getColoreSfondo() != null ? todo.getColoreSfondo() : new Color(255, 228, 230));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

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

        // ===== TITOLO =====
        JLabel titolo = new JLabel(troncaTesto(todo.getTitolo(), 35));
        titolo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titolo.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titolo);

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
            add(desc);
        }

        // ===== IMMAGINE (opzionale) =====
        if (todo.getImmagine() != null) {
            ImageIcon img = todo.getImmagine();
            ImageIcon scaled = scaleIconToWidth(img, MAX_WIDTH - INNER_PADDING);
            JLabel imgLabel = new JLabel(scaled);
            imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            imgLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            add(imgLabel);
        }

        // ===== FOOTER (Data + Pulsanti) =====
        JPanel footer = new JPanel(new BorderLayout(6, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dateLabel = new JLabel(todo.getDataScadenza() != null ? todo.getDataScadenza().toString() : "Senza data");
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.add(dateLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);

        // Bottone Links
        JButton linksBtn = new JButton("Links");
        linksBtn.addActionListener(e -> mostraLinks(todo));
        right.add(linksBtn);

        // Bottone ⋯ (menu)
        JButton menuBtn = new JButton("⋯");
        menuBtn.addActionListener(e -> mostraMenu(ctrl, todo, menuBtn));
        right.add(menuBtn);

        footer.add(right, BorderLayout.EAST);

        // Imposta dimensione massima orizzontale
        Dimension footerPref = footer.getPreferredSize();
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, footerPref.height));
        footer.setPreferredSize(new Dimension(MAX_WIDTH - INNER_PADDING, footerPref.height));

        add(Box.createRigidArea(new Dimension(0, 6)));
        add(footer);

        // Margine inferiore
        add(Box.createVerticalStrut(5));

        // Dimensioni dinamiche
        setMaximumSize(new Dimension(MAX_WIDTH, Integer.MAX_VALUE));
        revalidate();
        int prefH = getPreferredSize().height;
        setPreferredSize(new Dimension(MAX_WIDTH, Math.max(MIN_HEIGHT, prefH)));
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
        int w = src.getIconWidth();
        int h = src.getIconHeight();
        if (w <= maxWidth) return src;
        int newW = maxWidth;
        int newH = (int) ((double) h / w * newW);
        Image scaled = src.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }


}
