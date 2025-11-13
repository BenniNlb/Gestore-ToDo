package dao.postgresimpl;

import dao.BachecaDAO;
import database.DBConnection;
import model.Bacheca;
import model.TitoloBacheca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione di BachecaDAO per PostgreSQL.
 * Adattata da PostgresBoardDAO.
 */
public class PostgresBachecaDAO implements BachecaDAO {

    private static final Logger LOGGER = Logger.getLogger(PostgresBachecaDAO.class.getName());
    private Connection conn;

    public PostgresBachecaDAO() {
        this.conn = DBConnection.getConnection();
    }

    public PostgresBachecaDAO(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void addBacheca(Bacheca bacheca) {
        // --- MODIFICA: Aggiunta posizioneB ---
        String sql = "INSERT INTO bacheca (titolo, descrizione, id_utente, posizioneB) VALUES (?, ?, ?, ?) RETURNING id_bacheca";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bacheca.getTitolo().name());
            pstmt.setString(2, bacheca.getDescrizione());
            pstmt.setInt(3, bacheca.getIdUtente());
            pstmt.setInt(4, bacheca.getPosizioneB()); // --- MODIFICA ---

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bacheca.setId(rs.getInt("id_bacheca"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante addBacheca", e);
        }
    }

    @Override
    public Bacheca getBachecaById(int id) {
        String sql = "SELECT * FROM bacheca WHERE id_bacheca = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Bacheca(
                            rs.getInt("id_bacheca"),
                            TitoloBacheca.valueOf(rs.getString("titolo")),
                            rs.getString("descrizione"),
                            rs.getInt("id_utente"),
                            rs.getInt("posizioneB") // --- MODIFICA ---
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getBachecaById", e);
        }
        return null;
    }

    @Override
    public Bacheca getBachecaByTitoloAndUtente(TitoloBacheca titolo, int idUtente) {
        String sql = "SELECT * FROM bacheca WHERE titolo = ? AND id_utente = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titolo.name());
            pstmt.setInt(2, idUtente);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Bacheca(
                        rs.getInt("id_bacheca"),
                        TitoloBacheca.valueOf(rs.getString("titolo")),
                        rs.getString("descrizione"),
                        rs.getInt("id_utente"),
                        rs.getInt("posizioneB") // --- MODIFICA ---
                );
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero bacheca per titolo e utente", e);
        }
        return null;
    }

    @Override
    public List<Bacheca> getBachecheByUtente(int idUtente) {
        List<Bacheca> bacheche = new ArrayList<>();
        // --- MODIFICA: Aggiunto ORDER BY posizioneB ---
        String sql = "SELECT * FROM bacheca WHERE id_utente = ? ORDER BY posizioneB ASC";
        // --- FINE MODIFICA ---

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUtente);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bacheche.add(new Bacheca(
                            rs.getInt("id_bacheca"),
                            TitoloBacheca.valueOf(rs.getString("titolo")),
                            rs.getString("descrizione"),
                            rs.getInt("id_utente"),
                            rs.getInt("posizioneB") // --- MODIFICA ---
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getBachecheByUtente", e);
        }
        return bacheche;
    }

    @Override
    public void updateBacheca(Bacheca bacheca) {
        // --- MODIFICA: Aggiorna anche posizioneB ---
        String sql = "UPDATE bacheca SET descrizione = ?, posizioneB = ? WHERE id_bacheca = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bacheca.getDescrizione());
            pstmt.setInt(2, bacheca.getPosizioneB()); // --- MODIFICA ---
            pstmt.setInt(3, bacheca.getIdBacheca());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante updateBacheca", e);
        }
    }

    @Override
    public void deleteBacheca(int idBacheca) {
        String sql = "DELETE FROM bacheca WHERE id_bacheca = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idBacheca);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante deleteBacheca", e);
        }
    }

    @Override
    public List<Bacheca> getAllBacheche() {
        List<Bacheca> bacheche = new ArrayList<>();
        // --- MODIFICA: Aggiunto ORDER BY posizioneB ---
        String sql = "SELECT * FROM bacheca ORDER BY posizioneB ASC";
        try (Statement stmt = this.conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bacheche.add(new Bacheca(
                        rs.getInt("id_bacheca"),
                        TitoloBacheca.valueOf(rs.getString("titolo")),
                        rs.getString("descrizione"),
                        rs.getInt("id_utente"),
                        rs.getInt("posizioneB") // --- MODIFICA ---
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero di tutte le bacheche", e);
        }
        return bacheche;
    }

    @Override
    public List<Bacheca> getBachecheByUsername(String username) {
        List<Bacheca> bacheche = new ArrayList<>();
        // --- MODIFICA: Aggiunto ORDER BY posizioneB ---
        String sql = "SELECT b.* FROM bacheca b " +
                "JOIN utente u ON b.id_utente = u.id_utente " +
                "WHERE u.username = ? ORDER BY b.posizioneB ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bacheche.add(new Bacheca(
                        rs.getInt("id_bacheca"),
                        TitoloBacheca.valueOf(rs.getString("titolo")),
                        rs.getString("descrizione"),
                        rs.getInt("id_utente"),
                        rs.getInt("posizioneB") // --- MODIFICA ---
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getBachecheByUsername", e);
        }
        return bacheche;
    }

    @Override
    public void deleteAllBachecheByUserId(int userId) {
        String sql = "DELETE FROM bacheca WHERE id_utente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'eliminazione di tutte le bacheche per utente", e);
        }
    }
}