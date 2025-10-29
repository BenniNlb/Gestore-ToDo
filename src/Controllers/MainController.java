package Controllers;

import Model.ToDo;
import Model.TitoloBacheca;

import java.time.LocalDate;
import java.awt.Color;
import java.util.List;

public class MainController {

    private final BachecaController bachecaCtrl;
    private final ToDoController todoCtrl;

    public MainController() {
        this.bachecaCtrl = new BachecaController();
        this.todoCtrl = new ToDoController(bachecaCtrl);
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
                          Color colore,
                          TitoloBacheca inBacheca) {
        todoCtrl.creaToDo(inBacheca, titolo, data, link, descrizione, colore);
    }

    public void onAddBacheca(TitoloBacheca titolo, String descrizione) {
        bachecaCtrl.aggiungiBacheca(titolo, descrizione);
    }

    public void onDeleteBacheca(TitoloBacheca titolo) {
        bachecaCtrl.eliminaBacheca(titolo);
    }

    public void onDeleteToDo(ToDo td) {
        todoCtrl.eliminaToDo(td);
    }

    public void onToggleCompletato(ToDo td, boolean stato) {
        todoCtrl.setCompletato(td, stato);
    }

    /** Nuovo metodo per modificare un ToDo */
    public void onEditToDo(ToDo td,
                           String nuovoTitolo,
                           LocalDate nuovaData,
                           String nuovoLink,
                           String nuovaDescrizione) {
        todoCtrl.modificaToDo(td, nuovoTitolo, nuovaData, nuovoLink, nuovaDescrizione);
    }

    public List<ToDo> getScadenzeOggi() {
        return todoCtrl.getToDoByDate(LocalDate.now());
    }
}
