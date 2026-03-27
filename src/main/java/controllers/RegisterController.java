package controllers;

import dao.BachecaDAO;
import dao.UtenteDAO;
import dao.postgresimpl.PostgresBachecaDAO;
import dao.postgresimpl.PostgresUtenteDAO;
import util.PasswordHasher;
import model.Bacheca;
import model.TitoloBacheca;
import model.Utente;

import java.util.Arrays;

/**
 * Controller (Control) responsabile della gestione della logica di registrazione.
 * <p>
 * Si occupa unicamente di validare i dati, interagire con il database per il
 * salvataggio del nuovo utente e generare le bacheche predefinite.
 * Rispetta il pattern MVC non avendo alcun riferimento diretto alle View.
 */
public class RegisterController {

    /**
     * Oggetto DAO per l'accesso ai dati degli utenti.
     */
    private final UtenteDAO utenteDAO;

    /**
     * Oggetto DAO per l'accesso ai dati delle bacheche (per la creazione di default).
     */
    private final BachecaDAO bachecaDAO;

    /**
     * Costruisce un nuovo RegisterController inizializzando le implementazioni DAO necessarie.
     */
    public RegisterController() {
        this.utenteDAO = new PostgresUtenteDAO();
        this.bachecaDAO = new PostgresBachecaDAO();
    }

    /**
     * Esegue il tentativo di registrazione di un nuovo utente.
     * <p>
     * Effettua i controlli formali sui parametri, verifica l'unicità dell'username,
     * effettua l'hashing della password e salva la nuova entità sul database.
     *
     * @param username L'username scelto dall'utente.
     * @param password La password in chiaro.
     * @param confirmPassword La conferma della password.
     * @throws Exception Se i campi non sono validi o se l'utente esiste già.
     */
    public void attemptRegister(String username, String password, String confirmPassword) throws Exception {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            throw new Exception("Tutti i campi sono obbligatori.");
        }
        if (!password.equals(confirmPassword)) {
            throw new Exception("Le password non corrispondono.");
        }
        if (utenteDAO.utenteEsiste(username)) {
            throw new Exception("Questo username è già in uso.");
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        Utente nuovoUtente = new Utente(username, hashedPassword);

        utenteDAO.addUtente(nuovoUtente);

        // Recupera l'utente appena creato dal DB per ottenere l'ID assegnato
        Utente utenteCreato = utenteDAO.getUtenteByUsername(username);
        if (utenteCreato != null) {
            creaBachecheDefault(utenteCreato);
        }
    }

    /**
     * Crea e salva nel database le bacheche predefinite per il nuovo utente.
     *
     * @param utente L'oggetto {@link Utente} a cui associare le bacheche.
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
}