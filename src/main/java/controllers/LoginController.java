package controllers;

import dao.UtenteDAO;
import dao.postgresimpl.PostgresUtenteDAO;
import database.DBConnection;
import util.PasswordHasher;
import gui.views.LoginView;
import gui.views.MainView;
import gui.views.RegisterView; // IMPORTA RegisterView
import model.Utente;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;

/**
 * Controller (o Manager) per la logica di Login.
 */
public class LoginController {

    private final LoginView loginView;
    private final UtenteDAO utenteDAO;

    public LoginController(LoginView loginView) {
        this.loginView = loginView;

        Connection conn = DBConnection.getConnection();
        this.utenteDAO = new PostgresUtenteDAO();

        this.loginView.addLoginListener(e -> attemptLogin());

        // MODIFICATO: Collega il link
        this.loginView.addRegisterLinkListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRegisterView();
            }
        });
    }

    /**
     * Tenta di eseguire il login.
     */
    private void attemptLogin() {
        String user = loginView.getUsername();
        String pass = loginView.getPassword();

        if (user.isEmpty() || pass.isEmpty()) {
            loginView.mostraErrore("Username e Password sono obbligatori.");
            return;
        }

        Utente utente = utenteDAO.getUtenteByUsername(user);

        // MODIFICATO: Logica di errore
        if (utente != null && PasswordHasher.checkPassword(pass, utente.getPassword())) {
            // LOGIN RIUSCITO
            loginView.mostraSuccesso("Benvenuto " + utente.getLogin() + "!");
            loginView.dispose();

            Utente utenteLoggato = utente;
            SwingUtilities.invokeLater(() -> new MainView(utenteLoggato));
        } else {
            // LOGIN FALLITO (Errore generico)
            loginView.mostraErrore("Username o password sbagliati.");
        }
    }

    /**
     * Chiude la finestra di Login e apre quella di Registrazione.
     */
    private void openRegisterView() {
        loginView.dispose();
        SwingUtilities.invokeLater(() -> new RegisterView().setVisible(true));
    }
}