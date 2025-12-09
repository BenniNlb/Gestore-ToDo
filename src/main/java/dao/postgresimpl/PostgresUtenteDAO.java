package dao.postgresimpl;

import database.DBConnection;
import model.Utente;
import dao.UtenteDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * PostgreSQL implementation of the {@link UtenteDAO} interface.
 * <p>
 * Provides concrete methods to interact with the 'utente' table
 * in the PostgreSQL database.
 */
public class PostgresUtenteDAO implements UtenteDAO {
    private final Connection connection;
    private static final Logger LOGGER = Logger.getLogger(PostgresUtenteDAO.class.getName());
    private static final String PASSWORD_COLUMN = "password";

    /**
     * Default constructor.
     * Gets the Singleton connection from {@link DBConnection}.
     */
    public PostgresUtenteDAO() {
        this.connection = DBConnection.getConnection();
    }

    /**
     * Constructor that accepts an external connection.
     * Useful for testing purposes or managed transactions.
     *
     * @param connection The SQL connection to use.
     */
    public PostgresUtenteDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * {@inheritDoc}
     * Uses the PostgreSQL "RETURNING id_utente" clause to retrieve
     * the generated ID and set it on the User object.
     */
    @Override
    public void addUtente(Utente utente) {
        String sql ="INSERT INTO utente(username,password) VALUES (?, ?) RETURNING id_utente";
        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, utente.getUsername());
            stmt.setString(2, utente.getPassword()); // Password ALREADY HASHED

            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    int generateId = rs.getInt("id_utente");
                    utente.setId(generateId); // Sets the ID on the object
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during addUtente: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Utente getUtenteByUsername(String username) {
        String sql = "SELECT id_utente, username, password FROM utente WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Utente(
                            rs.getInt("id_utente"),
                            rs.getString("username"),
                            rs.getString(PASSWORD_COLUMN)
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during getUtenteByUsername: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUtente(Utente utente) {
        String sql = "UPDATE utente SET password = ? WHERE id_utente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, utente.getPassword()); // Update hash
            stmt.setInt(2, utente.getIdUtente());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during updateUtente: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUtenteById(int id) {
        String sql = "DELETE FROM utente WHERE id_utente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during deleteUtenteById: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Utente> getAllUtenti() {
        List<Utente> utenti = new ArrayList<>();
        String sql = "SELECT id_utente, username, password FROM utente";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                utenti.add(new Utente(
                        rs.getInt("id_utente"),
                        rs.getString("username"),
                        rs.getString(PASSWORD_COLUMN)
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during getAllUtenti: " + e.getMessage(), e);
        }
        return utenti;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Utente getUtenteById(int id) {
        String sql = "SELECT id_utente, username, password FROM utente WHERE id_utente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Utente(
                            rs.getInt("id_utente"),
                            rs.getString("username"),
                            rs.getString(PASSWORD_COLUMN)
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during getUtenteById: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Uses "SELECT 1" for a light and efficient existence check.
     */
    @Override
    public boolean utenteEsiste(String username) {
        String sql = "SELECT 1 FROM utente WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // true if it finds a row, false otherwise
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during utenteEsiste", e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * Uses the PostgreSQL "ILIKE" operator for a case-insensitive search.
     * Excludes the current user from the results.
     */
    @Override
    public List<Utente> searchUtenti(String query, int idUtenteAttuale) {
        List<Utente> utenti = new ArrayList<>();
        // Searches for users with similar username AND that are not the current user
        String sql = "SELECT id_utente, username, password FROM utente " +
                "WHERE username ILIKE ? AND id_utente != ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%"); // ILIKE for case-insensitive
            stmt.setInt(2, idUtenteAttuale); // Excludes logged user

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    utenti.add(new Utente(
                            rs.getInt("id_utente"),
                            rs.getString("username"),
                            rs.getString(PASSWORD_COLUMN)
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error during searchUtenti", e);
        }
        return utenti;
    }
}