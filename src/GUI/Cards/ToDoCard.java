package GUI.Cards;
import Model.ToDo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
public class ToDoCard extends JPanel {
    public ToDoCard(ToDo td) {
        setLayout(new BorderLayout());
        setBackground(td.getColoreSfondo() != null
                ? td.getColoreSfondo() : Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel lblTitolo = new JLabel(td.getTitolo());
        lblTitolo.setFont(new Font("SansSerif", Font.BOLD, 14));
        if (td.getDataScadenza() != null
                && td.getDataScadenza().isBefore(LocalDate.now())
                && !td.isCompletato()) {
            lblTitolo.setForeground(Color.RED);
        }
        JLabel lblData = new JLabel(
                td.getDataScadenza() != null
                        ? td.getDataScadenza().toString()
                        : "");
        lblData.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblData.setForeground(Color.DARK_GRAY);
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(getBackground());
        top.setBorder(new EmptyBorder(5,5,5,5));
        top.add(lblTitolo, BorderLayout.WEST);
        top.add(lblData, BorderLayout.EAST);
        add(top, BorderLayout.CENTER);
    }
}