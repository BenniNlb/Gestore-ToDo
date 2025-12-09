package gui.views;

import controllers.RegisterController;
import util.ColorsConstant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

/**
 * Rappresenta la vista (View) dedicata alla registrazione di nuovi utenti nel sistema.
 * <p>
 * Questa classe estende {@link JFrame} e fornisce l'interfaccia grafica necessaria
 * per l'inserimento dei dati di registrazione (username, password e conferma password).
 * Agisce come componente Boundary nel pattern MVC, delegando la logica di business
 * al {@link RegisterController}.
 */
public class RegisterView extends JFrame {

    /**
     * Campo di testo per l'inserimento dell'username scelto dall'utente.
     */
    JTextField usernameField;

    /**
     * Campo per l'inserimento della password (caratteri oscurati).
     */
    JPasswordField passwordField;

    /**
     * Campo per la conferma della password (deve coincidere con il primo).
     */
    JPasswordField confirmPasswordField;

    /**
     * Pulsante per inviare i dati e completare la registrazione.
     */
    JButton registerButton;

    /**
     * Etichetta cliccabile che permette di tornare alla schermata di login.
     */
    JLabel loginLink;

    /**
     * Riferimento al controller che gestisce la logica di registrazione.
     */
    private RegisterController controller;

    /**
     * Costruisce una nuova finestra di registrazione.
     * <p>
     * Configura le proprietà principali della finestra (titolo, dimensioni, non ridimensionabile),
     * inizializza i componenti grafici e istanzia il controller associato.
     */
    public RegisterView() {
        setTitle("Registrazione");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();

        this.controller = new RegisterController(this);
    }

    /**
     * Inizializza, configura e dispone tutti i componenti grafici all'interno della finestra.
     * Definisce il layout, i colori, i bordi e i campi di input.
     */
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

        loginLink = new JLabel("Hai già un account? Accedi");
        loginLink.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginLink.setForeground(Color.BLUE.darker());
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(loginLink);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    /**
     * Recupera il testo inserito nel campo username.
     * Rimuove eventuali spazi vuoti iniziali e finali.
     *
     * @return La stringa dell'username.
     */
    public String getUsername() {
        return usernameField.getText().trim();
    }

    /**
     * Recupera il testo inserito nel campo password.
     *
     * @return La stringa della password.
     */
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    /**
     * Recupera il testo inserito nel campo di conferma password.
     *
     * @return La stringa della conferma password.
     */
    public String getConfirmPassword() {
        return new String(confirmPasswordField.getPassword());
    }

    /**
     * Registra un listener per gestire l'evento di registrazione.
     * Il listener viene associato sia al pulsante "Registrati" che al campo
     * di conferma password (per permettere l'invio tramite il tasto Invio).
     *
     * @param listener L'{@link ActionListener} da associare agli eventi di input.
     */
    public void addRegisterListener(ActionListener listener) {
        registerButton.addActionListener(listener);
        confirmPasswordField.addActionListener(listener);
    }

    /**
     * Registra un listener per gestire il click sul link "Accedi".
     * Permette di navigare verso la schermata di login.
     *
     * @param listener Il {@link MouseAdapter} da associare all'etichetta del link.
     */
    public void addLoginLinkListener(MouseAdapter listener) {
        loginLink.addMouseListener(listener);
    }

    /**
     * Mostra un messaggio di errore all'utente tramite una finestra di dialogo modale.
     *
     * @param messaggio Il testo dell'errore da visualizzare.
     */
    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Mostra un messaggio di successo all'utente tramite una finestra di dialogo modale.
     *
     * @param messaggio Il testo di successo da visualizzare.
     */
    public void mostraSuccesso(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }
}