package Controllers;
import Model.TitoloBacheca;

import java.time.LocalDate;

public class MainController {

    private final BachecaController bachecaCtrl;
    private final ToDoController    todoCtrl;

    public MainController() {
        this.bachecaCtrl = new BachecaController();
        this.todoCtrl    = new ToDoController(bachecaCtrl);
    }

    public BachecaController getBachecaController() {
        return bachecaCtrl;
    }

    public ToDoController getToDoController() {
        return todoCtrl;
    }

    /** Azioni delegate dal CardChoice **/
    public void onAddToDo(String titolo,
                          LocalDate data,
                          String link,
                          String descrizione,
                          java.awt.Color colore,
                          TitoloBacheca inBacheca) {
        todoCtrl.creaToDo(inBacheca, titolo, data, link, descrizione, colore);
    }

    public void onAddBacheca(TitoloBacheca titolo, String descrizione) {
        bachecaCtrl.aggiungiBacheca(titolo, descrizione);
    }

    public void onDeleteBacheca(TitoloBacheca titolo) {
        bachecaCtrl.eliminaBacheca(titolo);
    }

    public void onDeleteToDo(Model.ToDo td) {
        todoCtrl.eliminaToDo(td);
    }

    public void onToggleCompletato(Model.ToDo td, boolean stato) {
        todoCtrl.setCompletato(td, stato);
    }

    public java.util.List<Model.ToDo> getScadenzeOggi() {
        return todoCtrl.getToDoByDate(LocalDate.now());
    }
}

