package controllers;

import dao.BachecaDAO;
import dao.postgresimpl.PostgresBachecaDAO;
import model.Bacheca;
import model.TitoloBacheca;
import model.Utente;

import java.util.*;

/**
 * Controller che gestisce le bacheche.
 * MODIFICATO: Usa una List ordinata dal DB (tramite 'posizioneB').
 */
public class BachecaController {

    private List<Bacheca> bacheche;
    private final List<Runnable> listeners = new ArrayList<>();
    private final Utente utenteLoggato;
    private final BachecaDAO bachecaDAO;

    public BachecaController(Utente utente) {
        this.utenteLoggato = utente;
        this.bachecaDAO = new PostgresBachecaDAO();
        this.bacheche = new ArrayList<>();

        // Carica le bacheche dell'utente dal DB
        loadBachecheFromDB();
    }

    /**
     * Carica le bacheche dal DB.
     * Il DAO ora le restituisce GIA ordinate per 'posizioneB'.
     */
    private void loadBachecheFromDB() {
        List<Bacheca> bachecheList = bachecaDAO.getBachecheByUtente(utenteLoggato.getIdUtente());

        if (bachecheList.isEmpty()) {
            // Primo login: crea e salva le bacheche di default con posizione
            int pos = 0;
            for (TitoloBacheca t : Arrays.asList(
                    TitoloBacheca.UNIVERSITA,
                    TitoloBacheca.LAVORO,
                    TitoloBacheca.TEMPO_LIBERO)) {

                Bacheca nuovaBacheca = new Bacheca(t, "", utenteLoggato.getIdUtente(), pos++); // --- MODIFICA ---
                bachecaDAO.addBacheca(nuovaBacheca); // Salva su DB
                bachecheList.add(nuovaBacheca); // Aggiungi alla lista
            }
        }

        // --- MODIFICA: La lista è già ordinata dal DAO ---
        this.bacheche = bachecheList;
        // --- FINE MODIFICA ---
    }

    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable l : listeners) {
            try { l.run(); } catch (Exception ignored) { }
        }
    }

    public List<Bacheca> getAllBacheche() {
        return bacheche;
    }

    public Bacheca getBacheca(TitoloBacheca titolo) {
        for (Bacheca b : bacheche) {
            if (b.getTitolo() == titolo) {
                return b;
            }
        }
        return null;
    }

    public void aggiungiBacheca(TitoloBacheca titolo, String descrizione) {
        if (!EnumSet.of(TitoloBacheca.UNIVERSITA, TitoloBacheca.LAVORO, TitoloBacheca.TEMPO_LIBERO)
                .contains(titolo)) {
            throw new IllegalArgumentException("Titolo non valido");
        }

        if (getBacheca(titolo) != null) {
            throw new IllegalArgumentException("Bacheca '" + titolo + "' già esistente");
        }

        // --- MODIFICA: Calcola la nuova posizioneB ---
        int nuovaPosizione = this.bacheche.size();
        Bacheca nuovaBacheca = new Bacheca(titolo, descrizione != null ? descrizione : "", utenteLoggato.getIdUtente(), nuovaPosizione);
        // --- FINE MODIFICA ---

        bachecaDAO.addBacheca(nuovaBacheca); // Salva su DB

        // --- MODIFICA: Aggiunge alla lista (non serve riordinare) ---
        bacheche.add(nuovaBacheca); // Aggiungi alla lista
        // --- FINE MODIFICA ---

        notifyListeners();
    }

    public void eliminaBacheca(TitoloBacheca titolo) {
        Bacheca b = getBacheca(titolo);
        if (b == null) {
            throw new IllegalArgumentException("Bacheca '" + titolo + "' inesistente");
        }

        if (bacheche.size() <= 1) {
            throw new IllegalArgumentException("Impossibile eliminare l'ultima bacheca.");
        }

        bachecaDAO.deleteBacheca(b.getIdBacheca()); // Elimina da DB
        bacheche.remove(b); // Rimuovi da lista

        // --- NUOVO: Aggiorna l'ordine delle bacheche rimanenti ---
        salvaOrdineBacheche();
        // --- FINE NUOVO ---

        notifyListeners();
    }

    // --- NUOVO METODO ---
    /**
     * Scorre le bacheche in memoria (che sono ordinate)
     * e aggiorna il campo 'posizioneB' nel DB.
     */
    private void salvaOrdineBacheche() {
        for (int i = 0; i < bacheche.size(); i++) {
            Bacheca b = bacheche.get(i);
            // --- MODIFICA: usa getPosizioneB/setPosizioneB ---
            if (b.getPosizioneB() != i) {
                b.setPosizioneB(i);
                bachecaDAO.updateBacheca(b); // Salva la nuova posizioneB
            }
            // --- FINE MODIFICA ---
        }
    }
    // --- FINE NUOVO METODO ---

    public void modificaDescrizioneBacheca(TitoloBacheca titolo, String nuovaDescrizione) {
        Bacheca b = getBacheca(titolo);
        if (b != null) {
            b.setDescrizione(nuovaDescrizione);
            bachecaDAO.updateBacheca(b); // Salva modifica su DB
            notifyListeners();
        }
    }

    public void notifyChange() {
        notifyListeners();
    }
}