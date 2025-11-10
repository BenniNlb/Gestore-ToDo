// File: src/database/DBConnection.java
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Questa classe gestisce la connessione al database PostgreSQL.
 */
public class DBConnection {
    private static Connection connection = null;
    // MODIFICATO: Aggiornato il nome del database a "Gestore-ToDo"
    private static final String URL = "jdbc:postgresql://localhost:5432/Gestore-ToDo";
    private static final String USER = "benedetta"; // Il tuo username
    private static final String PASSWORD = "Viad2419"; // La tua password
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    private DBConnection() {
    }

    /**
     * Questo metodo restituisce una connessione al database PostgreSQL.
     * Se la connessione è già stata stabilita, restituisce quella esistente.
     *
     * @return Connection oggetto di connessione al database
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
     * Questo metodo chiude la connessione al database se è aperta.
     * Se la connessione è già chiusa, non fa nulla.
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