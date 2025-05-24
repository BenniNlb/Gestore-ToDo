package Controllers;

import Model.Bacheca;
import Model.TitoloBacheca;

import java.time.LocalDate;
import java.util.*;

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
        // Creo la bacheca fissa SCADENZE_DI_OGGI
        bacheche.put(TitoloBacheca.SCADENZE_DI_OGGI,
                new Bacheca(TitoloBacheca.SCADENZE_DI_OGGI, "ToDo in scadenza oggi"));
    }

    /** Permette a chi usa il controller di registrare un callback da eseguire quando cambia lo stato */
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

    public List<Bacheca> getAllBacheche() {
        return new ArrayList<>(bacheche.values());
    }

    public Bacheca getBacheca(TitoloBacheca titolo) {
        return bacheche.get(titolo);
    }

    /*Aggiunge una nuova bacheca tra UNIVERSITA, LAVORO o TEMPO_LIBERO.
      Lancia IllegalArgumentException se:
      - titolo non valido
      - titolo già esistente
      Dopo l’inserimento, notifica i listener.
     */
    public void aggiungiBacheca(TitoloBacheca titolo, String descrizione) {
        if (titolo == TitoloBacheca.SCADENZE_DI_OGGI)
            throw new IllegalArgumentException("Non puoi aggiungere SCADENZE_DI_OGGI");
        if (!EnumSet.of(TitoloBacheca.UNIVERSITA, TitoloBacheca.LAVORO, TitoloBacheca.TEMPO_LIBERO)
                .contains(titolo)) {
            throw new IllegalArgumentException("Titolo non valido");
        }
        if (bacheche.containsKey(titolo)) {
            throw new IllegalArgumentException("Bacheca '" + titolo + "' già esistente");
        }
        bacheche.put(titolo, new Bacheca(titolo, descrizione != null ? descrizione : ""));
        notifyListeners();  // <— notifica la view
    }

    /**
     * Elimina una bacheca esistente (tranne SCADENZE_DI_OGGI).
     * Lancia IllegalArgumentException se non esiste o è SCADENZE_DI_OGGI.
     * Dopo la rimozione, notifica i listener.
     */
    public void eliminaBacheca(TitoloBacheca titolo) {
        if (titolo == TitoloBacheca.SCADENZE_DI_OGGI)
            throw new IllegalArgumentException("Non puoi eliminare SCADENZE_DI_OGGI");
        if (!bacheche.containsKey(titolo))
            throw new IllegalArgumentException("Bacheca '" + titolo + "' inesistente");
        bacheche.remove(titolo);
        notifyListeners();  // <— notifica la view
    }

    /**
     * Ripopola la bacheca SCADENZE_DI_OGGI con tutti i ToDo in scadenza oggi.
     * Deve essere chiamato da ToDoController ogni volta che viene aggiunto/modificato un ToDo.
     */
    public void aggiornaScadenzeOggi(List<Model.ToDo> tuttiToDo) {
        Bacheca scOggi = bacheche.get(TitoloBacheca.SCADENZE_DI_OGGI);
        scOggi.getToDos().clear();
        LocalDate oggi = LocalDate.now();
        for (Model.ToDo td : tuttiToDo) {
            if (td.getDataScadenza() != null && td.getDataScadenza().equals(oggi)) {
                scOggi.aggiungiToDo(td);
            }
        }
        notifyListeners();  // se vuoi che anche l’aggiornamento scadenze ridisegni la UI
    }
}