package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una bacheca (o categoria) all'interno del sistema di gestione attività.
 * <p>
 * Ogni bacheca funge da contenitore logico per una lista di attività ({@link ToDo}).
 * Una bacheca è caratterizzata da:
 * <ul>
 * <li>Un <b>Titolo</b> prefissato (es. Università, Lavoro).</li>
 * <li>Una <b>Descrizione</b> opzionale.</li>
 * <li>Un <b>Proprietario</b> (l'utente che l'ha creata).</li>
 * <li>Una <b>Posizione</b> che ne determina l'ordine di visualizzazione nella dashboard.</li>
 * </ul>
 */
public class Bacheca {

    /**
     * Identificativo univoco della bacheca nel database.
     */
    private int idBacheca;

    /**
     * Il titolo o categoria della bacheca.
     */
    private TitoloBacheca titolo;

    /**
     * Breve descrizione testuale della bacheca.
     */
    private String descrizione;

    /**
     * Identificativo dell'utente proprietario della bacheca.
     */
    private int idUtente;

    /**
     * Lista delle attività (ToDo) contenute in questa bacheca.
     */
    private List<ToDo> toDos;

    /**
     * Indice numerico che rappresenta l'ordine di visualizzazione della bacheca.
     */
    private int posizioneB;

    /**
     * Costruisce una nuova istanza di {@code Bacheca}.
     * <p>
     * Questo costruttore viene utilizzato per creare nuovi oggetti prima che siano
     * stati persistiti nel database (l'ID viene assegnato successivamente).
     *
     * @param titolo      Il titolo della bacheca (definito dall'enum {@link TitoloBacheca}).
     * @param descrizione Una descrizione testuale della bacheca.
     * @param idUtente    L'identificativo univoco dell'utente proprietario.
     * @param posizioneB  L'indice numerico per l'ordinamento nella UI.
     */
    public Bacheca(TitoloBacheca titolo, String descrizione, int idUtente, int posizioneB) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.idUtente = idUtente;
        this.posizioneB = posizioneB;
        this.toDos = new ArrayList<>();
    }

    /**
     * Costruisce un'istanza di {@code Bacheca} con un ID esistente.
     * <p>
     * Questo costruttore è utilizzato dai DAO (Data Access Objects) per ricostruire
     * l'oggetto in memoria a partire dai dati letti dal database.
     *
     * @param idBacheca   L'identificativo univoco della bacheca.
     * @param titolo      Il titolo della bacheca.
     * @param descrizione La descrizione della bacheca.
     * @param idUtente    L'identificativo dell'utente proprietario.
     * @param posizioneB  L'indice di ordinamento della bacheca.
     */
    public Bacheca(int idBacheca, TitoloBacheca titolo, String descrizione, int idUtente, int posizioneB) {
        this.idBacheca = idBacheca;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.idUtente = idUtente;
        this.posizioneB = posizioneB;
        this.toDos = new ArrayList<>();
    }

    /**
     * Restituisce l'identificativo univoco della bacheca.
     *
     * @return L'ID della bacheca.
     */
    public int getIdBacheca() {
        return idBacheca;
    }

    /**
     * Restituisce il titolo della bacheca.
     *
     * @return Il valore enum {@link TitoloBacheca} associato.
     */
    public TitoloBacheca getTitolo() {
        return titolo;
    }

    /**
     * Restituisce la descrizione della bacheca.
     *
     * @return Una stringa contenente la descrizione.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Restituisce l'identificativo dell'utente proprietario della bacheca.
     *
     * @return L'ID dell'utente.
     */
    public int getIdUtente() {
        return idUtente;
    }

    /**
     * Restituisce la lista delle attività (ToDo) contenute in questa bacheca.
     *
     * @return Una lista di oggetti {@link ToDo}.
     */
    public List<ToDo> getToDos() {
        return toDos;
    }

    /**
     * Restituisce l'indice di posizione della bacheca.
     * <p>
     * Questo valore viene utilizzato per ordinare le bacheche orizzontalmente
     * nell'interfaccia utente.
     *
     * @return Un intero rappresentante la posizione (0-based).
     */
    public int getPosizioneB() {
        return posizioneB;
    }

    /**
     * Imposta l'indice di posizione della bacheca.
     *
     * @param posizioneB Il nuovo indice di posizione da assegnare.
     */
    public void setPosizioneB(int posizioneB) {
        this.posizioneB = posizioneB;
    }

    /**
     * Imposta l'identificativo univoco della bacheca.
     * <p>
     * Questo metodo viene solitamente invocato dal DAO subito dopo l'inserimento
     * nel database per sincronizzare l'ID generato.
     *
     * @param idBacheca Il nuovo ID della bacheca.
     */
    public void setId(int idBacheca) {
        this.idBacheca = idBacheca;
    }

    /**
     * Modifica la descrizione della bacheca.
     *
     * @param descrizione La nuova descrizione da impostare.
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Imposta l'intera lista di attività (ToDo) contenute nella bacheca.
     * <p>
     * Utilizzato principalmente per popolare la bacheca dopo aver caricato
     * le attività associate dal database.
     *
     * @param toDos La lista di oggetti {@link ToDo} da associare.
     */
    public void setToDos(List<ToDo> toDos) {
        this.toDos = toDos;
    }

    /**
     * Aggiunge una singola attività alla lista locale della bacheca in memoria.
     *
     * @param todo L'oggetto {@link ToDo} da aggiungere.
     */
    public void aggiungiToDo(ToDo todo) {
        toDos.add(todo);
    }

    /**
     * Rimuove una singola attività dalla lista locale della bacheca in memoria.
     *
     * @param todo L'oggetto {@link ToDo} da rimuovere.
     */
    public void rimuoviToDo(ToDo todo) {
        toDos.remove(todo);
    }

    /**
     * Svuota completamente la lista delle attività presenti nella bacheca in memoria.
     */
    public void svuotaToDos() {
        this.toDos.clear();
    }
}