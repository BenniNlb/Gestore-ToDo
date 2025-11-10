package controllers;

import dao.ToDoDAO;
import dao.postgresimpl.PostgresToDoDAO;
import model.ToDo;
import model.Bacheca;
import model.TitoloBacheca;
import model.Utente;

import java.awt.Color;
import java.time.LocalDate;
import java.util.*;
import javax.swing.ImageIcon;

public class ToDoController {

    private final BachecaController bachecaCtrl;
    private final Utente utenteLoggato;

    private final ToDoDAO todoDAO;

    public ToDoController(Utente utente, BachecaController bCtrl) {
        this.utenteLoggato = utente;
        this.bachecaCtrl = bCtrl;
        this.todoDAO = new PostgresToDoDAO();

        // Carica i ToDo per ogni bacheca
        loadToDosFromDB();
    }

    /**
     * Carica i ToDo dal DB per ogni bacheca gestita dal BachecaController.
     */
    private void loadToDosFromDB() {
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            List<ToDo> toDos = todoDAO.getAllToDosByBacheca(b.getIdBacheca());
            b.setToDos(toDos); // "Idrata" il modello in memoria
        }
    }

    public ToDo creaToDo(TitoloBacheca inBacheca,
                         String titolo,
                         LocalDate dataScadenza,
                         List<String> linkURLs,
                         String descrizione,
                         Color coloreSfondo,
                         ImageIcon immagine) {
        if (titolo == null || titolo.trim().isEmpty())
            throw new IllegalArgumentException("Titolo obbligatorio");
        if (dataScadenza == null)
            throw new IllegalArgumentException("Data scadenza obbligatoria");

        Bacheca b = bachecaCtrl.getBacheca(inBacheca);
        if (b == null) throw new IllegalArgumentException("Bacheca di destinazione non trovata: " + inBacheca);

        // --- MODIFICATO: Costruttore ToDo aggiornato ---
        // Passa l'ID della bacheca e l'ID utente reale
        ToDo td = new ToDo(titolo, b.getIdBacheca(), utenteLoggato.getIdUtente());
        // --- FINE MODIFICA ---

        td.setDataScadenza(dataScadenza);
        td.setLinkURLs(linkURLs);
        td.setDescrizione(descrizione);
        td.setColoreSfondo(coloreSfondo);
        td.setImmagine(immagine);

        todoDAO.addToDo(td); // Salva su DB (questo imposta anche l'ID sul ToDo)

        b.aggiungiToDo(td); // Aggiunge al modello in memoria

        bachecaCtrl.notifyChange();
        return td;
    }

    public void eliminaToDo(ToDo td) {
        todoDAO.deleteToDo(td.getIdToDo()); // Elimina da DB

        // Rimuovi da modello in memoria
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            b.rimuoviToDo(td);
        }
        bachecaCtrl.notifyChange();
    }

    public void setCompletato(ToDo td, boolean completato) {
        td.setCompletato(completato);
        todoDAO.updateToDo(td); // Salva stato su DB
        bachecaCtrl.notifyChange();
    }

    public List<ToDo> getToDoByDate(LocalDate date) {
        // Ora usa il DAO e l'utente loggato
        return todoDAO.getToDosByDate(utenteLoggato.getIdUtente(), date);
    }

    public List<ToDo> getAllToDos() {
        List<ToDo> all = new ArrayList<>();
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            all.addAll(b.getToDos()); // Usa dati in memoria (già caricati)
        }
        return all;
    }

    public List<ToDo> searchToDo(String query) {
        // Ora usa il DAO e l'utente loggato
        return todoDAO.searchToDos(utenteLoggato.getIdUtente(), query);
    }

    public void modificaToDo(ToDo td,
                             String nuovoTitolo,
                             LocalDate nuovaData,
                             List<String> nuoviLink,
                             String nuovaDescrizione,
                             ImageIcon nuovaImmagine,
                             TitoloBacheca nuovaBacheca,
                             Color nuovoColore) {
        if (td == null) throw new IllegalArgumentException("ToDo nullo");

        Bacheca bachecaCorrente = bachecaCtrl.getAllBacheche().stream()
                .filter(b -> b.getToDos().contains(td))
                .findFirst()
                .orElse(null);

        Bacheca bDest = bachecaCtrl.getBacheca(nuovaBacheca);

        td.setTitolo(nuovoTitolo);
        td.setDataScadenza(nuovaData);
        td.setLinkURLs(nuoviLink);
        td.setDescrizione(nuovaDescrizione);
        td.setImmagine(nuovaImmagine);
        td.setColoreSfondo(nuovoColore);

        if (bDest == null) {
            // L'utente ha selezionato una bacheca non valida? Non dovrebbe succedere
            // Salva solo le modifiche al ToDo
            todoDAO.updateToDo(td);
            bachecaCtrl.notifyChange();
            return;
        }

        // Se la bacheca è cambiata → sposta il ToDo
        if (bachecaCorrente != null && !bachecaCorrente.equals(bDest)) {
            bachecaCorrente.rimuoviToDo(td);
            td.setIdBacheca(bDest.getIdBacheca()); // Aggiorna l'ID bacheca nel ToDo
            bDest.aggiungiToDo(td);
        } else if (bachecaCorrente == null) {
            td.setIdBacheca(bDest.getIdBacheca());
            bDest.aggiungiToDo(td);
        }

        todoDAO.updateToDo(td); // Salva modifiche (inclusa nuova bacheca) su DB
        bachecaCtrl.notifyChange();
    }
}