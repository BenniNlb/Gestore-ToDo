package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestisce la connessione al database PostgreSQL utilizzando il pattern Singleton.
 * <p>
 * Questa classe funge da punto centralizzato per l'accesso al database "Gestore-ToDo".
 * Garantisce che esista un'unica istanza di {@link Connection} attiva per l'intera applicazione,
 * ottimizzando l'uso delle risorse e semplificando la gestione delle connessioni.
 */
public class DBConnection {

    /**
     * L'istanza statica della connessione (Singleton).
     */
    private static Connection connection = null;

    /**
     * URL di connessione JDBC per il database PostgreSQL locale.
     * Punta al database "Gestore-ToDo" sulla porta 5432.
     */
    private static final String URL = "jdbc:postgresql://localhost:5432/Gestore-ToDo";

    /**
     * Username per l'autenticazione al database.
     */
    private static final String USER = "benedetta";

    /**
     * Password per l'autenticazione al database.
     */
    private static final String PASSWORD = "Viad2419";

    /**
     * Logger per la registrazione di eventi di connessione ed errori.
     */
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    /**
     * Costruttore privato per impedire l'istanziazione diretta della classe.
     * Implementa il pattern Singleton.
     */
    private DBConnection() {
    }

    /**
     * Restituisce l'unica istanza della connessione al database.
     * <p>
     * Implementa la "Lazy Initialization": se la connessione non è ancora stata creata
     * o è stata chiusa, viene stabilita una nuova connessione caricando il driver PostgreSQL.
     * Altrimenti, viene restituita l'istanza esistente.
     *
     * @return L'oggetto {@link Connection} attivo verso il database.
     */
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                LOGGER.info("Connessione al database stabilita.");
            } catch (ClassNotFoundException e) {
                LOGGER.severe("Errore: Driver PostgreSQL non trovato.");
                e.printStackTrace();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Errore di connessione al database", e);
                e.printStackTrace();
            }
        }
        return connection;
    }

    /**
     * Chiude la connessione al database se attualmente aperta.
     * <p>
     * Rilascia le risorse occupate dalla connessione e imposta l'istanza statica
     * a {@code null}, permettendo una successiva riconnessione pulita tramite {@link #getConnection()}.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                LOGGER.info("Connessione al database chiusa.");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Errore durante la chiusura della connessione al database:", e);
                e.printStackTrace();
            }
        }
    }
}