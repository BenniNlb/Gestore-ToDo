package dao;

import model.Utente;
import java.util.List;

/**
 * Interfaccia (DAO) per le operazioni di persistenza relative all'entità {@link Utente}.
 * <p>
 * Definisce il contratto astratto per la gestione degli utenti nel database,
 * disaccoppiando la logica di business dai dettagli di implementazione dello storage.
 */
public interface UtenteDAO {

    /**
     * Inserisce un nuovo utente nel database.
     * <p>
     * L'implementazione deve gestire la generazione dell'ID univoco e
     * impostarlo sull'oggetto {@link Utente} passato come parametro.
     *
     * @param utente L'oggetto {@link Utente} da salvare (con password già hashata).
     */
    void addUtente(Utente utente);

    /**
     * Recupera un utente specifico dal database tramite il suo ID univoco.
     *
     * @param id L'identificativo univoco dell'utente da cercare.
     * @return L'oggetto {@link Utente} corrispondente, o {@code null} se non trovato.
     */
    Utente getUtenteById(int id);

    /**
     * Recupera un utente specifico dal database tramite il suo username.
     * <p>
     * Questo metodo è fondamentale per il processo di login.
     *
     * @param username L'username (login) dell'utente da cercare.
     * @return L'oggetto {@link Utente} corrispondente, o {@code null} se non trovato.
     */
    Utente getUtenteByUsername(String username);

    /**
     * Recupera l'elenco completo di tutti gli utenti registrati nel sistema.
     *
     * @return Una {@link List} contenente tutti gli oggetti {@link Utente}.
     */
    List<Utente> getAllUtenti();

    /**
     * Aggiorna le informazioni di un utente esistente nel database.
     * <p>
     * Utilizzato tipicamente per cambiare la password o altri dettagli anagrafici.
     *
     * @param utente L'oggetto {@link Utente} con i dati aggiornati.
     */
    void updateUtente(Utente utente);

    /**
     * Elimina un utente dal database utilizzando il suo ID.
     *
     * @param id L'identificativo dell'utente da rimuovere.
     */
    void deleteUtenteById(int id);

    /**
     * Verifica se un determinato username è già presente nel database.
     * <p>
     * Utilizzato durante la fase di registrazione per garantire l'univocità del login.
     *
     * @param username L'username da controllare.
     * @return {@code true} se l'username è già in uso, {@code false} altrimenti.
     */
    boolean utenteEsiste(String username);

    /**
     * Cerca utenti nel database il cui username contiene la stringa di query specificata.
     * <p>
     * I risultati escludono l'utente che sta effettuando la ricerca, rendendo questo
     * metodo ideale per funzionalità di ricerca collaboratori o condivisione.
     *
     * @param query           La sottostringa da cercare nell'username (es. "mario").
     * @param idUtenteAttuale L'ID dell'utente che sta cercando (da escludere dai risultati).
     * @return Una {@link List} di {@link Utente} che corrispondono ai criteri di ricerca.
     */
    List<Utente> searchUtenti(String query, int idUtenteAttuale);
}