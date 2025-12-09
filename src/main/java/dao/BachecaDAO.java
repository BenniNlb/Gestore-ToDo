package dao;

import model.Bacheca;
import model.TitoloBacheca;
import java.util.List;

/**
 * Interfaccia (DAO) per le operazioni di persistenza relative all'entità {@link Bacheca}.
 * <p>
 * Definisce il contratto astratto per la gestione delle bacheche nel database,
 * separando la logica di business dai dettagli di implementazione dello storage.
 * Include metodi per creare, leggere, aggiornare ed eliminare bacheche,
 * oltre a query specifiche per il recupero basato su utente e titolo.
 */
public interface BachecaDAO {

    /**
     * Inserisce una nuova bacheca nel database.
     * <p>
     * L'implementazione deve gestire la generazione dell'ID univoco e
     * impostarlo sull'oggetto {@link Bacheca} passato come parametro.
     *
     * @param bacheca L'oggetto Bacheca da salvare.
     */
    void addBacheca(Bacheca bacheca);

    /**
     * Recupera una bacheca specifica dal database tramite il suo ID univoco.
     *
     * @param id L'identificativo univoco della bacheca da cercare.
     * @return L'oggetto {@link Bacheca} corrispondente, o {@code null} se non trovato.
     */
    Bacheca getBachecaById(int id);

    /**
     * Recupera l'elenco completo di tutte le bacheche presenti nel sistema.
     * <p>
     * Metodo di utilità generale, restituisce tutte le bacheche indipendentemente dall'utente.
     *
     * @return Una {@link List} contenente tutti gli oggetti {@link Bacheca}.
     */
    List<Bacheca> getAllBacheche();

    /**
     * Aggiorna i dati di una bacheca esistente nel database.
     * <p>
     * Utilizzato per modificare la descrizione o aggiornare la posizione (ordinamento)
     * della bacheca.
     *
     * @param bacheca L'oggetto {@link Bacheca} contenente i dati aggiornati.
     */
    void updateBacheca(Bacheca bacheca);

    /**
     * Elimina una bacheca dal database utilizzando il suo ID.
     *
     * @param id L'identificativo della bacheca da rimuovere.
     */
    void deleteBacheca(int id);

    /**
     * Recupera una bacheca specifica dato il suo titolo e l'ID dell'utente proprietario.
     * <p>
     * Utile per verificare l'esistenza di una bacheca (es. "Lavoro") per un determinato utente
     * prima di crearne una nuova.
     *
     * @param titolo   Il {@link TitoloBacheca} da cercare.
     * @param idUtente L'ID dell'utente proprietario.
     * @return L'oggetto {@link Bacheca} corrispondente, o {@code null} se non trovato.
     */
    Bacheca getBachecaByTitoloAndUtente(TitoloBacheca titolo, int idUtente);

    /**
     * Recupera tutte le bacheche appartenenti a un utente specifico.
     * <p>
     * La lista restituita dovrebbe rispettare l'ordinamento definito dal campo {@code posizioneB}.
     *
     * @param idUtente L'ID dell'utente di cui cercare le bacheche.
     * @return Una {@link List} delle {@link Bacheca} dell'utente.
     */
    List<Bacheca> getBachecheByUtente(int idUtente);

    /**
     * Recupera tutte le bacheche appartenenti a un utente specifico, cercandolo tramite username.
     * <p>
     * Questo metodo implica una join tra le tabelle utente e bacheca.
     *
     * @param username L'username dell'utente.
     * @return Una {@link List} delle {@link Bacheca} dell'utente.
     */
    List<Bacheca> getBachecheByUsername(String username);

    /**
     * Elimina tutte le bacheche associate a un determinato ID utente.
     * <p>
     * Utilizzato tipicamente durante la cancellazione di un account utente per
     * rimuovere i dati correlati (cascata).
     *
     * @param userId L'ID dell'utente di cui eliminare le bacheche.
     */
    void deleteAllBachecheByUserId(int userId);
}