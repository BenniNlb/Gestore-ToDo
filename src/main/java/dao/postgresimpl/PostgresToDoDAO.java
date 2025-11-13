package dao.postgresimpl;

import dao.ToDoDAO;
import database.DBConnection;
import model.*;
import dao.UtenteDAO;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione specifica per PostgreSQL del contratto ToDoDAO.
 * Adattata per il modello ToDo.java con Color, LocalDate e ImageIcon.
 */
public class PostgresToDoDAO implements ToDoDAO {

    private static final Logger LOGGER = Logger.getLogger(PostgresToDoDAO.class.getName());
    private Connection conn;
    private UtenteDAO utenteDAO; // Ci serve per la condivisione

    public PostgresToDoDAO() {
        this.conn = DBConnection.getConnection();
        this.utenteDAO = new PostgresUtenteDAO(this.conn);
    }

    // Costruttore (come quello del prof)
    public PostgresToDoDAO(Connection connection, UtenteDAO utenteDAO) {
        this.conn = connection;
        this.utenteDAO = utenteDAO;
    }

    // --- Helper per Colore -> Stringa ---
    private String colorToString(Color c) {
        if (c == null) return null;
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // --- Helper per Stringa -> Colore ---
    private Color stringToColor(String s) {
        if (s == null || !s.startsWith("#")) return Color.WHITE; // Default
        return Color.decode(s);
    }

    // --- Helper per ImageIcon -> byte[] ---
    private byte[] imageIconToBytes(ImageIcon icon) {
        if (icon == null) return null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Image img = icon.getImage();
            BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            ImageIO.write(bi, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore conversione ImageIcon in byte[]", e);
            return null;
        }
    }

    // --- Helper per byte[] -> ImageIcon ---
    private ImageIcon bytesToImageIcon(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            return new ImageIcon(ImageIO.read(bais));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore conversione byte[] in ImageIcon", e);
            return null;
        }
    }

    @Override
    public void addToDo(ToDo todo) {
        String sql = "INSERT INTO todo (titolo, descrizione, data_scadenza, colore_sfondo, immagine, stato, posizione, id_bacheca, id_utente_creatore) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_todo";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todo.getTitolo());
            pstmt.setString(2, todo.getDescrizione());
            pstmt.setObject(3, todo.getDataScadenza()); // LocalDate
            pstmt.setString(4, colorToString(todo.getColoreSfondo()));
            pstmt.setBytes(5, imageIconToBytes(todo.getImmagine()));
            pstmt.setBoolean(6, todo.isCompletato());
            pstmt.setInt(7, todo.getPosizione());
            pstmt.setInt(8, todo.getIdBacheca());
            pstmt.setInt(9, todo.getIdUtenteCreatore());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    todo.setId(rs.getInt(1));
                    updateLinksForToDo(todo.getIdToDo(), todo.getLinkURLs());
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante addToDo", e);
        }
    }

    @Override
    public ToDo getToDoById(int id) {
        String sql = "SELECT * FROM todo WHERE id_todo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return hydrateToDo(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getToDoById", e);
        }
        return null;
    }

    @Override
    public List<ToDo> getAllToDosByBacheca(int idBacheca) {
        List<ToDo> todos = new ArrayList<>();
        String sql = "SELECT * FROM todo WHERE id_bacheca = ? ORDER BY posizione ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idBacheca);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    todos.add(hydrateToDo(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getAllToDosByBacheca", e);
        }
        return todos;
    }

    // Helper per "costruire" un ToDo da un ResultSet
    private ToDo hydrateToDo(ResultSet rs) throws SQLException {
        ToDo td = new ToDo(
                rs.getInt("id_todo"),
                rs.getString("titolo"),
                rs.getString("descrizione"),
                rs.getObject("data_scadenza", LocalDate.class),
                stringToColor(rs.getString("colore_sfondo")),
                rs.getBoolean("stato"),
                rs.getInt("posizione"),
                rs.getInt("id_bacheca"),
                rs.getInt("id_utente_creatore")
        );

        td.setLinksDalDB(getLinksForToDo(td.getIdToDo()));
        td.setImmagine(bytesToImageIcon(rs.getBytes("immagine")));

        // --- MODIFICA CHIAVE ---
        // Popoliamo la nuova mappa delle condivisioni
        td.setCondivisioniDalDB(getCondivisioni(td.getIdToDo()));
        // --- FINE MODIFICA ---

        return td;
    }

    // Helper per caricare i link
    private List<String> getLinksForToDo(int idTodo) {
        List<String> links = new ArrayList<>();
        String sql = "SELECT url FROM todo_links WHERE id_todo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTodo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    links.add(rs.getString("url"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Errore getLinksForToDo", e);
        }
        return links;
    }

    // Helper per aggiornare i link (cancella e ricrea)
    private void updateLinksForToDo(int idTodo, List<String> links) {
        String sqlDelete = "DELETE FROM todo_links WHERE id_todo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
            pstmt.setInt(1, idTodo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore updateLinks (DELETE)", e);
        }

        if (links == null || links.isEmpty()) return;

        String sqlInsert = "INSERT INTO todo_links (id_todo, url) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
            for (String link : links) {
                pstmt.setInt(1, idTodo);
                pstmt.setString(2, link);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore updateLinks (INSERT)", e);
        }
    }

    @Override  // <-- AGGIUNGI QUESTA RIGA
    public List<ToDo> getToDosByDate(int idUtente, LocalDate date) {
        List<ToDo> todos = new ArrayList<>();
        // Query: Seleziona ToDo dell'utente O ToDo condivisi con l'utente
        String sql = "SELECT t.* FROM todo t " +
                "LEFT JOIN bacheca b ON t.id_bacheca = b.id_bacheca " +
                "LEFT JOIN todo_condivisione tc ON t.id_todo = tc.id_todo " +
                "WHERE (b.id_utente = ? OR tc.id_utente = ?) AND t.data_scadenza = ? " +
                "GROUP BY t.id_todo"; // GROUP BY per evitare duplicati

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUtente);
            pstmt.setInt(2, idUtente);
            pstmt.setObject(3, date);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    todos.add(hydrateToDo(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getToDosByDate", e);
        }
        return todos;
    }

    @Override
    public List<ToDo> getToDosEntroData(int idUtente, LocalDate endDate) {
        List<ToDo> todos = new ArrayList<>();
        String sql = "SELECT t.* FROM todo t " +
                "LEFT JOIN bacheca b ON t.id_bacheca = b.id_bacheca " +
                "LEFT JOIN todo_condivisione tc ON t.id_todo = tc.id_todo " +
                "WHERE (b.id_utente = ? OR tc.id_utente = ?) AND t.stato = false " +
                "AND t.data_scadenza <= ? AND t.data_scadenza >= CURRENT_DATE " +
                "GROUP BY t.id_todo";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUtente);
            pstmt.setInt(2, idUtente);
            pstmt.setObject(3, endDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    todos.add(hydrateToDo(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getToDosEntroData", e);
        }
        return todos;
    }

    @Override
    public List<ToDo> searchToDos(int idUtente, String query) {
        List<ToDo> todos = new ArrayList<>();
        String sql = "SELECT t.* FROM todo t " +
                "LEFT JOIN bacheca b ON t.id_bacheca = b.id_bacheca " +
                "LEFT JOIN todo_condivisione tc ON t.id_todo = tc.id_todo " +
                "WHERE (b.id_utente = ? OR tc.id_utente = ?) " +
                "AND (t.titolo ILIKE ? OR t.descrizione ILIKE ?) " +
                "GROUP BY t.id_todo";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String likeQuery = "%" + query.toLowerCase(Locale.ROOT) + "%";
            pstmt.setInt(1, idUtente);
            pstmt.setInt(2, idUtente);
            pstmt.setString(3, likeQuery);
            pstmt.setString(4, likeQuery);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    todos.add(hydrateToDo(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore during searchToDos", e);
        }
        return todos;
    }

    @Override
    public void updateToDo(ToDo todo) {
        String sql = "UPDATE todo SET titolo = ?, descrizione = ?, data_scadenza = ?, colore_sfondo = ?, " +
                "immagine = ?, stato = ?, posizione = ?, id_bacheca = ?, id_utente_creatore = ? " +
                "WHERE id_todo = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todo.getTitolo());
            pstmt.setString(2, todo.getDescrizione());
            pstmt.setObject(3, todo.getDataScadenza());
            pstmt.setString(4, colorToString(todo.getColoreSfondo()));
            pstmt.setBytes(5, imageIconToBytes(todo.getImmagine()));
            pstmt.setBoolean(6, todo.isCompletato());
            pstmt.setInt(7, todo.getPosizione());
            pstmt.setInt(8, todo.getIdBacheca());
            pstmt.setInt(9, todo.getIdUtenteCreatore());
            pstmt.setInt(10, todo.getIdToDo());

            pstmt.executeUpdate();

            updateLinksForToDo(todo.getIdToDo(), todo.getLinkURLs());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante updateToDo", e);
        }
    }

    @Override
    public void deleteToDo(int idTodo) {
        String sql = "DELETE FROM todo WHERE id_todo = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idTodo);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante deleteToDo", e);
        }
    }

    @Override
    public List<ToDo> getAllToDos() {
        // Non implementato, usiamo metodi specifici per utente
        return new ArrayList<>();
    }

    @Override
    public void markAllToDosAsCompletedByBacheca(int idBacheca) {
        String sql = "UPDATE todo SET stato = true WHERE id_bacheca = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idBacheca);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante markAllToDosAsCompleted", e);
        }
    }

    // --- Metodi Condivisione (MODIFICATI) ---

    /**
     * MODIFICATO: Ritorna una Mappa di Utenti e i loro permessi.
     */
    @Override
    public Map<Utente, PermessoCondivisione> getCondivisioni(int idTodo) {
        Map<Utente, PermessoCondivisione> mappa = new HashMap<>();
        String sql = "SELECT id_utente, permesso FROM todo_condivisione WHERE id_todo = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTodo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Utente u = utenteDAO.getUtenteById(rs.getInt("id_utente"));
                    PermessoCondivisione p = PermessoCondivisione.fromString(rs.getString("permesso"));
                    if (u != null) {
                        mappa.put(u, p);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore getCondivisioni", e);
        }
        return mappa;
    }

    /**
     * MODIFICATO: Aggiunge il permesso.
     */
    @Override
    public void aggiungiCondivisione(int idTodo, int idUtente, PermessoCondivisione permesso) {
        // ON CONFLICT: Se l'utente è già condiviso, aggiorna solo il permesso
        String sql = "INSERT INTO todo_condivisione (id_todo, id_utente, permesso) VALUES (?, ?, ?) " +
                "ON CONFLICT (id_todo, id_utente) DO UPDATE SET permesso = EXCLUDED.permesso";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTodo);
            pstmt.setInt(2, idUtente);
            pstmt.setString(3, permesso.name()); // Salva come stringa (es. "MODIFICA")
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore aggiungiCondivisione", e);
        }
    }

    /**
     * NUOVO: Aggiorna solo il permesso di un utente già condiviso.
     */
    @Override
    public void aggiornaPermessoCondivisione(int idTodo, int idUtente, PermessoCondivisione permesso) {
        String sql = "UPDATE todo_condivisione SET permesso = ? WHERE id_todo = ? AND id_utente = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, permesso.name());
            pstmt.setInt(2, idTodo);
            pstmt.setInt(3, idUtente);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore aggiornaPermessoCondivisione", e);
        }
    }


    @Override
    public void rimuoviCondivisione(int idTodo, int idUtente) {
        String sql = "DELETE FROM todo_condivisione WHERE id_todo = ? AND id_utente = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTodo);
            pstmt.setInt(2, idUtente);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore rimuoviCondivisione", e);
        }
    }

    // --- NUOVA IMPLEMENTAZIONE DAO PER FIX BUG ---
    @Override
    public List<ToDo> getSharedToDosForUser(int idUtente, TitoloBacheca titoloBacheca) {
        List<ToDo> todos = new ArrayList<>();
        // Seleziona ToDo (t)
        // uniti alla tabella condivisione (tc)
        // uniti alla bacheca del creatore (b_creatore)
        // dove l'utente condiviso è (idUtente)
        // e il titolo della bacheca del creatore corrisponde
        String sql = "SELECT t.* " +
                "FROM todo t " +
                "JOIN todo_condivisione tc ON t.id_todo = tc.id_todo " +
                "JOIN bacheca b_creatore ON t.id_bacheca = b_creatore.id_bacheca " +
                "WHERE tc.id_utente = ? " +
                "  AND b_creatore.titolo = ? " +
                "GROUP BY t.id_todo " +
                "ORDER BY t.posizione ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUtente);
            pstmt.setString(2, titoloBacheca.name()); // Es. "LAVORO"

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    todos.add(hydrateToDo(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante getSharedToDosForUser", e);
        }
        return todos;
    }
    // --- FINE NUOVO ---
}