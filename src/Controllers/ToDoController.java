package Controllers;

import Model.ToDo;
import Model.Bacheca;
import Model.TitoloBacheca;

import java.awt.Color;
import java.time.LocalDate;
import java.util.*;
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
        if (b == null) throw new IllegalArgumentException("Bacheca di destinazione non trovata: " + inBacheca);
        b.aggiungiToDo(td);

        bachecaCtrl.notifyChange();
        return td;
    }

    public void eliminaToDo(ToDo td) {
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            b.rimuoviToDo(td);
        }
        bachecaCtrl.notifyChange();
    }

    public void setCompletato(ToDo td, boolean completato) {
        td.setCompletato(completato);
        bachecaCtrl.notifyChange();
    }

    public List<ToDo> getToDoByDate(LocalDate date) {
        List<ToDo> result = new ArrayList<>();
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            for (ToDo td : b.getToDos()) {
                if (date != null && date.equals(td.getDataScadenza())) {
                    result.add(td);
                }
            }
        }
        return result;
    }

    public List<ToDo> getAllToDos() {
        List<ToDo> all = new ArrayList<>();
        for (Bacheca b : bachecaCtrl.getAllBacheche()) {
            all.addAll(b.getToDos());
        }
        return all;
    }

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

    /** Nuovo metodo per modificare un ToDo */
    public void modificaToDo(ToDo td,
                             String nuovoTitolo,
                             LocalDate nuovaData,
                             String nuovoLink,
                             String nuovaDescrizione) {
        if (td == null) throw new IllegalArgumentException("ToDo nullo");

        td.setTitolo(nuovoTitolo);
        td.setDataScadenza(nuovaData);
        td.setLinkURL(nuovoLink);
        td.setDescrizione(nuovaDescrizione);

        bachecaCtrl.notifyChange();
    }
}
