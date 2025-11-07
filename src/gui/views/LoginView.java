// NOTA: al momento LoginFrame e MainFrame non sono collegati tra loro.
// Per gestire correttamente l’apertura del MainFrame dopo il login
// è necessario un backend con database (DAO/Repository) per:
//  1. Verificare le credenziali dell’utente (login/password).
//  2. Caricare dal DB le bacheche e i ToDo associati a quell’utente.
// Solo a valle di tale integrazione potremo passare l’utente autenticato
// al MainController e popolare la UI con i suoi dati personali.

package gui.views;
import javax.swing.*;
import java.awt.*;
import gui.ColorsConstant;

public class LoginView extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;
    JButton loginButton;

    public LoginView() {
        setTitle("Gestore ToDo");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        // Pannello principale con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Titolo in alto a sinistra
        JLabel titleLabel = new JLabel("Gestore ToDo");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel con BoxLayout verticale
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Label e campo Username
        JLabel userLabel = new JLabel("UserName");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        userLabel.setForeground(Color.BLACK);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(userLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        usernameField.setBackground(ColorsConstant.LIGHT_GREY);
        usernameField.setForeground(Color.BLACK);
        usernameField.setCaretColor(Color.BLACK);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createLineBorder(ColorsConstant.GREY, 1));
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Label e campo Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        passLabel.setForeground(Color.BLACK);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        passwordField.setBackground(ColorsConstant.LIGHT_GREY);
        passwordField.setForeground(Color.BLACK);
        passwordField.setCaretColor(Color.BLACK);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createLineBorder(ColorsConstant.GREY, 1));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Bottone Enter
        loginButton = new JButton("Enter");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(ColorsConstant.GREY);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(100, 35));
        formPanel.add(loginButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Logica Login
        /*
        loginButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            if (user.equals("admin") && pass.equals("admin")) {
                JOptionPane.showMessageDialog(this, "Benvenuto " + user + "!");
                // this.dispose();
                // new MainFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Credenziali errate.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
        */
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}

