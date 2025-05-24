package GUI.Panels;
import GUI.Cards.ToDoCard;
import Controllers.MainController;
import Model.ToDo;
import GUI.ColorsConstant;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
public class InScadenzaPanel extends JPanel {
    public InScadenzaPanel(MainController mainCtrl) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(ColorsConstant.Murrey, 1));
        JLabel header = new JLabel("Scadenze di oggi");
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setBorder(new EmptyBorder(10,10,10,10));
        add(header, BorderLayout.NORTH);
        List<ToDo> scadOggi = mainCtrl.getScadenzeOggi();
        if (scadOggi.isEmpty()) {
            JLabel empty = new JLabel("Non ci sono scadenze per oggi");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            add(empty, BorderLayout.CENTER);
        } else {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(Color.WHITE);
            list.setBorder(new EmptyBorder(10,10,10,10));
            for (ToDo td : scadOggi) {
                list.add(new ToDoCard(td));
                list.add(Box.createRigidArea(new Dimension(0,5)));
            }
            JScrollPane scroll = new JScrollPane(
                    list,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );
            scroll.getViewport().setBackground(Color.WHITE);
            add(scroll, BorderLayout.CENTER);
        }
    }
}