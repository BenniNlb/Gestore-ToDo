package dao;

import model.PermessoCondivisione;
import model.ToDo;
import model.TitoloBacheca;
import model.Utente;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interfaccia (DAO) per le operazioni di persistenza relative all'entità {@link ToDo}.
 * <p>
 * Definisce il contratto astratto per la gestione del ciclo di vita delle attività nel database.
 * Include metodi per:
 * <ul>
 * <li>Operazioni CRUD (Create, Read, Update, Delete).</li>
 * <li>Gestione delle relazioni con le bacheche.</li>
 * <li>Filtri avanzati per data e ricerca testuale.</li>
 * <li>Gestione granulare dei permessi di condivisione con altri utenti.</li>
 * </ul>
 */
public interface ToDoDAO {

    /**
     * Inserisce un nuovo ToDo nel database.
     * <p>
     * L'implementazione deve gestire la generazione dell'ID univoco e
     * impostarlo sull'oggetto {@link ToDo} passato come parametro.
     *
     * @param todo L'oggetto {@link ToDo} da salvare.
     */
    void addToDo(ToDo todo);

    /**
     * Recupera un ToDo specifico dal database tramite il suo ID.
     *
     * @param id L'identificativo univoco del ToDo.
     * @return L'oggetto {@link ToDo} corrispondente, o {@code null} se non trovato.
     */
    ToDo getToDoById(int id);

    /**
     * Recupera tutti i ToDo presenti nel sistema.
     * <p>
     * Metodo di utilità generale, tipicamente utilizzato per debug o amministrazione.
     *
     * @return Una lista contenente tutti i {@link ToDo}.
     */
    List<ToDo> getAllToDos();

    /**
     * Aggiorna i dati di un ToDo esistente nel database.
     *
     * @param todo L'oggetto {@link ToDo} contenente i dati aggiornati.
     */
    void updateToDo(ToDo todo);

    /**
     * Elimina un ToDo dal database tramite il suo ID.
     *
     * @param id L'identificativo del ToDo da eliminare.
     */
    void deleteToDo(int id);

    /**
     * Recupera tutti i ToDo appartenenti a una specifica bacheca.
     *
     * @param idBacheca L'ID della bacheca di appartenenza.
     * @return Una lista di {@link ToDo} ordinati per posizione.
     */
    List<ToDo> getAllToDosByBacheca(int idBacheca);

    /**
     * Imposta lo stato di tutti i ToDo di una bacheca come "completato".
     *
     * @param idBacheca L'ID della bacheca target.
     */
    void markAllToDosAsCompletedByBacheca(int idBacheca);

    /**
     * Recupera i ToDo (sia propri che condivisi) associati a un utente
     * che scadono entro una certa data.
     * <p>
     * Include solo i ToDo non ancora completati.
     *
     * @param idUtente L'ID dell'utente.
     * @param endDate  La data limite (inclusa) per la scadenza.
     * @return Una lista di {@link ToDo} in scadenza.
     */
    List<ToDo> getToDosEntroData(int idUtente, LocalDate endDate);

    /**
     * Cerca i ToDo (sia propri che condivisi) associati a un utente
     * che contengono la query specificata nel titolo o nella descrizione.
     *
     * @param idUtente L'ID dell'utente che effettua la ricerca.
     * @param query    La stringa di testo da cercare.
     * @return Una lista di {@link ToDo} che corrispondono ai criteri.
     */
    List<ToDo> searchToDos(int idUtente, String query);

    // --- Metodi per Condivisione ---

    /**
     * Recupera la mappa degli utenti e dei relativi permessi per un determinato ToDo.
     *
     * @param idTodo L'ID del ToDo di cui recuperare le condivisioni.
     * @return Una mappa dove la chiave è l'{@link Utente} e il valore è il {@link PermessoCondivisione}.
     */
    Map<Utente, PermessoCondivisione> getCondivisioni(int idTodo);

    /**
     * Aggiunge una nuova condivisione per un ToDo verso un utente specifico.
     * Se la condivisione esiste già, aggiorna il permesso.
     *
     * @param idTodo   L'ID del ToDo da condividere.
     * @param idUtente L'ID dell'utente destinatario.
     * @param permesso Il livello di permesso concesso.
     */
    void aggiungiCondivisione(int idTodo, int idUtente, PermessoCondivisione permesso);

    /**
     * Aggiorna il livello di permesso per una condivisione esistente.
     *
     * @param idTodo   L'ID del ToDo.
     * @param idUtente L'ID dell'utente.
     * @param permesso Il nuovo livello di permesso.
     */
    void aggiornaPermessoCondivisione(int idTodo, int idUtente, PermessoCondivisione permesso);

    /**
     * Rimuove una condivisione, revocando l'accesso al ToDo per l'utente specificato.
     *
     * @param idTodo   L'ID del ToDo.
     * @param idUtente L'ID dell'utente da rimuovere.
     */
    void rimuoviCondivisione(int idTodo, int idUtente);

    /**
     * Recupera i ToDo che sono stati condivisi con un utente specifico
     * e che appartengono, lato creatore, a una bacheca con un determinato titolo.
     * <p>
     * Questo metodo permette di visualizzare i ToDo condivisi nella bacheca
     * "corrispondente" dell'utente ricevente (es. un ToDo creato nella bacheca "Lavoro"
     * di Alice apparirà nella bacheca "Lavoro" di Bob).
     *
     * @param idUtente      L'ID dell'utente ricevente.
     * @param titoloBacheca Il titolo della bacheca di origine da filtrare.
     * @return Una lista di {@link ToDo} condivisi.
     */
    List<ToDo> getSharedToDosForUser(int idUtente, TitoloBacheca titoloBacheca);

    /**
     * Recupera i ToDo (sia propri che condivisi) associati a un utente
     * che scadono esattamente nella data specificata.
     *
     * @param idUtente L'ID dell'utente.
     * @param date     La data di scadenza esatta.
     * @return Una lista di {@link ToDo}.
     */
    List<ToDo> getToDosByDate(int idUtente, LocalDate date);
}