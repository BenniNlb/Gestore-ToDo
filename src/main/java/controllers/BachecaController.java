package controllers;

import dao.BachecaDAO;
import dao.postgresimpl.PostgresBachecaDAO;
import model.Bacheca;
import model.TitoloBacheca;
import model.Utente;

import java.util.*;

/**
 * Controller (Control) responsabile della gestione delle bacheche.
 * <p>
 * Questa classe gestisce la logica di business relativa alle bacheche ({@link Bacheca})
 * di un utente specifico. Le sue responsabilità includono:
 * <ul>
 * <li>Caricamento delle bacheche dal database (o creazione di quelle di default per i nuovi utenti).</li>
 * <li>Aggiunta e rimozione di bacheche, garantendo la coerenza dei dati.</li>
 * <li>Gestione dell'ordinamento personalizzato delle bacheche (campo {@code posizioneB}).</li>
 * <li>Notifica agli osservatori (View) quando lo stato del modello cambia.</li>
 * </ul>
 */
public class BachecaController {

    /**
     * Lista in memoria delle bacheche dell'utente, mantenuta ordinata per posizione.
     */
    private List<Bacheca> bacheche;

    /**
     * Lista di listener (osservatori) da notificare in caso di cambiamenti.
     */
    private final List<Runnable> listeners = new ArrayList<>();

    /**
     * L'utente attualmente loggato nel sistema.
     */
    private final Utente utenteLoggato;

    /**
     * Oggetto DAO per l'accesso ai dati delle bacheche nel database.
     */
    private final BachecaDAO bachecaDAO;

    /**
     * Costruisce un nuovo controller per le bacheche.
     * <p>
     * Inizializza il DAO, imposta l'utente corrente e carica immediatamente
     * lo stato delle bacheche dal database.
     *
     * @param utente L'utente di cui gestire le bacheche.
     */
    public BachecaController(Utente utente) {
        this.utenteLoggato = utente;
        this.bachecaDAO = new PostgresBachecaDAO();
        this.bacheche = new ArrayList<>();

        loadBachecheFromDB();
    }

    /**
     * Carica la lista delle bacheche dal database.
     * <p>
     * Se l'utente non ha ancora nessuna bacheca (es. primo accesso),
     * il metodo crea automaticamente le tre bacheche di default (Università, Lavoro, Tempo Libero),
     * le salva nel database e le carica in memoria.
     */
    private void loadBachecheFromDB() {
        List<Bacheca> bachecheList = bachecaDAO.getBachecheByUtente(utenteLoggato.getIdUtente());

        if (bachecheList.isEmpty()) {
            int pos = 0;
            for (TitoloBacheca t : Arrays.asList(
                    TitoloBacheca.UNIVERSITA,
                    TitoloBacheca.LAVORO,
                    TitoloBacheca.TEMPO_LIBERO)) {

                Bacheca nuovaBacheca = new Bacheca(t, "", utenteLoggato.getIdUtente(), pos++);
                bachecaDAO.addBacheca(nuovaBacheca);
                bachecheList.add(nuovaBacheca);
            }
        }

        this.bacheche = bachecheList;
    }

    /**
     * Registra un listener che verrà eseguito ogni volta che il modello delle bacheche cambia.
     *
     * @param listener Un oggetto {@link Runnable} contenente la logica di aggiornamento (es. refresh della UI).
     */
    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Notifica tutti i listener registrati di un cambiamento nel modello.
     * I listener vengono eseguiti in sequenza. Eventuali eccezioni nei listener vengono ignorate
     * per non interrompere il flusso di notifica.
     */
    private void notifyListeners() {
        for (Runnable l : listeners) {
            try { l.run(); } catch (Exception ignored) { }
        }
    }

    /**
     * Restituisce la lista completa delle bacheche dell'utente.
     *
     * @return Una {@link List} di oggetti {@link Bacheca}.
     */
    public List<Bacheca> getAllBacheche() {
        return bacheche;
    }

    /**
     * Cerca e restituisce una bacheca specifica in base al titolo.
     *
     * @param titolo Il {@link TitoloBacheca} da cercare.
     * @return L'oggetto {@link Bacheca} corrispondente, o {@code null} se non trovato.
     */
    public Bacheca getBacheca(TitoloBacheca titolo) {
        for (Bacheca b : bacheche) {
            if (b.getTitolo() == titolo) {
                return b;
            }
        }
        return null;
    }

    /**
     * Aggiunge una nuova bacheca per l'utente.
     * <p>
     * Verifica che il titolo sia valido e che la bacheca non esista già.
     * La nuova bacheca viene posizionata alla fine della lista.
     *
     * @param titolo      Il titolo della nuova bacheca.
     * @param descrizione Una descrizione opzionale.
     * @throws IllegalArgumentException Se il titolo non è valido o la bacheca esiste già.
     */
    public void aggiungiBacheca(TitoloBacheca titolo, String descrizione) {
        if (!EnumSet.of(TitoloBacheca.UNIVERSITA, TitoloBacheca.LAVORO, TitoloBacheca.TEMPO_LIBERO)
                .contains(titolo)) {
            throw new IllegalArgumentException("Titolo non valido");
        }

        if (getBacheca(titolo) != null) {
            throw new IllegalArgumentException("Bacheca '" + titolo + "' già esistente");
        }

        int nuovaPosizione = this.bacheche.size();
        Bacheca nuovaBacheca = new Bacheca(titolo, descrizione != null ? descrizione : "", utenteLoggato.getIdUtente(), nuovaPosizione);

        bachecaDAO.addBacheca(nuovaBacheca);

        bacheche.add(nuovaBacheca);

        notifyListeners();
    }

    /**
     * Elimina una bacheca esistente.
     * <p>
     * Rimuove la bacheca dal database e dalla memoria, quindi ricalcola e salva
     * l'ordine delle bacheche rimanenti per evitare "buchi" nella sequenza delle posizioni.
     *
     * @param titolo Il titolo della bacheca da eliminare.
     * @throws IllegalArgumentException Se la bacheca non esiste o è l'ultima rimasta (non è permesso avere zero bacheche).
     */
    public void eliminaBacheca(TitoloBacheca titolo) {
        Bacheca b = getBacheca(titolo);
        if (b == null) {
            throw new IllegalArgumentException("Bacheca '" + titolo + "' inesistente");
        }

        if (bacheche.size() <= 1) {
            throw new IllegalArgumentException("Impossibile eliminare l'ultima bacheca.");
        }

        bachecaDAO.deleteBacheca(b.getIdBacheca());
        bacheche.remove(b);

        salvaOrdineBacheche();

        notifyListeners();
    }

    /**
     * Ricalcola e persiste l'ordine delle bacheche.
     * <p>
     * Itera sulla lista in memoria e aggiorna il campo {@code posizioneB} nel database
     * per ogni bacheca il cui indice è cambiato. Questo garantisce che l'ordine visivo
     * venga mantenuto al prossimo avvio.
     */
    private void salvaOrdineBacheche() {
        for (int i = 0; i < bacheche.size(); i++) {
            Bacheca b = bacheche.get(i);
            if (b.getPosizioneB() != i) {
                b.setPosizioneB(i);
                bachecaDAO.updateBacheca(b);
            }
        }
    }

    /**
     * Modifica la descrizione di una bacheca esistente.
     *
     * @param titolo           Il titolo della bacheca da modificare.
     * @param nuovaDescrizione La nuova descrizione da impostare.
     */
    public void modificaDescrizioneBacheca(TitoloBacheca titolo, String nuovaDescrizione) {
        Bacheca b = getBacheca(titolo);
        if (b != null) {
            b.setDescrizione(nuovaDescrizione);
            bachecaDAO.updateBacheca(b);
            notifyListeners();
        }
    }

    /**
     * Metodo di utilità per forzare la notifica di cambiamento ai listener.
     * Utilizzato da altri controller per segnalare modifiche indirette (es. modifica di un ToDo).
     */
    public void notifyChange() {
        notifyListeners();
    }
}