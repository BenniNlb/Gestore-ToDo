package gui.cards;

import controllers.MainController;
import gui.dialogs.tododialog.AddEditToDoDialog;
import model.PermessoCondivisione;
import model.ToDo;
import model.Utente;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;

import java.time.LocalDate;
import java.util.Map;

/**
 * Pannello grafico che rappresenta una singola attività (ToDo) sotto forma di "card".
 * <p>
 * Questo componente è l'unità visiva base delle bacheche. Visualizza le informazioni
 * essenziali del ToDo (titolo, checkbox di completamento, anteprima descrizione, immagine,
 * data di scadenza) e fornisce controlli interattivi per modificarlo, eliminarlo o condividerlo.
 * <p>
 * Supporta il Drag &amp; Drop, permettendo all'utente di trascinare la card per riordinarla
 * o spostarla in un'altra bacheca.
 */
public class ToDoCard extends JPanel {

    /**
     * Altezza minima garantita per la card, per evitare che collassi se vuota.
     */
    private static final int MIN_HEIGHT = 90;

    /**
     * Il DataFlavor utilizzato per il trasferimento dei dati durante il Drag &amp; Drop.
     * Identifica univocamente l'oggetto {@link ToDo} trasferito.
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
     * Costruisce una nuova card per visualizzare un ToDo.
     * <p>
     * Configura il layout, lo stile (sfondo, bordi), inizializza i componenti interni
     * (titolo, descrizione, pulsanti) e imposta i listener per gli eventi del mouse
     * e il Drag &amp; Drop.
     *
     * @param todo      L'oggetto {@link ToDo} da visualizzare.
     * @param ctrl      Il controller principale per gestire le azioni utente.
     * @param cardWidth La larghezza desiderata per la card (in pixel).
     * @param draggable {@code true} se la card deve essere trascinabile (Drag &amp; Drop), {@code false} altrimenti.
     */
    public ToDoCard(ToDo todo, MainController ctrl, int cardWidth, boolean draggable) {
        setLayout(new BorderLayout(0, 4)); // 4px di gap verticale
        setBackground(todo.getColoreSfondo() != null ? todo.getColoreSfondo() : Color.WHITE);

        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        Insets insets = getBorder().getBorderInsets(this);
        int contentWidth = cardWidth - insets.left - insets.right;
        if (contentWidth <= 0) contentWidth = 100; // Fallback

        final int idUtenteLoggato = ctrl.getUtenteLoggato().getIdUtente();
        final boolean isAuthor = (idUtenteLoggato == todo.getIdUtenteCreatore());
        final PermessoCondivisione mioPermesso = todo.getPermessoPerUtente(ctrl.getUtenteLoggato());
        final boolean canEdit = isAuthor || PermessoCondivisione.MODIFICA.equals(mioPermesso);
        final boolean canDelete = isAuthor;
        final boolean canManageShares = isAuthor;


        // --- Logica DnD ---
        if (draggable && canEdit) {
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

        JPanel topRow = new JPanel(new BorderLayout(5, 0));
        topRow.setOpaque(false);

        final JTextArea titolo = new JTextArea(todo.getTitolo());
        titolo.setLineWrap(true);
        titolo.setWrapStyleWord(true);
        titolo.setEditable(false);
        titolo.setOpaque(false);
        titolo.setFocusable(false);
        titolo.setBorder(null);
        titolo.setFont(new Font("SansSerif", Font.BOLD, 15));

        JCheckBox checkCompletato = new JCheckBox();
        checkCompletato.setOpaque(false);
        checkCompletato.setSelected(todo.isCompletato());
        checkCompletato.setEnabled(canEdit);

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

        Map<Utente, PermessoCondivisione> condivisioni = todo.getCondivisioni();
        if (condivisioni != null && !condivisioni.isEmpty()) {
            JLabel shareIcon = new JLabel("👥 " + condivisioni.size()); // Testo emoji
            shareIcon.setFont(new Font("SansSerif", Font.PLAIN, 16));
            shareIcon.setToolTipText("Condiviso con " + condivisioni.size() + " utenti");
            shareIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            shareIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

            shareIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    mostraCondivisioni(todo, ctrl);
                }
            });
            topRow.add(shareIcon, BorderLayout.EAST);
        }

        add(topRow, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

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

        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        footer.setOpaque(false);

        JLabel dateLabel = new JLabel(todo.getDataScadenza() != null ? todo.getDataScadenza().toString() : "Senza data");
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton linksBtn = new JButton("Links");
        linksBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        linksBtn.setToolTipText("Mostra link");
        linksBtn.addActionListener(e -> mostraLinks(todo));
        right.add(linksBtn);

        JButton menuBtn = new JButton("⋯");
        menuBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        menuBtn.addActionListener(e -> mostraMenu(ctrl, todo, menuBtn, canEdit, canDelete, canManageShares));
        right.add(menuBtn);

        footer.add(dateLabel);
        footer.add(Box.createHorizontalGlue());
        footer.add(right);

        Dimension footerPref = footer.getPreferredSize();
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, footerPref.height));

        add(footer, BorderLayout.SOUTH);
    }


    private void mostraCondivisioni(ToDo todo, MainController ctrl) {
        Map<Utente, PermessoCondivisione> condivisioni = todo.getCondivisioni();
        int idUtenteLoggato = ctrl.getUtenteLoggato().getIdUtente();
        int idAutore = todo.getIdUtenteCreatore();

        Utente autore = ctrl.getUtenteById(idAutore);
        if (autore == null) {
            JOptionPane.showMessageDialog(this, "Errore nel recuperare l'autore.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("<html><b>Partecipanti:</b><br>");

        String nomeAutore = autore.getUsername();
        String etichettaTuAutore = "";
        if (autore.getIdUtente() == idUtenteLoggato) {
            etichettaTuAutore = " (tu)";
        }
        sb.append("•  ")
                .append(nomeAutore)
                .append(etichettaTuAutore)
                .append(" (<i>Autore</i>)<br>");

        if (condivisioni != null) {
            for (Map.Entry<Utente, PermessoCondivisione> entry : condivisioni.entrySet()) {
                Utente utenteCondiviso = entry.getKey();

                if (utenteCondiviso.getIdUtente() == idAutore) continue;

                String nomeCondiviso = utenteCondiviso.getUsername();
                String ruolo = entry.getValue().toString();
                String etichettaTuCondiviso = "";
                if (utenteCondiviso.getIdUtente() == idUtenteLoggato) {
                    etichettaTuCondiviso = " (tu)";
                }

                sb.append("•  ")
                        .append(nomeCondiviso)
                        .append(etichettaTuCondiviso)
                        .append(" (<i>")
                        .append(ruolo)
                        .append("</i>)<br>");
            }
        }
        sb.append("</html>");

        JOptionPane.showMessageDialog(this, new JLabel(sb.toString()), "Condivisioni", JOptionPane.INFORMATION_MESSAGE);
    }


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


    private void mostraMenu(MainController ctrl, ToDo todo, JButton anchor, boolean canEdit, boolean canDelete, boolean canManageShares) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem edit = new JMenuItem("Modifica");
        edit.addActionListener(ev -> {
            Window owner = SwingUtilities.getWindowAncestor(ToDoCard.this);
            new AddEditToDoDialog(owner, ctrl, todo, true);
        });
        edit.setEnabled(canEdit);
        popup.add(edit);

        JMenuItem share = new JMenuItem("Gestisci Condivisioni...");
        share.addActionListener(ev -> {
            Window owner = SwingUtilities.getWindowAncestor(ToDoCard.this);
            new AddEditToDoDialog(owner, ctrl, todo, false);
        });
        share.setEnabled(canManageShares);
        popup.add(share);

        popup.addSeparator();

        JMenuItem del = new JMenuItem("Elimina");
        del.setForeground(Color.RED);
        del.addActionListener(ev -> {
            int conf = JOptionPane.showConfirmDialog(this, "Eliminare questo ToDo?", "Conferma", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                ctrl.onDeleteToDo(todo);
            }
        });
        del.setEnabled(canDelete);
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

        Image scaled = src.getImage().getScaledInstance(newW, newH, Image.SCALE_AREA_AVERAGING);
        return new ImageIcon(scaled);
    }
}