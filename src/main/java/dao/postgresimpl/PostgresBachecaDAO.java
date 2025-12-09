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
 * Implementazione PostgreSQL dell'interfaccia {@link BachecaDAO}.
 * <p>
 * Fornisce metodi concreti per interagire con la tabella 'bacheca' nel database.
 * Gestisce le operazioni CRUD (Create, Read, Update, Delete) e si assicura
 * che le bacheche vengano recuperate nell'ordine corretto grazie al campo 'posizioneB'.
 */
public class PostgresBachecaDAO implements BachecaDAO {

    private static final Logger LOGGER = Logger.getLogger(PostgresBachecaDAO.class.getName());
    private Connection conn;

    /**
     * Costruisce un'istanza del DAO utilizzando la connessione predefinita.
     * Ottiene la connessione singleton tramite {@link DBConnection}.
     */
    public PostgresBachecaDAO() {
        this.conn = DBConnection.getConnection();
    }

    /**
     * Costruisce un'istanza del DAO utilizzando una connessione specifica.
     * Utile per testing o per gestire transazioni personalizzate.
     *
     * @param connection La connessione al database da utilizzare.
     */
    public PostgresBachecaDAO(Connection connection) {
        this.conn = connection;
    }

    /**
     * Inserisce una nuova bacheca nel database.
     * <p>
     * Utilizza la clausola {@code RETURNING id_bacheca} per ottenere immediatamente
     * l'ID generato dal database e aggiornare l'oggetto passato come parametro.
     *
     * @param bacheca L'oggetto Bacheca da salvare.
     */
    @Override
    public void addBacheca(Bacheca bacheca) {
        String sql = "INSERT INTO bacheca (titolo, descrizione, id_utente, posizioneB) VALUES (?, ?, ?, ?) RETURNING id_bacheca";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bacheca.getTitolo().name());
            pstmt.setString(2, bacheca.getDescrizione());
            pstmt.setInt(3, bacheca.getIdUtente());
            pstmt.setInt(4, bacheca.getPosizioneB());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bacheca.setId(rs.getInt("id_bacheca"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante addBacheca", e);
        }
    }

    /**
     * Recupera una bacheca tramite il suo ID univoco.
     *
     * @param id L'ID della bacheca da cercare.
     * @return L'oggetto {@link Bacheca} trovato, o {@code null} se non esiste.
     */
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
                            rs.getInt("posizioneB")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getBachecaById", e);
        }
        return null;
    }

    /**
     * Recupera una bacheca specifica dato il titolo e l'ID dell'utente.
     *
     * @param titolo   Il titolo della bacheca.
     * @param idUtente L'ID dell'utente proprietario.
     * @return L'oggetto {@link Bacheca} corrispondente, o {@code null} se non trovato.
     */
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
                        rs.getInt("posizioneB")
                );
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero bacheca per titolo e utente", e);
        }
        return null;
    }

    /**
     * Recupera tutte le bacheche di un determinato utente.
     * <p>
     * I risultati sono ordinati in base al campo {@code posizioneB} (ascendente)
     * per garantire che l'ordine visualizzato nell'interfaccia sia corretto e consistente.
     *
     * @param idUtente L'ID dell'utente.
     * @return Una lista ordinata di oggetti {@link Bacheca}.
     */
    @Override
    public List<Bacheca> getBachecheByUtente(int idUtente) {
        List<Bacheca> bacheche = new ArrayList<>();
        String sql = "SELECT * FROM bacheca WHERE id_utente = ? ORDER BY posizioneB ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUtente);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bacheche.add(new Bacheca(
                            rs.getInt("id_bacheca"),
                            TitoloBacheca.valueOf(rs.getString("titolo")),
                            rs.getString("descrizione"),
                            rs.getInt("id_utente"),
                            rs.getInt("posizioneB")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getBachecheByUtente", e);
        }
        return bacheche;
    }

    /**
     * Aggiorna i dati di una bacheca esistente.
     * <p>
     * Permette di modificare la descrizione e la posizione ({@code posizioneB}) della bacheca.
     *
     * @param bacheca L'oggetto Bacheca con i dati aggiornati.
     */
    @Override
    public void updateBacheca(Bacheca bacheca) {
        String sql = "UPDATE bacheca SET descrizione = ?, posizioneB = ? WHERE id_bacheca = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, bacheca.getDescrizione());
            pstmt.setInt(2, bacheca.getPosizioneB());
            pstmt.setInt(3, bacheca.getIdBacheca());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante updateBacheca", e);
        }
    }

    /**
     * Elimina una bacheca dal database.
     *
     * @param idBacheca L'ID della bacheca da eliminare.
     */
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

    /**
     * Recupera tutte le bacheche presenti nel sistema.
     * <p>
     * Metodo di utilità generale, restituisce le bacheche ordinate per posizione.
     *
     * @return Una lista di tutte le bacheche.
     */
    @Override
    public List<Bacheca> getAllBacheche() {
        List<Bacheca> bacheche = new ArrayList<>();
        String sql = "SELECT * FROM bacheca ORDER BY posizioneB ASC";
        try (Statement stmt = this.conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bacheche.add(new Bacheca(
                        rs.getInt("id_bacheca"),
                        TitoloBacheca.valueOf(rs.getString("titolo")),
                        rs.getString("descrizione"),
                        rs.getInt("id_utente"),
                        rs.getInt("posizioneB")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero di tutte le bacheche", e);
        }
        return bacheche;
    }

    /**
     * Recupera le bacheche di un utente cercandolo per username.
     * <p>
     * Esegue una JOIN tra le tabelle {@code bacheca} e {@code utente}.
     *
     * @param username L'username dell'utente.
     * @return Una lista ordinata delle bacheche dell'utente.
     */
    @Override
    public List<Bacheca> getBachecheByUsername(String username) {
        List<Bacheca> bacheche = new ArrayList<>();
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
                        rs.getInt("posizioneB")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getBachecheByUsername", e);
        }
        return bacheche;
    }

    /**
     * Elimina tutte le bacheche associate a un determinato ID utente.
     *
     * @param userId L'ID dell'utente.
     */
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