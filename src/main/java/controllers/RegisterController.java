package controllers;

import dao.*;
import dao.postgresimpl.*;
import util.PasswordHasher;
import gui.views.LoginView;
import gui.views.RegisterView;
import model.Bacheca;
import model.TitoloBacheca;
import model.Utente;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * Controller per la logica di Registrazione.
 */
public class RegisterController {

    private final RegisterView registerView;
    private final UtenteDAO utenteDAO;
    private final BachecaDAO bachecaDAO; // Ci serve per creare le bacheche di default

    public RegisterController(RegisterView registerView) {
        this.registerView = registerView;
        this.utenteDAO = new PostgresUtenteDAO();
        this.bachecaDAO = new PostgresBachecaDAO();

        // Collega i pulsanti
        this.registerView.addRegisterListener(e -> attemptRegister());
        this.registerView.addLoginLinkListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLoginView();
            }
        });
    }

    /**
     * Tenta di registrare un nuovo utente.
     */
    private void attemptRegister() {
        String user = registerView.getUsername();
        String pass = registerView.getPassword();
        String confirmPass = registerView.getConfirmPassword();

        // 1. Controlli di validità
        if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            registerView.mostraErrore("Tutti i campi sono obbligatori.");
            return;
        }
        if (!pass.equals(confirmPass)) {
            registerView.mostraErrore("Le password non corrispondono.");
            return;
        }
        if (utenteDAO.utenteEsiste(user)) {
            registerView.mostraErrore("Questo username è già in uso.");
            return;
        }

        // 2. Registrazione
        String hashedPassword = PasswordHasher.hashPassword(pass);
        Utente nuovoUtente = new Utente(user, hashedPassword);

        utenteDAO.addUtente(nuovoUtente); // Salva utente e ottiene l'ID

        // 3. Creazione Bacheche di Default
        creaBachecheDefault(nuovoUtente);

        registerView.mostraSuccesso("Registrazione completata! Ora puoi effettuare il login.");
        openLoginView();
    }

    /**
     * Crea e salva nel DB le 3 bacheche di default per un nuovo utente.
     */
    private void creaBachecheDefault(Utente utente) {
        for (TitoloBacheca t : Arrays.asList(
                TitoloBacheca.UNIVERSITA,
                TitoloBacheca.LAVORO,
                TitoloBacheca.TEMPO_LIBERO)) {

            Bacheca nuovaBacheca = new Bacheca(t, "", utente.getIdUtente());
            bachecaDAO.addBacheca(nuovaBacheca); // Salva su DB
        }
    }

    /**
     * Chiude la finestra di Registrazione e apre quella di Login.
     */
    private void openLoginView() {
        registerView.dispose();
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}