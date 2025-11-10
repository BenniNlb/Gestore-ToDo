package dao.postgresimpl;

import database.DBConnection;
import model.Utente;
import dao.UtenteDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PostgresUtenteDAO implements UtenteDAO {
    private final Connection connection;
    private static final Logger LOGGER = Logger.getLogger(PostgresUtenteDAO.class.getName());
    private static final String PASSWORD_COLUMN = "password";

    public PostgresUtenteDAO() {
        // Usa la nostra classe di connessione singleton
        this.connection = DBConnection.getConnection();
    }

    // Costruttore del prof
    public PostgresUtenteDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void addUtente(Utente utente) {
        // Adattato allo script SQL (id_utente)
        String sql ="INSERT INTO utente(username,password) VALUES (?, ?) RETURNING id_utente";
        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, utente.getUsername());
            stmt.setString(2, utente.getPassword());
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                int generateId = rs.getInt("id_utente");
                utente.setId(generateId);
            }
        }   catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la creazione del utente: " + e.getMessage(), e);
        }
    }

    @Override
    public Utente getUtenteByUsername(String username) {
        String sql = "SELECT id_utente, username, password FROM utente WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Adattato al costruttore di model.Utente
                return new Utente(
                        rs.getInt("id_utente"),
                        rs.getString("username"),
                        rs.getString(PASSWORD_COLUMN)
                );
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il recupero dell'utente per username: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void updateUtente(Utente utente) {
        String sql = "UPDATE utente SET password = ? WHERE id_utente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, utente.getPassword());
            stmt.setInt(2, utente.getIdUtente());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiornamento dell'utente: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteUtenteById(int id) {
        String sql = "DELETE FROM utente WHERE id_utente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'eliminazione dell'utente per ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Utente> getAllUtenti() {
        List<Utente> utenti = new ArrayList<>();
        String sql = "SELECT id_utente, username, password FROM utente";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Adattato al costruttore di model.Utente
                utenti.add(new Utente(
                        rs.getInt("id_utente"),
                        rs.getString("username"),
                        rs.getString(PASSWORD_COLUMN)
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il recupero di tutti gli utenti: " + e.getMessage(), e);
        }
        return utenti;
    }

    @Override
    public Utente getUtenteById(int id) {
        String sql = "SELECT id_utente, username, password FROM utente WHERE id_utente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Adattato al costruttore di model.Utente
                return new Utente(
                        rs.getInt("id_utente"),
                        rs.getString("username"),
                        rs.getString(PASSWORD_COLUMN)
                );
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il recupero dell'utente per ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean utenteEsiste(String username) {
        String sql = "SELECT 1 FROM utente WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // true se trova una riga
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il controllo esistenza utente", e);
        }
        return false;
    }
}