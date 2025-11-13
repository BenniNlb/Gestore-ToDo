package controllers;

import model.ToDo;
import model.TitoloBacheca;
import model.Bacheca; // Importato
import model.PermessoCondivisione;
import model.Utente;
import dao.ToDoDAO;
import dao.UtenteDAO;
import dao.postgresimpl.PostgresToDoDAO;
import dao.postgresimpl.PostgresUtenteDAO;
import database.DBConnection;

import java.awt.Color;
import java.time.LocalDate;
import javax.swing.ImageIcon;
import java.util.List;

public class MainController {

    private final BachecaController bachecaCtrl;
    private final ToDoController todoCtrl;
    private final Utente utenteLoggato;

    public MainController(Utente utente) {
        this.utenteLoggato = utente;

        this.bachecaCtrl = new BachecaController(utenteLoggato);

        UtenteDAO utenteDAO = new PostgresUtenteDAO(DBConnection.getConnection());
        ToDoDAO todoDAO = new PostgresToDoDAO(DBConnection.getConnection(), utenteDAO);

        this.todoCtrl = new ToDoController(utenteLoggato, bachecaCtrl, todoDAO, utenteDAO);
    }

    public Utente getUtenteLoggato() {
        return utenteLoggato;
    }

    public BachecaController getBachecaController() {
        return bachecaCtrl;
    }

    public ToDoController getToDoController() {
        return todoCtrl;
    }

    public void onEditBachecaDescrizione(TitoloBacheca titolo, String nuovaDescrizione) {
        bachecaCtrl.modificaDescrizioneBacheca(titolo, nuovaDescrizione);
    }

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

    public void onEditToDo(ToDo td,
                           String nuovoTitolo,
                           LocalDate nuovaData,
                           java.util.List<String> nuoviLink,
                           String nuovaDescrizione,
                           ImageIcon nuovaImmagine,
                           TitoloBacheca nuovaBacheca,
                           Color nuovoColore) {
        todoCtrl.modificaToDo(td, nuovoTitolo, nuovaData, nuoviLink, nuovaDescrizione, nuovaImmagine, nuovaBacheca, nuovoColore);
    }

    public java.util.List<ToDo> getScadenzeOggi() {
        return todoCtrl.getToDoByDate(LocalDate.now());
    }

    public java.util.List<ToDo> getScadenzePerData(LocalDate date) {
        return todoCtrl.getToDoByDate(date);
    }

    // --- Metodi Ponte per la Condivisione ---

    public List<Utente> cercaUtenti(String query) {
        return todoCtrl.cercaUtenti(query);
    }

    public void onAggiungiCondivisione(ToDo todo, Utente utente, PermessoCondivisione permesso) {
        todoCtrl.onAggiungiCondivisione(todo, utente, permesso);
    }

    public void onModificaPermesso(ToDo todo, Utente utente, PermessoCondivisione nuovoPermesso) {
        todoCtrl.onModificaPermesso(todo, utente, nuovoPermesso);
    }

    public void onRimuoviCondivisione(ToDo todo, Utente utente) {
        todoCtrl.onRimuoviCondivisione(todo, utente);
    }

    public Utente getUtenteById(int id) {
        return todoCtrl.getUtenteById(id);
    }

    public void onSalvaOrdineBacheca(Bacheca bacheca) {
        todoCtrl.salvaOrdineBacheca(bacheca);
    }
}