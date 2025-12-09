package gui.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import util.ColorsConstant;
import controllers.LoginController;

/**
 * Rappresenta la finestra di autenticazione (Login) dell'applicazione.
 * <p>
 * Questa classe funge da vista (View) nel pattern MVC per la fase di accesso.
 * Fornisce i campi per l'inserimento di username e password, un pulsante per
 * confermare l'accesso e un collegamento per navigare verso la schermata di registrazione.
 * <p>
 * La logica di business associata a questa vista è gestita dal {@link LoginController}.
 */
public class LoginView extends JFrame {

    /**
     * Campo di testo per l'inserimento dell'username.
     */
    JTextField usernameField;

    /**
     * Campo di testo oscurato per l'inserimento della password.
     */
    JPasswordField passwordField;

    /**
     * Pulsante per avviare la procedura di login.
     */
    JButton loginButton;

    /**
     * Etichetta cliccabile che permette all'utente di passare alla vista di registrazione.
     */
    JLabel registerLink;

    /**
     * Riferimento al controller che gestisce la logica di questa vista.
     */
    private LoginController controller;

    /**
     * Costruisce la finestra di login.
     * <p>
     * Configura le proprietà della finestra (titolo, dimensioni, non ridimensionabile),
     * inizializza i componenti grafici tramite {@link #initComponents()} e istanzia
     * il controller associato.
     */
    public LoginView() {
        setTitle("Accesso");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();

        this.controller = new LoginController(this);
    }

    /**
     * Inizializza, configura e dispone i componenti grafici all'interno della finestra.
     * Definisce il layout, applica gli stili (colori, bordi, font) definiti in
     * {@link ColorsConstant} e aggiunge i campi di input e i pulsanti.
     */
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

        JLabel userLabel = new JLabel("UserName");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        userLabel.setForeground(Color.BLACK);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(userLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        usernameField.setBackground(ColorsConstant.LIGHT_GREY);
        usernameField.setForeground(Color.BLACK);
        usernameField.setCaretColor(Color.BLACK);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createLineBorder(ColorsConstant.GREY, 1));
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        passLabel.setForeground(Color.BLACK);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setBackground(ColorsConstant.LIGHT_GREY);
        passwordField.setForeground(Color.BLACK);
        passwordField.setCaretColor(Color.BLACK);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createLineBorder(ColorsConstant.GREY, 1));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        loginButton = new JButton("Accedi");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(ColorsConstant.GREY);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(100, 35));
        formPanel.add(loginButton);

        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        registerLink = new JLabel("Non sei registrato? Registrati qui");
        registerLink.setFont(new Font("SansSerif", Font.PLAIN, 12));
        registerLink.setForeground(Color.BLUE.darker());
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(registerLink);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    // --- METODI DI SUPPORTO ---

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
     * Recupera la password inserita nel campo protetto.
     *
     * @return La stringa della password.
     */
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    /**
     * Registra un listener per gestire l'evento di login.
     * Il listener viene associato sia al pulsante "Accedi" che ai campi di testo
     * (per permettere l'invio tramite il tasto Invio).
     *
     * @param listener L'{@link ActionListener} fornito dal controller.
     */
    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
        usernameField.addActionListener(listener);
        passwordField.addActionListener(listener);
    }

    /**
     * Registra un listener per gestire il click sul link di registrazione.
     * Permette di navigare verso la {@link RegisterView}.
     *
     * @param listener Il {@link MouseAdapter} fornito dal controller.
     */
    public void addRegisterLinkListener(MouseAdapter listener) {
        registerLink.addMouseListener(listener);
    }

    /**
     * Mostra un messaggio di errore all'utente tramite una finestra di dialogo modale.
     *
     * @param messaggio Il testo dell'errore da visualizzare.
     */
    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
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