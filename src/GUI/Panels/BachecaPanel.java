package GUI.Panels;
import GUI.Cards.ToDoCard;
import GUI.ColorsConstant;
import Model.Bacheca;
import Model.ToDo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
public class BachecaPanel extends JPanel {
    public BachecaPanel(Bacheca bacheca) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(ColorsConstant.Murrey, 1));
// Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(10,10,10,10));
        JLabel title = new JLabel(bacheca.getTitolo().toString());
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        JLabel desc = new JLabel(bacheca.getDescrizione() != null
                ? bacheca.getDescrizione() : "");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        header.add(title, BorderLayout.NORTH);
        header.add(desc, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
// Body
        List<ToDo> todos = bacheca.getToDos();
        if (todos.isEmpty()) {
            JLabel empty = new JLabel("Non ci sono ToDo da fare");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            add(empty, BorderLayout.CENTER);
        } else {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(Color.WHITE);
            list.setBorder(new EmptyBorder(10,10,10,10));
            for (ToDo td : todos) {
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