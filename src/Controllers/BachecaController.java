package Controllers;

import Model.Bacheca;
import Model.TitoloBacheca;

import java.util.*;

/**
 * Controller che gestisce le bacheche (Università, Lavoro, Tempo Libero).
 * Non gestisce la vista "Scadenze di oggi", che è una sezione separata della UI.
 */
public class BachecaController {

    private final Map<TitoloBacheca, Bacheca> bacheche = new EnumMap<>(TitoloBacheca.class);
    private final List<Runnable> listeners = new ArrayList<>();

    public BachecaController() {
        // Creo le 3 bacheche iniziali senza descrizione
        for (TitoloBacheca t : Arrays.asList(
                TitoloBacheca.UNIVERSITA,
                TitoloBacheca.LAVORO,
                TitoloBacheca.TEMPO_LIBERO)) {
            bacheche.put(t, new Bacheca(t, ""));
        }
    }

    /** Permette di registrare listener da notificare quando cambia lo stato delle bacheche */
    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable l : listeners) {
            try {
                l.run();
            } catch (Exception ignored) { }
        }
    }

    /** Restituisce la lista di tutte le bacheche esistenti */
    public List<Bacheca> getAllBacheche() {
        return new ArrayList<>(bacheche.values());
    }

    /** Restituisce la bacheca corrispondente al titolo dato */
    public Bacheca getBacheca(TitoloBacheca titolo) {
        return bacheche.get(titolo);
    }

    /**
     * Aggiunge una nuova bacheca tra UNIVERSITA, LAVORO o TEMPO_LIBERO.
     * Lancia IllegalArgumentException se:
     * - titolo non valido
     * - titolo già esistente
     * Dopo l’inserimento, notifica i listener.
     */
    public void aggiungiBacheca(TitoloBacheca titolo, String descrizione) {
        if (!EnumSet.of(TitoloBacheca.UNIVERSITA, TitoloBacheca.LAVORO, TitoloBacheca.TEMPO_LIBERO)
                .contains(titolo)) {
            throw new IllegalArgumentException("Titolo non valido");
        }
        if (bacheche.containsKey(titolo)) {
            throw new IllegalArgumentException("Bacheca '" + titolo + "' già esistente");
        }
        bacheche.put(titolo, new Bacheca(titolo, descrizione != null ? descrizione : ""));
        notifyListeners();
    }

    /**
     * Elimina una bacheca esistente.
     * Lancia IllegalArgumentException se non esiste.
     * Dopo la rimozione, notifica i listener.
     */
    public void eliminaBacheca(TitoloBacheca titolo) {
        if (!bacheche.containsKey(titolo))
            throw new IllegalArgumentException("Bacheca '" + titolo + "' inesistente");

        if (bacheche.size() <= 1) {
            throw new IllegalArgumentException("Impossibile eliminare l'ultima bacheca. Deve rimanere almeno una bacheca.");
        }

        bacheche.remove(titolo);
        notifyListeners();
    }


    /**
     * Metodo pubblico per forzare la notifica ai listener registrati.
     * Viene usato quando le modifiche vengono fatte da altri controller (es. ToDoController).
     */
    public void notifyChange() {
        notifyListeners(); // chiama l'attuale metodo privato che esegue i listener
    }

}