package controllers;

import model.ToDo;
import model.TitoloBacheca;
import model.Bacheca;
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

/**
 * Controller principale (Facade/Boundary) dell'applicazione post-login.
 * <p>
 * Questa classe funge da punto di ingresso unificato per la vista principale ({@link gui.views.BoardView}).
 * Inizializza e coordina i controller specializzati ({@link BachecaController} e {@link ToDoController}),
 * nascondendo la complessità della logica di business e dell'accesso ai dati alla vista.
 * <p>
 * Tutte le azioni dell'utente nella dashboard (creazione ToDo, gestione bacheche, ricerca, filtri)
 * passano attraverso questo controller.
 */
public class MainController {

    /**
     * Controller delegato alla gestione delle bacheche.
     */
    private final BachecaController bachecaCtrl;

    /**
     * Controller delegato alla gestione dei ToDo.
     */
    private final ToDoController todoCtrl;

    /**
     * L'utente attualmente loggato nel sistema.
     */
    private final Utente utenteLoggato;

    /**
     * Costruisce il MainController.
     * <p>
     * Inizializza la catena di controller e i DAO necessari per l'utente che ha effettuato l'accesso.
     * Configura le connessioni al database e prepara l'ambiente per la sessione utente.
     *
     * @param utente L'oggetto {@link Utente} che ha superato l'autenticazione.
     */
    public MainController(Utente utente) {
        this.utenteLoggato = utente;

        this.bachecaCtrl = new BachecaController(utenteLoggato);

        UtenteDAO utenteDAO = new PostgresUtenteDAO(DBConnection.getConnection());
        ToDoDAO todoDAO = new PostgresToDoDAO(DBConnection.getConnection(), utenteDAO);

        this.todoCtrl = new ToDoController(utenteLoggato, bachecaCtrl, todoDAO, utenteDAO);
    }

    /**
     * Restituisce l'utente attualmente loggato.
     *
     * @return L'oggetto {@link Utente} della sessione corrente.
     */
    public Utente getUtenteLoggato() {
        return utenteLoggato;
    }

    /**
     * Restituisce l'istanza del controller delle bacheche.
     *
     * @return Il {@link BachecaController} attivo.
     */
    public BachecaController getBachecaController() {
        return bachecaCtrl;
    }

    /**
     * Restituisce l'istanza del controller dei ToDo.
     *
     * @return Il {@link ToDoController} attivo.
     */
    public ToDoController getToDoController() {
        return todoCtrl;
    }

    /**
     * Metodo ponte per modificare la descrizione di una bacheca.
     * Inoltra la richiesta al {@link BachecaController}.
     *
     * @param titolo           Il {@link TitoloBacheca} della bacheca da modificare.
     * @param nuovaDescrizione La nuova descrizione da impostare.
     */
    public void onEditBachecaDescrizione(TitoloBacheca titolo, String nuovaDescrizione) {
        bachecaCtrl.modificaDescrizioneBacheca(titolo, nuovaDescrizione);
    }

    /**
     * Metodo ponte per aggiungere un nuovo ToDo.
     * Raccoglie i dati dalla vista e inoltra la richiesta di creazione al {@link ToDoController}.
     *
     * @param titolo      Il titolo del ToDo.
     * @param data        La data di scadenza.
     * @param linkURLs    La lista di link associati.
     * @param descrizione La descrizione dettagliata.
     * @param colore      Il colore di sfondo.
     * @param inBacheca   Il {@link TitoloBacheca} di destinazione.
     * @param immagine    L'immagine allegata.
     */
    public void onAddToDo(String titolo,
                          LocalDate data,
                          java.util.List<String> linkURLs,
                          String descrizione,
                          Color colore,
                          TitoloBacheca inBacheca,
                          ImageIcon immagine) {
        todoCtrl.creaToDo(inBacheca, titolo, data, linkURLs, descrizione, colore, immagine);
    }

    /**
     * Metodo ponte per aggiungere una nuova bacheca.
     * Inoltra la richiesta al {@link BachecaController}.
     *
     * @param titolo      Il {@link TitoloBacheca} della nuova bacheca.
     * @param descrizione La descrizione opzionale.
     */
    public void onAddBacheca(TitoloBacheca titolo, String descrizione) {
        bachecaCtrl.aggiungiBacheca(titolo, descrizione);
    }

    /**
     * Metodo ponte per eliminare una bacheca.
     * Inoltra la richiesta al {@link BachecaController}.
     *
     * @param titolo Il {@link TitoloBacheca} della bacheca da eliminare.
     */
    public void onDeleteBacheca(TitoloBacheca titolo) {
        bachecaCtrl.eliminaBacheca(titolo);
    }

    /**
     * Metodo ponte per eliminare un ToDo.
     * Inoltra la richiesta al {@link ToDoController}.
     *
     * @param td Il {@link ToDo} da eliminare.
     */
    public void onDeleteToDo(ToDo td) {
        todoCtrl.eliminaToDo(td);
    }

    /**
     * Metodo ponte per modificare lo stato di completamento di un ToDo.
     * Inoltra la richiesta al {@link ToDoController}.
     *
     * @param td    Il {@link ToDo} da aggiornare.
     * @param stato {@code true} se completato, {@code false} altrimenti.
     */
    public void onToggleCompletato(ToDo td, boolean stato) {
        todoCtrl.setCompletato(td, stato);
    }

    /**
     * Metodo ponte per modificare un ToDo esistente.
     * Inoltra la richiesta di aggiornamento completa al {@link ToDoController}.
     * <p>
     * <b>Nota:</b> Questo metodo accetta molti parametri per gestire in un'unica
     * transazione l'aggiornamento di tutti i campi del ToDo. L'avviso di SonarLint
     * sui troppi parametri è soppresso intenzionalmente per questa scelta architetturale.
     *
     * @param td               Il {@link ToDo} da modificare.
     * @param nuovoTitolo      Il nuovo titolo.
     * @param nuovaData        La nuova data di scadenza.
     * @param nuoviLink        La nuova lista di link.
     * @param nuovaDescrizione La nuova descrizione.
     * @param nuovaImmagine    La nuova immagine.
     * @param nuovaBacheca     La nuova bacheca di destinazione (gestisce lo spostamento).
     * @param nuovoColore      Il nuovo colore di sfondo.
     */
    @SuppressWarnings("common-java:LongParameterList")
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

    /**
     * Metodo ponte per ottenere i ToDo in scadenza nella giornata odierna.
     * Inoltra la richiesta al {@link ToDoController}.
     *
     * @return La lista di {@link ToDo} in scadenza oggi.
     */
    public java.util.List<ToDo> getScadenzeOggi() {
        return todoCtrl.getToDoByDate(LocalDate.now());
    }

    /**
     * Metodo ponte per ottenere i ToDo in scadenza in una data specifica.
     * Inoltra la richiesta al {@link ToDoController}.
     *
     * @param date La data di scadenza da cercare.
     * @return La lista di {@link ToDo} corrispondenti.
     */
    public java.util.List<ToDo> getScadenzePerData(LocalDate date) {
        return todoCtrl.getToDoByDate(date);
    }

    /**
     * Metodo ponte per cercare utenti nel sistema (per la funzionalità di condivisione).
     * Inoltra la richiesta al {@link ToDoController}.
     *
     * @param query La stringa di ricerca (username).
     * @return La lista di {@link Utente} trovati.
     */
    public List<Utente> cercaUtenti(String query) {
        return todoCtrl.cercaUtenti(query);
    }

    /**
     * Metodo ponte per aggiungere una condivisione a un ToDo.
     * Inoltra la richiesta al {@link ToDoController}.
     *
     * @param todo     Il {@link ToDo} da condividere.
     * @param utente   L'{@link Utente} con cui condividere.
     * @param permesso Il livello di {@link PermessoCondivisione} da assegnare.
     */
    public void onAggiungiCondivisione(ToDo todo, Utente utente, PermessoCondivisione permesso) {
        todoCtrl.onAggiungiCondivisione(todo, utente, permesso);
    }

    /**
     * Metodo ponte per modificare il permesso di una condivisione esistente.
     * Inoltra la richiesta al {@link ToDoController}.
     *
     * @param todo          Il {@link ToDo} interessato.
     * @param utente        L'{@link Utente} interessato.
     * @param nuovoPermesso Il nuovo livello di {@link PermessoCondivisione}.
     */
    public void onModificaPermesso(ToDo todo, Utente utente, PermessoCondivisione nuovoPermesso) {
        todoCtrl.onModificaPermesso(todo, utente, nuovoPermesso);
    }

    /**
     * Metodo ponte per rimuovere una condivisione.
     * Inoltra la richiesta al {@link ToDoController}.
     *
     * @param todo   Il {@link ToDo} da cui rimuovere la condivisione.
     * @param utente L'{@link Utente} da rimuovere.
     */
    public void onRimuoviCondivisione(ToDo todo, Utente utente) {
        todoCtrl.onRimuoviCondivisione(todo, utente);
    }

    /**
     * Metodo ponte per recuperare un utente tramite ID.
     * Inoltra la richiesta al {@link ToDoController}.
     *
     * @param id L'ID dell'utente.
     * @return L'oggetto {@link Utente} corrispondente.
     */
    public Utente getUtenteById(int id) {
        return todoCtrl.getUtenteById(id);
    }

    /**
     * Metodo ponte per salvare l'ordine dei ToDo in una bacheca.
     * Inoltra la richiesta al {@link ToDoController}, tipicamente dopo un'operazione di Drag &amp; Drop.
     *
     * @param bacheca La {@link Bacheca} di cui salvare l'ordine.
     */
    public void onSalvaOrdineBacheca(Bacheca bacheca) {
        todoCtrl.salvaOrdineBacheca(bacheca);
    }
}