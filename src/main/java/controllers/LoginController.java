package controllers;

import dao.UtenteDAO;
import dao.postgresimpl.PostgresUtenteDAO;
import util.PasswordHasher;
import model.Utente;

/**
 * Controller (Control) responsabile della gestione della logica di autenticazione (Login).
 * <p>
 * Questa classe gestisce la logica di business pura: interagisce con il livello
 * di accesso ai dati ({@link UtenteDAO}), verifica l'esistenza dell'utente e
 * controlla la validità dell'hash della password. Non ha alcuna dipendenza
 * diretta con le classi della View (rispettando rigorosamente il pattern MVC).
 */
public class LoginController {

    /**
     * Oggetto DAO per l'accesso ai dati degli utenti nel database.
     */
    private final UtenteDAO utenteDAO;

    /**
     * Costruisce un nuovo LoginController.
     * <p>
     * Istanzia il DAO necessario per le operazioni di verifica e interrogazione sul database.
     */
    public LoginController() {
        this.utenteDAO = new PostgresUtenteDAO();
    }

    /**
     * Esegue il tentativo di login validando le credenziali fornite.
     * <p>
     * Il metodo esegue i seguenti passaggi logici:
     * <ol>
     * <li>Valida che i campi non siano vuoti.</li>
     * <li>Cerca l'utente nel database tramite username.</li>
     * <li>Verifica la corrispondenza della password (confrontando gli hash).</li>
     * </ol>
     *
     * @param username L'username inserito dall'utente.
     * @param password La password in chiaro inserita dall'utente.
     * @return L'oggetto {@link Utente} autenticato e recuperato dal DB.
     * @throws Exception Se i campi sono vuoti o le credenziali risultano errate.
     */
    public Utente attemptLogin(String username, String password) throws Exception {
        if (username.isEmpty() || password.isEmpty()) {
            throw new Exception("Username e Password sono obbligatori.");
        }

        Utente utente = utenteDAO.getUtenteByUsername(username);

        if (utente != null && PasswordHasher.checkPassword(password, utente.getPassword())) {
            return utente;
        } else {
            throw new Exception("Username o password sbagliati.");
        }
    }
}