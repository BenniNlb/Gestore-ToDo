package controllers;

import dao.BachecaDAO;
import dao.postgresimpl.PostgresBachecaDAO;
import model.Bacheca;
import model.TitoloBacheca;
import model.Utente;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller che gestisce le bacheche.
 * Ora carica i dati dal DB invece che in memoria.
 */
public class BachecaController {

    private Map<TitoloBacheca, Bacheca> bacheche;
    private final List<Runnable> listeners = new ArrayList<>();
    private final Utente utenteLoggato;

    private final BachecaDAO bachecaDAO;

    public BachecaController(Utente utente) {
        this.utenteLoggato = utente;
        this.bachecaDAO = new PostgresBachecaDAO();

        // Carica le bacheche dell'utente dal DB
        loadBachecheFromDB();
    }

    /**
     * Carica le bacheche dal DB. Se l'utente non ne ha,
     * crea e salva le 3 bacheche di default.
     */
    private void loadBachecheFromDB() {
        List<Bacheca> bachecheList = bachecaDAO.getBachecheByUtente(utenteLoggato.getIdUtente());

        if (bachecheList.isEmpty()) {
            // Primo login: crea e salva le bacheche di default
            for (TitoloBacheca t : Arrays.asList(
                    TitoloBacheca.UNIVERSITA,
                    TitoloBacheca.LAVORO,
                    TitoloBacheca.TEMPO_LIBERO)) {

                Bacheca nuovaBacheca = new Bacheca(t, "", utenteLoggato.getIdUtente());
                bachecaDAO.addBacheca(nuovaBacheca); // Salva su DB
                bachecheList.add(nuovaBacheca); // Aggiungi alla lista
            }
        }

        // Converte la Lista in una Mappa per la logica esistente
        this.bacheche = bachecheList.stream()
                .collect(Collectors.toMap(Bacheca::getTitolo, Function.identity()));
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
        return new ArrayList<>(bacheche.values());
    }

    public Bacheca getBacheca(TitoloBacheca titolo) {
        return bacheche.get(titolo);
    }

    public void aggiungiBacheca(TitoloBacheca titolo, String descrizione) {
        if (!EnumSet.of(TitoloBacheca.UNIVERSITA, TitoloBacheca.LAVORO, TitoloBacheca.TEMPO_LIBERO)
                .contains(titolo)) {
            throw new IllegalArgumentException("Titolo non valido");
        }
        if (bacheche.containsKey(titolo)) {
            throw new IllegalArgumentException("Bacheca '" + titolo + "' già esistente");
        }

        // CORRETTO: Passa l'ID utente reale
        Bacheca nuovaBacheca = new Bacheca(titolo, descrizione != null ? descrizione : "", utenteLoggato.getIdUtente());
        bachecaDAO.addBacheca(nuovaBacheca); // Salva su DB

        bacheche.put(titolo, nuovaBacheca); // Aggiungi alla mappa in memoria
        notifyListeners();
    }

    public void eliminaBacheca(TitoloBacheca titolo) {
        if (!bacheche.containsKey(titolo))
            throw new IllegalArgumentException("Bacheca '" + titolo + "' inesistente");

        if (bacheche.size() <= 1) {
            throw new IllegalArgumentException("Impossibile eliminare l'ultima bacheca.");
        }

        Bacheca b = bacheche.get(titolo);
        bachecaDAO.deleteBacheca(b.getIdBacheca()); // Elimina da DB

        bacheche.remove(titolo); // Rimuovi da mappa
        notifyListeners();
    }

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