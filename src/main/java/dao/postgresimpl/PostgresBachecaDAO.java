package dao.postgresimpl;

import dao.BachecaDAO;
import database.DBConnection;
import model.Bacheca;
import model.TitoloBacheca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        String sql = "INSERT INTO bacheca (titolo, descrizione, id_utente) VALUES (?, ?, ?) RETURNING id_bacheca";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bacheca.getTitolo().name()); // Salva l'enum come Stringa
            pstmt.setString(2, bacheca.getDescrizione());
            pstmt.setInt(3, bacheca.getIdUtente());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bacheca.setId(rs.getInt("id_bacheca")); // Imposta l'ID generato
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
                            TitoloBacheca.valueOf(rs.getString("titolo")), // Converte String in Enum
                            rs.getString("descrizione"),
                            rs.getInt("id_utente")
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
                        rs.getInt("id_utente")
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
        String sql = "SELECT * FROM bacheca WHERE id_utente = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUtente);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bacheche.add(new Bacheca(
                            rs.getInt("id_bacheca"),
                            TitoloBacheca.valueOf(rs.getString("titolo")),
                            rs.getString("descrizione"),
                            rs.getInt("id_utente")
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
        String sql = "UPDATE bacheca SET descrizione = ? WHERE id_bacheca = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bacheca.getDescrizione());
            pstmt.setInt(2, bacheca.getIdBacheca());

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

    // Metodi non presenti nel nostro modello ma presenti nel DAO del prof
    @Override
    public List<Bacheca> getAllBacheche() {
        // Implementazione non richiesta dal nostro controller, ma presente nell'interfaccia
        return new ArrayList<>();
    }

    @Override
    public List<Bacheca> getBachecheByUsername(String username) {
        // Implementazione non richiesta al momento
        return new ArrayList<>();
    }

    @Override
    public void deleteAllBachecheByUserId(int userId) {
        // Implementazione non richiesta al momento
    }
}