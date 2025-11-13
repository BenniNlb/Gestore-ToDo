package dao;

import model.PermessoCondivisione;
import model.ToDo;
import model.TitoloBacheca; // NUOVO IMPORT
import model.Utente;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ToDoDAO {

    void addToDo(ToDo todo);

    ToDo getToDoById(int id);

    List<ToDo> getAllToDos(); // Dal file del prof

    void updateToDo(ToDo todo);

    void deleteToDo(int id);

    List<ToDo> getAllToDosByBacheca(int idBacheca); // Nome modificato da getToDosByBoardId

    void markAllToDosAsCompletedByBacheca(int idBacheca); // Nome modificato

    // --- Metodi aggiunti per le nostre funzionalità ---


    List<ToDo> getToDosEntroData(int idUtente, LocalDate endDate);

    List<ToDo> searchToDos(int idUtente, String query);

    // --- Metodi per Condivisione (MODIFICATI) ---

    /**
     * MODIFICATO: Ritorna una mappa di Utenti e i loro permessi.
     */
    Map<Utente, PermessoCondivisione> getCondivisioni(int idTodo);

    /**
     * MODIFICATO: Aggiunge un utente con un permesso specifico.
     */
    void aggiungiCondivisione(int idTodo, int idUtente, PermessoCondivisione permesso);

    /**
     * NUOVO: Aggiorna il permesso di un utente già condiviso.
     */
    void aggiornaPermessoCondivisione(int idTodo, int idUtente, PermessoCondivisione permesso);

    /**
     * Rimuove un utente dalla condivisione.
     */
    void rimuoviCondivisione(int idTodo, int idUtente);

    // --- NUOVO METODO PER FIX BUG ---
    /**
     * Recupera i ToDo condivisi con un utente per una specifica bacheca (per nome)
     */
    List<ToDo> getSharedToDosForUser(int idUtente, TitoloBacheca titoloBacheca);
    // --- FINE NUOVO ---

    List<ToDo> getToDosByDate(int idUtente, LocalDate date);
}