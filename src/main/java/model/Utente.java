package model;

// Rimosso import java.util.UUID;

public class Utente {
    private int idUtente;
    private String username;
    private String password; // Questo sarà l'hash

    /**
     * Costruttore per un NUOVO utente (quando non hai ancora un ID dal DB).
     * La password passata qui DEVE essere già hashata.
     */
    public Utente(String login, String hashedPassword) {
        // L'ID non viene più generato qui, ma dal database (SERIAL)
        this.username = login;
        this.password = hashedPassword;
    }

    /**
     * Costruttore per un utente LETTO DAL DATABASE (ID esistente).
     * Usato dal DAO per "idratare" l'oggetto.
     */
    public Utente(int idUtente, String username, String hashedPassword) {
        this.idUtente = idUtente;
        this.username = username;
        this.password = hashedPassword;
    }

    // --- Metodi Getter ---

    public int getIdUtente() {
        return idUtente;
    }

    public String getUsername() {
        return username;
    }

    public String getLogin() { // Mantenuto per compatibilità
        return username;
    }

    public String getPassword() {
        return password;
    }

    // --- Metodo Setter (Richiesto dal DAO del prof) ---

    /**
     * Imposta l'ID dell'utente.
     * Chiamato dal DAO dopo l'INSERT... RETURNING id.
     */
    public void setId(int id) {
        this.idUtente = id;
    }

    // --- NUOVA MODIFICA ---
    /**
     * Override di toString per mostrare correttamente
     * l'username nelle JList
     */
    @Override
    public String toString() {
        return this.username;
    }
    // --- FINE MODIFICA ---
}