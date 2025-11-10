package controllers;

import model.ToDo;
import model.TitoloBacheca;
import model.Bacheca;
import model.Utente; // IMPORTA UTENTE

import java.awt.Color;
import java.time.LocalDate;
import javax.swing.ImageIcon;
import java.util.List; // IMPORTA LIST

public class MainController {

    private final BachecaController bachecaCtrl;
    private final ToDoController todoCtrl;
    private final Utente utenteLoggato; // NUOVO

    // --- MODIFICATO: Il costruttore accetta l'Utente ---
    public MainController(Utente utente) {
        this.utenteLoggato = utente;
        // Passa l'utente ai controller figli
        this.bachecaCtrl = new BachecaController(utenteLoggato);
        this.todoCtrl = new ToDoController(utenteLoggato, bachecaCtrl);
    }
    // --- FINE MODIFICA ---

    public BachecaController getBachecaController() {
        return bachecaCtrl;
    }

    public ToDoController getToDoController() {
        return todoCtrl;
    }

    public void onEditBachecaDescrizione(TitoloBacheca titolo, String nuovaDescrizione) {
        bachecaCtrl.modificaDescrizioneBacheca(titolo, nuovaDescrizione);
    }

    /** Azioni delegate (aggiornate per lista di link e immagine) **/
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
    public void onEditToDo(ToDo td,
                           String nuovoTitolo,
                           LocalDate nuovaData,
                           java.util.List<String> nuoviLink,
                           String nuovaDescrizione,
                           ImageIcon nuovaImmagine,
                           TitoloBacheca nuovaBacheca,
                           Color nuovoColore) { // Aggiunto colore
        todoCtrl.modificaToDo(td, nuovoTitolo, nuovaData, nuoviLink, nuovaDescrizione, nuovaImmagine, nuovaBacheca, nuovoColore);
    }

    public java.util.List<ToDo> getScadenzeOggi() {
        return todoCtrl.getToDoByDate(LocalDate.now());
    }

    public java.util.List<ToDo> getScadenzePerData(LocalDate date) {
        return todoCtrl.getToDoByDate(date);
    }
}