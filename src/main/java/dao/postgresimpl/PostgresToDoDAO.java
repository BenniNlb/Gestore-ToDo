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
 * Implementazione PostgreSQL dell'interfaccia {@link ToDoDAO}.
 * <p>
 * Questa classe gestisce la persistenza dei dati relativi ai {@link ToDo}.
 * Include la logica per:
 * <ul>
 * <li>Mappare i tipi di dati Java complessi ({@code Color}, {@code ImageIcon}, {@code LocalDate})
 * ai tipi supportati dal database (VARCHAR, BYTEA, DATE).</li>
 * <li>Gestire le relazioni many-to-many per i link e le condivisioni.</li>
 * <li>Eseguire query complesse per il recupero di ToDo condivisi e filtrati.</li>
 * </ul>
 */
public class PostgresToDoDAO implements ToDoDAO {

    private static final Logger LOGGER = Logger.getLogger(PostgresToDoDAO.class.getName());
    private Connection conn;
    private UtenteDAO utenteDAO;

    /**
     * Costruisce un'istanza del DAO utilizzando la connessione predefinita.
     * Inizializza internamente un {@link PostgresUtenteDAO} per gestire le dipendenze sugli utenti.
     */
    public PostgresToDoDAO() {
        this.conn = DBConnection.getConnection();
        this.utenteDAO = new PostgresUtenteDAO(this.conn);
    }

    /**
     * Costruisce un'istanza del DAO con dipendenze iniettate.
     * Utile per testing o configurazioni avanzate.
     *
     * @param connection La connessione al database.
     * @param utenteDAO  L'istanza del DAO utenti da utilizzare.
     */
    public PostgresToDoDAO(Connection connection, UtenteDAO utenteDAO) {
        this.conn = connection;
        this.utenteDAO = utenteDAO;
    }

    // --- Metodi Helper per Conversione Tipi ---

    /**
     * Converte un oggetto {@link Color} in una stringa esadecimale (es. "#RRGGBB").
     *
     * @param c Il colore da convertire.
     * @return La rappresentazione esadecimale del colore, o {@code null} se l'input è nullo.
     */
    private String colorToString(Color c) {
        if (c == null) return null;
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Converte una stringa esadecimale in un oggetto {@link Color}.
     *
     * @param s La stringa esadecimale (es. "#RRGGBB").
     * @return L'oggetto Color corrispondente, o {@code Color.WHITE} in caso di formato invalido o nullo.
     */
    private Color stringToColor(String s) {
        if (s == null || !s.startsWith("#")) return Color.WHITE;
        return Color.decode(s);
    }

    /**
     * Converte un'immagine {@link ImageIcon} in un array di byte (formato PNG).
     * Utilizzato per salvare l'immagine nel campo BLOB/BYTEA del database.
     *
     * @param icon L'icona da convertire.
     * @return L'array di byte dell'immagine, o {@code null} se l'input è nullo o si verifica un errore IO.
     */
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

    /**
     * Converte un array di byte (letto dal DB) in un oggetto {@link ImageIcon}.
     *
     * @param bytes L'array di byte dell'immagine.
     * @return L'oggetto ImageIcon ricostruito, o {@code null} se l'input è nullo o vuoto.
     */
    private ImageIcon bytesToImageIcon(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            return new ImageIcon(ImageIO.read(bais));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore conversione byte[] in ImageIcon", e);
            return null;
        }
    }

    /**
     * Inserisce un nuovo ToDo nel database.
     * <p>
     * Salva tutti i campi principali e recupera l'ID generato. Successivamente,
     * invoca {@link #updateLinksForToDo} per salvare i link associati.
     *
     * @param todo Il ToDo da salvare.
     */
    @Override
    public void addToDo(ToDo todo) {
        String sql = "INSERT INTO todo (titolo, descrizione, data_scadenza, colore_sfondo, immagine, stato, posizione, id_bacheca, id_utente_creatore) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_todo";

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

    /**
     * Recupera un ToDo tramite ID.
     *
     * @param id L'ID del ToDo.
     * @return L'oggetto ToDo completamente popolato (idratato), o {@code null} se non trovato.
     */
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

    /**
     * Recupera tutti i ToDo di una bacheca, ordinati per posizione.
     *
     * @param idBacheca L'ID della bacheca.
     * @return Una lista ordinata di ToDo.
     */
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

    /**
     * Metodo helper privato per ricostruire un oggetto {@link ToDo} da un {@link ResultSet}.
     * Popola tutti i campi e recupera separatamente link, immagini e condivisioni.
     *
     * @param rs Il ResultSet posizionato sulla riga corrente.
     * @return L'oggetto ToDo popolato.
     * @throws SQLException Se si verifica un errore di accesso ai dati.
     */
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
        td.setCondivisioniDalDB(getCondivisioni(td.getIdToDo()));

        return td;
    }

    /**
     * Recupera la lista di URL associati a un ToDo.
     *
     * @param idTodo L'ID del ToDo.
     * @return Lista di stringhe URL.
     */
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

    /**
     * Aggiorna i link associati a un ToDo.
     * Strategia: elimina tutti i link esistenti per quel ToDo e inserisce i nuovi.
     *
     * @param idTodo L'ID del ToDo.
     * @param links  La nuova lista di link.
     */
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

    /**
     * Recupera i ToDo (propri o condivisi) che scadono in una data specifica.
     * Esegue una JOIN tra ToDo, Bacheca e Condivisioni per filtrare correttamente.
     *
     * @param idUtente L'ID dell'utente che richiede i dati.
     * @param date     La data di scadenza.
     * @return Lista di ToDo corrispondenti.
     */
    @Override
    public List<ToDo> getToDosByDate(int idUtente, LocalDate date) {
        List<ToDo> todos = new ArrayList<>();
        String sql = "SELECT t.* FROM todo t " +
                "LEFT JOIN bacheca b ON t.id_bacheca = b.id_bacheca " +
                "LEFT JOIN todo_condivisione tc ON t.id_todo = tc.id_todo " +
                "WHERE (b.id_utente = ? OR tc.id_utente = ?) AND t.data_scadenza = ? " +
                "GROUP BY t.id_todo";

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

    /**
     * Recupera i ToDo (propri o condivisi) non completati che scadono entro una data.
     *
     * @param idUtente L'ID dell'utente.
     * @param endDate  La data limite.
     * @return Lista di ToDo in scadenza.
     */
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

    /**
     * Cerca ToDo per titolo o descrizione (case-insensitive), inclusi quelli condivisi.
     *
     * @param idUtente L'ID dell'utente.
     * @param query    La stringa di ricerca.
     * @return Lista di ToDo trovati.
     */
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

    /**
     * Aggiorna un ToDo esistente nel database.
     * Aggiorna anche i link associati.
     *
     * @param todo Il ToDo con i dati aggiornati.
     */
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

    /**
     * Elimina un ToDo dal database.
     *
     * @param idTodo L'ID del ToDo da eliminare.
     */
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

    /**
     * Non implementato: recupera tutti i ToDo del sistema.
     * In questo sistema si accede ai ToDo sempre tramite utente o bacheca.
     *
     * @return Lista vuota.
     */
    @Override
    public List<ToDo> getAllToDos() {
        return new ArrayList<>();
    }

    /**
     * Segna come completati tutti i ToDo di una bacheca specifica.
     *
     * @param idBacheca L'ID della bacheca.
     */
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

    /**
     * Recupera la mappa delle condivisioni per un ToDo.
     *
     * @param idTodo L'ID del ToDo.
     * @return Mappa {@code Utente -> PermessoCondivisione}.
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
     * Aggiunge o aggiorna una condivisione.
     * Utilizza {@code ON CONFLICT} per gestire inserimenti e aggiornamenti in un'unica query.
     *
     * @param idTodo   L'ID del ToDo.
     * @param idUtente L'ID dell'utente.
     * @param permesso Il permesso da assegnare.
     */
    @Override
    public void aggiungiCondivisione(int idTodo, int idUtente, PermessoCondivisione permesso) {
        String sql = "INSERT INTO todo_condivisione (id_todo, id_utente, permesso) VALUES (?, ?, ?) " +
                "ON CONFLICT (id_todo, id_utente) DO UPDATE SET permesso = EXCLUDED.permesso";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idTodo);
            pstmt.setInt(2, idUtente);
            pstmt.setString(3, permesso.name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore aggiungiCondivisione", e);
        }
    }

    /**
     * Aggiorna il permesso di una condivisione esistente.
     *
     * @param idTodo   L'ID del ToDo.
     * @param idUtente L'ID dell'utente.
     * @param permesso Il nuovo permesso.
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

    /**
     * Rimuove una condivisione.
     *
     * @param idTodo   L'ID del ToDo.
     * @param idUtente L'ID dell'utente da rimuovere.
     */
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

    /**
     * Recupera i ToDo condivisi con un utente, filtrandoli in base al titolo della bacheca originale.
     * Implementa la logica per cui un ToDo condiviso appare nella bacheca "corrispondente" dell'utente.
     *
     * @param idUtente      L'ID dell'utente ricevente.
     * @param titoloBacheca Il titolo della bacheca da filtrare (es. LAVORO).
     * @return Lista di ToDo condivisi.
     */
    @Override
    public List<ToDo> getSharedToDosForUser(int idUtente, TitoloBacheca titoloBacheca) {
        List<ToDo> todos = new ArrayList<>();
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
            pstmt.setString(2, titoloBacheca.name());

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
}