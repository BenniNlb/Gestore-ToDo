package gui.views;

import controllers.RegisterController;
import util.ColorsConstant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Nuova finestra per la registrazione di un utente.
 */
public class RegisterView extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;
    JPasswordField confirmPasswordField; // NUOVO: Conferma password
    JButton registerButton;
    JLabel loginLink; // Torna al login

    private RegisterController controller;

    public RegisterView() {
        setTitle("Registrazione");
        setSize(500, 400); // Leggermente più alta per il campo in più
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();

        // Collega al suo controller
        this.controller = new RegisterController(this);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Registrati");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 25, 25));

        // Username
        JLabel userLabel = new JLabel("UserName");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(userLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        usernameField.setBackground(ColorsConstant.LIGHT_GREY);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createLineBorder(ColorsConstant.GREY, 1));
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setBackground(ColorsConstant.LIGHT_GREY);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createLineBorder(ColorsConstant.GREY, 1));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Conferma Password
        JLabel confirmPassLabel = new JLabel("Conferma Password");
        confirmPassLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        confirmPassLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(confirmPassLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        confirmPasswordField.setBackground(ColorsConstant.LIGHT_GREY);
        confirmPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmPasswordField.setBorder(BorderFactory.createLineBorder(ColorsConstant.GREY, 1));
        formPanel.add(confirmPasswordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Bottone Registrati
        registerButton = new JButton("Registrati");
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.setForeground(Color.WHITE);
        registerButton.setBackground(ColorsConstant.GREY);
        registerButton.setFocusPainted(false);
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.setPreferredSize(new Dimension(100, 35));
        formPanel.add(registerButton);

        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Link per tornare al Login
        loginLink = new JLabel("Hai già un account? Accedi");
        loginLink.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginLink.setForeground(Color.BLUE.darker());
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(loginLink);

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

    public String getConfirmPassword() {
        return new String(confirmPasswordField.getPassword());
    }

    public void addRegisterListener(ActionListener listener) {
        registerButton.addActionListener(listener);
        confirmPasswordField.addActionListener(listener); // Attiva con Invio
    }

    public void addLoginLinkListener(MouseAdapter listener) {
        loginLink.addMouseListener(listener);
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
    }

    public void mostraSuccesso(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }
}