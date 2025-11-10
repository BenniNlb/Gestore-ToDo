package dao;

import model.ToDo;
import java.time.LocalDate;
import java.util.List;

/**
 * Interfaccia per la gestione delle operazioni sui ToDo.
 * Unione del file del prof e dei metodi necessari al progetto.
 */
public interface ToDoDAO {

    void addToDo(ToDo todo);

    ToDo getToDoById(int id);

    List<ToDo> getAllToDos();

    void updateToDo(ToDo todo);

    void deleteToDo(int id);

    List<ToDo> getAllToDosByBacheca(int idBacheca); // Nome modificato da getToDosByBoardId

    void markAllToDosAsCompletedByBacheca(int idBacheca); // Nome modificato da markAllToDosAsCompletedByBoardId

    // --- Metodi aggiunti per le nostre funzionalità ---

    List<ToDo> getToDosByDate(int idUtente, LocalDate date);

    List<ToDo> getToDosEntroData(int idUtente, LocalDate endDate);

    List<ToDo> searchToDos(int idUtente, String query);
}