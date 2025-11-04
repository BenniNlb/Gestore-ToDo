package Controllers;

import Model.ToDo;
import Model.TitoloBacheca;

import java.awt.Color;
import java.time.LocalDate;
import java.util.List;
import javax.swing.ImageIcon;

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

    /** Azioni delegate dal CardChoice (aggiornata per lista di link e immagine) **/
    public void onAddToDo(String titolo,
                          LocalDate data,
                          java.util.List<String> linkURLs,
                          String descrizione,
                          Color colore,
                          TitoloBacheca inBacheca,
                          ImageIcon immagine) {
        todoCtrl.creaToDo(inBacheca, titolo, data, linkURLs, descrizione, colore, immagine);
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

    /** Metodo per modificare un ToDo (aggiornato) */
    // MODIFICATO: ora riceve anche la bacheca di destinazione
    public void onEditToDo(ToDo td,
                           String nuovoTitolo,
                           LocalDate nuovaData,
                           java.util.List<String> nuoviLink,
                           String nuovaDescrizione,
                           ImageIcon nuovaImmagine,
                           TitoloBacheca nuovaBacheca) {
        todoCtrl.modificaToDo(td, nuovoTitolo, nuovaData, nuoviLink, nuovaDescrizione, nuovaImmagine, nuovaBacheca);
    }

    public java.util.List<ToDo> getScadenzeOggi() {
        return todoCtrl.getToDoByDate(LocalDate.now());
    }
}
