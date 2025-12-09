package model;

/**
 * Rappresenta un utente registrato all'interno del sistema informativo.
 * <p>
 * Questa classe funge da entità principale per la gestione dell'autenticazione
 * e dell'identificazione dell'utente. Contiene le credenziali di accesso
 * (username e hash della password) e l'identificativo univoco generato dal database.
 */
public class Utente {

    /**
     * Identificativo univoco dell'utente nel database.
     */
    private int idUtente;

    /**
     * Username scelto dall'utente per l'accesso al sistema.
     */
    private String username;

    /**
     * Hash della password dell'utente (per motivi di sicurezza non viene salvata in chiaro).
     */
    private String password;

    /**
     * Costruisce un nuovo oggetto {@code Utente} non ancora persistito nel database.
     * <p>
     * Questo costruttore viene utilizzato durante la fase di registrazione,
     * prima che il database assegni un ID univoco.
     *
     * @param login          L'username scelto dall'utente per l'accesso.
     * @param hashedPassword La password dell'utente, già sottoposta a processo di hashing.
     */
    public Utente(String login, String hashedPassword) {
        this.username = login;
        this.password = hashedPassword;
    }

    /**
     * Costruisce un oggetto {@code Utente} ricostruendo lo stato dal database.
     * <p>
     * Questo costruttore viene utilizzato dai DAO (Data Access Object) per
     * mappare i record della tabella utenti in oggetti Java.
     *
     * @param idUtente       L'identificativo univoco recuperato dal database.
     * @param username       L'username dell'utente.
     * @param hashedPassword La stringa contenente l'hash della password.
     */
    public Utente(int idUtente, String username, String hashedPassword) {
        this.idUtente = idUtente;
        this.username = username;
        this.password = hashedPassword;
    }

    /**
     * Restituisce l'identificativo univoco dell'utente.
     *
     * @return L'ID dell'utente (intero).
     */
    public int getIdUtente() {
        return idUtente;
    }

    /**
     * Restituisce l'username utilizzato per il login.
     *
     * @return La stringa dell'username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce l'username dell'utente.
     * <p>
     * Alias di {@link #getUsername()}, mantenuto per compatibilità con eventuali
     * interfacce che richiedono esplicitamente il metodo "getLogin".
     *
     * @return La stringa dell'username.
     */
    public String getLogin() {
        return username;
    }

    /**
     * Restituisce la password dell'utente in formato hash.
     * <p>
     * <b>Nota:</b> Per motivi di sicurezza, la password in chiaro non viene mai memorizzata nell'oggetto.
     *
     * @return La stringa contenente l'hash della password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Imposta l'identificativo univoco dell'utente.
     * <p>
     * Questo metodo viene tipicamente invocato dal DAO subito dopo l'inserimento
     * di un nuovo utente nel database, per sincronizzare l'oggetto Java con
     * la chiave primaria generata (es. tramite serial o auto-increment).
     *
     * @param id Il nuovo ID assegnato dal database.
     */
    public void setId(int id) {
        this.idUtente = id;
    }

    /**
     * Restituisce una rappresentazione stringa dell'oggetto, corrispondente all'username.
     * <p>
     * Questo override è fondamentale per permettere la corretta visualizzazione
     * degli oggetti {@code Utente} all'interno di componenti Swing come {@code JList} o {@code JComboBox},
     * dove viene invocato implicitamente questo metodo per il rendering del testo.
     *
     * @return L'username dell'utente.
     */
    @Override
    public String toString() {
        return this.username;
    }
}