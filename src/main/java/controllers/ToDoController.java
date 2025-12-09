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

/**
 * Controller (Control) responsabile della gestione delle attività (ToDo).
 * <p>
 * Questa classe gestisce la logica di business relativa ai {@link ToDo}, orchestrando
 * le operazioni tra il modello (ToDo, Bacheca, Utente) e il livello di persistenza (DAO).
 * Le sue responsabilità principali includono:
 * <ul>
 * <li>Creazione, modifica ed eliminazione dei ToDo.</li>
 * <li>Gestione dello stato di completamento.</li>
 * <li>Spostamento dei ToDo tra bacheche diverse.</li>
 * <li>Gestione delle condivisioni con altri utenti.</li>
 * <li>Recupero dei dati filtrati per data o tramite ricerca testuale.</li>
 * </ul>
 */
public class ToDoController {

    /**
     * Riferimento al controller delle bacheche per accedere alla struttura organizzativa.
     */
    private final BachecaController bachecaCtrl;

    /**
     * L'utente attualmente loggato che sta eseguendo le operazioni.
     */
    private final Utente utenteLoggato;

    /**
     * DAO per l'accesso ai dati dei ToDo.
     */
    private final ToDoDAO todoDAO;

    /**
     * DAO per l'accesso ai dati degli utenti (necessario per le condivisioni).
     */
    private final UtenteDAO utenteDAO;

    /**
     * Costruisce un nuovo controller per i ToDo.
     * <p>
     * Inizializza le dipendenze e carica immediatamente i ToDo dal database
     * per popolare le bacheche in memoria.
     *
     * @param utente    L'utente loggato.
     * @param bCtrl     Il controller delle bacheche.
     * @param todoDAO   L'implementazione del DAO per i ToDo.
     * @param utenteDAO L'implementazione del DAO per gli utenti.
     */
    public ToDoController(Utente utente, BachecaController bCtrl, ToDoDAO todoDAO, UtenteDAO utenteDAO) {
        this.utenteLoggato = utente;
        this.bachecaCtrl = bCtrl;
        this.todoDAO = todoDAO;
        this.utenteDAO = utenteDAO;

        loadToDosFromDB();
    }

    /**
     * Carica i ToDo dal database e li associa alle rispettive bacheche in memoria.
     * <p>
     * Per ogni bacheca dell'utente, recupera:
     * <ol>
     * <li>I ToDo creati dall'utente stesso.</li>
     * <li>I ToDo condivisi con l'utente da altri, che appartengono a una bacheca
     * con lo stesso titolo (es. un ToDo "Lavoro" condiviso apparirà nella bacheca "Lavoro").</li>
     * </ol>
     * I risultati vengono uniti evitando duplicati (con priorità ai ToDo propri).
     */
    private void loadToDosFromDB() {
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {

            List<ToDo> myToDos = todoDAO.getAllToDosByBacheca(b.getIdBacheca());

            List<ToDo> sharedToDos = todoDAO.getSharedToDosForUser(
                    utenteLoggato.getIdUtente(),
                    b.getTitolo()
            );

            Map<Integer, ToDo> combined = new LinkedHashMap<>();
            for (ToDo td : myToDos) {
                combined.put(td.getIdToDo(), td);
            }
            for (ToDo td : sharedToDos) {
                combined.putIfAbsent(td.getIdToDo(), td);
            }

            b.setToDos(new ArrayList<>(combined.values()));
        }
    }

    /**
     * Crea un nuovo ToDo, lo salva nel database e aggiorna il modello in memoria.
     *
     * @param inBacheca    Il titolo della bacheca di destinazione.
     * @param titolo       Il titolo dell'attività.
     * @param dataScadenza La data di scadenza.
     * @param linkURLs     Lista di link associati.
     * @param descrizione  Descrizione dettagliata.
     * @param coloreSfondo Colore di sfondo per la card.
     * @param immagine     Immagine allegata.
     * @return L'oggetto {@link ToDo} creato e persistito.
     * @throws IllegalArgumentException Se il titolo è vuoto, la data è nulla o la bacheca non esiste.
     */
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

        td.setPosizione(b.getToDos().size());

        todoDAO.addToDo(td);

        b.aggiungiToDo(td);

        bachecaCtrl.notifyChange();
        return td;
    }

    /**
     * Elimina un ToDo dal sistema.
     * <p>
     * Rimuove l'attività dal database e dalla lista in memoria della bacheca corrente.
     * Successivamente, aggiorna l'ordine dei ToDo rimanenti per mantenere la sequenza.
     *
     * @param td Il ToDo da eliminare.
     */
    public void eliminaToDo(ToDo td) {
        todoDAO.deleteToDo(td.getIdToDo());

        Bacheca bachecaCorrente = null;
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            if (b.getToDos().contains(td)) {
                b.rimuoviToDo(td);
                bachecaCorrente = b;
                break;
            }
        }

        if (bachecaCorrente != null) {
            salvaOrdineBacheca(bachecaCorrente);
        }

        bachecaCtrl.notifyChange();
    }

    /**
     * Aggiorna lo stato di completamento di un ToDo.
     *
     * @param td         Il ToDo da aggiornare.
     * @param completato {@code true} per segnarlo come completato, {@code false} altrimenti.
     */
    public void setCompletato(ToDo td, boolean completato) {
        td.setCompletato(completato);
        todoDAO.updateToDo(td);
        bachecaCtrl.notifyChange();
    }

    /**
     * Recupera tutti i ToDo (propri e condivisi) che scadono in una data specifica.
     *
     * @param date La data di scadenza da cercare.
     * @return Una lista di {@link ToDo} corrispondenti.
     */
    public List<ToDo> getToDoByDate(LocalDate date) {
        return todoDAO.getToDosByDate(utenteLoggato.getIdUtente(), date);
    }

    /**
     * Restituisce una lista piatta di tutti i ToDo presenti nelle bacheche in memoria.
     *
     * @return Una lista contenente tutti i {@link ToDo} visibili all'utente.
     */
    public List<ToDo> getAllToDos() {
        List<ToDo> all = new ArrayList<>();
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            all.addAll(b.getToDos());
        }
        return all;
    }

    /**
     * Cerca i ToDo che contengono una determinata stringa nel titolo o nella descrizione.
     *
     * @param query La stringa di ricerca.
     * @return Una lista di {@link ToDo} corrispondenti.
     */
    public List<ToDo> searchToDo(String query) {
        return todoDAO.searchToDos(utenteLoggato.getIdUtente(), query);
    }

    /**
     * Modifica le proprietà di un ToDo esistente.
     * <p>
     * Gestisce anche lo spostamento del ToDo da una bacheca all'altra, se la bacheca
     * di destinazione è diversa da quella corrente. In tal caso, aggiorna le posizioni
     * sia nella bacheca di origine che in quella di destinazione.
     *
     * @param td               Il ToDo da modificare.
     * @param nuovoTitolo      Il nuovo titolo.
     * @param nuovaData        La nuova data di scadenza.
     * @param nuoviLink        La nuova lista di link.
     * @param nuovaDescrizione La nuova descrizione.
     * @param nuovaImmagine    La nuova immagine.
     * @param nuovaBacheca     La nuova bacheca di appartenenza.
     * @param nuovoColore      Il nuovo colore di sfondo.
     * @throws IllegalArgumentException Se il ToDo passato è nullo.
     */
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

            td.setPosizione(bDest.getToDos().size());
            bDest.aggiungiToDo(td);

            salvaOrdineBacheca(bachecaCorrente);

        } else if (bachecaCorrente == null) {
            td.setIdBacheca(bDest.getIdBacheca());
            td.setPosizione(bDest.getToDos().size());
            bDest.aggiungiToDo(td);
        }

        todoDAO.updateToDo(td);
        salvaOrdineBacheca(bDest);

        bachecaCtrl.notifyChange();
    }

    /**
     * Cerca utenti nel sistema per nome, escludendo l'utente loggato.
     * Utile per selezionare utenti con cui condividere un'attività.
     *
     * @param query La stringa di ricerca (username).
     * @return Una lista di {@link Utente} trovati.
     */
    public List<Utente> cercaUtenti(String query) {
        return utenteDAO.searchUtenti(query, utenteLoggato.getIdUtente());
    }

    /**
     * Aggiunge una nuova condivisione per un ToDo.
     *
     * @param todo     Il ToDo da condividere.
     * @param utente   L'utente con cui condividere.
     * @param permesso Il livello di permesso concesso.
     */
    public void onAggiungiCondivisione(ToDo todo, Utente utente, PermessoCondivisione permesso) {
        todoDAO.aggiungiCondivisione(todo.getIdToDo(), utente.getIdUtente(), permesso);
        todo.aggiungiOModificaCondivisione(utente, permesso);
        bachecaCtrl.notifyChange();
    }

    /**
     * Modifica il permesso di una condivisione esistente.
     *
     * @param todo          Il ToDo condiviso.
     * @param utente        L'utente la cui condivisione viene modificata.
     * @param nuovoPermesso Il nuovo livello di permesso.
     */
    public void onModificaPermesso(ToDo todo, Utente utente, PermessoCondivisione nuovoPermesso) {
        todoDAO.aggiornaPermessoCondivisione(todo.getIdToDo(), utente.getIdUtente(), nuovoPermesso);
        todo.aggiungiOModificaCondivisione(utente, nuovoPermesso);
        bachecaCtrl.notifyChange();
    }

    /**
     * Rimuove una condivisione, revocando l'accesso al ToDo per l'utente specificato.
     *
     * @param todo   Il ToDo.
     * @param utente L'utente da rimuovere.
     */
    public void onRimuoviCondivisione(ToDo todo, Utente utente) {
        todoDAO.rimuoviCondivisione(todo.getIdToDo(), utente.getIdUtente());
        todo.rimuoviCondivisione(utente);
        bachecaCtrl.notifyChange();
    }

    /**
     * Recupera un utente specifico tramite il suo ID.
     *
     * @param id L'identificativo dell'utente.
     * @return L'oggetto {@link Utente} corrispondente.
     */
    public Utente getUtenteById(int id) {
        return utenteDAO.getUtenteById(id);
    }

    /**
     * Ricalcola e persiste l'ordine dei ToDo all'interno di una bacheca.
     * <p>
     * Itera sulla lista dei ToDo della bacheca (che riflette l'ordine visivo corrente)
     * e aggiorna il campo {@code posizione} nel database per ogni elemento il cui indice
     * è cambiato.
     *
     * @param bacheca La bacheca di cui salvare l'ordinamento.
     */
    public void salvaOrdineBacheca(Bacheca bacheca) {
        List<ToDo> toDos = bacheca.getToDos();
        for (int i = 0; i < toDos.size(); i++) {
            ToDo td = toDos.get(i);
            if (td.getPosizione() != i) {
                td.setPosizione(i);
                todoDAO.updateToDo(td);
            }
        }
    }
}