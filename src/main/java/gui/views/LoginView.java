package gui.views;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import util.ColorsConstant;
import controllers.LoginController;

public class LoginView extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;
    JButton loginButton;
    JLabel registerLink; // Sostituisce il pulsante Registrati

    private LoginController controller;

    public LoginView() {
        setTitle("Accesso"); // MODIFICATO: Titolo
        setSize(500, 350); // MODIFICATO: Finestra più piccola
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();

        this.controller = new LoginController(this);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Accesso");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        // Label e campo Username
        JLabel userLabel = new JLabel("UserName");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        userLabel.setForeground(Color.BLACK);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(userLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        usernameField = new JTextField();
        // MODIFICATO: Ristretto
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
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
        // MODIFICATO: Ristretto
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setBackground(ColorsConstant.LIGHT_GREY);
        passwordField.setForeground(Color.BLACK);
        passwordField.setCaretColor(Color.BLACK);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createLineBorder(ColorsConstant.GREY, 1));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Bottone Accedi
        loginButton = new JButton("Accedi"); // MODIFICATO: Testo
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(ColorsConstant.GREY);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(100, 35));
        formPanel.add(loginButton);

        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- NUOVO: Link per Registrarsi ---
        registerLink = new JLabel("Non sei registrato? Registrati qui");
        registerLink.setFont(new Font("SansSerif", Font.PLAIN, 12));
        registerLink.setForeground(Color.BLUE.darker());
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(registerLink);
        // --- FINE NUOVO ---

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    // --- METODI DI SUPPORTO ---

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
        usernameField.addActionListener(listener);
        passwordField.addActionListener(listener);
    }

    // NUOVO: Listener per il link
    public void addRegisterLinkListener(MouseAdapter listener) {
        registerLink.addMouseListener(listener);
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    public void mostraSuccesso(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }
}