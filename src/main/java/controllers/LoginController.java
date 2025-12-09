package controllers;

import dao.UtenteDAO;
import dao.postgresimpl.PostgresUtenteDAO;
import database.DBConnection;
import util.PasswordHasher;
import gui.views.LoginView;
import gui.views.BoardView;
import gui.views.RegisterView;
import model.Utente;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;

/**
 * Controller (Control) responsabile della gestione del processo di autenticazione (Login).
 * <p>
 * Questa classe agisce da intermediario tra la vista di login ({@link LoginView})
 * e il livello di accesso ai dati ({@link UtenteDAO}). Gestisce la verifica delle credenziali,
 * l'hashing delle password per il confronto e la navigazione verso la dashboard principale
 * o la schermata di registrazione.
 */
public class LoginController {

    /**
     * Riferimento alla vista di login gestita da questo controller.
     */
    private final LoginView loginView;

    /**
     * Oggetto DAO per l'accesso ai dati degli utenti nel database.
     */
    private final UtenteDAO utenteDAO;

    /**
     * Costruisce un nuovo LoginController.
     * <p>
     * Inizializza la connessione al database (indirettamente tramite il DAO),
     * istanzia il DAO utente e registra i listener per i pulsanti e i link
     * presenti nella vista di login.
     *
     * @param loginView L'istanza della vista di login da controllare.
     */
    public LoginController(LoginView loginView) {
        this.loginView = loginView;

        Connection conn = DBConnection.getConnection();
        this.utenteDAO = new PostgresUtenteDAO();

        this.loginView.addLoginListener(e -> attemptLogin());

        this.loginView.addRegisterLinkListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRegisterView();
            }
        });
    }

    /**
     * Esegue il tentativo di login utilizzando le credenziali inserite nella vista.
     * <p>
     * Il metodo esegue i seguenti passaggi:
     * <ol>
     * <li>Recupera username e password dalla vista.</li>
     * <li>Valida che i campi non siano vuoti.</li>
     * <li>Cerca l'utente nel database tramite username.</li>
     * <li>Verifica la corrispondenza della password (confrontando gli hash).</li>
     * <li>Se successo: chiude la login view e apre la {@link BoardView}.</li>
     * <li>Se fallimento: mostra un messaggio di errore.</li>
     * </ol>
     */
    private void attemptLogin() {
        String user = loginView.getUsername();
        String pass = loginView.getPassword();

        if (user.isEmpty() || pass.isEmpty()) {
            loginView.mostraErrore("Username e Password sono obbligatori.");
            return;
        }

        Utente utente = utenteDAO.getUtenteByUsername(user);

        if (utente != null && PasswordHasher.checkPassword(pass, utente.getPassword())) {
            loginView.mostraSuccesso("Benvenuto " + utente.getLogin() + "!");
            loginView.dispose();

            Utente utenteLoggato = utente;
            SwingUtilities.invokeLater(() -> new BoardView(utenteLoggato));
        } else {
            loginView.mostraErrore("Username o password sbagliati.");
        }
    }

    /**
     * Gestisce la navigazione verso la schermata di registrazione.
     * <p>
     * Chiude la finestra di login corrente e apre una nuova istanza di {@link RegisterView}.
     */
    private void openRegisterView() {
        loginView.dispose();
        SwingUtilities.invokeLater(() -> new RegisterView().setVisible(true));
    }
}