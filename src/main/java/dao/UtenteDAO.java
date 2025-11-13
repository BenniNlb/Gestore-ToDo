package dao;

import model.Utente;
import java.util.List;

/**
 * Interfaccia per la gestione delle operazioni sugli utenti (Utente).
 * Basata sul file del professore
 */
public interface UtenteDAO {

    void addUtente(Utente utente);

    Utente getUtenteById(int id);

    Utente getUtenteByUsername(String username);

    List<Utente> getAllUtenti();

    void updateUtente(Utente utente);

    void deleteUtenteById(int id);

    /**
     * Controlla se un username è già in uso.
     * @param username L'username da controllare.
     * @return true se l'utente esiste, false altrimenti.
     */
    boolean utenteEsiste(String username);

    /**
     * Cerca gli utenti per username, escludendo l'utente corrente.
     * @param query Il testo da cercare (es. "mario")
     * @param idUtenteAttuale L'ID dell'utente che sta cercando (per escluderlo)
     * @return Una lista di utenti che corrispondono.
     */
    List<Utente> searchUtenti(String query, int idUtenteAttuale);
}