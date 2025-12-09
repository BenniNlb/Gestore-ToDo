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
 * Controller (Control) responsabile della gestione del processo di registrazione di nuovi utenti.
 * <p>
 * Questa classe gestisce l'interazione tra la vista di registrazione ({@link RegisterView})
 * e il livello di persistenza. Si occupa di:
 * <ul>
 * <li>Validare i dati di input (username, password, conferma password).</li>
 * <li>Verificare l'unicità dell'username nel database tramite {@link UtenteDAO}.</li>
 * <li>Creare il nuovo utente con password cifrata.</li>
 * <li>Inizializzare le bacheche predefinite per il nuovo utente tramite {@link BachecaDAO}.</li>
 * </ul>
 */
public class RegisterController {

    /**
     * Riferimento alla vista di registrazione gestita da questo controller.
     */
    private final RegisterView registerView;

    /**
     * Oggetto DAO per l'accesso ai dati degli utenti.
     */
    private final UtenteDAO utenteDAO;

    /**
     * Oggetto DAO per l'accesso ai dati delle bacheche.
     * Necessario per creare le bacheche di default al momento della registrazione.
     */
    private final BachecaDAO bachecaDAO;

    /**
     * Costruisce un nuovo RegisterController.
     * <p>
     * Inizializza i DAO necessari (Postgres implementation) e registra i listener
     * per i pulsanti e i link presenti nella vista di registrazione.
     *
     * @param registerView L'istanza della vista di registrazione da controllare.
     */
    public RegisterController(RegisterView registerView) {
        this.registerView = registerView;
        this.utenteDAO = new PostgresUtenteDAO();
        this.bachecaDAO = new PostgresBachecaDAO();

        this.registerView.addRegisterListener(e -> attemptRegister());
        this.registerView.addLoginLinkListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLoginView();
            }
        });
    }

    /**
     * Esegue il tentativo di registrazione di un nuovo utente.
     * <p>
     * Recupera i dati dalla vista, esegue i controlli di validazione (campi vuoti,
     * corrispondenza password, esistenza utente) e, se i controlli passano,
     * procede con l'hashing della password, il salvataggio dell'utente e
     * la generazione delle sue bacheche di default.
     */
    private void attemptRegister() {
        String user = registerView.getUsername();
        String pass = registerView.getPassword();
        String confirmPass = registerView.getConfirmPassword();

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

        String hashedPassword = PasswordHasher.hashPassword(pass);
        Utente nuovoUtente = new Utente(user, hashedPassword);

        utenteDAO.addUtente(nuovoUtente);

        creaBachecheDefault(nuovoUtente);

        registerView.mostraSuccesso("Registrazione completata! Ora puoi effettuare il login.");
        openLoginView();
    }

    /**
     * Crea e salva nel database le tre bacheche predefinite per il nuovo utente.
     * <p>
     * Le bacheche create sono: Università, Lavoro e Tempo Libero, ordinate
     * rispettivamente nelle posizioni 0, 1 e 2.
     *
     * @param utente L'oggetto {@link Utente} appena creato a cui associare le bacheche.
     */
    private void creaBachecheDefault(Utente utente) {
        int pos = 0;
        for (TitoloBacheca t : Arrays.asList(
                TitoloBacheca.UNIVERSITA,
                TitoloBacheca.LAVORO,
                TitoloBacheca.TEMPO_LIBERO)) {

            Bacheca nuovaBacheca = new Bacheca(t, "", utente.getIdUtente(), pos++);
            bachecaDAO.addBacheca(nuovaBacheca);
        }
    }

    /**
     * Gestisce la navigazione verso la schermata di login.
     * <p>
     * Chiude la finestra di registrazione corrente e apre una nuova istanza di {@link LoginView}.
     */
    private void openLoginView() {
        registerView.dispose();
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}