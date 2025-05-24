package Controllers;

import Model.ToDo;
import Model.Bacheca;
import Model.TitoloBacheca;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ToDoController {
    private final BachecaController bachecaCtrl;

    public ToDoController(BachecaController bCtrl) {
        this.bachecaCtrl = bCtrl;
    }

    public ToDo creaToDo(TitoloBacheca inBacheca,
                         String titolo,
                         LocalDate dataScadenza,
                         String linkURL,
                         String descrizione,
                         Color coloreSfondo) {
        if (titolo == null || titolo.trim().isEmpty())
            throw new IllegalArgumentException("Titolo obbligatorio");
        if (dataScadenza == null)
            throw new IllegalArgumentException("Data scadenza obbligatoria");

        ToDo td = new ToDo(titolo);
        td.setDataScadenza(dataScadenza);
        td.setLinkURL(linkURL);
        td.setDescrizione(descrizione);
        td.setColoreSfondo(coloreSfondo);

        Bacheca b = bachecaCtrl.getBacheca(inBacheca);
        b.aggiungiToDo(td);
        // Chiamata aggiornaScadenze per gestire SCADENZE_DI_OGGI
        // E importante: dopo qualsiasi modifica ai ToDo, aggiorniamo la bacheca
        // delle scadenze e poi notifichiamo la UI tramite BachecaController.
        aggiornaScadenzeGenerale(); // Chiamiamo un nuovo metodo che aggiorna tutte le bacheche
        return td;
    }

    public void eliminaToDo(ToDo td) {
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            b.rimuoviToDo(td);
        }
        aggiornaScadenzeGenerale(); // Aggiorna tutte le bacheche dopo l'eliminazione
    }

    public void setCompletato(ToDo td, boolean completato) {
        td.setCompletato(completato);
        aggiornaScadenzeGenerale(); // Aggiorna tutte le bacheche dopo il cambio di stato
    }

    public List<ToDo> getToDoByDate(LocalDate date) {
        List<ToDo> result = new ArrayList<>();
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            for (ToDo td : b.getToDos()) {
                if (date.equals(td.getDataScadenza())) {
                    result.add(td);
                }
            }
        }
        return result;
    }

    // Questo metodo gestisce l'aggiornamento di tutte le bacheche e la notifica alla UI.
    // Viene chiamato dopo ogni operazione di modifica ToDo.
    private void aggiornaScadenzeGenerale() {
        // Prima, aggiorniamo la bacheca "Scadenze di oggi" (che già notifica la UI)
        bachecaCtrl.aggiornaScadenzeOggi(getAllToDos());
        // Se si modificano ToDo in bacheche diverse da SCADENZE_DI_OGGI
        // dobbiamo anche notificare il cambiamento per tutte le altre bacheche.
        // Dato che BachecaController.aggiornaScadenzeOggi() già chiama notifyListeners(),
        // questo è sufficiente. Se aggiornassimo altre bacheche, dovremmo notificare di nuovo.
        // Ma, in questo caso, la refreshCenter della MainFrame ridisegnerà tutto
        // dopo la notifica di aggiornaScadenzeOggi().
    }

    /**
     * Ritorna tutti i ToDo presenti in tutte le bacheche (incluse le scadenze di oggi)
     */
    public List<ToDo> getAllToDos() {
        List<ToDo> all = new ArrayList<>();
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            // È importante escludere la bacheca SCADENZE_DI_OGGI da getAllToDos()
            // se non vogliamo duplicati quando la bacheca viene ripopolata.
            // O altrimenti, assicurarsi che i ToDo non siano già presenti.
            if (b.getTitolo() != TitoloBacheca.SCADENZE_DI_OGGI) {
                all.addAll(b.getToDos());
            }
        }
        return all;
    }

    /**
     * Cerca tutti i ToDo che contengono la query nel titolo o nella descrizione
     */
    public List<ToDo> searchToDo(String query) {
        if (query == null || query.trim().isEmpty())
            return new ArrayList<>();

        String q = query.toLowerCase(Locale.ROOT);
        return getAllToDos().stream()
                .filter(td ->
                        (td.getTitolo() != null && td.getTitolo().toLowerCase().contains(q)) ||
                                (td.getDescrizione() != null && td.getDescrizione().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }
}