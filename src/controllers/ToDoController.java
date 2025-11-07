package controllers;

import model.ToDo;
import model.Bacheca;
import model.TitoloBacheca;

import java.awt.Color;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;

public class ToDoController {

    private final BachecaController bachecaCtrl;

    public ToDoController(BachecaController bCtrl) {
        this.bachecaCtrl = bCtrl;
    }

    /**
     * Ora accetta una lista di link e un'ImageIcon opzionale.
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

        ToDo td = new ToDo(titolo);
        td.setDataScadenza(dataScadenza);
        td.setLinkURLs(linkURLs);
        td.setDescrizione(descrizione);
        td.setColoreSfondo(coloreSfondo);
        td.setImmagine(immagine);

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
                                (td.getDescrizione() != null && td.getDescrizione().toLowerCase().contains(q)) ||
                                (td.getLinkURLs() != null && td.getLinkURLs().stream().anyMatch(l -> l.toLowerCase().contains(q)))
                )
                .collect(Collectors.toList());
    }

    /** Modifica: ora permette anche di aggiornare link, immagine, bacheca E COLORE */
    public void modificaToDo(ToDo td,
                             String nuovoTitolo,
                             LocalDate nuovaData,
                             List<String> nuoviLink,
                             String nuovaDescrizione,
                             ImageIcon nuovaImmagine,
                             TitoloBacheca nuovaBacheca,
                             Color nuovoColore) { // <-- 1. AGGIUNGI QUESTO PARAMETRO
        if (td == null) throw new IllegalArgumentException("ToDo nullo");

        // Trova la bacheca corrente che contiene il ToDo (se esiste)
        Bacheca bachecaCorrente = bachecaCtrl.getAllBacheche().stream()
                .filter(b -> b.getToDos().contains(td))
                .findFirst()
                .orElse(null);

        // Trova la bacheca di destinazione
        Bacheca bDest = bachecaCtrl.getBacheca(nuovaBacheca);

        // Aggiorna i campi del ToDo
        td.setTitolo(nuovoTitolo);
        td.setDataScadenza(nuovaData);
        td.setLinkURLs(nuoviLink);
        td.setDescrizione(nuovaDescrizione);
        td.setImmagine(nuovaImmagine);
        td.setColoreSfondo(nuovoColore); // <-- 2. AGGIUNGI QUESTA LINEA

        if (bDest == null) {
            // Se non esiste la bacheca di destinazione, mantieni lo stato precedente ma aggiorna i campi
            bachecaCtrl.notifyChange();
            return;
        }

        // Se la bacheca è cambiata → sposta il ToDo
        if (bachecaCorrente != null && !bachecaCorrente.equals(bDest)) {
            bachecaCorrente.rimuoviToDo(td);
            bDest.aggiungiToDo(td);
        } else if (bachecaCorrente == null) {
            // Se non era in nessuna bacheca (improbabile), aggiungilo comunque
            bDest.aggiungiToDo(td);
        }

        bachecaCtrl.notifyChange();
    }
}
