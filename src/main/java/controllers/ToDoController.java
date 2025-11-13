package controllers;

import dao.ToDoDAO;
import dao.UtenteDAO;
import model.ToDo;
import model.Bacheca;
import model.PermessoCondivisione;
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
    private final UtenteDAO utenteDAO;

    public ToDoController(Utente utente, BachecaController bCtrl, ToDoDAO todoDAO, UtenteDAO utenteDAO) {
        this.utenteLoggato = utente;
        this.bachecaCtrl = bCtrl;
        this.todoDAO = todoDAO;
        this.utenteDAO = utenteDAO;

        // Carica i ToDo per ogni bacheca
        loadToDosFromDB();
    }

    /**
     * MODIFICATO: Carica sia i ToDo creati che quelli condivisi.
     */
    private void loadToDosFromDB() {
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {

            // 1. Get ToDos CREATI da me per questa bacheca
            List<ToDo> myToDos = todoDAO.getAllToDosByBacheca(b.getIdBacheca());

            // 2. Get ToDos CONDIVISI con me che appartengono a una bacheca con lo stesso titolo
            List<ToDo> sharedToDos = todoDAO.getSharedToDosForUser(
                    utenteLoggato.getIdUtente(),
                    b.getTitolo() // Es. "LAVORO"
            );

            // 3. Combina e de-duplica (usa una Mappa per evitare duplicati)
            Map<Integer, ToDo> combined = new LinkedHashMap<>();
            for (ToDo td : myToDos) {
                combined.put(td.getIdToDo(), td);
            }
            for (ToDo td : sharedToDos) {
                // Aggiunge solo se non è già presente (quelli creati da me hanno priorità)
                combined.putIfAbsent(td.getIdToDo(), td);
            }

            // 4. Imposta la lista finale sulla bacheca
            b.setToDos(new ArrayList<>(combined.values()));
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

        ToDo td = new ToDo(titolo, b.getIdBacheca(), utenteLoggato.getIdUtente());

        td.setDataScadenza(dataScadenza);
        td.setLinkURLs(linkURLs);
        td.setDescrizione(descrizione);
        td.setColoreSfondo(coloreSfondo);
        td.setImmagine(immagine);

        // --- NUOVA LOGICA POSIZIONE ---
        // Imposta la posizione come ultimo elemento della lista
        td.setPosizione(b.getToDos().size());
        // --- FINE NUOVA LOGICA ---

        todoDAO.addToDo(td); // Salva su DB (questo imposta anche l'ID sul ToDo)

        b.aggiungiToDo(td); // Aggiunge al modello in memoria

        bachecaCtrl.notifyChange();
        return td;
    }

    public void eliminaToDo(ToDo td) {
        todoDAO.deleteToDo(td.getIdToDo()); // Elimina da DB

        Bacheca bachecaCorrente = null;
        // Rimuovi da modello in memoria
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            if (b.getToDos().contains(td)) {
                b.rimuoviToDo(td);
                bachecaCorrente = b;
                break;
            }
        }

        // Se abbiamo rimosso un todo, dobbiamo aggiornare l'ordine
        if (bachecaCorrente != null) {
            salvaOrdineBacheca(bachecaCorrente);
        }

        bachecaCtrl.notifyChange();
    }

    public void setCompletato(ToDo td, boolean completato) {
        td.setCompletato(completato);
        todoDAO.updateToDo(td); // Salva stato su DB
        bachecaCtrl.notifyChange();
    }

    public List<ToDo> getToDoByDate(LocalDate date) {
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
            todoDAO.updateToDo(td);
            bachecaCtrl.notifyChange();
            return;
        }

        if (bachecaCorrente != null && !bachecaCorrente.equals(bDest)) {
            bachecaCorrente.rimuoviToDo(td);
            td.setIdBacheca(bDest.getIdBacheca());

            // --- NUOVA LOGICA POSIZIONE ---
            // Imposta la posizione come ultimo elemento della *nuova* lista
            td.setPosizione(bDest.getToDos().size());
            bDest.aggiungiToDo(td);

            // Aggiorna l'ordine della vecchia bacheca
            salvaOrdineBacheca(bachecaCorrente);
            // --- FINE NUOVA LOGICA ---

        } else if (bachecaCorrente == null) {
            td.setIdBacheca(bDest.getIdBacheca());
            td.setPosizione(bDest.getToDos().size()); // Imposta posizione
            bDest.aggiungiToDo(td);
        }

        todoDAO.updateToDo(td);
        // Salva il nuovo ordine della bacheca di destinazione
        salvaOrdineBacheca(bDest);

        bachecaCtrl.notifyChange();
    }

    // --- METODI PER CONDIVISIONE ---

    public List<Utente> cercaUtenti(String query) {
        return utenteDAO.searchUtenti(query, utenteLoggato.getIdUtente());
    }

    public void onAggiungiCondivisione(ToDo todo, Utente utente, PermessoCondivisione permesso) {
        todoDAO.aggiungiCondivisione(todo.getIdToDo(), utente.getIdUtente(), permesso);
        todo.aggiungiOModificaCondivisione(utente, permesso);
        bachecaCtrl.notifyChange();
    }

    public void onModificaPermesso(ToDo todo, Utente utente, PermessoCondivisione nuovoPermesso) {
        todoDAO.aggiornaPermessoCondivisione(todo.getIdToDo(), utente.getIdUtente(), nuovoPermesso);
        todo.aggiungiOModificaCondivisione(utente, nuovoPermesso);
        bachecaCtrl.notifyChange();
    }


    public void onRimuoviCondivisione(ToDo todo, Utente utente) {
        todoDAO.rimuoviCondivisione(todo.getIdToDo(), utente.getIdUtente());
        todo.rimuoviCondivisione(utente);
        bachecaCtrl.notifyChange();
    }

    public Utente getUtenteById(int id) {
        return utenteDAO.getUtenteById(id);
    }

    // --- NUOVO METODO PER SALVARE L'ORDINE ---
    /**
     * Scorre la lista dei ToDo in una bacheca (che è già ordinata in memoria)
     * e aggiorna il campo 'posizione' nel database per ciascuno.
     */
    public void salvaOrdineBacheca(Bacheca bacheca) {
        List<ToDo> toDos = bacheca.getToDos();
        for (int i = 0; i < toDos.size(); i++) {
            ToDo td = toDos.get(i);
            // Aggiorna solo se la posizione è cambiata
            if (td.getPosizione() != i) {
                td.setPosizione(i);
                todoDAO.updateToDo(td); // Salva la nuova posizione
            }
        }
    }
    // --- FINE NUOVO METODO ---
}